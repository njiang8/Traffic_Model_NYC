/**
 * Disaster ABM in MASON
 * @author Annetta Burger
 * 2018-19
 */

package disaster;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeMap;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.planargraph.Node;

import sim.engine.SimState;
import sim.field.geo.GeomVectorField;
import sim.util.Bag;
import sim.util.geo.GeomPlanarGraph;
import sim.util.geo.GeomPlanarGraphDirectedEdge;
import sim.util.geo.GeomPlanarGraphEdge;
import sim.util.geo.MasonGeometry;

//Agent class for Individual agents
/**
*  Individual subclass of Agent builds out the individual characteristics of the agent 
*  Inherits the movement characteristics of Agent
*/

public class Indv extends Agent{

	//====================================
	//
	//      ATTRIBUTES
	//
	// Unique to individual agents
	//
	//====================================
	
	// Demographics
	private int age; 
	private String sex;
    
	// Ego social networks
    public HashMap <String, Indv> idsToHouseMembers = new HashMap <String, Indv> ();  // current household
	private ArrayList <String> hholdnet = new ArrayList <String> (); // all household members
	// GIS, road network, and Census Data
	private String tract = "";
	private String county = "";
	// to be passed with super constructor
	private String homeID = "";  // in lieu of startID
	private String workID = "";  // in lieu of endID
	private String hmRdID = "";  // in lieu of startRdID
	private String wrkRdID = ""; // in lieu of endRdID
	private Node workNode;  // in lieu of startNode
	private Node homeNode;  // in lieu of endNode
	
	// Schedule data and commuting and movement statistics
	int work; //0: work from home; 1: commute to work
	int status; //0: at home; 1: on commute; 2:at work
	
	boolean StayAtHome; // agent is a stay-at-home agent
	boolean ToWork;  // agent going to (needs to be at) work
	
	boolean atWork;  // agent is at work
	boolean atHome;  // agent is at home
	boolean onCommute;// agent is commuting
	boolean fleeing;  // agent is fleeing
	boolean isHomeless;  // agent no longer has a home
	private double commutedist;
	private int tcommuteStart;
	private int tcommuteEnd;
	private int tcommuteTime;
	int last_p_index = 0;
	double small_dist = 0.0;
	
	
	ArrayList<GeomPlanarGraphDirectedEdge> commutePath = // path agent travels
			new ArrayList<GeomPlanarGraphDirectedEdge>();
	HashMap <Node, Integer> goalnodesToSchedule =  // provides the time schedule of path goalNodes 
			new HashMap <Node, Integer> ();
	//HashMap <Integer, ArrayList<GeomPlanarGraphDirectedEdge>> scheduleToPaths =  // provides the path for a set time
	//		new HashMap <Integer, ArrayList<GeomPlanarGraphDirectedEdge>> ();
	
	
	
	// Emergency Response Attributes
	private boolean firstResp = false;		// flag for ID as first responder 
	private double distFromGroundZero = 9999;				// distance agent is from ground zero
	
	
	//==================================
    /** 	
     * 	Individual Agent Getters and Setters
     * 	Agent characteristics come from the demographics data files
     * 	Agent schedule comes from information derived from the data files
     */
	//===================================
	
	public void setTract(String x)		{ this.tract = x; }
	public void setCounty(String x)		{ this.county = x; }
	public void setIsFirstResp(boolean x)	{ this.firstResp = x; }		// first responder flag
    public void setAge(int x)			{ this.age = x; }
    public void setHomeID(String x)		{ this.homeID = x; }
    public void setWorkID(String x)		{ this.workID = x; }
    public void setHmRdID(String x)		{ this.hmRdID = x; }
    public void setWrkRdID(String x)	{ this.wrkRdID = x; }
    public void setHomeNode(Node x)		{ this.homeNode = x; }
    public void setWorkNode(Node x)		{ this.workNode = x; }
    
//    public void setToWork(boolean x)	{ this.ToWork = x; }
//    public void setatWork(boolean x)	{ this.atWork = x; }
//    public void setatHome(boolean x)	{ this.atHome = x; }
//    public void setonCommute(boolean x)	{ this.onCommute = x; }
    
    public void setWork(int x) {this.work = x;}
    public void setStatus(int x) {this.status = x;}
    
