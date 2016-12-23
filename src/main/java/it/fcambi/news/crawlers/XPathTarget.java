package it.fcambi.news.crawlers;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class XPathTarget 
{
	
	private String include;
	private String exclude;
	
	public XPathTarget ( String include, String exclude )
	{
		this.include = include;
		this.exclude = exclude;
	}
	
	public Elements select ( Document d )
	{
		Document copy = d.clone ();
		if ( ! exclude.equals ("") )
		{
			Elements to_remove = copy.select ( exclude );
			to_remove.remove ();
		}
		return copy.select ( include );
	}
	
	public String toString ()
	{
		return include;
	}
	
}
