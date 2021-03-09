// Disaster ABM in MASON
// Annetta Burger, 2018

package disaster;


/**
 * Class to build the basic simulation environment
 * Builds the physical infrastructure into a set of grids representing the environment
 * The set of grids includes one for the social -- individual agents in the population
 */

// Class imports
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.planargraph.Node;

import ec.util.MersenneTwisterFast;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomVectorField;
import sim.io.geo.ShapeFileImporter;
import sim.util.Bag;
import sim.util.geo.GeomPlanarGraph;
import sim.util.geo.GeomPlanarGraphDirectedEdge;
import sim.util.geo.GeomPlanarGraphEdge;
import sim.util.geo.MasonGeometry;
import sim.field.network.Edge;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Random;
import java.util.Scanner;


public class WorldBuilder {

	static double dis = 0.1;
	
	/**
	 * Reporting variables to confirm input data
	 */
//	private static int reassignedNan = 0;
//	private static int reassignedSchool = 0;
//	private static int reassignedDaycare = 0;
//	private static int reassignedHomeID = 0;
	private static int nullWrkIDs = 0;
	private static int nulldcWrkIDs = 0;
	private static int nullschlWrkIDs = 0;
	private static int nullhmWrkIDs = 0;
	private static int outofareaWrkIDs = 0;
	private static int wrkoutofRdNet = 0;
	private static int schloutofRdNet = 0;
	private static int dcareoutofRdNet = 0;
	private static int hmoutofRdNet = 0;
	private static int validWrkIDs = 0;
	private static int validHmIDs = 0;
//	private static int validcCareIDs = 0;
	private static int validSchlIDs = 0;
	private static int validDcareIDs = 0;
	private static int invalidWrkIDs = 0;
	private static int badCommutepath = 0; // returns if there is no AStar Path found
	private static int longestpath = 0;
	private static int totalpath = 0;
	private static int avgpath = 0;
	private static double longestcomdist = 0;
	private static double totalcomdist = 0;
	private static double avgcomdist = 0;
	private static int noDrivers = 0;
	//int reassignedWrkRdIDs = 0;
	//int reassignedHomeRdIDs = 0;
	
	// Need a global MBR
	static Envelope globalMBR;
	
	// RdIDs are used to associate real world road networks with the ABM
	// road network edges and nodes.
	Set<String> wrknoRdIDs = new HashSet<String>();
	
	public static HashMap <String, String> wrkToRdIDs = new HashMap <String, String> ();
	public static HashMap <String, String> schlToRdIDs = new HashMap <String, String> ();
	public static HashMap <String, String> dCareToRdIDs = new HashMap <String, String> ();
	public static HashMap <String, String> cWrkToRdIDs = new HashMap <String, String> (); // total daytime locations of children (school +  daycare)
//	public static HashMap <String, String> schlToRdIDs = new HashMap <String, String> ();
//	public static HashMap <String, String> daycareToRdIDs = new HashMap <String, String> ();
	// used to check which workplaces are outside the commuter region
	private static ArrayList<String> outwrkIDs = new ArrayList<String> ();
	private static ArrayList<String> rdIDs = new ArrayList<String> ();
	
	// Used for ??? --- delete??
	private static ArrayList<String> farout_agents = new ArrayList<String>(); // used to filter out agents that have too long a commute
	
	// Used for ?? --- delete??
	private static boolean firstResponder = false;	// first responder flag default value
	
	
	//========================================
	//
	//     INITIALIZE SIMULATION ENVIRONMENT
	//
	//========================================
	
	/**
	 * Initialize World with createEnvironment and createPopulation
	 * Main WorldBuilder method
	 * @param world
	 * @throws IOException
	 */
	static public void initialize (World world) throws IOException {
		
		System.out.println("WorldBuilder->initialize->");	
		createEnvironment(world); // load map layers
		createPopulation(world);  // create population from synthetic population data
		
	}
	
	
	//===============================	
	//
	//	CORE WORLDBUILDER METHODS
	//
	//===============================	

	//==================================	
	//	Load Map Layers and Road Network
	//==================================	
	
