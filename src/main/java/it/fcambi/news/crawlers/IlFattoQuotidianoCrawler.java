package it.fcambi.news.crawlers;
import java.util.List;

import it.fcambi.news.model.Article;
import it.fcambi.news.model.Newspaper;

public class IlFattoQuotidianoCrawler extends SimpleCrawler
{
	
	public IlFattoQuotidianoCrawler ()
	{
		super ();
		this.newspaper = Newspaper.IL_FATTO_QUOTIDIANO;
		this.homepage_url = "http://www.ilfattoquotidiano.it/";
		this.rss_url = "http://www.ilfattoquotidiano.it/feed/";
		this.article_url_regex = "http://www.ilfattoquotidiano.it/[0-9]{4}/[0-9]{2}/[0-9]{2}/[^/]+/[0-9]+/";
		
		add_xpath_target ( "title", "[itemprop=headline]", "" );
		add_xpath_target ( "title", ".main-article h1", "" );
		add_xpath_target ( "description", ".catenaccio > p", "" );
		add_xpath_target ( "body", "[id=article-body-id]", "" );
		add_xpath_target ( "body", ".article-body", ".inner-pagination" );
		
		add_article_link_in_homepage_target ( ".primo-piano a" );
		add_article_link_in_homepage_target ( ".right-column a" );
		add_article_link_in_homepage_target ( ".left-column a" );
		
		
	}
	
}
