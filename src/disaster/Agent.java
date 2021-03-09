/**
 * Disaster ABM in MASON
 * @author Annetta Burger
 * 2018-19
 */

package disaster;

//import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.geo.GeomVectorField;
import sim.util.Bag;
import sim.util.geo.GeomPlanarGraph;
//import sim.util.Int2D;
import sim.util.geo.GeomPlanarGraphDirectedEdge;
import sim.util.geo.GeomPlanarGraphEdge;
import sim.util.geo.MasonGeometry;
import sim.util.geo.PointMoveTo;

import java.util.ArrayList;
import java.util.Iterator;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.linearref.LengthIndexedLine;
import com.vividsolutions.jts.planargraph.Node;

// Agent class 
/**
 *  Our simple agent from the GridLock GeoMASON example.  The agent travels a directed graph 
 *  roadnetwork using an AStar Algorithm
 *  This superclass of human agents is the parent to
 *  Indv and Group subclasses
 */
@SuppressWarnings("restriction")
public class Agent implements Steppable {

    private static final long serialVersionUID = -1113018274619047013L;
    
	// internal version of the world, i.e. how the agent perceives it
	// now don't need to pass the world in all Indv methods
	// updates to the individual's world occur internal to the class methods
	World state; 
	
	protected String agentID = "";
	
    public int healthStatus = -1;		// agent's health, -1 = not set
	
	// Goals
	private String goal;  		// varies between "commute", "findshelter", "shelter", "flee", "family"
	private Coordinate goalPoint;    // used to travel to a point not on a network
	private Node goalNode;		// used to set network paths that are not commutes
	public ArrayList<Node> multiGoalNodes = new ArrayList<Node>();  // list of goal nodes used for carpools and other multilocation paths
	//public Queue<Node> multiGoalNodes;  // list of goal nodes used for carpools and other multilocation paths
	
	// GIS, road network, and Movement Data
	private String startID = "";  // homeID
	private String endID = "";  // workID
	private String startRdID = "";  // homeRdID
	private String endRdID = "";  // workRdID
	private Node startNode;  // homeNode
	private Node endNode;  // workNode
	
    // point that denotes agent's position
	//  private Point location;
	private MasonGeometry location;
    Coordinate currentCoord = null;
    Coordinate lastCoord1 = null;
    Coordinate lastCoord2 = null;
	
	// Used by agent to walk along line segment
	protected LengthIndexedLine segment = null;
	double startIndex = 0.0; // start position of current line
	double endIndex = 0.0; // end position of current line
	double currentIndex = 0.0; // current location along line
	
	double p_edge = 0.0;
	double start_p_edge = 0.0;
	double edge_end_p = 0.0;
		
	// Edge handling
	GeomPlanarGraphEdge currentEdge = null;
	int linkDirection = 1;
	double speed = 0; // used for network travel
	boolean pathset = false; // whether agent has a path to travel
	ArrayList<GeomPlanarGraphDirectedEdge> currentPath = // path agent travels
			new ArrayList<GeomPlanarGraphDirectedEdge>();
	int indexOnPath = 0;  // index of which edge on the path
	int pathDirection = 1;
	boolean reachedDestination = false;
	PointMoveTo pointMoveTo = new PointMoveTo();
	int pathLength = 0;
	int disconnectedPaths = 0;

	// Path handling
	ArrayList<ArrayList<GeomPlanarGraphDirectedEdge>> multiPath = // list of paths
			new ArrayList<ArrayList<GeomPlanarGraphDirectedEdge>>();
	int multipathDirection = 1; // used for multipath travel
	int multipathIndex = 0; // index of multiPath; tracks which path agent is on
	boolean reachedFinalDestination = false; // used for multipath travel

