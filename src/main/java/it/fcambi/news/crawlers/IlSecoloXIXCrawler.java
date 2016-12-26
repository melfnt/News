package it.fcambi.news.crawlers;
import java.util.List;

import it.fcambi.news.model.Article;
import it.fcambi.news.model.Newspaper;

public class IlSecoloXIXCrawler extends SimpleCrawler
{
	
	public IlSecoloXIXCrawler ()
	{
		super ();
		this.newspaper = Newspaper.IL_SECOLO_XIX;
		this.homepage_url = "http://www.ilsecoloxix.it/";
		this.rss_url = "http://www.ilsecoloxix.it/homepage/rss/homepage.xml";
		this.article_url_regex = "http://www\\.ilsecoloxix\\.it/p/(cultura|italia|mondo)/[0-9]{4}/[0-9]{2}/[0-9]{2}/.*";
		
		add_xpath_target ( "title", "#left-articolo h1", "" );
		add_xpath_target ( "description", "#non-existent", "" );
		add_xpath_target ( "body", "#testo-articolo p", "b" );
		
		add_article_link_in_homepage_target ( "#primo-piano a" );
		add_article_link_in_homepage_target ( "#secondo-piano a" );
		add_article_link_in_homepage_target ( "#rullo-left a" );
		add_article_link_in_homepage_target ( "#rullo-spalla a" );
		
	}
	
}
