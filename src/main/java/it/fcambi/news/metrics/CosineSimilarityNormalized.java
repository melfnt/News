package it.fcambi.news.metrics;

public class CosineSimilarityNormalized extends CosineSimilarity 
{

    public double compute(double[] a, double[] b) 
    {

        if (a.length != b.length)
            throw new IllegalArgumentException();

        double den = 0;
        for (int i=0; i<a.length; i++)
        {
            den += a[i]*b[i];
        }

        return den;

    }

    public String getName() {
        return "cosine";
    }

}
