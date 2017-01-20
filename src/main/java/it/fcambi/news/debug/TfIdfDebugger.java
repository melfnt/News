package it.fcambi.news.debug;

import it.fcambi.news.model.Article;
import it.fcambi.news.data.WordVector;

import java.util.Random;
import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import it.fcambi.news.Logging;

public class TfIdfDebugger
{
	
	private static final Logger log = Logging.registerLogger(TfIdfDebugger.class.getName());
	    
	private static final long _SEED = 42;
	private static final int _DEBUG_ONE_ARTICLE_EVERY = 1000;
	private static final String _DEBUG_DIRECTORY = "debug/";
	
	private String filename_suffix = "";
	private int _A;
	private int _B;
	
	private Set<Long> already_debugged = new HashSet <Long>();
	
	public TfIdfDebugger ( String filename_suffix )
	{
		Random r = new Random ( _SEED );
		this._A = r.nextInt ( _DEBUG_ONE_ARTICLE_EVERY );
		this._B = r.nextInt ( _DEBUG_ONE_ARTICLE_EVERY );
		
		log.info("Initializing new TfIdfDebugger with parameters A="+_A+" and B="+_B);
		
		if ( ! new File ( _DEBUG_DIRECTORY ).exists () ) 
		{
			new File( _DEBUG_DIRECTORY ).mkdirs();
		}
		
		this.filename_suffix = filename_suffix;
		
	}
	
	public void conditional_debug ( Article a, WordVector vector )
	{
		long id = a.getId();
		try
		{		
			if (   (  ( this._A * id + this._B ) % _DEBUG_ONE_ARTICLE_EVERY  ) == 3  &&  ! already_debugged.contains (id)   )
			{
				log.info("conditional debugging on article with id="+id+". Its permutation is "+(  ( this._A * id + this._B ) % _DEBUG_ONE_ARTICLE_EVERY  ) );
				FileWriter fw = new FileWriter( _DEBUG_DIRECTORY + "article_" + id + filename_suffix );
				List <Double> values = vector.getValues ();
				List <String> words = vector.getWords ();
				for ( int i=0; i<values.size(); ++i )
				{
					Double v = values.get (i);
					String w = words.get (i);
					if ( v != 0 )
					{
						fw.write ( v + "\t" + w + "\n" );
					}
				}
				fw.close ();
				already_debugged.add (id);
			}
		} catch ( IOException e )
		{
			log.warning ("Error while writing to file for article with id"+id+": "+e.toString());
		}
	}
	
}
