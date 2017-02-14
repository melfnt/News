package it.fcambi.news.crawlers;
import java.util.List;

import it.fcambi.news.model.Article;
import it.fcambi.news.model.Newspaper;

public class IlPostCrawler extends SimpleCrawler
{
	
	public IlPostCrawler ()
	{
		super ();
		this.newspaper = Newspaper.IL_POST;
		this.homepage_url = "http://www.ilpost.it/";
		this.rss_url = "http://www.ilpost.it/feed/";
		this.article_url_regex = "http://www\\.ilpost\\.it/[0-9]{4}/[0-9]{2}/[0-9]{2}/.+/";
		
		add_xpath_target ( "title", "h1.entry-title", "" );
		add_xpath_target ( "description", "h2.tit2", "" );
		add_xpath_target ( "body", "#singleBody", "" );
		
		add_article_link_in_homepage_target ( "#content article a" );
		add_article_link_in_homepage_target ( "#content .lanci a" );
		
	}
	
}