    public void setisHomeless(boolean x) { this.isHomeless = x; }
    public void set_tcommuteStart(int x)	{ this.tcommuteStart = x; }
    public void set_tcommuteEnd(int x)	{ this.tcommuteEnd = x; }
    public void set_tcommuteTime(int x)	{ this.tcommuteTime = x; }
    public void setcommuteDist(double x)	{ this.commutedist = x; }
    public void sethholdnet(ArrayList<String> x)  { this.hholdnet = x; }

    public String getTract()  			{ return this.tract; }
	public String getCounty()			{ return this.county; }
	public boolean getIsFirstResp()		{ return this.firstResp; }		// first responder flag
    public int getAge()    				{ return this.age; }
    //public char   getSex()    			{ return this.sex; }
    //public int getHhtype()				{ return this.hhtype; }
    public boolean getStayAtHome()		{ return this.StayAtHome; }
    public String getHomeID() 			{ return this.homeID; }
    public String getWorkID() 			{ return this.workID; }
    public String getHmRdID()			{ return this.hmRdID; }
    public String getWrkRdID()			{ return this.wrkRdID; } 
    public Node getHomeNode()			{ return this.homeNode; }
    public Node getWorkNode()			{ return this.workNode; }
    public boolean getToWork()			{ return this.ToWork; }
    public boolean getatWork()			{ return this.atWork; }  
    public boolean getatHome()			{ return this.atHome; }
    public boolean getonCommute()		{ return this.onCommute; }
    public boolean getisHomeless()		{ return this.isHomeless; }
    public int get_tcommuteStart()		{ return this.tcommuteStart; }
    public int get_tcommuteEnd()		{ return this.tcommuteEnd; }
    public int get_tcommuteTime()		{ return this.tcommuteTime; }
    public double getcommuteDist()		{ return this.commutedist; }
    public ArrayList<String> getHholdnet()  { return this.hholdnet; }
    public int getWork() {return this.work;}
    public int getStatus() {return this.status;}
	
    
    
	//====================================
    /**
     * Indv -- individual agent constructor
     * @param world
     * @param census
     * @param county
     * @param ID
     * @param home
     * @param work
     * @param homeroad
     * @param workroad
     * @param housenet
     */
	//====================================
    
//	public Indv(World world, String census, String county, String ID, String home, String work, String homeroad, String workroad) {
	public Indv(World world, String census, String county, String ID, String age, String home, String work, String homeroad, String workroad, ArrayList<String> housenet) {
		super(world); // super constructor sets superclass initialization data
		//System.out.println("Indv->creating indv agent");
		
		setID(ID);
		setTract(census);
		setCounty(county);
		setAge(Integer.parseInt(age));
		setHomeID(home);
		setWorkID(work);
		setisHomeless(false);
		setHealthStatus(1); // healthy
		setHmRdID(homeroad);
		setWrkRdID(workroad);
		setStatus(0); //All agents at home
		
		Monitors.atHomeCount += 1; // all agents start at home
	
		// Count all the stay-at-home agents in the population
		if ( getHomeID() == getWorkID() ) {
			setWork(0);
			//setStayAtHome(true);
			Monitors.stayAtHome ++; // tracks all the Stay-At-Home Agents
		}
		else {setWork(1);}
		//System.out.println("Indv->basic rdID info: " + getHmRdID() + " " + getWrkRdID());
		//System.out.println("Indv->housenet: " + getHholdnet());
		//System.out.println("Indv->hmrdID: " + getHmRdID() + " edge: " + world.idsToEdges.get(getHmRdID()));
		//System.out.println("Indv->wrkrdID: " + getWrkRdID() + " edge: " + world.idsToEdges.get(getWrkRdID()));
		
		GeomPlanarGraphEdge startingEdge = world.idsToEdges.get(hmRdID);
		GeomPlanarGraphEdge goalEdge = world.idsToEdges.get(wrkRdID);
		
		if (startingEdge == null) {
			Monitors.badhomeNode += 1;
			System.out.println("Indv->bad HomeID -- no Node!!");
			pathset = false;
		}
		else {
			// set up information about where agent node is and where it's going
			// agent's home and work locations are at one end Node of the RdID edge
			setHomeNode(startingEdge.getDirEdge(0).getFromNode());
			setWorkNode(goalEdge.getDirEdge(0).getToNode());
			//System.out.println("Indv>WorkNode: " + getWorkNode() + " " + getWorkNode().getCoordinate());
			// set the agent(parent) class attributes needed for transitionToNextEdge() when commuting
			setStartNode(getHomeNode());
			setEndNode(getWorkNode());
		
			// set the location to be displayed
			GeometryFactory fact = new GeometryFactory();
			setLocation(new MasonGeometry(fact.createPoint(new Coordinate(10, 10))));
			Coordinate startCoord = null;
			startCoord = getHomeNode().getCoordinate();
			updatePosition(startCoord);
			currentCoord = startCoord;
		
			setGoal("commute");		
			setGoalNode(this.workNode); // WorkNode is created at initialization -- see EndNode in Agent superclass
		
			this.getGeometry().setUserData(this);  // make object & health status attribute accessible for grouping and Portrayals (indicates color, etc.)
		
			pathset = setCommutepaths(); // pathset is a boolean that agent has a path
		}
		//System.out.println("Indv>End Constructor WorkNode: " + getWorkNode().getCoordinate());
	}
	
