package it.fcambi.news.clustering;

import it.fcambi.news.Pair;
import it.fcambi.news.data.Text;
import it.fcambi.news.data.WordVector;
import it.fcambi.news.model.Article;
import it.fcambi.news.model.MatchingArticle;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//DEBUG
import it.fcambi.news.debug.TfIdfDebugger;
import java.util.Scanner;
import it.fcambi.news.data.GlobalTFIDFWordVector;
import it.fcambi.news.data.TFIDFWordVectorFactory;

public class MatchMapGenerator {

    protected Map<Long, Text> keywordCache = new ConcurrentHashMap<>();
    protected Map<Long, Text> bodyCache = new ConcurrentHashMap<>();

    protected MatchMapGeneratorConfiguration config;

    protected AtomicInteger progress;
    protected int toMatchArticlesSize;
		
	//DEBUG
	protected TfIdfDebugger tf_idf_debugger = new TfIdfDebugger ( "CAMBI" );

    public MatchMapGenerator(MatchMapGeneratorConfiguration config) {
        this.config = config;
        progress = new AtomicInteger();
    }

    public MatchMapGenerator(MatchMapGeneratorConfiguration config, Map<Long, Text> bodyCache, Map<Long, Text> keywordCache) {
        progress = new AtomicInteger();
        this.config = config;
        this.bodyCache = bodyCache;
        this.keywordCache = keywordCache;
    }

    /**
     * @param articlesToMatch          Set of articles to match
     * @param knownArticles           Set of previously clustered articles
     * @return Map that bind each article with a list of possible matchings
     */
    public Map<Article, List<MatchingArticle>> generateMap(Collection<Article> articlesToMatch, Collection<Article> knownArticles) {
        progress.set(0);
        toMatchArticlesSize = articlesToMatch.size();

        /* Preprocess articles */
        Stream.concat(articlesToMatch.stream(), knownArticles.stream()).parallel()
                .filter(article -> !(bodyCache.containsKey(article.getId()) && keywordCache.containsKey(article.getId())))
                .distinct()
                .forEach(article -> {
            Text body = getTextAndApplyFilters(article.getBody());
            bodyCache.put(article.getId(), body);
            Text title = getTextAndApplyFilters(article.getTitle());
            Text description = getTextAndApplyFilters(article.getDescription());
            keywordCache.put(article.getId(), config.getKeywordSelectionFn().apply(title, description, body));
        });

        // Source Article -> Similarities with all articles
        Map<Article, List<MatchingArticle>> matchMap;

        //~ matchMap = articlesToMatch.parallelStream().map(article -> {
        matchMap = articlesToMatch.stream().map(article -> {

            List<MatchingArticle> matchingArticles = knownArticles.parallelStream()
                    .filter(match -> !config.getIgnorePairPredicate().test(article, match))
                    .map(match -> {

                
				//DEBUG
				System.out.println ("Article id: "+article.getId());
				System.out.println ("Keywords: \n"+bodyCache.get(article.getId()));
				
				WordVector w = config.getWordVectorFactory().createNewVector();
				w.setWordsFrom(keywordCache.get(article.getId()), keywordCache.get(match.getId()));
                w.setValuesFrom(bodyCache.get(article.getId()));
				
				//DEBUG
				System.out.println ("[GLOBAL] creating new global tf-idf vector");
				GlobalTFIDFWordVector gw = new GlobalTFIDFWordVector ( (  ( TFIDFWordVectorFactory ) config.getWordVectorFactory()  ).get_dictionary () );
				System.out.println ("[GLOBAL] setting (words) from "+bodyCache.get(article.getId()));
				w.setWordsFrom (keywordCache.get(article.getId()));
                w.setValuesFrom(bodyCache.get(article.getId()));
				
				new Scanner(System.in).nextLine ();
				
                WordVector v = config.getWordVectorFactory().createNewVector();
                v.setWords(w.getWords());
                v.setValuesFrom(bodyCache.get(match.getId()));

                //DEBUG
				tf_idf_debugger.conditional_debug ( article, w );
				tf_idf_debugger.conditional_debug ( match, v );
				
                
                MatchingArticle a = new MatchingArticle();
                a.setArticle(match);

                config.getMetrics().forEach(metric ->
                        a.addSimilarity(metric.getName(), metric.compute(w.toArray(), v.toArray())));

                return a;
            }).collect(Collectors.toList());

            progress.incrementAndGet();
            return new Pair<>(article, matchingArticles);

        }).collect(Collectors.toConcurrentMap(Pair::getKey, Pair::getValue));

        return matchMap;
    }

    protected Text getTextAndApplyFilters(String s) {
        if (s == null) return new Text();
        Text t = config.getStringToTextFn().apply(s);
        config.getTextFilters().forEach(t::applyFilter);
        return t;
    }

    public double getProgress() {
        return (double)this.progress.get()/toMatchArticlesSize;
    }

    public Map<Long, Text> getKeywordCache() {
        return keywordCache;
    }

    public Map<Long, Text> getBodyCache() {
        return bodyCache;
    }
}