	// Non-network path movement
    private double moveRateKmPerStep = 0.0;  // in units kilometers/step (minute)
	GeometryFactory fact = new GeometryFactory();	// used to create points for movement
	// methods using these attributes are Indv, and Group classes for the reroute, gotoDetour and Detour methods
	// a basic reroute method for the parent Agent class still needs to be written
	boolean needReroute = false; // used to indicate the agent needs to find a new path
	boolean haveDetour = false; // used to indicate the agent has a detour
	boolean onDetour; // agent is detouring to destination
	
	
    /** 
	 *	Agent Getters and Setters
     */
    public void setHealthStatus(int x) 	{ this.healthStatus = x; }
	public void setGoal(String x)		{ this.goal = x; }
	public void setGoalNode(Node x)		{ this.goalNode = x; }
	public void setGoalPoint(Coordinate x) { this.goalPoint = x; }
	public void setLocation(MasonGeometry x) { this.location = x; }
	//public void setSegment(LengthIndexedLine x) { this.segment = x; }
    public void setID(String x)			{ this.agentID = x; }
    public void setStartID(String x)	{ this.startID = x; }
    public void setEndID(String x)		{ this.endID = x; }
    public void setStartRdID(String x)	{ this.startRdID = x; }
    public void setEndRdID(String x)	{ this.endRdID = x; }
    public void setStartNode(Node x)	{ this.startNode = x; }
    public void setEndNode(Node x)		{ this.endNode = x; }  
    public void setPathLength(int x)	{ this.pathLength = x; }
    public void setMoveRateKmPerStep(double x) { this.moveRateKmPerStep = x; }
    public void setneedReroute(boolean x) { this.needReroute = x; }
    public void sethaveDetour(boolean x)  { this.haveDetour = x; }
    public void setonDetour(boolean x)	{ this.onDetour = x; }

    public int getHealthStatus()		{ return this.healthStatus; }
    public String getGoal()				{ return this.goal; }
    public Node getGoalNode()			{ return this.goalNode; }
    public Coordinate getGoalPoint()	{ return this.goalPoint; }
    public MasonGeometry getLocation()	{ return this.location; }
    
    public LengthIndexedLine getSegment()	{ return this.segment; }
    
    public String getID()     			{ return this.agentID; }
    public String getStartID() 			{ return this.startID; }
    public String getEndID() 			{ return this.endID; }
    public String getStartRdID()		{ return this.startRdID; }
    public String getEndRdID()			{ return this.endRdID; } 
    public Node getStartNode()			{ return this.startNode; }
    public Node getEndNode()			{ return this.endNode; }
    public int getPathLength()			{ return this.pathLength; }
    public double getMoveRateKmPerStep()		{ return this.moveRateKmPerStep; }
    public boolean getneedReroute()		{ return this.needReroute; }
    public boolean gethaveDetour()		{ return this.haveDetour; }
    public boolean getonDetour()		{ return this.onDetour; }
 
    
	/** Constructor Function 
	 * @param world
	 * @param census
	 * @param agent identifier
	 * @param home location
	 * @param work location
	 * @param home road identifier
	 * @param work road identifier
	 */
    public Agent(World world){
    	state = world;  // set the agent state with world
    }

 
   /**	Plots a path between two nodes in the road network 
    * 	@param world
    * 	@param starting node
    * 	@param goal node
    *	@return path
    */
   public ArrayList<GeomPlanarGraphDirectedEdge> findNewAStarPath(Node startNode, Node endNode)
   {   
       // get the home and work Nodes with which this Agent is associated
       Node currentJunction = startNode;
       Node destinationJunction = endNode;

       if (currentJunction == null)
       {
           return null; // just a check
       }
       // find the appropriate A* path between them
       AStar pathfinder = new AStar();
       ArrayList<GeomPlanarGraphDirectedEdge> path =
           pathfinder.astarPath(currentJunction, destinationJunction);
       //System.out.println("Agent->findNewAStarPath->have path: " + path.size());

       // if the path works, return it
       if (path != null && path.size() > 0) {
    	   		setPathLength(path.size());	
    	   		return path;
       }
       else {
    	   		return null;
       }

   }

   
   /**
    * Set up agent to begin agent traveling on a path
    * @param World
    * @param ArrayList<GeomPlanarGraphDirectedEdge>
    * @return Boolean True if there is a valid path and the agent is set up
    */
   public boolean beginPath (ArrayList<GeomPlanarGraphDirectedEdge> path) {
       // if the path works, lay it in
	   
       if (path != null && path.size() > 0)
       {  		
    	   if (multipathDirection > 0) {
	        // set up how to traverse this first link
      	    //System.out.println("Agent->beginPath->indexOnPath->" + indexOnPath);
    		indexOnPath = 0;
	        GeomPlanarGraphEdge edge =
	            (GeomPlanarGraphEdge) path.get(0).getEdge();
	        //System.out.println("have path edge");
	        //System.out.println("edgeTraffic hashmap: " + geoTest.edgeTraffic);
	        //System.out.println("Is HashMap Empty? "+ geoTest.edgeTraffic.isEmpty());
	           
	        setupEdge(edge);
	           
	        //System.out.println("set up path edge");
	
	        // update the current position for this link
	        updatePosition(segment.extractPoint(currentIndex));
	           
	        //System.out.println("Agent->beginPath->Update position at path's beginning; path length: " + currentPath.size() + " " + getID());       
	        return true;
    	   }
    	   else {
   	        // set up how to traverse this first link
    		indexOnPath = path.size()-1;
   	        //System.out.println("Agent->beginPath->indexOnPath->" + indexOnPath);
   	        GeomPlanarGraphEdge edge =
   	            (GeomPlanarGraphEdge) path.get(path.size()-1).getEdge();
   	        System.out.println("have path edge");
   	        //System.out.println("edgeTraffic hashmap: " + geoTest.edgeTraffic);
   	        //System.out.println("Is HashMap Empty? "+ geoTest.edgeTraffic.isEmpty());
   	           
   	        setupEdge(edge);
   	           
   	        //System.out.println("set up path edge");
   	
   	        // update the current position for this link
   	        updatePosition(segment.extractPoint(currentIndex));
   	           
   	        //System.out.println("Agent->beginPath->Update position at path's beginning; path length: " + currentPath.size() + " " + getID());       
   	        return true;  
    	   }
    	   
       }
       else {
    	   	   System.out.println("Agent->beginPath->Not a valid path");
    	   	   return false;
       }
   }


