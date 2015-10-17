package it.fcambi.news.model;

/**
 * Created by Francesco on 24/09/15.
 */

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Represent an Article from a newspaper
 */
@Entity
public class Article {

    /**
     * Unique key for article
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /**
     * Headline of the article
     */
    private String title;

    /**
     * Bunch of text that introduces the content of the article
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Content of the article
     */
    @Lob
    private String body;

    /**
     * Where the article came from
     */
    private Newspaper source;

    @ManyToMany(mappedBy = "articles")
    @OrderBy("timestamp")
    @JsonBackReference
    private List<FrontPage> frontPages;

    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updated;

    @ManyToOne
    @JsonManagedReference
    private News news;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private ArticleHtmlSource sourceHtml;

    @Column(columnDefinition = "TEXT")
    private String sourceUrl;


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Newspaper getSource() {
        return source;
    }

    public void setSource(Newspaper source) {
        this.source = source;
    }

    public Date getCreated() {
        return created;
    }

    public Date getUpdated() {
        return updated;
    }

    public String getSourceHtml() {
        return sourceHtml.getHtml();
    }

    public void setSourceHtml(String sourceHtml) {
        this.sourceHtml = new ArticleHtmlSource();
        this.sourceHtml.setHtml(sourceHtml);
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public News getNews() {
        return news;
    }

    public void setNews(News news) {
        this.news = news;
    }

    public List<FrontPage> getFrontPages() {
        return frontPages;
    }

    @PrePersist
    protected void onCreate() {
        created = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updated = new Date();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Article
        && ((Article) obj).getId() > 0
        && ((Article) obj).getId() == this.id);
    }
}
