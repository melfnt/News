package it.fcambi.news.tasks;

import it.fcambi.news.Application;
import it.fcambi.news.async.Task;
import it.fcambi.news.clustering.MatchMapGenerator;
import it.fcambi.news.clustering.MatchMapGeneratorWithGlobalTfIdf;
import it.fcambi.news.clustering.MatchMapGeneratorConfiguration;
import it.fcambi.news.clustering.Matcher;
import it.fcambi.news.model.*;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.*;
import java.util.stream.Collectors;

//DEBUG
import it.fcambi.news.debug.CardinalityDebugger;

/**
 * Created by Francesco on 06/11/15.
 */
public class IncrementalClusteringTask extends Task {

    private MatchMapGeneratorConfiguration matchMapConfiguration;
    private Matcher matcher;
    protected List<Article> articlesToBeClustered;
    private Clustering clustering;
	
    public IncrementalClusteringTask(MatchMapGeneratorConfiguration matchMapConfiguration, Matcher matcher,
                                     List<Article> articlesToBeClustered, Clustering clustering) {
        super();
        this.matchMapConfiguration = matchMapConfiguration;
        this.matcher = matcher;
        this.articlesToBeClustered = articlesToBeClustered;
        this.clustering = clustering;
        
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public String getDescription() {
        return "Groups articles about the same event.";
    }

    @Override
    protected void executeTask() throws Exception {
        progress.set(0);

        if (Thread.interrupted()) return;

        EntityManager em = Application.createEntityManager();
        em.getTransaction().begin();
        clustering = em.merge(clustering);

        articlesToBeClustered = articlesToBeClustered.stream().map(em::merge).collect(Collectors.toList());

		//DEBUG
		//~ CardinalityDebugger union_debugger = new CardinalityDebugger ( "UNION" );
		//~ CardinalityDebugger intersection_debugger = new CardinalityDebugger ( "INTERSECTION" );
		//~ CardinalityDebugger body_length_debugger = new CardinalityDebugger ( "BODY_LENGTH" );
		
        //~ //Configure match map generator
        //~ MatchMapGenerator matchMapGenerator = new MatchMapGenerator(matchMapConfiguration);
        //Configure match map generator so that it uses global tf idf vectors
        MatchMapGeneratorWithGlobalTfIdf matchMapGenerator = new MatchMapGeneratorWithGlobalTfIdf ( matchMapConfiguration );
        //~ MatchMapGeneratorWithGlobalTfIdf matchMapGenerator = new MatchMapGeneratorWithGlobalTfIdf ( matchMapConfiguration, union_debugger, intersection_debugger, body_length_debugger );

        //Prepare set with all articles from existing clusters
        String select = "select a from Article a where key(a.news)=:clusteringName";
        List<Article> classifiedArticles;
        try {
            classifiedArticles = em.createQuery(select, Article.class)
                    .setParameter("clusteringName", clustering.getName())
                    .getResultList();
        } catch (NoResultException e) {
            classifiedArticles = new Vector<>();
        }

        final double progressIncrementA = 0.99/articlesToBeClustered.size();

        //TODO Remove
        classifiedArticles.forEach(a -> {
            assert a.getNews(clustering) != null;
            
        });

        Set<News> newsToMerge = new HashSet<>();

		// FOR GLOBALTFIDF ONLY
        matchMapGenerator.process_articles_and_add_to_cache ( articlesToBeClustered );
        matchMapGenerator.process_articles_and_add_to_cache ( classifiedArticles );
        
        
        //Find a fitting cluster for each article one by one
        // updating classifiedArticles each iteration
        for (int i=0; i<articlesToBeClustered.size() && !Thread.currentThread().isInterrupted(); i++) {
            Article article = articlesToBeClustered.get(i);
			
            //Match map generation
            List<Article> articleToCluster = new LinkedList<>();
            articleToCluster.add(article);
            Map<Article, List<MatchingArticle>> matchMap = matchMapGenerator.generateMap(
                    articleToCluster, classifiedArticles);

            //Find best match
            Map<Article, MatchingNews> bestMatchMap = matcher.findBestMatch(matchMap);
            MatchingNews bestMatchingNews = bestMatchMap.get(article);
            if (bestMatchingNews != null) {
                // Cluster found, add article to cluster
                article.setNews(clustering, bestMatchingNews.getNews());
                article.getNews(clustering).addArticle(article);
            } else {
                // New cluster for this article
                News newCluster = News.createForArticle(article, clustering);
                newCluster = em.merge(newCluster);
                article.setNews(clustering, newCluster);
            }
            newsToMerge.add(article.getNews(clustering));

            //Updates classifiedArticle
            classifiedArticles.add(article);

            progress.add(progressIncrementA);

        }
		
		//~ // DEBUG
		//~ union_debugger.print_report ();
		//~ intersection_debugger.print_report ();
		//~ body_length_debugger.print_report ();

        if (!Thread.currentThread().isInterrupted()) {
            em.getTransaction().commit();
            progress.set(1);
        } else {
            em.getTransaction().rollback();
        }

        em.close();
    }
}