	/**
	 * Initialize World and build GeomVectorFields from data files
	 * Data files imported through Parameters Class
	 * @param world
	 * @throws IOException 
	 */
	static public void createEnvironment (World world) throws IOException {
		
		// read in the road maps to create the roads geometry vector field
		System.out.println("WorldBuilder->createEnvironment->reading roads layer...");			
		File roadfile = new File(Parameters.roadsShape);  
		URL roadURL;
		try {
			roadURL = roadfile.toURL();
			ShapeFileImporter.read(roadURL, world.roads);
		} catch (Exception e) {
			e.printStackTrace();
		}
		globalMBR = world.roads.getMBR();
			
        // read in the census tracts map file to create background
		System.out.println("WorldBuilder>createEnvironment->reading background layer...");
		File censusfile = new File(Parameters.censusShape);
		URL censusURL;
		try {
			censusURL = censusfile.toURL();
			ShapeFileImporter.read(censusURL, world.censusTracts);
		} catch (Exception e) {
			e.printStackTrace();
		}		
		globalMBR.expandToInclude(world.censusTracts.getMBR());

        // read in the water map shapefile to create background
		System.out.println("WorldBuilder>createEnvironment->reading in water layer...");
        // read in the tracts to create the background
		File waterfile = new File(Parameters.waterShape);
		URL waterURL;
		try {
			waterURL = waterfile.toURL();
			ShapeFileImporter.read(waterURL, world.waterField);
		} catch (Exception e) {
			e.printStackTrace();
		}		
		System.out.println("WorldBuilder>createEnvironment->expand MBR for water layer...");
		globalMBR.expandToInclude(world.waterField.getMBR());
		
		System.out.println("WorldBuilder>createEnvironment->set fields...");
        // update so that everyone knows what the standard MBR is
        world.roads.setMBR(globalMBR);
        world.waterField.setMBR(globalMBR);
        world.censusTracts.setMBR(globalMBR);
        
		System.out.println("WorldBuilder->createEnvironment->read in environmental data layers...");
		
		
		// create the road network the agents will traverse
        System.out.println("WorldBuilder->createEnvironment->creating network...");
        world.roadNetwork.createFromGeomField(world.roads);
        
        // count nodes for data input report in WorldBuilder
        Iterator iNode = world.roadNetwork.nodeIterator();
        int icounter = 0;
        while (iNode.hasNext()) {
        		icounter++;
        		iNode.next();
        }      
        System.out.println("WorldBuilder->createEnvironment->number of nodes " + icounter);

        // count edges for data input report in WorldBuilder
        Iterator iEdge = world.roadNetwork.edgeIterator();       
        icounter = 0;
        while (iEdge.hasNext()) {
        		icounter++;
        		iEdge.next();
        }        
        System.out.println("WorldBuilder->createEnvironment->number of edges " + icounter);
        
        
        // set speed limits for traffic
        for (Object o : world.roadNetwork.getEdges())
        {
            GeomPlanarGraphEdge e = (GeomPlanarGraphEdge) o;
            
            String ID = Integer.toString(e.getIntegerAttribute("rdID")); // Use this attribute title for small map
            //String ID = Integer.toString(e.getIntegerAttribute("ID"));     // Use this attribute title for large map
            String MTFCC = e.getStringAttribute("MTFCC");

            /**
             * MTFCC Road types in the road network
             * S1100 (num 24369) is a primary road 
             * S1200 (num 37017) is a secondary road
             * S1400 (num 190405) is a residential road; sometimes scenic highway  
             * S1500 (num 20) is a 4WD vehicular travel road (often private)
             * S1630 (num 9267) is a ramp
             * S1640 (num 903) is a service drive
             * S1710 (num 1211) is a walkway or pedestrian trail
             * S1720 (num 14) is a stair
             * S1730 (num 27) is an alley
             * S1740 (num 372) is a limited access (logging, oil, etc.)
             * S1750 (num 56) is internal census use
             * S1780 (num 56) is a parking lot road
             * S1820 (num 6) is a bike trail
             */
            
            double speedlimit;
            // NEED TO FIX SPEED LIMIT
            if (MTFCC.startsWith("S1100") || MTFCC.startsWith("S1200") ) {
            		speedlimit = Spacetime.HIGHWAY;
            		// System.out.println("MTFCC: " + MTFCC + " SpeedLimit: " + speedlimit);
            }
            else {
            		speedlimit = Spacetime.RESIDENTIAL;
            		// System.out.println("MTFCC: " + MTFCC + " SpeedLimit: " + speedlimit);
            }
            
            double edgeDistance = (e.getDoubleAttribute("distance")) ;  // edge distance in kilometers
            // System.out.println("Edge distance from QGIS: " + edgeDistance);
            double edgelength = e.getLine().getLength();
            // System.out.println("Edge length from road network segments: " + e.getLine().getLength()); // edge length
            // System.out.println("Edge length in kilometers: " +Spacetime.latdegToKilometers(edgelength));
            
            // System.out.println(ID);

            world.idsToEdges.put(ID, e);  // world.idsToEdges.put(e.getDoubleAttribute("ID_ID").intValue(), e);
            world.edgesToDistance.put(e, edgeDistance);
            world.edgesToSpeedLimit.put(e, speedlimit);
            
            e.setData(new ArrayList<Agent>());
        }
        
        //world.worldResults.exportRdIDs(world.idsToEdges, "rdIDs");  // temp use to create a list of rdIDs in the network
        System.out.println("WorldBuilder->createEnvironment->Number of network edges in HashMap: " + world.idsToEdges.size());
        
        
        /**
         * adds nodes corresponding to road intersections to GeomVectorField
         * <p/>
         * @param nodeIterator  Points to first node
         * @param intersections GeomVectorField containing intersection geometry
         * <p/>
         * Nodes will belong to a planar graph populated from LineString network.
         */
        
        GeometryFactory fact = new GeometryFactory();
        Coordinate coord = null;
        Point point = null;
        int counter = 0;

        Iterator<?> nodeIterator = world.roadNetwork.nodeIterator();
        
        while (nodeIterator.hasNext())
        {
        	// Create a GeometryVectorField of points representing road intersections
            Node node = (Node) nodeIterator.next();
            coord = node.getCoordinate();
            point = fact.createPoint(coord);

            world.roadIntersections.addGeometry(new MasonGeometry(point));
            counter++;
        }
        
        /**
		 * Read in file to use for check on which agents have work outside the commuter region
		 */
		try {
			
			FileInputStream fstream = new FileInputStream(Parameters.rdIDfile);
			
			BufferedReader w = new BufferedReader(new InputStreamReader(fstream));
			String t;		
			
			while ( (t = w.readLine()) != null ){ // read in all data
				
				String [] field = t.split(",");
				String rdid = field[1]; //road id
				
				//System.out.println(rdid);
				// add rdIDs to an Array List
				rdIDs.add(rdid);
				//System.out.println(t + " " + rdIDs.contains(t));
			}
			
			// clean up
			w.close(); 			
			
		} catch (Exception e) {
			System.out.println("WorldBuilder->createEnvironment->Read WorkIDs ERROR: issue with outerwork file: " + e);
		}
		System.out.println("WorldBuilder->createEnvironment->added rdIDs for verification: arraylist of length: " + rdIDs.size());
		
	}
	
	
	//=========================================
	//    Create Agent Population
	//=========================================
	 
