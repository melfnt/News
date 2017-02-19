package it.fcambi.news.debug;

import java.io.FileWriter;

public class CardinalityDebugger 
{
	
	private static final String _DEBUG_DIRECTORY = "debug/";
	
	private String name;
	private int total_size = 0;
	private int num_instances = 0;
	
	
	public CardinalityDebugger ( String name )
	{
		this.name = name;
	}
	
	public synchronized void add_instance ( int size )
	{
		this.num_instances ++;
		this.total_size += size;
	}
	
	public void print_report ()
	{
		try
		{
			FileWriter fw = new FileWriter( _DEBUG_DIRECTORY + "cardinality_of_" + name );
			
			fw.write ( name+"\n\n" );
			fw.write ( "number of instances: "+num_instances +"\n");
			fw.write ( "avg value: "+((float) total_size/num_instances) +"\n");
			
			fw.close ();
		}
		catch ( Exception e )
		{
			System.err.println (e);
		}
	}

}
