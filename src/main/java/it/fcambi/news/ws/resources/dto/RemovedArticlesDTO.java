package it.fcambi.news.ws.resources.dto;

import it.fcambi.news.model.Article;
import it.fcambi.news.model.Newspaper;

import java.util.List;

public class RemovedArticlesDTO
{

    protected List<Long> removed_articles;
    protected long cluster_id;
    protected String annotator_name;
    
	public long getClusterId ()
	{
		return this.cluster_id;
	}
	
	public List<Long> getRemovedArticles ()
	{
		return this.removed_articles;
	}
	
	public String getAnnotatorName ()
	{
		return this.annotator_name;
	}
	
	public void setClusterId ( long id )
	{
		this.cluster_id = id;
	}
	
	public void setRemovedArticles ( List<Long> removed_articles )
	{
		this.removed_articles = removed_articles;
	}
	
	public void setAnnotatorName ( String name )
	{
		this.annotator_name = name;
	}
	
	public String toString ()
	{
		return annotator_name+" removed "+removed_articles.toString ()+" from cluster with id "+cluster_id;
	}
	
	public boolean is_valid ()
	{
		return this.cluster_id != -1;
	}

}
