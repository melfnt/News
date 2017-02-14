package it.fcambi.news.crawlers;

import it.fcambi.news.model.Article;
import it.fcambi.news.model.Newspaper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import it.fcambi.news.Logging;

import java.util.logging.Logger;


import java.util.regex.Pattern;

public class SimpleCrawler implements Crawler
{
	private Logger logger;
	
	private Map <String, List<XPathTarget>> xpath_targets = new HashMap <String, List<XPathTarget>> ();
	private List <String> article_link_in_homepage_targets = new LinkedList <String> ();
	private List <String> article_link_in_rss_targets = new LinkedList <String> ();
	private Pattern URL_PATTERN;
	
	protected Newspaper newspaper;
	protected String homepage_url = "www.example.com";
	protected String rss_url = "www.example.com";
	protected String article_url_regex = null;
	
	public SimpleCrawler ()
	{
		xpath_targets.put ("body", new LinkedList<XPathTarget>());
		xpath_targets.put ("title", new LinkedList<XPathTarget>());
		xpath_targets.put ("description", new LinkedList<XPathTarget>());
	}
	
	private String get_first_available_target ( List <XPathTarget> targets, Document d )
	{
		for ( XPathTarget target : targets )
		{
			Elements found = target.select ( d );
			StringBuilder ret = new StringBuilder ();
			for ( Element el: found )
			{
				ret.append ( el.text ()+". " );
			}
			if ( ret.length() > 0 )
			{
				return ret.toString ();
			}
		}
		return null;
	}
	
	public Article getArticle(String url) throws IOException, CrawlerCannotReadArticleException
	{
		Document d = Jsoup.connect( url ).get();
		String title = get_first_available_target ( xpath_targets.get("title"), d );
		String description = get_first_available_target ( xpath_targets.get("description"), d );
		String body = get_first_available_target ( xpath_targets.get("body"), d );
		
		if ( title == null ) throw new CrawlerCannotReadArticleException ( "Can't find article title on "+url+"\n Paths searched: "+xpath_targets.get("title") );
		if ( description == null ) description = "";
		if ( body == null ) throw new CrawlerCannotReadArticleException ( "Can't find article body on "+url+"\n Paths searched: "+xpath_targets.get("body") );
		
		Article a = new Article ();
		a.setSource ( this.newspaper );
		a.setSourceUrl( url );
		a.setTitle ( title );
		a.setDescription ( description );
		a.setBody(body);
		
		return a;
	}

	
	private Elements retrieve_article_link_elms ( String url, List<String> paths_to_link ) throws IOException
	{
		Document d = Jsoup.connect( url ).get();
		Elements el = new Elements ();
		
		URL_PATTERN = Pattern.compile(article_url_regex);
		
		//~ logger.info ("order in which the paths are evaluated:");
		for ( String path : paths_to_link )
		{
			//~ logger.info (" --> "+path);
			el.addAll ( d.select ( path ) );
		}
		
		return el;
	}
	
	private List <String> get_href ( Elements links )
	{
		Set <String> urls = new LinkedHashSet <String> ();

		String url;
		//~ logger.info ("getting href...");
        for ( Element e: links ) 
        {
            url = e.attr("href");
            int queryStringIndex = url.lastIndexOf('?');
            if (queryStringIndex > 0)
                url = url.substring (0, queryStringIndex);
            if ( url.startsWith (".") )
				url = homepage_url+"/"+url.substring (1);
            if ( url.startsWith ("/") )
				url = homepage_url+url.substring (1);
            
            if (isArticleAtUrlParsable(url))
            {
				//~ logger.info (" --> "+url);
                urls.add(url);
			}
        }
        
        //~ logger.info ("returning "+new LinkedList (urls).toString());
        
		return new LinkedList (urls);
	}
	
	private List <String> get_text ( Elements links )
	{
		Set <String> urls = new HashSet <String> ();

		String url;
        for ( Element e: links ) 
        {
            url = e.text();
            int queryStringIndex = url.lastIndexOf('?');
            if (queryStringIndex > 0)
                url = url.substring (0, queryStringIndex);
            if (isArticleAtUrlParsable(url))
                urls.add(url);
        }
        
		return new LinkedList <String> (urls);
	}
	
    public List<String> retrieveArticleUrlsFromHomePage() throws IOException
    {
		URL_PATTERN = Pattern.compile( article_url_regex );
		Elements links = retrieve_article_link_elms ( homepage_url, article_link_in_homepage_targets );
		return get_href ( links );
	}

    public Collection<String> retrieveArticleUrlsFromFeed() throws IOException
    {
		URL_PATTERN = Pattern.compile( article_url_regex );
		Elements links = retrieve_article_link_elms ( rss_url, article_link_in_rss_targets );
		return get_text ( links );
	}

    public Newspaper getNewspaper()
    {
		return this.newspaper;
	}
	
	public void add_xpath_target ( String field, String path_to_include, String path_to_exclude )
	{
		List <XPathTarget> targets = xpath_targets.get ( field );
		if ( targets != null )
		{
			targets.add ( new XPathTarget ( path_to_include, path_to_exclude ) );
		}
	}
	
	public void add_article_link_in_homepage_target ( String path_to_a_elm )
	{
		article_link_in_homepage_targets.add ( path_to_a_elm );
	}
	
	public void add_article_link_in_rss_target ( String path_to_a_elm )
	{
		article_link_in_rss_targets.add ( path_to_a_elm );
	}
	
	private boolean isArticleAtUrlParsable ( String url )
	{
		return URL_PATTERN.matcher(url).matches();
    }
    
    public void test ()
	{
		logger = Logging.registerLogger("it.fcambi.news.crawlers.SimpleCrawler_"+newspaper);
		System.out.println ("testing crawler for "+newspaper+"...");
		try
		{
		
			List <String> urls = retrieveArticleUrlsFromHomePage ();

			logger.info ("-------------");
			logger.info (urls.size()+" urls in homepage");
			logger.info ("-------------");
			
			logger.info (urls.toString());

			for ( String url : urls )
			{
				Article a = getArticle (url);
				logger.info ("downloaded article");
				logger.info (" url = "+url);
				logger.info (" title = "+a.getTitle ());
				logger.info (" description = "+a.getDescription ());
				logger.info (" Body = "+a.getBody ());			
			}
			logger.info ("RIUSCITO");
		}
		catch ( Exception e )
		{
	        logger.info ("NON RIUSCITO");
	        logger.info (e.toString());
		}
		System.out.println ("finished: see logs/it.fcambi.news.crawlers.SimpleCrawler_"+newspaper+" for details");
	}
	
}
