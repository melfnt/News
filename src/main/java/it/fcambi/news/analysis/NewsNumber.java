package it.fcambi.news.analysis;

import it.fcambi.news.Application;
import it.fcambi.news.async.Task;
import it.fcambi.news.clustering.MatchMapGenerator;
import it.fcambi.news.clustering.MatchMapGeneratorWithGlobalTfIdf;
import it.fcambi.news.clustering.MatchMapGeneratorConfiguration;
import it.fcambi.news.clustering.Matcher;
import it.fcambi.news.clustering.MatcherFactory;
import it.fcambi.news.metrics.Metric;
import it.fcambi.news.model.*;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.io.FileWriter;

public class NewsNumber
{
	
	private static final String _DEBUG_DIRECTORY = "debug/";
	private String clustering_name;
	//~ private String date_from = "2017-02-15 00:00:00";
	//~ private String date_to = "2017-02-22 00:00:00";
	private String date_from = "2015-10-13 12:19:03";
	private String date_to = "2017-02-22 12:43:31";
	
	private Timestamp parse_date ( String date_str )
	{
		try
		{
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
			Date parsedDate = dateFormat.parse( date_str );
			return new Timestamp(parsedDate.getTime());
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	public NewsNumber ( String clustering_name )
	{
		this.clustering_name = clustering_name;
	}
	
	/**
	 * 
	 * time range in minutes
	 * 
	 * */
	public void do_analysis ( long time_range )
	{
		EntityManager em = Application.createEntityManager();
		
		Clustering clustering = em.find (Clustering.class, clustering_name);
		
		List<Article> articles = em.createQuery("select a from Article a where key(a.news) = :clustring and a.created between :datefrom and :dateto ", Article.class)
								.setParameter ("datefrom", parse_date(date_from))
								.setParameter ("dateto", parse_date(date_to))
								.setParameter ("clustring", clustering_name)
								.getResultList()
								.stream().sorted ((a1, a2) -> a1.getCreated().compareTo(a2.getCreated())).collect(Collectors.toList());
		
		try
		{
			FileWriter fw = new FileWriter( _DEBUG_DIRECTORY + "number_of_news" );
			
			Set <News> news = new HashSet <News> ();
			Timestamp end = parse_date ( date_to );
			
			int total_articles = articles.size();
			
			for ( Timestamp current = parse_date ( date_from ); current.before (end); current = new Timestamp(current.getTime() + ( time_range * 60000L)) )
			{
				Iterator <Article> it = articles.iterator();
				while ( it.hasNext() )
				{
					Article a = it.next();
					if ( a.getCreated().before(current) )
					{
						news.add ( a.getNews ( clustering ) );
						it.remove();
					}
					else
					{
						break;
					}
				}
				
				fw.write ( (total_articles - articles.size() ) + "\t" +news.size()+"\n" );
			}
			
			fw.close ();
		}
		catch ( Exception e )
		{
			System.err.println (e);
		}
		
		
		
	}
	
    
}
