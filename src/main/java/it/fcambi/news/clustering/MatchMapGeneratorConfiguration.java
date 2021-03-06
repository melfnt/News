package it.fcambi.news.clustering;

import it.fcambi.news.data.FrequenciesWordVectorFactory;
import it.fcambi.news.data.Text;
import it.fcambi.news.data.WordVectorFactory;
import it.fcambi.news.filters.TextFilter;
import it.fcambi.news.metrics.CosineSimilarity;
import it.fcambi.news.metrics.Metric;
import it.fcambi.news.model.Cluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Created by Francesco on 30/10/15.
 */
public class MatchMapGeneratorConfiguration {

    private List<TextFilter> textFilters = new ArrayList<>();
    private Collection<Metric> metrics = new HashSet<>();
    private Function<String, Text> stringToTextFn;
    private KeywordsSelectionFunction keywordSelectionFn;
    private BiPredicate<Cluster, Cluster> ignorePairPredicate;
    private WordVectorFactory wordVectorFactory;

    public static KeywordsSelectionFunction headlineAndCapitalsKeywords = (title, description, body) -> {

        Text capitals = body.words().stream()
                .filter(w -> w.length() > 0 && Character.isUpperCase(w.charAt(0)))
                .collect(Text.collector());

        return new Text(title, description, capitals);

    };

    public static KeywordsSelectionFunction headlineKeywords = (title, description, body) -> new Text(title, description);

    public static Function<String, Text> onlyAlphaSpaceSeparated = s -> new Text(s
//            .replaceAll("[à]+", "a")
//            .replaceAll("[èé]+", "e")
//            .replaceAll("[ì]", "i")
//            .replaceAll("[ò]", "o")
//            .replaceAll("[ù]", "u")
            .replaceAll("[^\\p{Alpha}\\p{Space}àéèìòù]", " "), "\\p{Space}+");

    public static BiPredicate<Cluster, Cluster> ignoreReflectiveMatch = (a,b) -> a.equals(b);

    public MatchMapGeneratorConfiguration setKeywordSelectionFunction(KeywordsSelectionFunction k) {
        this.keywordSelectionFn = k;
        return this;
    }

    public MatchMapGeneratorConfiguration setStringToTextFunction(Function<String, Text> fn) {
        this.stringToTextFn = fn;
        return this;
    }

    public MatchMapGeneratorConfiguration addTextFilter(TextFilter tf) {
        this.textFilters.add(tf);
        return this;
    }

    public void removeTextFilter(TextFilter tf) {
        this.textFilters.remove(tf);
    }

    public MatchMapGeneratorConfiguration addMetric(Metric m) {
        this.metrics.add(m);
        return this;
    }

    public void removeMetric(Metric m) {
        this.metrics.remove(m);
    }

    public MatchMapGeneratorConfiguration setIgnorePairPredicate(BiPredicate<Cluster, Cluster> p) {
        this.ignorePairPredicate = p;
        return this;
    }

    public MatchMapGeneratorConfiguration setWordVectorFactory(WordVectorFactory f) {
        this.wordVectorFactory = f;
        return this;
    }

    public List<TextFilter> getTextFilters() {
        return textFilters;
    }

    public Collection<Metric> getMetrics() {
        if (metrics.size() == 0)
            metrics.add(new CosineSimilarity());
        return metrics;
    }

    public Function<String, Text> getStringToTextFn() {
        if (stringToTextFn == null)
            stringToTextFn = MatchMapGeneratorConfiguration.onlyAlphaSpaceSeparated;
        return stringToTextFn;
    }

    public KeywordsSelectionFunction getKeywordSelectionFn() {
        if (keywordSelectionFn == null)
            keywordSelectionFn = MatchMapGeneratorConfiguration.headlineAndCapitalsKeywords;
        return keywordSelectionFn;
    }

    public BiPredicate<Cluster, Cluster> getIgnorePairPredicate() {
        if (ignorePairPredicate == null)
            ignorePairPredicate = MatchMapGeneratorConfiguration.ignoreReflectiveMatch;
        return ignorePairPredicate;
    }

    public WordVectorFactory getWordVectorFactory() {
        if (wordVectorFactory == null)
            wordVectorFactory = new FrequenciesWordVectorFactory();
        return wordVectorFactory;
    }

}
