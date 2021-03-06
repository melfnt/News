package it.fcambi.news.clustering;

import it.fcambi.news.model.TFDictionary;
import it.fcambi.news.Pair;
import it.fcambi.news.data.Text;
import it.fcambi.news.data.GlobalTFIDFWordVector;
import it.fcambi.news.data.TFIDFWordVectorFactory;
import it.fcambi.news.model.Article;
import it.fcambi.news.model.MatchingArticle;

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//DEBUG
import it.fcambi.news.debug.CardinalityDebugger;
import it.fcambi.news.debug.TfIdfDebugger;
import java.util.Scanner;

public class MatchMapGeneratorWithGlobalTfIdf extends MatchMapGenerator
{

    protected Map<Long, GlobalTFIDFWordVector> vector_cache = new ConcurrentHashMap <Long, GlobalTFIDFWordVector> ();
    protected TFDictionary dictionary;
    
    private CardinalityDebugger union_debugger = new CardinalityDebugger ("UNION");
    private CardinalityDebugger intersection_debugger = new CardinalityDebugger ("INTERSECTION");
    private CardinalityDebugger body_length_debugger = new CardinalityDebugger ("BODY_LENGTH");
    
    public MatchMapGeneratorWithGlobalTfIdf ( MatchMapGeneratorConfiguration config ) 
    {
		super (config);
		this.tf_idf_debugger = new TfIdfDebugger ( "GLOBAL" );
		
		this.dictionary = (  ( TFIDFWordVectorFactory ) config.getWordVectorFactory()  ).get_dictionary ();
		
    }
    
    public MatchMapGeneratorWithGlobalTfIdf ( MatchMapGeneratorConfiguration config, CardinalityDebugger union_debugger, CardinalityDebugger intersection_debugger, CardinalityDebugger body_length_debugger ) 
    {
		
		this (config);
		this.union_debugger = union_debugger;
		this.intersection_debugger = intersection_debugger;
		this.body_length_debugger = body_length_debugger;
		
    }
    
    
    public void process_articles_and_add_to_cache ( Collection<Article> articles )
    {
		articles.stream().parallel()
                .filter( article -> !( vector_cache.containsKey(article.getId()) ) )
                .distinct()
                .forEach(article -> {
            Text body = getTextAndApplyFilters(article.getBody());
            Text title = getTextAndApplyFilters(article.getTitle());
            Text description = getTextAndApplyFilters(article.getDescription());
			
			GlobalTFIDFWordVector w = new GlobalTFIDFWordVector ( dictionary );
            w.setWordsFrom (  config.getKeywordSelectionFn().apply (title, description, body) );
            w.setValuesFrom (  body );
			this.vector_cache.put ( article.getId (), w ); 
			
			//DEBUG
			//~ tf_idf_debugger.conditional_debug ( article, w );
			//~ body_length_debugger.add_instance ( config.getKeywordSelectionFn().apply (title, description, body).words().size() );
			
        });
	}

    /**
     * @param articlesToMatch          Set of articles to match
     * @param knownArticles           Set of previously clustered articles
     * @return Map that bind each article with a list of possible matchings
     */
    public Map<Article, List<MatchingArticle>> generateMap(Collection<Article> articlesToMatch, Collection<Article> knownArticles)
    {
        progress.set(0);
        toMatchArticlesSize = articlesToMatch.size();

        // Source Article -> Similarities with all articles
        Map<Article, List<MatchingArticle>> matchMap;

        matchMap = articlesToMatch.parallelStream().map(article -> 
        {
			
			GlobalTFIDFWordVector w = this.vector_cache.get(article.getId());
			Set <String> words_in_w = new HashSet ( w.getWords () );
			
            List<MatchingArticle> matchingArticles = knownArticles.parallelStream()
                    .filter(match -> !config.getIgnorePairPredicate().test(article, match))
                    .map(match -> {

                GlobalTFIDFWordVector v = this.vector_cache.get(match.getId());				
                Set <String> intersection = new HashSet<String>( v.getWords () );
                intersection.retainAll ( words_in_w );
                
                // DEBUG
                //~ intersection_debugger.add_instance ( intersection.size() );
                //~ Set <String> union = new HashSet<String>( v.getWords () );
                //~ union.addAll ( words_in_w );
                //~ union_debugger.add_instance ( union.size () );
                
                MatchingArticle a = new MatchingArticle();
                a.setArticle(match);
				
				double [] w_array = w.get_weight_for ( new ArrayList<String>(intersection) );
				double [] v_array = v.get_weight_for ( new ArrayList<String>(intersection) );
				
                config.getMetrics().forEach(metric ->
                //DEBUG
                {
                        a.addSimilarity ( metric.getName(), metric.compute( w_array, v_array ) ) ;
					//	tf_idf_debugger.conditional_metric_debug ( metric.getName(), article.getId(), match.getId(), metric.compute( w_array, v_array ), w_array, v_array, new ArrayList<String> (union) );
				} );
                return a;
            }).collect(Collectors.toList());

            progress.incrementAndGet();
            return new Pair<>(article, matchingArticles);

        }).collect(Collectors.toConcurrentMap(Pair::getKey, Pair::getValue));

        return matchMap;
    }

}