   /** Called every tick by the scheduler */
   /** moves the agent along the path 
    * @param world
    */
   public void step(SimState state) // dummy step do we need this in the superclass?
   {

	   this.state = (World) state; // update agent's state every step
	   
   }
   
   
   /**
    * Method moves the agent towards a goal coordinate
    * Returns false, if the agent doesn't move and is at the goal coordinate
    * @param coord
    * @return
    */
   boolean moveToCoord (Coordinate coord) {
	   //System.out.println("Agent>moveToCoord>" + getID() + " " + currentCoord + " move to " + coord);
	   // Basic movement method, moves agent towards a coordinate at its moveRate
	   // If already at the coordinate return false
	   if (currentCoord.equals2D(coord)) {
		   //System.out.println("Agent>moveToCoord>" + getID() + " already at " + currentCoord);
		   return false;
	   }
	   else {
		   // move agent
		   //System.out.println("Agent>moveToCoord>" + getID() + " moving");
		   // find x/y of the from and to coordinates
//		   double goalLat = coord.y;
//		   double goalLong = coord.x;
		   double currLat = currentCoord.y;
		   double currLong = currentCoord.x;
		   
		   // find the distances to the goal coordinate x/y and the move coordinate 
		   double latDist = coord.y - currentCoord.y;
		   double longDist = coord.x - currentCoord.x;
		   //System.out.println("Agent>latdist " + latDist + " longdist " + longDist);
		   double distToCoord = Math.sqrt( (latDist*latDist) + (longDist*longDist) );
		   //System.out.println("Agent>distance to coord>" + distToCoord);
		   //System.out.println("Agent>movementRate>" + getMoveRateKmPerStep());
		   double speed = Spacetime.kilometersToDegrees(getMoveRateKmPerStep());
		   //System.out.println("Agent>coordspeed>" + speed);
		   
		   if (distToCoord < speed) {
			   //System.out.println("Agent>distance to coord is less than movement rate " + speed + " go to " + coord);
			   currentCoord = coord;
			   updatePosition(currentCoord); 
			   //System.out.println("Agent>moveToCoord>" + getID() + " within movementRate moved to goalcoord " + currentCoord);
		   }
		   
		   else {
			   // calculate the movement coordinate for long and lat for this step based on the MovementRate
			   // movement is relative to the lat/long movement, 
			   // i.e. using Euclidean Distance for each new coordinate x/y
			   double moveCoordLong = ( speed * longDist / distToCoord ) + currLong;
			   double moveCoordLat = ( speed * latDist / distToCoord ) + currLat;
			   //System.out.println("Agent>movement>" + moveCoordLong + " " + moveCoordLat + " moveRate: " + getMovementRate());
		   
			   // if the movement to a new lat/long is less than the goal lat/long, move to the goal coord
			   //if ( (moveCoordLong <= goalLong) || (moveCoordLat <= goalLat) ) {
			   	   Coordinate tempCoord = new Coordinate(moveCoordLong, moveCoordLat);
				   currentCoord = tempCoord;
				   updatePosition(currentCoord);
				   //System.out.println("Agent>moveToCoord>" + getID() + " moved to tempcoord");
			   //}
		   }
		   
		   //System.out.println("Agent>moveToCoord>" + getID() + currentCoord);
		   return true;
	   }
   }

   
	//===============================	
	//
	//	REROUTE METHODS
	//
	//===============================	
   
