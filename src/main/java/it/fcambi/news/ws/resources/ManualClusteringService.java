package it.fcambi.news.ws.resources;

import it.fcambi.news.Application;
import it.fcambi.news.Logging;
import it.fcambi.news.model.Article;
import it.fcambi.news.model.ClusteringRevision;
import it.fcambi.news.ws.resources.dto.RemovedArticlesDTO;
import it.fcambi.news.ws.resources.dto.ClusterDTO;
import it.fcambi.news.ws.resources.dto.ArticleDTO;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.persistence.TypedQuery;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.logging.Logger;
import java.util.Random;
import java.util.List;
import java.util.stream.Collectors;

@Path("/manualClustering")
public class ManualClusteringService
{
	
	private final long _FIRST_SELECTABLE_CLUSTER = 76506;
	private final int _NUMBER_OF_ARTICLES_TO_BE_SHOWN = 100;
	private final Logger log = Logging.registerLogger("it.fcambi.news.ManualClustering");
	private final Random random_generator = new Random ();
	
	@POST
    @Produces(MediaType.APPLICATION_JSON)
	public Response getARandomClusterToPerformTask () 
	{
        try
        {
			log.info("Anonymus user wants to perform a manual clustering");
			
			ClusterDTO cluster = getRandomCluster ();
			
			//System.out.println ("articleDTOs = " + articleDTOs.toString ());
			
			return Response.status(200).entity(cluster).build();
		}
		catch (Exception e)
		{
			System.err.println (e);
			e.printStackTrace ();
		}
		return null;
    }
    
    private ClusterDTO getRandomCluster ()
    {
		EntityManager em = Application.createEntityManager();
		long selected_cluster = em.createQuery("select n.id from News n join n.articles k where n.id>:firstSelectableCluster group by n.id having count(*)>1 order by rand()", Long.class)
								.setParameter("firstSelectableCluster", _FIRST_SELECTABLE_CLUSTER-1)
								.setMaxResults( 1 )
								.getSingleResult().longValue();
				
		TypedQuery<Article> select = em.createQuery("select a from News n join n.articles a where n.id=:newsId order by rand()", Article.class)
									 .setParameter("newsId", selected_cluster)
									 .setMaxResults( _NUMBER_OF_ARTICLES_TO_BE_SHOWN );
		
		List <ArticleDTO> articleDTOs = select.getResultList().stream()
			.map(ArticleDTO::createFrom)
			.collect(Collectors.toList());
		
		ClusterDTO cluster = new ClusterDTO ( selected_cluster, articleDTOs );
		
		em.close();
			
		return cluster;
	}
	
	@POST
	@Path("/submit")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response authenticateUser( RemovedArticlesDTO param )
    {
		
        if ( param.is_valid () )
        {
			log.info (param.toString());
			System.out.println (param.toString());
			
			EntityManager em = Application.createEntityManager();
			List <Long> removed_articles = param.getRemovedArticles ();
			
			String annotator_name = param.getAnnotatorName ();
			long news_id = param.getClusterId ();
			
			for ( Long article_id: removed_articles )
			{
			
				ClusteringRevision cr = new ClusteringRevision ();
				
				
				cr.setAnnotatorName ( annotator_name );
				cr.setArticleId ( article_id.longValue() );
				cr.setNewsId ( news_id );

				System.out.println ("adding to db line: "+cr.toString());
				
				em.getTransaction().begin();
				em.persist(cr);
				em.getTransaction().commit();
			}
        }
        return Response.ok().entity("Ok.").build();
    }
    
}
