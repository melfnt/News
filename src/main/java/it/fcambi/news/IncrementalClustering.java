package it.fcambi.news;

import it.fcambi.news.clustering.MatchMapGenerator;
import it.fcambi.news.clustering.MatchMapGeneratorConfiguration;
import it.fcambi.news.clustering.HighestMeanOverThresholdMatcher;
import it.fcambi.news.clustering.Matcher;
import it.fcambi.news.data.TFIDFWordVectorFactory;
import it.fcambi.news.filters.NoiseWordsTextFilter;
import it.fcambi.news.filters.StemmerTextFilter;
import it.fcambi.news.metrics.CosineSimilarity;
import it.fcambi.news.metrics.Metric;
import it.fcambi.news.model.*;

import javax.persistence.EntityManager;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Francesco on 29/10/15.
 */
public class IncrementalClustering {

    public static void main(String[] args) {

        PersistenceManager persistenceManager = new PersistenceManager("it.fcambi.news.jpa.local");
        EntityManager em = persistenceManager.createEntityManager();

        List<Article> articles = em.createQuery("select a from Article a where a.news is not null", Article.class)
                .getResultList();

        List<Article> classifiedArticles = new ArrayList<>();

        Metric metric = new CosineSimilarity();
        TFDictionary dictionary = em.find(TFDictionary.class, "italian_stemmed");

        MatchMapGeneratorConfiguration conf = new MatchMapGeneratorConfiguration()
                .addMetric(metric)
                .addTextFilter(new NoiseWordsTextFilter())
                .addTextFilter(new StemmerTextFilter())
                .setWordVectorFactory(new TFIDFWordVectorFactory(dictionary));
        MatchMapGenerator generator = new MatchMapGenerator(conf);

        NumberFormat percent = NumberFormat.getPercentInstance();
        percent.setMaximumFractionDigits(2);

        for (int i=0; i<articles.size()-1; i++) {

            Map<Article, List<MatchingArticle>> map = generator.generateMap(articles.subList(i, i+1), classifiedArticles);

            Matcher matcher = new HighestMeanOverThresholdMatcher();
            Map<Article, MatchingNews> bestMatch = matcher.findBestMatch(metric, map, 0.47);

            bestMatch.keySet().forEach(article -> {
                if (bestMatch.get(article) != null) {
                    article.setNews(bestMatch.get(article).getNews());
                } else {
                    article.setNews(new News());
                    article.getNews().setDescription(article.getTitle());
                    article.getNews().setArticles(new ArrayList<>());
                }
                article.getNews().getArticles().add(article);
                classifiedArticles.add(article);
            });

            System.out.println("Clustering status "+percent.format((double)i/(articles.size()-1)));

        }

        Set<News> generatedClusters = new HashSet<>();
        classifiedArticles.forEach(article -> generatedClusters.add(article.getNews()));

        List<News> expectedClusters = em.createQuery("select n from News n", News.class).getResultList();

        //Now check congruency between predicted and effective graph
        DoubleSummaryStatistics stats = generatedClusters.stream().mapToDouble(row -> {

            return expectedClusters.stream().map(col -> {

                //Compute jaccard
                long intersection = row.getArticles().stream().filter(a -> col.getArticles().contains(a)).collect(Collectors.counting());
                long union = Stream.of(row, col).flatMap(l -> l.getArticles().stream()).collect(Collectors.toSet()).size();

                return (double)intersection / union;

            }).max(Double::compare).get();

        }).summaryStatistics();

        System.out.println("Average Jacc: "+stats.getAverage());
        System.out.println("Min "+stats.getMin()+"\tMax "+stats.getMax());
        System.out.println("# of clusters obtained "+stats.getCount());
        System.out.println("# of clusters expected "+expectedClusters.size());

        em.close();
        persistenceManager.close();

    }

}
