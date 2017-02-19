package it.fcambi.news.debug;

import java.math.BigInteger;
import java.io.FileWriter;

public class CardinalityDebugger 
{
	
	private static final String _DEBUG_DIRECTORY = "debug/";
	
	private String name;
	private BigInteger total_size = BigInteger.ZERO;
	private BigInteger num_instances = BigInteger.ZERO;
	
	
	public CardinalityDebugger ( String name )
	{
		this.name = name;
	}
	
	public synchronized void add_instance ( int size )
	{
		this.num_instances = this.num_instances.add (BigInteger.ONE);
		this.total_size = this.total_size.add (BigInteger.valueOf (size) );
	}
	
	public void print_report ()
	{
		try
		{
			FileWriter fw = new FileWriter( _DEBUG_DIRECTORY + "cardinality_of_" + name );
			
			fw.write ( name+"\n\n" );
			fw.write ( "number of instances: "+num_instances.toString() +"\n");
			fw.write ( "avg value: "+(total_size.divide(num_instances)).toString() +"\n");
			
			fw.close ();
		}
		catch ( Exception e )
		{
			System.err.println (e);
		}
	}

}