   // not tested
   public boolean passableCoord (GeomVectorField blockedarea, Coordinate coord) {
	   MasonGeometry testGeo = new MasonGeometry(fact.createPoint(coord));	   
	   Bag areaobjects = blockedarea.getCoveringObjects(testGeo.getGeometry());
		  if ( areaobjects.isEmpty() ) {
			  return false;
		  }	  
		  else return true;
   }

   
	//===============================	
	//
	//	AGENT HELPER METHODS
	//
	//===============================	
     
   
   /**
   * This method is to find the nearest geometry from a given geometry in a given GeomVectorField. 
   * Method is taken from DARPA Project Code; Authors include Hamdi Kavak and Joonseok Kim at GMU GGS
   * @param from
   * @param field
   * @param startDistance
   * @return
   */
   public static MasonGeometry findNearestGeometry(MasonGeometry from, GeomVectorField field, double startDistance) {
	   // we assume the index of field is up-to-dated.
	   int MAXIMUM_THRESHOLD = 3;

	   // TODO: we need to optimize NNQ later
	   // For now, we gradually increase a distance for the nearest neighbor query, and
	   // then perform range query.
	   Bag candidates = field.getGeometries();
	   double dist = startDistance;
	   // if size of data is small enough, we will not perform any range query any
	   // more.
	   if (candidates.size() > MAXIMUM_THRESHOLD) {
		   // filter step
		   while (true) {
			   // simply increase or decrease distance to find an appropriate number of
			   // candidates
			   candidates = field.getObjectsWithinDistance(from, dist);
			   if (candidates.size() == 0)
				   dist *= 10;
			   else if (candidates.size() > MAXIMUM_THRESHOLD)
				   dist *= 0.5;
			   else
				   break;
		   }
	   }

	   double minDist = Double.MAX_VALUE;
	   MasonGeometry nearest = null;
	   // refinement step: now it's time to find the nearest among candidates
	   for (Object ele : candidates) {
		   MasonGeometry geo = (MasonGeometry) ele;
		   double tmp = geo.geometry.distance(from.geometry);
		   if (minDist > tmp) {
			   nearest = geo;
			   minDist = tmp;
		   }
	   }

	   return nearest;
   }
   
   
   public static Node findNearestNode(MasonGeometry point, GeomVectorField nodefield, GeomPlanarGraph roadNet, double startDistance) {
	   Bag candidates = nodefield.getGeometries(); //world.roadIntersections.getGeometries();
	   double dist = startDistance;
	   while (true) {
		   // simply increase or decrease distance to find an appropriate number of candidates
		   candidates = nodefield.getObjectsWithinDistance(point, dist);  //world.roadIntersections.getObjectsWithinDistance(point, dist);
		   if (candidates.size() == 0) {
			   dist *= 10;
		   }
		   else if (candidates.size() > 3) {
			   dist *= 0.5;			   
		   }
		   else { break; }	   
	   }
	   
	   double minDist = Double.MAX_VALUE;
	   MasonGeometry nearest = null;
	   // refinement step: now it's time to find the nearest among candidates
	   for (Object ele : candidates) {
		   MasonGeometry geo = (MasonGeometry) ele;
		   double tmp = geo.geometry.distance(point.geometry);
		   if (minDist > tmp) {
			   nearest = geo;
			   minDist = tmp;
		   }
	   }
	   
	   Bag nearestNodes = nodefield.getCoveredObjects(nearest); 
	   //System.out.println("Agent>findNearestNode>number of nodes at this point: " + nearestNodes.size());
	   MasonGeometry nearestObj = (MasonGeometry) nearestNodes.get(0);
	   Coordinate nearestNodeCoord = nearestObj.geometry.getCoordinate();
	   //System.out.println("Agent>findNearestNode>node coordinate " + nearestNode.getCoordinate());
	   
	   Node nearestNode = null; 
	   
       Iterator<?> nodeIterator = roadNet.nodeIterator();
       
       while (nodeIterator.hasNext())
       {
       	// Create a GeometryVectorField of points representing road intersections
           Node node = (Node) nodeIterator.next();
           Coordinate coord = node.getCoordinate();
           if (nearestNodeCoord == coord) {
        	   nearestNode = node;
           }
       }
       //System.out.println("Agent>findNearestNode>nearest node " + nearestNode + " " + nearestNode.getCoordinate());
       return nearestNode;
   }
   
   
   /**
    * Calculates the length of a path in kilometers to get agent commuting distance
    * @param path
    * @return distance in kilometers
    */
   public double getpathDistance (ArrayList<GeomPlanarGraphDirectedEdge> path) {
	   
	    //System.out.println("pathDistance");   
  		double distance = 0;

   		for (GeomPlanarGraphDirectedEdge directedEdge: path) {
   			double x =  Spacetime.degToKilometers(AStar.length(directedEdge));
   			distance = distance + x;
   		}
   		
   		//System.out.println("agent path distance: " + distance);
   		return distance;	
   		
   }
   
   
   /**
    * Methods to quickly get the goal node of a path regardless of link direction
    * @param path
    * @return GoalNode
    */
   public Node getGoalNode (ArrayList<GeomPlanarGraphDirectedEdge> path) {
	   //System.out.println("Path size: " + path.size());
	   //System.out.println("Start node: " + path.get(0).getFromNode().getCoordinate());
	   //for (GeomPlanarGraphDirectedEdge currentedge: path) 
		//   System.out.println(currentedge.getToNode().getCoordinate());
       if (path.size() == 1) {
	   		Node lastNode = path.get(0).getToNode();
	   		//System.out.println("Path size: " + path.size());
	   		//System.out.println("Start node: " + path.get(0).getFromNode().getCoordinate());
		    //System.out.println("Goal node: " + lastNode.getCoordinate());
	   		return lastNode;
       }
       else if (path.size() > 1) {
	   		   int pathIndex = path.size() - 1;
	   		   //System.out.println("Path size: " + path.size());
	   		   //System.out.println("Start node: " + path.get(0).getFromNode().getCoordinate());
	   		   Node lastNode = path.get(pathIndex).getToNode();
	   		   //System.out.println("Goal node: " + lastNode.getCoordinate());
	   		   return lastNode;
       }
       else if (path.size() <= 0) {
    	   		//System.out.println("no goal node");
    	   		return null;
       }
	   else return null;
  }
   
   
   /**
    * Method to quickly get the start node of a path
    * @param path
    * @return StartNode
    */
   public Node getStartNode (ArrayList<GeomPlanarGraphDirectedEdge> path) {
       if (path.size() >= 1) {
	   		Node firstNode = path.get(0).getFromNode();
		    //System.out.println("Start node: " + firstNode.getCoordinate());
	   		return firstNode;
	   		
       }
	   else return null;
   }
   
   
   /**
    * Method to quickly get the end node of a path
    * @param path
    * @return EndNode
    */
   public Node getEndNode (ArrayList<GeomPlanarGraphDirectedEdge> path) {
	   int pathSize = path.size() - 1;
	   Node lastNode = null;
       if (path != null) {
	   		lastNode = path.get(pathSize).getToNode();
		    //System.out.println("End node: " + lastNode.getCoordinate());
       }

	   return lastNode;
   }
   

