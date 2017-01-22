package it.fcambi.news.data;

import it.fcambi.news.model.TFDictionary;

import java.util.List;
import java.util.Map;
import java.util.HashMap;


public class GlobalTFIDFWordVector extends TFIDFWordVector 
{
	
	private Map <String, Double> weights_map = new HashMap <String, Double> ();
	
	public GlobalTFIDFWordVector ( TFDictionary dict )
	{
		super ( dict );
	}
	
    @Override
    public void setFrom(Text...texts)
    {
        super.setFrom ( texts );
        List <Double> weights = this.getValues ();
        List <String> words = this.getWords ();
        for ( int i=0; i<words.size(); ++i )
        {
			this.weights_map.put ( words.get(i), weights.get(i) );
		}
    }
    
    public double[] get_weight_for ( List <String> words )
    {
		double [] ret = new double [ words.size () ];
		for ( int i=0; i<words.size(); ++i )
		{
			ret[i] = this.weights_map.get ( words.get(i) );
		}
		return ret;
	}
    
}
