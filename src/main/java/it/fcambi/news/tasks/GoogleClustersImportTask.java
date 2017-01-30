package it.fcambi.news.tasks;

import it.fcambi.news.Application;
import it.fcambi.news.Logging;
import it.fcambi.news.async.Task;
import it.fcambi.news.crawlers.*;
import it.fcambi.news.model.Article;
import it.fcambi.news.model.News;
import it.fcambi.news.model.FrontPage;
import it.fcambi.news.model.Clustering;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Iterator;

import javax.persistence.TypedQuery;
import javax.persistence.NoResultException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

import java.util.function.Function;
import java.util.stream.Collectors;

public class GoogleClustersImportTask extends Task
{

    private static final Logger logger = Logging.registerLogger(GoogleClustersImportTask.class.getName());

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public String getDescription() {
        return "Import google news full coverage in a local clustering called 'google_news'";
    }

    @Override
    public void executeTask() throws IllegalArgumentException, UnsupportedEncodingException, IOException
    {
		logger.info ("task started");
        progress.set(0);
        EntityManager em = Application.createEntityManager();
		
		Clustering clustering = em.find( Clustering.class, "google_news" );
        if (clustering == null)
        {
			throw new IllegalArgumentException("Cannot find Google News clustering");
        }
		
        Map <String, Article> to_be_clustered = get_article_to_be_clustered ( em );
        
        Map <String, Article> already_clustered = get_already_clustered_articles ( em, clustering );
		
        // in case some articles from today has already been clustered 
        already_clustered.forEach ( (url,article) -> to_be_clustered.remove ( url ) );
        
        logger.info ("articles to be clustered: "+to_be_clustered);
		logger.info ("articles already clustered: "+already_clustered);
		
		
        while ( to_be_clustered.size () > 0 )
        {
			Article article = get_one_article ( to_be_clustered );
			String search_url = "https://www.google.com/search?hl=it&gl=it&tbm=nws&authuser=0&q=" + URLEncoder.encode( article.getTitle (), "UTF-8");
			
			logger.info ("picked a random article from the ones that have to be clustered: "+article);
			logger.info ("search url: "+search_url);
			
			Document search_result_page = Jsoup.connect( search_url ).userAgent("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:51.0) Gecko/20100101 Firefox/51.0").referrer("https://news.google.com/").get();
			Elements full_coverage_a_elms = search_result_page.select ( "div.g a._T6c" );
			
			// it does exist a full_coverage for article
			if ( full_coverage_a_elms.size() > 0 )
			{
				// TODO: what if there is more than one full coverage?
				String full_coverage_url = full_coverage_a_elms.get(0).attr("href");
					
				logger.info ("Full coverage do exist");
				logger.info ("Full coverage url: "+full_coverage_url);
				
				// TODO: other pages than the first
				Document full_coverage_page = Jsoup.connect( full_coverage_url ).userAgent("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:51.0) Gecko/20100101 Firefox/51.0").referrer("https://news.google.com/").get();
				Elements subjects_a_elms = full_coverage_page.select ( "div.topic div.story a" );
				
				News known_news = null;
				
				// check if an already clustered article is in the coverage and retrieve its news
				for ( Element subject_a_elm : subjects_a_elms )
				{
					String subject_url = subjects_a_elms.attr("href");
					logger.info ("subject url in full coverage: "+subject_url);
					Article subject = already_clustered.get ( subject_url );
					if ( subject != null )
					{
						known_news = subject.getNews ( clustering );
						logger.info ("It was already clustered by us: it news is "+known_news);
					}
				}
				
				// if no already clustered article is in the coverage, let's create a new one
				if ( known_news == null )
				{
					known_news = News.createForArticle ( article, clustering );
					logger.info ("It was not already clustered by us: I'll initialize another news for it: "+known_news);
					article.setNews ( clustering, known_news );
					to_be_clustered.remove ( article.getSourceUrl() );
				}
				
				// put all the articles to be clustered in the coverage in the same news in which there is article
				for ( Element subject_a_elm : subjects_a_elms )
				{
					String subject_url = subjects_a_elms.attr("href");
					Article subject = to_be_clustered.get ( subject_url );
					if ( subject != null )
					{
						logger.info ("subject url in full coverage that needs to be clustered: "+subject_url);
						subject.setNews ( clustering, known_news );
						to_be_clustered.remove ( subject_url );
					}
				}
			
			}
			else // article has not a full coverage
			{
				News known_news = News.createForArticle ( article, clustering );
				logger.info ("full coverage does not exist, I've created a new news for this article: "+known_news);
				article.setNews ( clustering, known_news );
				to_be_clustered.remove ( article.getSourceUrl() );
			}
		
		}
        
	}
	
	private Map <String, Article> get_already_clustered_articles ( EntityManager em, Clustering clustering )
	{
        
        Map <String, Article> already_clustered;
        try
        {
			TypedQuery<Article> select = em.createQuery("select a from News n join n.articles a where n.clustering=:clustering", Article.class)
					 .setParameter("clustering", clustering);
			already_clustered = select.getResultList().stream().filter( article-> article.getSourceUrl()!=null ).collect(Collectors.toMap( Article::getSourceUrl, Function.identity() ));
        }
        catch (NoResultException e) 
        {
            already_clustered = new HashMap <String, Article> ();
		}
		
		return already_clustered;
       
	}
	
	private Map <String, Article> get_article_to_be_clustered ( EntityManager em )
	{
		
        TypedQuery<Article> select = em.createQuery("select a from Article a where TIMESTAMPDIFF(HOUR, a.created, NOW()) < 24", Article.class);
        Map <String, Article> to_be_clustered;
        try
        {
			to_be_clustered = select.getResultList().stream().filter( article-> article.getSourceUrl()!=null ).collect(Collectors.toMap( Article::getSourceUrl, Function.identity() ));
		}
		catch (NoResultException e)
		{
			to_be_clustered = new HashMap <String, Article> ();
		}
		return to_be_clustered;
	}
	
	private Article get_one_article ( Map <String,Article> article_map )
	{
		Map.Entry<String,Article> entry=article_map.entrySet().iterator().next();
		return entry.getValue();
	}
	
}