   /** 
    * Sets the Agent up to proceed along an Edge
    * @param edge the GeomPlanarGraphEdge to traverse next
    */
   void setupEdge(GeomPlanarGraphEdge edge)
   {
	   //System.out.println("enter setupEdge indexOnPath " + indexOnPath + " currentIndex " + currentIndex);
       //System.out.println("startIndex " + startIndex + " endIndex " + endIndex);
       // clean up on old edge -- take agent off the edge
       if (currentEdge != null)
       {
    	   		//System.out.println("current edge not null");
    	   		//System.out.println("current edge: " + currentEdge);
    	   		//System.out.println("edgeTraffic: " + world.edgeTraffic);

           ArrayList<Agent> traffic = this.state.edgeTraffic.get(currentEdge);
           //System.out.println("got current edge");
           traffic.remove(this);
           //System.out.println("agent off current edge");
       }
       currentEdge = edge;
       
       //System.out.println(currentEdge + " current edge updated with " + edge);
       //System.out.println(world.edgeTraffic);
       //System.out.println("Is HashMap Empty? "+ world.edgeTraffic.isEmpty());
       //System.out.println(world.edgeTraffic.get(currentEdge));

       // update new edge traffic
       if (this.state.edgeTraffic.get(currentEdge) == null)
       {
    	       //System.out.println("no agents on edge");
           this.state.edgeTraffic.put(currentEdge, new ArrayList<Agent>());
           //System.out.println("created new list of agent traffic on edge");
       }
       //System.out.println("add agent " + this.getEndID());
       this.state.edgeTraffic.get(currentEdge).add(this);
       //System.out.println("update edge");

       // set up the new segment and index info
       LineString line = null;
       // post-impact some edges are destroyed, agents needs a reroute
       // if there is no line, reroute, else get the line
       if (edge.getLine() == null) {
    	   System.out.println("Agent>transitionToNextEdge>reroute");
    	   setneedReroute(true);
    	   return;
       }
       else {
    	   line = edge.getLine();
       }
       segment = new LengthIndexedLine(line);
       //startIndex = Spacetime.degToKilometers(segment.getStartIndex());
       edge_end_p = Spacetime.degToKilometers(segment.getEndIndex());
       linkDirection = 1;
       //System.out.println("start index " + startIndex + " end index" + endIndex);

       // check to ensure that Agent is moving in the right direction
       // then set currentIndex to start of path 
       // and set the link direction
       double distanceToStart = line.getStartPoint().distance(location.geometry),
           distanceToEnd = line.getEndPoint().distance(location.geometry);

       if (distanceToStart <= distanceToEnd)
       { // closer to start
           currentIndex = startIndex;
           linkDirection = 1;
       } else if (distanceToEnd < distanceToStart)
       { // closer to end
           currentIndex = endIndex;
           linkDirection = -1;
       }
       //System.out.println("exit setupEdge indexOnPath " + indexOnPath + " currentIndex " + currentIndex);
       //System.out.println("startIndex " + startIndex + " endIndex " + endIndex + " linkDirection " + linkDirection);

   }