    /** Set paths of an Agent: find an A* path to work!
    * 	@param state
    * 	@return whether or not the agent successfully found a path to work
    */
   public boolean setCommutepaths()
   { 
	   // Set schedule and paths
	   // Create commute path and schedule
	   //System.out.println("Indv->setCommutepaths->goalNode: " + getHomeNode().getCoordinate());
	   this.lastCoord1 = this.currentCoord;
	   this.lastCoord2 = this.currentCoord;
	   if (getHomeNode() != getWorkNode()) {
	       commutePath = findNewAStarPath(getHomeNode(), getWorkNode());
	       
	       if ( commutePath == null )  {
	    	   		System.out.println("Indv->setCommutepaths->no available path; no agent initialized");
	    	   		return false;
	       }
	       else { 
	    	   		set_tcommuteStart(100);
	    	   		set_tcommuteEnd(600);
	    	   		
	    	   		//set_tcommuteStart(730);  // @step 450 the first day/ 1890/3330/etc.
	    	   		//set_tcommuteEnd(1830);  // @step 1110 the first day/ 2550/3990/etc.
	       
	    	   		//System.out.println("set commute times");
	    	   		//System.out.println("get goal Node: " + getGoalNode(commutePath));
	    	   		goalnodesToSchedule.put(getGoalNode(commutePath), get_tcommuteStart());
	    	   		//System.out.println("stored path");
	    	   		//scheduleToPaths.put(get_tcommuteStart(), commutePath);  // put commuting path into the schedule
	    	   		//System.out.println("Indv->setCommutepaths->stored commute start");
	       
	    	   		currentPath = commutePath;	    	   
	    	   		beginPath(currentPath);
	    	       	setcommuteDist(getpathDistance(currentPath));
	    	       	//setcommuteDist(getDistance(currentPath));
	    	   	   return true;
	       }
	       
	   }
	   else { 
		   if (getHomeNode() == getWorkNode()) {
		   	   System.out.println("Indv->setCommutepaths->Initialized agent / commute start @time " + get_tcommuteStart() +
		   	   		" no commute; agent at home: " + getLocation());
		   }
		   else {
			   Monitors.badagent += 1;
			   System.out.println("Indv->setCommutepaths->No commute path and HomeNode doesn't match WorkNode; badagent");
		   }
		   return false;
	   }

   }
   
   
   /** Called every tick by the scheduler */
   /** moves the agent along the path 
    * @param world
    */
   public void step(SimState world){
	   state = (World) world; // update perception of the world at each step
	   //Individuals continue routine, if they are alive and well 
	   routine();
	   
	   //validation out put
	   long currentStep = this.state.schedule.getSteps();
	   if ( Parameters.exportVizData )
       {
           if ( ((currentStep >= 50) && (currentStep <= 150))  ) {
        	   World.writeVizData((int) currentStep, agentID, speed);}
       } // record viz data
   }

   
   
