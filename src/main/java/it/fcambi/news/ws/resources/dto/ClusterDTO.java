package it.fcambi.news.ws.resources.dto;

import it.fcambi.news.model.Article;
import it.fcambi.news.model.Newspaper;

import it.fcambi.news.ws.resources.dto.ArticleDTO;

import java.util.List;

public class ClusterDTO
{

    protected List <ArticleDTO> articles;
    protected long id;
    
	public ClusterDTO ( long cluster_id, List <ArticleDTO> articles )
	{
		this.id = cluster_id;
		this.articles = articles;
	}
	
	public long getId ()
	{
		return this.id;
	}
	
	public List<ArticleDTO> getArticles ()
	{
		return this.articles;
	}

}