	/** Create buildings and agent population from demographic input file
	 *  Also, creates the agent's ArrayList of household social connections
	 * @param world
	 * @throws IOException 
	 */
	static public void createPopulation(World world) throws IOException {
		System.out.println("WorldBuilder->createPopulation->");
		
		//******************
		//Read In Outer Work places
		//******************
		
		try {
				FileInputStream fstream = new FileInputStream(Parameters.outerwrkfile);
				BufferedReader w = new BufferedReader(new InputStreamReader(fstream));
				String t;
				w.readLine(); // get rid of the header			
				
				while ( (t = w.readLine()) != null ){ // read in all data
			
					String [] field = t.split(",");
					String workID = field[1];	// work id number
					
					// add IDs to an Array List
					outwrkIDs.add(workID);			
					//lines++;	
					//System.out.println(workID + " " + outwrkIDs.contains(workID));
				}
				// clean up
				w.close(); 						
			} 
		catch (Exception e) {
				System.out.println("WorldBuilder->createPopulation->Read WorkIDs ERROR: issue with outerwork file: " + e);
			}
			System.out.println("WorldBuilder->createPopulation->added outerWrkIDs for verification: arraylist of length: " + outwrkIDs.size());
				
		//=======================
		// read work roadIDs
		//=======================

		// Read in the work to roadIDs file and create a HashMap for RoadID assignments
		try {
				//int lines = 0;
				FileInputStream fstream = new FileInputStream(Parameters.workfile);
				BufferedReader w = new BufferedReader(new InputStreamReader(fstream));
				String t;
				
				w.readLine(); // get rid of the header
				
				while ( (t=w.readLine()) != null ) { // read in all data
					String [] field = t.split(",");
					String workID = field[1]; // work ID number
					String rdID = field[2]; // school or daycare's nearest road ID number
					//System.out.println(workID);
					//System.out.println(rdID);
					
					int Type = 2; // building type 2 is a work place
					
					wrkToRdIDs.put(workID, rdID);
					
					if (world.idsToEdges.get(rdID) == null) {
						//System.out.println("WorldBuilder->createPopulation->work places outside area");
					}
					else {
						GeomPlanarGraphEdge edge = world.idsToEdges.get(rdID);
						addBuildingAgent(world, workID, rdID, Type, edge); // add building for the building location
					}
								
					//lines++;
					//System.out.println(wrkToRdIDs + " " + wrkToRdIDs.get(workID) + " " + lines + " lines");
				}
				
				System.out.println("WorldBuilder->createPopulation->assigned road edges to work IDs: " + wrkToRdIDs.size());
				// clean up
				w.close(); 	
			} 
		catch (Exception e) {
			System.out.println("WorldBuilder->createPopulation->Read workIDs ERROR: issue with wrkToRdIDs file: " + e);
		}
				
		
		//=======================
		// read school IDs
		//=======================

		// Read in the school locations file and create HashMap for assignments
		try {
			int lines = 0;
			
			FileInputStream fstream = new FileInputStream(Parameters.schoolfile);
			
			BufferedReader w = new BufferedReader(new InputStreamReader(fstream));
			String t;
			
			w.readLine(); // get rid of the header
			
			while ( (t=w.readLine()) != null ) { // read in all data
				String [] field = t.split(",");
				String schlID = field[1]; // school ID number
				String rdID = field[2]; // school or daycare's nearest road ID number
				//System.out.println(rdID);
				//System.out.println(schlID);
				int Type = 3; // building type 3 is a school
				
				schlToRdIDs.put(schlID,rdID);
				cWrkToRdIDs.put(schlID, rdID);  // wrk IDs for children
				
				if (world.idsToEdges.get(rdID) == null) {
					//System.out.println("WorldBuilder->createPopulation->school outside area");
				}
				else {
					GeomPlanarGraphEdge edge = world.idsToEdges.get(rdID);
					addBuildingAgent(world, schlID, rdID, Type, edge); // add building for the school location
				}
				
				
				lines++;
				
				//System.out.println(schlID + " " + schlToRdIDs.get(schlID) + " " + lines + " lines");
			}
			
			System.out.println("WorldBuilder->createPopulation->assigned road edges to school IDs: " + schlToRdIDs.size());
			//System.out.println("WorldBuilder->createPopulation->assigned road edges to cWrk IDs: " + cWrkToRdIDs.size());
			
			// clean up
			w.close(); 
			
		} catch (Exception e) {
			System.out.println("WorldBuilder->createPopulation->Read schlIDs ERROR: issue with schoolRdID file: " + e);
		}		
		
		
		//=======================
		// read daycare IDs
		//=======================

		// Read in the daycare file and create Hashmap for assignments
		try {
			int lines = 0;
			
			FileInputStream fstream = new FileInputStream(Parameters.daycarefile);
			
			BufferedReader w = new BufferedReader(new InputStreamReader(fstream));
			String t;
			
			w.readLine(); // get rid of the header
			
			while ( (t=w.readLine()) != null ) { // read in all data
				String [] field = t.split(",");
				String dcareID = field[1]; // school or daycare ID number
				String rdID = field[2]; // school or daycare's nearest road ID number
				//System.out.println(rdID);
				//System.out.println(dcareID);
				int Type = 4; // building Type 4 is a daycare
				
				dCareToRdIDs.put(dcareID,rdID);
				cWrkToRdIDs.put(dcareID, rdID);  // wrk IDs for children
				
				if (world.idsToEdges.get(rdID) == null) {
					//System.out.println("WorldBuilder->createPopulation->daycarel outside area");
				}
				else {
					GeomPlanarGraphEdge edge = world.idsToEdges.get(rdID);
					addBuildingAgent(world, dcareID, rdID, Type, edge); // add building for the daycare location
				}
				
				lines++;
				
				//System.out.println(dcareID + " " + dCareToRdIDs.get(dcareID) + " " + lines + " lines");
			}
			
			System.out.println("WorldBuilder->createPopulation->assigned road edges to dCare IDs: " + dCareToRdIDs.size());
			System.out.println("WorldBuilder->createPopulation->assigned road edges to cWrk IDs: " + cWrkToRdIDs.size());
			
			// clean up
			w.close(); 
			
		} catch (Exception e) {
			System.out.println("WorldBuilder->createPopulation->Read dcareIDs ERROR: issue with daycareRdID file: " + e);
		}
		
		
		//=======================
		// read population IDs
		//=======================

		// Read in the population file
		int counter = 0;
		try {
					
			FileInputStream fstream = new FileInputStream(Parameters.popfile);
					
			BufferedReader w = new BufferedReader(new InputStreamReader(fstream));
			String t;

			w.readLine(); // get rid of the header			
					
			while ( (t = w.readLine()) != null ){ // read in all data
				// note: can make the IDs smaller by scraping out the track numbers
				String [] field = t.split(",");
				
				//new load
				String agentID = field[1];	// ID number
				String tract = agentID.substring(0,11);
				String county = agentID.substring(0,5);
				String age = field[2];
				String sex = field[3];
				//int hhtype = Integer.parseInt(field[6]);
				String homeID = field[4];  // ID of home location
				String workID = field[6]; // ID of daytime location
				String workcounty = workID.substring(0,5); // used for testing movement
				String hmRdID = field[9];	// road ID number nearest home
				int Type = 1; // building Type 1 is a home
				
				// get road ID for daytime location 
				String wrkRdID = "";
				//System.out.println("agent work ID: " + workID);
				
				if (workID.contains("w")) {  // check if string has 'w' (work), if not
					wrkRdID = wrkToRdIDs.get(workID);
					if (wrkRdID == null) {
						if (outwrkIDs.contains(workID)) {
							//System.out.println("workID " + workID + " out of area.");
							outofareaWrkIDs++;
						}
						workID = homeID;
						wrkRdID = hmRdID;
						//System.out.println("workRdID: " + wrkRdID + " for " + workID);
						nullWrkIDs++;
					}
					else if (!rdIDs.contains(wrkRdID)) {
						//System.out.println("workID " + workID + " out of network area.");
						workID = homeID;
						wrkRdID = hmRdID;
						wrkoutofRdNet++;
					}
					else {
						//System.out.println("have workRdID: " + wrkRdID + " for " + workID);
						validWrkIDs++;
					}
				}
				else if (workID.contains("s")) {  // check if string has 's' (school)
					wrkRdID = schlToRdIDs.get(workID);
					if (wrkRdID == null) {
						//System.out.println("workRdID: " + wrkRdID + " for " + workID);
						nullschlWrkIDs++;
					}
					else if (!rdIDs.contains(wrkRdID)) {
						//System.out.println("workID " + workID + " out of network area.");
						workID = homeID;
						wrkRdID = hmRdID;
						schloutofRdNet++;
					}
					else {
						//System.out.println("have schlRdID: " + wrkRdID + " for " + workID);
						validSchlIDs++;
					}
				}
				else if (workID.contains("d")) {  // check if string has 'd' (daycare)
					wrkRdID = dCareToRdIDs.get(workID);
					if (wrkRdID == null) {
						wrkRdID = hmRdID; // if this is null, work defaults to home (until daycare file cleaned up)
						//System.out.println("workRdID: " + wrkRdID + " for " + workID);
						nulldcWrkIDs++;
					}
					else if (!rdIDs.contains(wrkRdID)) {
						//System.out.println("workID " + workID + " out of network area.");
						workID = homeID;
						wrkRdID = hmRdID;
						dcareoutofRdNet++;
					}
					else {
						//wrkRdID = hmRdID; // work defaults to home (until daycare file cleaned up)
						//System.out.println("have dCareRdID: " + wrkRdID + " for " + workID);
						validDcareIDs++;
					}
				}
				else if (workID.contains("h")) {  // check if string has 'h'
					wrkRdID = hmRdID;
					if (wrkRdID == null) {
						//System.out.println("workRdID: " + wrkRdID + " for " + workID);
						nullhmWrkIDs++;
					}
					else if (!rdIDs.contains(wrkRdID)) {
						//System.out.println("workID " + workID + " out of network area.");
						wrkRdID = hmRdID;
						hmoutofRdNet++;
					}
					//System.out.println("work from home: " + wrkRdID + " for " + workID);
					validHmIDs++;
				}
				else { // if workIDs are bad assign it the homeID
					workID = homeID;
					wrkRdID = hmRdID; // place holder for other daytime locations
					//System.out.println(workID + "not a valid workID; assigned home rdID: " + hmRdID);
					invalidWrkIDs++;
				}
				
				//System.out.println("agent census track: " + track);	
				//if (workcounty.equals("36061")) { 	// sample the population in these counties
					//System.out.println(agentID + " ");

					if (counter % 1 == 0) {  // sample the data file by a given factor
//						if (wrkRdID != hmRdID) {  
						
							// Add individual population to household network Hashmap and set agent household network
							//System.out.println(agentID + " in household " + homeID);
							add2household(world, homeID, agentID);		// put household in housenetwork hashmap		
							//System.out.println("Household keys: " + world.hholdnetworks.keySet());
							//ArrayList<String> household = world.hholdnetworks.get(homeID);
							// Get household to add to agent data
			    			ArrayList<String> housenet = world.hholdnetworks.get(homeID); 
			    			ArrayList<String> household = new ArrayList<String>();
			    			int size = housenet.size();
			    			if (size > 12) {  // to break the institutional homes into smaller groups
			    				Random rand = new Random();
			    				int hholdsize = rand.nextInt(12) + 1;
			    				for (int i = 0; i < hholdsize; i++) {
			    					String randNode = housenet.get((int) Math.floor(Math.random() * housenet.size()));
			    					household.add(randNode);
			    				}
			    				for (String x: household) {
				    			//System.out.println("household greater than 12: " + x);
			    				}
			    			}
			    			else {
			    				household = housenet;
				    			for (String x: household) {
					    			//System.out.println("household: " + x);
				    			}
			    			}

			    			//***create agent
//							Indv a = new Indv(world, tract, county, agentID, homeID, workID, hmRdID, wrkRdID);
							Indv a = new Indv(world, tract, county, agentID, age, homeID, workID, hmRdID, wrkRdID, 
									household);
							//System.out.println("WorldBuilder->createPopulation->created agent; at counter " + counter);
							
							// Population's Path Statistics
							int path = a.getPathLength();
							totalpath += path;
							if ( path > longestpath) {
								longestpath = path;
								//System.out.println("WorldBuilder->createPopulation->longestpath: " + longestpath + " currentpath: " + path);
							}
//							
//							if ( a.getcommuteDist() > 178 ) {
//								farout_agents.add(a.getID());
//								System.out.println("WorldBuilder->createPopulation->agent path > 178: " + a.getID() + ": " + a.getcommuteDist());
//							}
							
							double comdist = a.getcommuteDist();
							totalcomdist += comdist;
							if ( comdist > longestcomdist) {
								longestcomdist = comdist;
								//System.out.println("WorldBuilder->createPopulation->longestdist: " + longestcomdist + " agentcomdist: " + comdist);
							}
							
							if (!a.pathset)
							{
								WorldBuilder.badCommutepath += 1;
								Monitors.badagent += 1;
								Monitors.atHomeCount -= 1;
								if (a.StayAtHome) {
									Monitors.stayAtHome -= 1;
								}
								continue; // DON'T ADD IT if it's bad
							}
	
							else {
								MasonGeometry newGeometry = a.getGeometry();
								newGeometry.isMovable = true;
								world.indvs.addGeometry(newGeometry);
								Monitors.indvList.add(a); // ArrayList of the individuals
								world.schedule.scheduleRepeating(a);
								
								Monitors.agentpopulation++; 
								world.idsToIndvs.put(a.getID(),a); // add to HashMap used to retrieve agent objects, if there is a path
								//System.out.println("WorldBuilder->createPopulation->added ID key: " + a + " " + world.idsToIndvs.get(a.getID()));
								
								if (world.idsToEdges.get(hmRdID) == null) {
									//System.out.println("WorldBuilder->createPopulation->home outside area");
								}
								else {
									GeomPlanarGraphEdge edge = world.idsToEdges.get(hmRdID);
									addBuildingAgent(world, homeID, hmRdID, Type, edge); // add building for the home location
								}
							}
//						}
						//System.out.println("WorldBuilder>createPopulation>" + a.getID() + " End WorkNode: " + a.getWorkNode().getCoordinate());
					}
					
				}
				if (counter % 100 == 0) {
					System.out.println("WorldBuilder->createPopulation->Number of Indv agents inititalized: " + counter + " idsToIndvs: " + world.idsToIndvs.size());
				}
				counter++;
                world.indvs.setMBR(globalMBR);
                
				//System.out.println(agentID + " " + counter + " lines");
			//}
			//}
			
			avgpath = totalpath / counter;	
			avgcomdist = totalcomdist / counter;
			
			// clean up
			w.close(); 	
					
		} catch (Exception e) {
			System.out.println("WorldBuilder->createPopulation->Read population ERROR: issue with population file: " + e);
		}
		
		
		/**
		 * Check population -- clean up -- try to remove bad agents from lists and hashmaps
		 */
		Iterator<HashMap.Entry<String, Indv>> i = world.idsToIndvs.entrySet().iterator();
		System.out.println("WorldBuilder->createPopulation->clean->indvs:");
		int nullIDs = 0;
		while (i.hasNext()) {
			HashMap.Entry<String, Indv> pair = i.next();
			Indv a = pair.getValue();
			if (a == null) {
				System.out.print(pair.getKey() + ": null ");
				nullIDs += 1;
				world.idsToIndvs.remove(pair.getKey(), pair.getValue());
			}
			else {
				System.out.print(a.getID() + " " + a.getWorkID() + " ");
			}
		}
		System.out.println();
		System.out.println("WorldBuilder->createPopulation->clean->null agents: " + nullIDs);
		
		
//		updateHouseNets (world); // update the household networks of all networks to the groundtruth HashMap
		System.out.println("WorldBuilder->createPopulation->idsToIndvs size: " + world.idsToIndvs.size());
		
		System.out.println("*****************");
		System.out.println("WorldBuilder->createPopulation->Data Inputs Report: ");
		System.out.println("WorldBuilder->createPopulation->null fields from the data input (#null/#rows) WrkIDs:" + nullWrkIDs + "/" + wrkToRdIDs.size() +
				" schWrkIDs:" + nullschlWrkIDs + "/" + schlToRdIDs.size() + " dcWrkIDs:" + nulldcWrkIDs + "/" + dCareToRdIDs.size() + 
				" hmWrkIDs:" + nullhmWrkIDs + "/" + counter);
		System.out.println("WorldBuilder->createPopulation->out of road network area (#outofarea/#rows) wrk:" + wrkoutofRdNet + "/" + wrkToRdIDs.size() +
				" schl:" + schloutofRdNet + "/" + schlToRdIDs.size() + " daycare:" + dcareoutofRdNet + "/" + dCareToRdIDs.size() + 
				" hm:" + hmoutofRdNet + "/" + counter);
		System.out.println("WorldBuilder->createPopulation->Valid WrkIDs:" + validWrkIDs + " ValidSchlIDs:" + validSchlIDs +
				" Valid ChildCareIDs:" + validDcareIDs + " Valid HmIDs:" + validHmIDs + " Invalid WrkIDs:" + invalidWrkIDs);
		System.out.println("WorkdBuilder->createPopulation->Bad HomeNodes: " + Monitors.badhomeNode);
		System.out.println("WorldBuilder->createPopulation->Agents discarded due to bad commutepaths: " + WorldBuilder.badCommutepath);		
		System.out.println("WorldBuilder->createPopulation->Agents without commute path or valid homeID:" + Monitors.badagent);
		System.out.println("WorldBuilder->createPopulation->clean->null agents: " + nullIDs);
		//System.out.println("WorldBuilder->createPopulation->Daycare agents without drivers:" + noDrivers);
		System.out.println("WorldBuilder->createPopulation->Longest path: " + longestpath + "  Avg path: " + avgpath);
		System.out.println("WorldBuilder->createPopulation->Longest commute: " + longestcomdist + " Avg comdist: " + avgcomdist);
//		System.out.println("WorldBuilder->createPopulation->farout agents: ");
//		for (String id: farout_agents ) System.out.print("agent: ");
//		System.out.println("");
		System.out.println("");
		System.out.println("WorldBuilding->createPopulation->created building agents: " + Monitors.bldgcount);
		System.out.println("WorldBuilder->createPopulation->created agent population size: " + Monitors.agentpopulation + "  number of groups: " + Monitors.grouppopulation); 
				// can also check: + " " + world.idsToAgents.size());
		System.out.println("WorldBuilder->createPopulation->stay-at-home agents: " + Monitors.stayAtHome 
				+ "  Agents currently at home: " + Monitors.atHomeCount);
		
//		// Uncomment if you want to export the initial household networks
//		System.out.println("WorldBuilder->createPopulation->exporting household networks");		
//		world.worldResults.exportSocialNet(world.hholdnetworks, "test1");
		
	}

	
	//===============================	
	//
	//	WORLDBUILDER HELPER METHODS
	//
	//===============================	
	