	//======================================
	//
	//     ROUTINE
	//
	//======================================  
   /**
    * Agents conduct their daily routines
    * @param world
    * sets routine goals
    */
   void routine() {
	   long currentStep = this.state.schedule.getSteps();
	   int time = Spacetime.time24(currentStep);
	   
	   // Agents commute if they do not stay at home or are not internally displaced
	   //if (!getStayAtHome() && !getisHomeless()) {
	   if(getWork() == 1 && !getisHomeless()) {
		   // Routine schedule and time check; depends on work
		   // check the time to see if the agent needs to start commuting to or from work
		   if (time == get_tcommuteStart()) {  // commute to work
			   setStatus(1);//change status to 1, on commute
		   }	   
		   if (time == get_tcommuteEnd()) {  // commute from work
			   setStatus(1);//change status to 1, on commute
		   }
		   //Start commute
		   if (getStatus() == 1){commute();}   
		   }
	   	   
   }

   
   /** 	Move agent on its commute used in step before event
    *   Equivalent to travelPath in superclass, but specialized for commute
    * 	@param world
    */
   void commute()	{
	   
	   //System.out.println("Indv->commute->" + getID() + " " + currentCoord + " " + getEndNode().getCoordinate());
	   //System.out.println("currentpathlength: " + currentPath.size() + " linkDirection: " + linkDirection);
	   
	   //1, Change status and update monitor
       if (getSegment() == null) //check that we've been placed on an Edge    
       {
           return;
       } 
       else if (reachedDestination == true)// check if reach our destination
       {
//    	   System.out.println("Indv->step->routine->commute->" + getID() + " reached destination, need to flipPath");
//    	   System.out.println("Indv->step->routine->commute->" + getID() + " reached destination spped is " +speed);
    	   if (linkDirection > 0){
    		   setStatus(2);//at work
         	   Monitors.atHomeCount -= 1;
    		   Monitors.atWorkCount += 1;
        	   Monitors.onCommuteCount -= 1;
    	   }
    	   else if (linkDirection < 0){
    		   setStatus(0);//at home
    		   Monitors.atHomeCount += 1;
    		   Monitors.atWorkCount -= 1;
    		   Monitors.onCommuteCount -= 1;
    	   } 
    	   speed = 0;
    	   flipPath();
    	   //System.out.println("Indv->step->routine->commute->" + getID() + " after filp linkDirection: " + linkDirection);
    	   return;
       }

	   //2, get commute path data(traffic condition, speed limit) and commute data (speed)
	   long currentStep = this.state.schedule.getSteps();
	   double moveRate = this.state.edgesToSpeedLimit.get(currentEdge) / 60;
	   //double moveRate = Math.round(e_speed_limit * 100) / 100;  //km/min
	   //System.out.println("Inv-commute-> move rate:" +moveRate );
	   double travel_dist_m = 0;//distance travel per min

	   
	   //2.1 Set Commute Speed
	   //Speed will slow down during traffic jam
	   double total_traffic = getTotaltraffic(currentPath);//total traffic on the whole commute path
	   speed = traffic_jam(total_traffic, commutedist, moveRate);
	   //System.out.println("Inv-commute-> traffic:" +total_traffic +" and speed is: "+ speed);
	   travel_dist_m = Math.abs(speed);
	   
	   //System.out.println("Inv-commute->speed normal: Total commute distance" + travel_dist_m);
	   update_UI(travel_dist_m);
   }
   
