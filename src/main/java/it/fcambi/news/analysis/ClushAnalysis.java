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
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.io.FileWriter;

public class ClushAnalysis
{
	
	private static final String _DEBUG_DIRECTORY = "debug/";
	private String clustering_name;
	private String date_from = "2017-02-15 00:00:00";
	private String date_to = "2017-02-22 00:00:00";
	//~ private String date_from = "2015-10-13 12:19:03";
	//~ private String date_to = "2017-02-22 12:43:31";
	
	private Map <News, Integer> won_clush = new HashMap <News, Integer> ();

	public ClushAnalysis ( String clustering_name )
	{
		this.clustering_name = clustering_name;
	}
	
	private void add_a_won_clush ( News n )
	{
		Integer prev = won_clush.get(n);
		if ( prev == null )
		{
			won_clush.put ( n, 0 );
			prev = 0;
		}
		won_clush .put ( n, prev+1 );
	}
	
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
	
	public void do_analysis ( )
	{
		EntityManager em = Application.createEntityManager();
		
		Clustering clustering = em.find (Clustering.class, clustering_name);
		
		List<Article> articles = em.createQuery("select a from Article a where key(a.news) = :clustring and a.created between :datefrom and :dateto ", Article.class)
								.setParameter ("datefrom", parse_date(date_from))
								.setParameter ("dateto", parse_date(date_to))
								.setParameter ("clustring", clustering_name)
								.getResultList();
								
			
		articles.stream().parallel().forEach ( a ->
		//~ articles.stream().forEach ( a ->
		{
			FrontPage first_frontpage_for_a = a.getFrontPages().stream().min ( (fp1,fp2) -> fp1.getTimestamp().compareTo ( fp2.getTimestamp() ) ).get();
			int order = first_frontpage_for_a.orderOf (a);
			
			if ( order!= 0 )
			{
				//~ System.out.println ("article "+a+" apperaed in position "+order+" different from the first in frontpage "+first_frontpage_for_a.getId());
				List <Article> articles_in_frontpage = first_frontpage_for_a.getArticles ();
				
				for ( int i=0; i<order; ++i )
				{
					Article other = articles_in_frontpage.get (i);
					//~ System.out.println ("article "+other+" apperaed before, incrementing its cluster");
					
					add_a_won_clush ( other.getNews ( clustering ) );
					
				}
				
				//~ new Scanner ( System.in ).next();
				
			}
			
		});
			
		try
		{
			FileWriter fw = new FileWriter( _DEBUG_DIRECTORY + "number_of_news" );
			
			for ( Map.Entry<News, Integer> entry : won_clush.entrySet() )
			{
				News news = entry.getKey ();
				Integer how_many = entry.getValue ();
				fw.write (news.getId()+"\t"+how_many+"\n");
			}
			
			fw.close ();
		}
		catch ( Exception e )
		{
			System.err.println (e);
		}
		
		
		
	}
	
    
}
