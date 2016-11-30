package it.fcambi.news.ws.resources.dto;

import it.fcambi.news.model.Article;
import it.fcambi.news.model.Newspaper;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ArticleDTO
{

    protected long id;
    protected String title;
    protected String url;
    protected String body;
    protected Date created;
    protected String sourceNewspaper;
    
    public static ArticleDTO createFrom (Article a) 
    {
        ArticleDTO o = new ArticleDTO();
        o.created = a.getCreated ();
        o.id = a.getId();
        o.title = a.getTitle();
        o.url = a.getSourceUrl ();
        o.body = a.getBody ();
        o.sourceNewspaper = a.getSource ().toString ();
        System.out.println ("adding article...");
        return o;
    }

    public long getId()
    {
        return id;
    }

    public String getTitle()
    {
        return title;
    }

    public String getSource()
    {
        return sourceNewspaper;
    }
    
    public String getUrl()
    {
        return url;
    }

    public Date getCreated() {
        return created;
    }
    
    public String getBody() {
        return body;
    }
    
    public String toString ()
    {
		return "\""+title+"\",content="+body+"\n";
	}
	
	public ArticleDTO trimBody ( int char_number )
	{
		String dots = "...";
		if ( char_number >= dots.length () )
		{
			int start = 0;
			int end =  char_number - dots.length ();
			body = body.substring (start, end)+dots;
		}
		else
		{
			body = dots;
		}
		return this;
	}

}
