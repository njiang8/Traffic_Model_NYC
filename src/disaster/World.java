/** 
 *  Disaster ABM in MASON
 *  @author Annetta Burger
 *  2018-19
 *  
 *  Code shared on git@gitlab.orc.gmu.edu:aburger2/disaster.git
 */

package disaster;

import java.io.FileWriter;
import java.time.Clock;
import java.time.Instant;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

// Class Imports
import sim.engine.*;
import sim.util.*;
import sim.util.geo.GeomPlanarGraph;
import sim.util.geo.GeomPlanarGraphEdge;
import sim.util.geo.MasonGeometry;
import sim.field.geo.GeomVectorField;

import com.vividsolutions.jts.planargraph.Node;

import sim.engine.Steppable;

/**
 * The  simulation core.
 * 
 * The simulation can require a LOT of memory, so make sure the virtual machine has enough.
 * Do this by adding the following to the command line, or by setting up your run 
 * configuration in Eclipse to include the VM argument:
 * 
 * 		-Xmx2048M
 * 
 * With smaller simulations this chunk of memory is obviously not necessary. You can 
 * take it down to -Xmx800M or some such. If you get an OutOfMemory error, push it up.
 */

public class World extends SimState {

    private static final long serialVersionUID = 1L;
    public static long startTime = System.nanoTime(); // used for a timer in operations

	// create basic field/grid environments
    public GeomVectorField censusTracts = new GeomVectorField();  // visualize census tracts
    public GeomVectorField waterField = new GeomVectorField();  // visualize bodies of water
    
    // set up globals for buildings (ABM agents) and agent grid
    public GeomVectorField bldgField = new GeomVectorField(); // visualize building locations (home, work, school)
    
    // set up globals for agents and agent grid
    public GeomVectorField indvs = new GeomVectorField();  // holds individual agents on field
    public GeomVectorField groups = new GeomVectorField();  // holds group agents on field
    
    // set up globals for roads and road network
    public GeomVectorField roads = new GeomVectorField();
    public GeomVectorField roadIntersections = new GeomVectorField();
    public GeomPlanarGraph roadNetwork = new GeomPlanarGraph();
    //public SparseGrid2D allRoadNodes;
   
    // mapping between unique road network edge IDs and edge structures themselves
    HashMap <String, GeomPlanarGraphEdge> idsToEdges = 
    		new HashMap <String, GeomPlanarGraphEdge> ();
    HashMap <GeomPlanarGraphEdge, Double> edgesToDistance =   // length of edge in kilometers
    		new HashMap <GeomPlanarGraphEdge, Double> ();
    HashMap <GeomPlanarGraphEdge, Double> edgesToSpeedLimit =   // speed limit of the edge(road) segment
    		new HashMap <GeomPlanarGraphEdge, Double> ();
    HashMap <GeomPlanarGraphEdge, ArrayList <Agent>> edgeTraffic =   // list of agents on each edge
    		new HashMap <GeomPlanarGraphEdge, ArrayList <Agent>> ();
    
    // mapping of agents to their agentID
    HashMap <String, Indv> idsToIndvs = new HashMap <String, Indv> ();	// map of individuals to their IDs
    
    //HashMap <String, Group> idsToGrps = new HashMap <String, Group> (); // map of groups to their IDs
    // mapping for social networks
    public static HashMap <String, ArrayList<String>> hholdnetworks =    // groundtruth map of agents in each household
    		new HashMap <String, ArrayList<String>> ();
    
    // set up monitors and results
	public Monitors world_monitors = new Monitors();
    public Results worldResults = new Results();
    
    // output visualization data and agent location data
    public static FileWriter writerViz;
    public static FileWriter writerLoc;
    
    
	//======================================
	//
	//     CORE WORLD METHODS
	//
	//======================================
    
