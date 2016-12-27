package it.fcambi.news.tasks;

import it.fcambi.news.Application;
import it.fcambi.news.Logging;
import it.fcambi.news.async.Task;
import it.fcambi.news.crawlers.*;
import it.fcambi.news.model.Article;
import it.fcambi.news.model.FrontPage;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Francesco on 26/09/15.
 */
public class ArticlesDownloaderTask extends Task {

    private static final Logger log = Logging.registerLogger(ArticlesDownloaderTask.class.getName());

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public String getDescription() {
        return "Collects articles from each newspaper website";
    }

    @Override
    public void executeTask() 
    {
        progress.set(0);
        EntityManager em = Application.createEntityManager();

        //Crawl articles
        log.info("Article downloader process started.");
        List <Crawler> crawlers = new LinkedList <Crawler> ();
        
        
        crawlers.add (new LaRepubblicaCrawler());
        crawlers.add (new LaStampaCrawler());
        crawlers.add (new CorriereDellaSeraCrawler());
        crawlers.add (new AnsaCrawler());
        crawlers.add (new AdnkronosCrawler());
        crawlers.add (new IlGiornaleCrawler());
		crawlers.add ( new IlFattoQuotidianoCrawler () );
		crawlers.add ( new IlSecoloXIXCrawler () );
		crawlers.add ( new IlPostCrawler () );

        List<FrontPage> frontPages = new LinkedList<>();
        for (Crawler crawler : crawlers) {
            try {
                // Retrieve articles on the home page
                List<String> urls = null;
                int attempts = 10;
                while (attempts > 0) {
                    try {
                        urls = crawler.retrieveArticleUrlsFromHomePage();
                        attempts = 0;
                    } catch (Exception e) {
                        if (attempts > 0) {
                            attempts--;
                            Thread.sleep(10000);
                        } else
                            throw e;
                    }
                }
                List<Article> articles = new LinkedList<>();

                float statusArticleUnit = (95F/crawlers.size())/urls.size();

                // Download each article previously retrieved
                for (String url : urls) {
                    try {
                        Article article = crawler.getArticle(url);
                        if (article.getBody().length() > 0)
                            articles.add(article);
                    } catch (IOException | CrawlerCannotReadArticleException e) {
                        log.log(Level.WARNING, "Skipped article", e);
                    } finally {
                        progress.add(statusArticleUnit);
                    }
                }

                // Create front page and attach articles
                FrontPage p = new FrontPage();
                p.setArticles(articles);
                p.setNewspaper(crawler.getNewspaper());
                frontPages.add(p);

            } catch (Exception e) {
                log.log(Level.SEVERE, "Exception when retrieving articles with "+crawler.getClass().getName(), e);
            }
        }

        log.info("Articles download completed. Persisting articles...");

        //Persist articles on db
        float statusPageUnit = 5F/frontPages.size();
        

        for (FrontPage page : frontPages)
        {

            log.log (Level.INFO,"ALL THE "+page.getArticles().size()+" ARTICLES: "+page.getArticles().toString ());
            
            for (Article a : page.getArticles()) 
            {
				try
				{
					em.getTransaction().begin();
					List<Article> articles = em.createQuery("select a from Article a " +
							"where (a.title like concat('%', ?1,'%') or a.sourceUrl=?2) and a.source=?3 order by a.created desc", Article.class)
							.setParameter(1, a.getTitle())
							.setParameter(2, a.getSourceUrl())
							.setParameter(3, a.getSource())
							.getResultList();

					if (articles.size() > 0) {
						Article article = articles.get(0);
						// Replace detached article with the attached one
						page.getArticles().set(page.getArticles().indexOf(a), article);
						log.log(Level.INFO, "Skipped (exists) " + a.getTitle());
					} else {
						// If article doesn't exists on db
						// Persist it and attach
						em.persist(a);
						log.log(Level.INFO, "Persisted " + a.getTitle() + " from " + a.getSource().name());
					}
					em.getTransaction().commit();
				}
				catch ( Exception e )
				{
					log.log(Level.WARNING, "Skipped article because of an exception: "+a.getTitle()+" from "+ a.getSource().name()+"\n"+e);
					em.getTransaction().rollback();
				}
            }

			em.getTransaction().begin();
				
            //Persisting front page
            if (page.getArticles().size() > 0)
                em.persist(page);
            else
                log.warning("Front Page from "+page.getNewspaper().toString()+" has no articles. (Skipped)");
			
			em.getTransaction().commit();
			
            //Update progress
            progress.add(statusPageUnit);
        }

        em.close();

        progress.set(100);

        log.info("Articles download completed");
    }

}
