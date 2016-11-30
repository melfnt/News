package it.fcambi.news.ws.resources;

import it.fcambi.news.Application;
import it.fcambi.news.Logging;
import it.fcambi.news.model.auth.Session;
import it.fcambi.news.model.auth.User;
import it.fcambi.news.model.Article;
import it.fcambi.news.ws.resources.dto.ArticleDTO;
import it.fcambi.news.ws.resources.dto.ManualClusteringBeginRequestDTO;

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
	
	private final long _FIRST_SELECTABLE_CLUSTER = 66000;
	private final int _NUMBER_OF_ARTICLES_TO_BE_SHOWN = 20;
	private final Logger log = Logging.registerLogger("it.fcambi.news.ManualClustering");
	private final Random random_generator = new Random ();
	
	@POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
	public Response authenticateUser ( ManualClusteringBeginRequestDTO begin_request ) 
	{
        try
        {
			log.info("User "+begin_request.getName ()+" wants to perform a manual clustering");
			
			List <ArticleDTO> articleDTOs;
			
			do
			{
				articleDTOs = getRandomCluster ();
			}
			while ( articleDTOs.size() < 2 );
			
			System.out.println ("articleDTOs = " + articleDTOs.toString ());
			
			return Response.status(200).entity(articleDTOs).build();
		}
		catch (Exception e)
		{
			System.err.println (e);
			e.printStackTrace ();
		}
		return null;
    }
    
    private List <ArticleDTO> getRandomCluster ()
    {
		EntityManager em = Application.createEntityManager();
		//~ long max_cluster = em.createQuery("select max(id) from News", Long.class).getSingleResult().longValue();
		//~ long selected_cluster = _FIRST_SELECTABLE_CLUSTER + Math.abs (random_generator.nextLong ()) % ( max_cluster - _FIRST_SELECTABLE_CLUSTER );
		//~ long selected_cluster = 66843;
		long selected_cluster = em.createQuery("select n.id from News n join n.articles k where n.id>:firstSelectableCluster group by n.id having count(*)>1 order by rand()", Long.class)
								.setParameter("firstSelectableCluster", _FIRST_SELECTABLE_CLUSTER-1)
								.setMaxResults( 1 )
								.getSingleResult().longValue();
		
		//~ System.out.println ("max_cluster = " + max_cluster);
		System.out.println ("selected_cluster = "+ selected_cluster);
		
		//~ TypedQuery<Article> select = em.createQuery("select a from Article a join a.news n where n.id=:newsId order by rand()", Article.class)
		TypedQuery<Article> select = em.createQuery("select a from News n join n.articles a where n.id=:newsId order by rand()", Article.class)
									 .setParameter("newsId", selected_cluster)
									 .setMaxResults( _NUMBER_OF_ARTICLES_TO_BE_SHOWN );
		
		List <ArticleDTO> articleDTOs = select.getResultList().stream()
			.map(ArticleDTO::createFrom)
			.collect(Collectors.toList());
		
		em.close();
			
		return articleDTOs;
	}
}