    /**
     * Constructor
     */
    public World(long seed) {
    		// seed the World simulation
    		super(1);	// Set to one for debugging
    }

    
	/**
     * Initialization and start of the World Simulation
     */
    public void start() {
    		Results.deleteLogFile(); // used to record agent coordinate movements for testing 
    		
    		// Clock clock = Clock.systemUTC();
    		// Report simulation time
        	Clock clock = Clock.systemUTC();
        	Instant timerStart = clock.instant();
    		
    		System.out.println("World->start->at " + timerStart);
    		
    		
        	//==============================
        	//	CLASS INITIALIZATIONS
        	//==============================
    		
    		// initialize ABM monitors, results, and disaster effects
    		world_monitors.initialize_monitors();		
    		Results.initializeResults();
    	    //Effects NWMD = new Effects();
    		
    		super.start();
    		
    		// Initializing spatial layers, Group, and Individual classes in WorldBuilder
    		// access spatial input files and create the simulation environment
    		// access demographic input files and create agents
    		try {
				WorldBuilder.initialize(this);
			} catch (IOException e1) {
				// Catch block because of reading rdIDs for verification
				System.out.println("World>start->initialization error:");
				e1.printStackTrace();
			}
    		
        	//==============================
        	//	OPENING DATA FILES
        	//==============================
    		
        	// start saving visualization data
        	if ( Parameters.exportVizData )
        	{
    	   		System.out.println("World>start>opening file for visualization data");
    	   		try 
    	   		{
    	   			writerViz = Results.openExportVizDataFile(Parameters.exportVizDataFilename); //eg. "data/nWMD-viz-data.csv");
    	   		} catch (IOException e) 
    	   		{
    	    		System.out.println("World>start>error in opening file for visualization data:");
    	   			e.printStackTrace();
    	   		}
        	}
    		
        	// start saving agent location analysis data
        	if ( Parameters.saveAgentLocations )
        	{
    			System.out.println("World>start>opening file for agent location counts ploting data");
    			try 
    			{
    				writerLoc = Results.openAgentLocationDataFile("data/aLocCnts" + clock.instant() + ".csv");
    			} catch (IOException e) 
    			{
    				System.out.println("World>start>error in opening file for agent location counts data:");
    				e.printStackTrace();
    			}
        	}
        	
        	
    		//==============================
        	//	SCHEDULING
        	//==============================
        	
    		System.out.println("World->start->on to scheduling at " + clock.instant());
    		
    		//schedule.scheduleRepeating(NWMD);
    		
    		schedule.scheduleRepeating(indvs.scheduleSpatialIndexUpdater(), Integer.MAX_VALUE, 1.0);	
    		
    		schedule.scheduleRepeating(groups.scheduleSpatialIndexUpdater(), Integer.MAX_VALUE, 1.0);	
    		
       		// World (headless) data collection from the model run
       		this.schedule.scheduleRepeating(new Steppable()  {
        		public void step (SimState state)  {
        				
        			// note step for use here
        			int step = (int) state.schedule.getSteps();
    	
          		    // write agent location count data for analysis
          		    if ( ( (Parameters.saveAgentLocations) 
          		    		&& (step >= Parameters.saveLocationsStart) ) 
          		    		&& (step <=Parameters.saveLocationsStop) )
          		    {
    	      		   World.writeAgentLocationCountData(step);
    	   		    }
    	
    	   		    // Note end of recording data & end run
    	   		    if (step == Parameters.saveLocationsStop)  {
    	   		    	// if saving agent location counts, stop
    	   				if ( Parameters.saveAgentLocations ) {
    	   					try  { 
    	   						Results.closeAgentLocationsDataFile(writerLoc);
    	   					}
    	   					catch (IOException e)  {
    	   						System.out.println("World>finish>problem closing vis data file:");
          						e.printStackTrace();
          					}
    	   				}
          		    }
    	   		}  // end step
    	    		
        	});
    		
    		
    		System.out.println("World->start->on to movement");
    		
    }  // end start
 
    
    /**
     * 	End the World simulation
     */
    public void finish() {
    	
		System.out.println("World->finish->end cycle");
		
    	//==============================
    	//	CLOSING DATA FILES
    	//==============================

		if ( Parameters.exportVizData )
		{
			try
			{ 
				Results.closeExportVizDataFile(writerViz);
			}
			catch (IOException e)
			{
				System.out.println("World>finish>problem closing vis data file:");
				e.printStackTrace();
			}
		}// if exporting viz data

		if ( Parameters.saveAgentLocations )
		{
			try
			{ 
				Results.closeAgentLocationsDataFile(writerLoc);
			}
			catch (IOException e)
			{
				System.out.println("World>finish>problem closing vis data file:");
				e.printStackTrace();
			}
		}// if exporting agent location count data
		
		//==============================
    	//	ENDING SIMULATION RUN
    	//==============================
		
    	super.finish(); 
    	
    }
   
    
	//====================================
	//	MAIN
	//====================================
    
    /**
     * Main function allows simulation to be run in stand-alone, non-GUI mode
     */ 
    public static void main(String[] args) {
    		System.out.println("World->main->");
    		doLoop(World.class, args);
    		System.exit(0);
    }
    
    
	//====================================
	//	HELP METHODS FOR DATA COLLECTION
	//====================================
    
    public static void writeVizData(int step, String ID, double speed)  {
    	try  {
    		int year = 2010;
    		int month = 9;
    		int day = 1 + Spacetime.convertToday(step);  // adding 1 so first day is 1, not 0
    		int hourMin = Spacetime.time24(step);
    		Results.exportVizDataRow (World.writerViz, step, ID, speed);
    	}
    	catch (IOException e)  {
    		System.out.println("World>writeData>problem with writing row in agent visualzation data file:");
    		e.printStackTrace();
    	}
    }
    
    
    public static void writeAgentLocationCountData(int step)  {
    	try  {
    		Results.exportAgentLocationsDataRow (World.writerLoc, step);
    	}
    	catch (IOException e)  {
    		System.out.println("World>writeData>problem with writing row in agent location counts data file:");
    		e.printStackTrace();
    	}
    }
    
}

