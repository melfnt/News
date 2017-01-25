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
    public void setValuesFrom(Text...texts)
    {
        super.setValuesFrom ( texts );
        List <Double> weights = this.getValues ();
        List <String> words = this.getWords ();
        //~ System.out.println ("[GLOBAL] copying into the map");
        for ( int i=0; i<words.size(); ++i )
        {
			//~ System.out.println ("[GLOBAL] "+words.get(i)+" => "+weights.get(i));
			this.weights_map.put ( words.get(i), weights.get(i) );
		}
    }
    
    public double[] get_weight_for ( List <String> words )
    {
		double [] ret = new double [ words.size () ];
		for ( int i=0; i<words.size(); ++i )
		{
			Double val = this.weights_map.get ( words.get(i) );
			ret[i] = ( val==null ? 0 : val ); 
		}
		return ret;
	}
    
}
