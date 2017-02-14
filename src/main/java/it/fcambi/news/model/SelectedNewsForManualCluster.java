package it.fcambi.news.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;

@Entity
@Table(name = "selected_news_for_manual_cluster")
public class SelectedNewsForManualCluster
{

    /**
     * Unique key for article
     */
    @Id
    private long id;

    public long getId()
    {
        return id;
    }

}
