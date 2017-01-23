package it.fcambi.news.data;

import it.fcambi.news.model.TFDictionary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Francesco on 27/10/15.
 */
public class TFIDFWordVector extends FrequenciesWordVector {

    protected List<Double> weights = new ArrayList<>();

    protected TFDictionary dictionary;

    public TFIDFWordVector(TFDictionary dict) {
        super();
        if (dict == null)
            throw new IllegalArgumentException("Cannot instantiate TFIDFWordVector without TFDictionary.");
        dictionary = dict;
        //~ System.out.println ("creating new TFIDFWordVector");
    }

    @Override
    public void setFrom(Text...texts) {
        super.setFrom(texts);
        weights.clear();
        //~ System.out.println ("weights before "+weights);
        computeWeights(frequencies.stream().reduce(0.0, (a,b) -> a+b));
    }

    /**
     * Set weights as tfidf vector for each word in words and document
     * @param texts Texts parts of a SINGLE document
     */
    @Override
    public void setValuesFrom(Text... texts) {
        super.setValuesFrom(texts);
        weights.clear();
        //~ System.out.println ("set values: weights before "+weights);
        computeWeights(Arrays.stream(texts).mapToDouble(text -> text.words().size()).reduce(0.0, (a,b) -> a+b));
    }

    private void computeWeights(double documentSize)
    {
		//~ System.out.println ("computing weights - documentSize: "+documentSize);
		//~ System.out.println ("words in vector: "+words);
        for (int i=0; i < words.size(); i++) {
            //~ System.out.println ("current word: "+ words.get(i) );
            double w = (frequencies.get(i)/documentSize)*
                    Math.log((double)dictionary.getNumOfDocuments()/dictionary.getNumOfDocumentsWithWord(words.get(i)));
            //~ System.out.println ("tf value: "+frequencies.get(i)/documentSize);
            //~ System.out.println ("idf value: "+Math.log((double)dictionary.getNumOfDocuments()/dictionary.getNumOfDocumentsWithWord(words.get(i))));
            //~ System.out.println ("tf-idf value: "+w);
            weights.add(w);
        }
    }

    @Override
    public List<Double> getValues() {
        return this.weights;
    }

    @Override
    public void setValues(List<Double> values) {
        this.weights = values;
    }

    @Override
    public double[] toArray() {
        return weights.stream().mapToDouble(d -> d).toArray();
    }

    public List<Double> getFrequencies() {
        return this.frequencies;
    }

    public void setFrequencies(List<Double> frequencies) {
        this.frequencies = frequencies;
    }
    
}