	/**
	 * Align geometry vector fields for the MBR
	 * @param base
	 * @param others
	 */
    static void alignVectorFields(GeomGridField base, GeomVectorField[] others)
    {
        Envelope globalMBR = base.getMBR();
        for(GeomVectorField vf: others)
            globalMBR.expandToInclude(vf.getMBR());
        for(GeomVectorField vf: others)
            vf.setMBR(globalMBR);
    }
    
    
    /**
     * Adds agents to represent buildings
     * @param state
     * @param ID
     * @param RdID
     * @param Type
     * @param rdEdge
     */
    static void addBuildingAgent (World state, String ID, String RdID, int Type, GeomPlanarGraphEdge rdEdge) {
		Building b = new Building(state, ID, RdID, Type, rdEdge);
		Monitors.bldgcount++;
		//System.out.println("WorldBuilder->createPopulation->created building; building count: " + Monitors.bldgcount);
		MasonGeometry newGeometry = b.getGeometry();
		newGeometry.isMovable = false;
		state.bldgField.addGeometry(newGeometry);
		Monitors.bldgList.add(b);
		//state.schedule.scheduleRepeating(b);  // turn off until there are NWMD effects on bldgs
		//System.out.println("WorldBuilder->createPopulation->added bldg to schedule");
    }
    
    
    /**
     * Creates and updates the HashMap of household social networks
     * @param hholdID
     * @param agentID
     * @param world
     */
	private static void add2household (World world, String hholdID, String agentID) 
    {
    		// Create first key, object
    		if (world.hholdnetworks.isEmpty()) {
    			ArrayList<String> housenet = new ArrayList<String>();
    			housenet.add(agentID); // add first agentID to the housenet
    			world.hholdnetworks.put(hholdID, housenet);  // put household network in the array of household networks
    		}
    		// If household is in the map, add agent to its household list
    		else if (world.hholdnetworks.containsKey(hholdID)) {
    			world.hholdnetworks.get(hholdID).add(agentID);
    		}
    		// Otherwise add household as key and the agent stringID
    		else {
    			ArrayList<String> housenet = new ArrayList<String>(); 
    			housenet.add(agentID);
    			world.hholdnetworks.put(hholdID, housenet);
    		}
    		//System.out.println("Added " + world.hholdnetworks.get(hholdID) + " to household " + hholdID);
    }

	
	/**
	 * Updates each agent's known alive household network to initial model groundtruth
	 * Creates HashMap of household members for each individual
	 * @param world
	 */
	private static void updateHouseNets(World world) {
		//System.out.println("WorldBuilder->createPopulation->updateHouseNets->");
	    Iterator<Entry<String, Indv>> it = world.idsToIndvs.entrySet().iterator();
	    
	    while (it.hasNext()) {
			HashMap.Entry <String, Indv>pair = (Entry<String, Indv>)it.next();
			Indv a = pair.getValue();
			ArrayList<String> housenet = world.hholdnetworks.get(a.getHomeID()); // retrieve the groundtruth household network
			a.sethholdnet(housenet); // reset the household network (ArrayList<String>) of the agent
			// iterate through housenet and create HashMap
			//System.out.println("WorldBuilder->createPopulation->updateHouseNets->" + a.getID() + " " + a + " with: " + housenet);
			for (String ID: housenet) {
				a.idsToHouseMembers.put(ID, world.idsToIndvs.get(ID));
				//System.out.println("HashMap: " + a.idsToHouseMembers.get(ID));
			}
			
	        //System.out.println("WorldBuilder->createPopulation->updateHouseNets->" + a.getID() + " household network: " + a.getHholdnet());
	    }
	    
	    // check and count household sizes above 12
	    int counter = 0;
	    for (ArrayList<String> network: world.hholdnetworks.values()) {
	    	if (network.size() >= 12) {
	    		counter++;
	    	}
	    }
	    System.out.println("WorldBuilder->createPopulation->udpdateHouseNets-># of households >= 12: " + counter);
	}
	
	
	
	
	
	
	
	
	
	
	
	//==============================================
	//
	// 		VERIFICATION METHODS
	//
	//==============================================
	
	

	
}
