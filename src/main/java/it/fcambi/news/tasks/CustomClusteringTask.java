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

import javax.persistence.TypedQuery;

public class CustomClusteringTask extends IncrementalClusteringTask 
{
	
    public CustomClusteringTask(MatchMapGeneratorConfiguration matchMapConfiguration, Matcher matcher,
                                     List<Article> articlesToBeClustered, Clustering clustering) 
    {
        super(matchMapConfiguration, matcher, articlesToBeClustered, clustering );
        
        EntityManager em = Application.createEntityManager();
        
        TypedQuery <Article> select_articles = em.createQuery("select a from Article a where a.id in (select article_id from ClusteringRevision)", Article.class);
        
        this.articlesToBeClustered = select_articles.getResultList();
        
        //~ System.out.println ("articles to be reclustered: "+this.articlesToBeClustered);
                
    }

}