   /** 	
    * move the agent to the given coordinates 
    * @param world 
    * @param agent coordinate
    */
   public void updatePosition(Coordinate c)  {
	   this.lastCoord2 = this.lastCoord1;
	   this.lastCoord1 = this.currentCoord; // used to check movement
	   // System.out.println(getID() + " updateposition " + indexOnPath + " " + pathDirection);
       pointMoveTo.setCoordinate(c);
       location.geometry.apply(pointMoveTo);
       
       // Courtesy of Joonseok Kim:
       // geometryChanged() is added to fix display bug...it ensures the agent position is updated properly
       location.geometry.geometryChanged(); 
       //setLocation(this.getGeometry()); test code can be deleted
       //System.out.println("Agent->updatePosition->Coord " + currentCoord);
       //world.agents.setGeometryLocation(location,pointMoveTo);
   }


   /** return geometry representing agent location */
   public MasonGeometry getGeometry() {
       return location;
   }
   
   
	//==============================================
	//
	// 		VERIFICATION METHODS
	//
	//==============================================
  
   /**
    * Returns the first and last node coordinates of a given path
    * @param path
    */
   public void ckPathNodes(ArrayList<GeomPlanarGraphDirectedEdge> path) {
	   int lastIndex = path.size() - 1;
	   System.out.println("Agent->ckPathNodes-> index0 " + path.get(0).getCoordinate() + " index" + (lastIndex + 1) + " " + path.get(lastIndex).getCoordinate());
	   //System.out.println("Agent->ckPathNodes-> startNode " + " endNode " + lastIndex + " " + path.get(lastIndex).getCoordinate());
   }

   /**
    * Returns true if the path Nodes are full connected with its indexed edges
    * @param path
    * @return
    */
   public boolean ckPath(ArrayList<GeomPlanarGraphDirectedEdge> path) {
	   int lastIndex = path.size() - 1;
	   int currentIndex = 0;
	   boolean goodPath = true;
	   
	   while (currentIndex < lastIndex) {
		   GeomPlanarGraphDirectedEdge edge = path.get(currentIndex);
		   GeomPlanarGraphDirectedEdge nextedge = path.get(currentIndex + 1);
		   Node teststartNode = edge.getFromNode();
		   Node testendNode = edge.getToNode();
		   Node nextStartNode = nextedge.getFromNode();
		   Node nextEndNode = nextedge.getToNode();
		   
		   if ( endNode != nextStartNode ) {
			   goodPath = false;
			   System.out.println("Agent>ckPath>bad");
			   System.out.println("Agent>line1: " + teststartNode.getCoordinate() + " " + testendNode.getCoordinate());
			   System.out.println("Agent>line2: " + nextStartNode.getCoordinate() + " " + nextEndNode.getCoordinate());
			   return goodPath;
		   }
		   currentIndex += 1;
	   }
	   
	   return goodPath;
	   
   }
   
}