   /**Update UI*/
 	void update_UI(double dist) {
// 		//sum distance of the edges that can be traveled each time step\
 		long currentStep = this.state.schedule.getSteps();
 		double sum_dist = 0; //total distance traveled on this time step
 		double le_dist = 0;  //distance of specific edge
		int p_index = last_p_index;//path index      
		//double residual = 0.0;
 		
 		//System.out.println("Inv-commute->update_ui:path size: " +currentPath.size());
 		//System.out.println("Inv-commute->update_ui:outdide loop commute distance: " +commutedist);
 		//System.out.println("Inv-commute->update_ui:distance should be traveld at this time step: " +dist+ " and has traveled "+ sum_dist);
		System.out.println("Inv-commute->update_ui:outdide loop Step: " +currentStep+ "p index"+ p_index);
		if (linkDirection == 1){
  			GeomPlanarGraphEdge edge = (GeomPlanarGraphEdge) currentPath.get(p_index).getEdge();
  			while(sum_dist < dist) {
  				if(p_index == currentPath.size()) {
  					last_p_index = p_index - 1;
  					break;
  				}
  				else {
	  				edge = (GeomPlanarGraphEdge) currentPath.get(p_index).getEdge();
	  				setupEdge(edge);
	  				
	  				System.out.println("Inv-commute->update_ui:inside loop Step: " +currentStep+ "current edge distance "+ edge_end_p);	  				
	  				if(dist < edge_end_p) {
	  					small_dist += dist; 					
	  					System.out.println("Step: " +currentStep+ "current ip_index"+p_index+"agent move small distance; agent moved "+ small_dist);
	  					if(small_dist >= edge_end_p) {
	  						small_dist = 0;
	  						p_index++;
	  						if(p_index == currentPath.size() - 1) {
		  						reachedDestination = true;
		  						System.out.println("Inv-commute->update_ui:inside loop Step: " +currentStep+ "reached? "+ reachedDestination);
		  						}
	  					}
	  					else{break;}
	  				}
	  				else if (dist >= edge_end_p){
	  					sum_dist += edge_end_p;
	  					p_index ++;
	  					if(p_index == currentPath.size()) {
	  						reachedDestination = true;
	  						System.out.println("Inv-commute->update_ui:inside loop Step: " +currentStep+ "reached? "+ reachedDestination);
	  					}
	  				}
	  				}
  				}
  			currentCoord = segment.extractPoint(0.1);
  			updatePosition(currentCoord);
  			last_p_index = p_index;
  			}	
		
		
		
//		//back
//		if (linkDirection == -1){
//  			GeomPlanarGraphEdge edge = (GeomPlanarGraphEdge) currentPath.get(p_index).getEdge();
//  			while(sum_dist < dist) {
//  				if(p_index == 0) break;
//  				else {
//	  				edge = (GeomPlanarGraphEdge) currentPath.get(p_index).getEdge();
//	  				setupEdge(edge);
//	  				sum_dist += endIndex;
//	  				p_index --;
//	  				if(p_index == 0) {
//	  					reachedDestination = true;
//	  					System.out.println("Inv-commute->update_ui:back inside loop Step: " +currentStep+ "reached? "+ reachedDestination);
//	  				}
//  				}
//  			}
//  			currentCoord = segment.extractPoint(0.1);
//  			updatePosition(currentCoord);
//  			last_p_index = p_index;
//		}
 	}
 	

 	

	  /** Flip the agent's path around for commuting purposes */
	  void flipPath() {
	      reachedDestination = false;
	      pathDirection = -pathDirection;
	      linkDirection = -linkDirection;
		  //System.out.println(getID() + " flipPath pathD: " + pathDirection + " linkD: " + linkDirection);
	  }
  
	  /** Get the total Traffic condition of the commute path */
	  double getTotaltraffic(ArrayList<GeomPlanarGraphDirectedEdge> path) {
		   double t_traffic = 0;
		   for (int i = 0; i < path.size(); i++) {
			   //edge
			   GeomPlanarGraphEdge loop_edge = (GeomPlanarGraphEdge) currentPath.get(i).getEdge();
			   setupEdge(loop_edge);
			   t_traffic += this.state.edgeTraffic.get(loop_edge).size();
			}
		   return t_traffic - 14;
	  }
	  
	  
	  /**create traffic jam*/
	  double traffic_jam(double edgeTraffic, double commutedist, double moveRate){
		  double factor = 0;
		  double t_speed = 0.0;
		  if (Parameters.Scale == true) {
			  factor = (commutedist * 100) / (edgeTraffic * Parameters.num_represented * Math.log(edgeTraffic * Parameters.num_represented) + 1);
			  System.out.println("Ind->Traffic_jam-> " + Parameters.num_represented);
			  factor = Math.min(1, factor);
		      //distance
		      double gridprog = moveRate * linkDirection * factor;
		      t_speed =  Math.round(gridprog * 100.0)/100.0;
		      //System.out.println("Inv-traffic jam-> speed: " t_speed+ "Scale?" + Parameters.Scale);
		  }
		  else if(Parameters.Scale == false) {
			  factor = (commutedist * 100) / (edgeTraffic * Math.log(edgeTraffic) + 1) ;
			  //System.out.println(" factor " + factor);
			  factor = Math.min(1, factor);
		      //distance
		      double gridprog = moveRate * linkDirection * factor;
		      t_speed =  Math.round(gridprog * 100.0)/100.0;	       
		  }
		  System.out.println("Inv-traffic jam-> speed: " +t_speed+ "Scale?" + Parameters.Scale);
		  return t_speed;
		   
	      }
   
}
