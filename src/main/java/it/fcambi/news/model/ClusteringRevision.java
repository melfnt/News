package it.fcambi.news.model;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

import java.io.Serializable;

@Entity
@Table(name = "manually_removed_from_article_news")
public class ClusteringRevision implements Serializable
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String annotator_name;

    private long article_id;
    
	private long news_id;

    public String getAnnotatorName ()
    {
		return annotator_name;
	}

    public long getId ()
    {
		return id;
	}
    
    public long getArticleId ()
    {
		return article_id;
	}
    
	public long getNewsId ()
	{
		return news_id;
	}
	
    public void setAnnotatorName ( String annotator_name ) 
    {
		this.annotator_name = annotator_name;
	}
    
    public void setArticleId ( long article_id ) 
    {
		this.article_id = article_id;
	}
    
	public void setNewsId ( long news_id ) 
	{
		this.news_id = news_id;
	}
		
	public String toString ()
	{
		return "annotator:"+annotator_name+"\tnews:"+news_id+"\tarticle:"+article_id;
	}
	
}
