/**
 * Disaster ABM in MASON
 * @author Annetta Burger
 * 2018-19
 */

package disaster;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;

import sim.util.geo.MasonGeometry;
import sim.util.geo.PointMoveTo;
import sim.util.geo.GeomPlanarGraphEdge;
import sim.field.geo.GeomVectorField;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.planargraph.Node;

/**
 * Simple building class
 * Will hold agents and be impacted by disaster effects
 * @author Annetta
 *
 */

public class Building {
// public class Building implements Steppable {
	
	private static final long serialVersionUID = -1113018274619047013L;
	World world;
	
	private String bldgID = "";
	private String bldgRdID = "";
	private GeomPlanarGraphEdge rdEdge = null;
	private Node bldgNode;  // building road network node
	private int bldgType = 0;  // home=1, work=2, school=3, other=0
	private int bldgCondition = 0;   // rating of building condition re: infrastructure damage (r1 = 1, r2 = 2, r3 = 3)
	
	// point that denotes building's location
	private MasonGeometry location;
	Coordinate currentCoord = null;
	PointMoveTo pointMoveTo = new PointMoveTo();
	
    /** 	Agent Getters and Setters
     * 	Add buildings to the simulation and to the building GeomVectorField. 
     * 	Building characteristics come from the demographics data files
     */
	public void setLocation(MasonGeometry x) { this.location = x; }
    public void setID(String x)			{ this.bldgID = x; }
    public void setRdID(String x)		{ this.bldgRdID = x; }
    public void setNode( Node x)			{ this.bldgNode = x; }
    public void setType(int x)			{ this.bldgType = x; }
    public void setCondition(int x)		{ this.bldgCondition = x; }
    public void setEdge(GeomPlanarGraphEdge x)	{this.rdEdge = x; }
	
    public MasonGeometry getLocation()	{ return this.location; }
    public String getID()     			{ return this.bldgID; }
    public String getRdID()				{ return this.bldgRdID; }
    public Node getNode()				{ return this.bldgNode; }
    public int getType()					{ return this.bldgType; }
    public int getCondition()			{ return this.bldgCondition; }
    public GeomPlanarGraphEdge getEdge() 	{ return this.rdEdge; }
	
    /** Constructor Function
     * 
     */
    public Building(World world, String ID, String RdID, int Type, GeomPlanarGraphEdge rdEdge) {
    	
    		setID(ID);
    		setRdID(RdID);
    		setType(Type);
    		setCondition(0);
    		setEdge(rdEdge);
    	
    		//System.out.println("Building->basic info: " + getID() + " Type: " + getType());
    		
    		// Set Building Location Information
    		// set Building Node based on RdID Edge starting Node
    		//GeomPlanarGraphEdge rdEdge = world.idsToEdges.get(getRdID());
    		setNode(rdEdge.getDirEdge(0).getFromNode());
    		//System.out.println("Building->setNode");
    		// set the location to be displayed
    		GeometryFactory fact = new GeometryFactory();
    		setLocation(new MasonGeometry(fact.createPoint(new Coordinate(10, 10)))) ;
    		Coordinate bldgCoord = null;
    		bldgCoord = bldgNode.getCoordinate();
    		pointMoveTo.setCoordinate(bldgCoord);
    	    world.bldgField.setGeometryLocation(location,pointMoveTo);
    		
    		this.getGeometry().setUserData(this.getType());
    		
    		// Add bag of agents currently in the building
    	
    }
    
    
    /**
     * For future scheduling
     * Building may degrade after disaster (NWMD Effects)
     */
//    public void step(SimState state)  {
    	
//    		World world = (World) state;
//    		
//    		long currentStep = world.schedule.getSteps();
//    		int simTime = (int) world.schedule.getTime();
//    		System.out.println("Building->step->" + currentStep);
//    }
    
    
    /** return geometry representing agent location */
    public MasonGeometry getGeometry() {
        return location;
    }
    
}

