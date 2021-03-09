/** 
 *  Disaster ABM in MASON
 *  @author Annetta Burger and Bill Kennedy
 *  2018-19
 *  
 */

package disaster;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;


//=========================
/** 
 * Parameters Class to manage model inputs and testing parameters
 */
//=========================

// Holds model simulation parameters
public class Parameters 
{	
	
	private static final Boolean True = null;
	//================================
	/**
	 * Simulation Testing Parameters
	 */
	//================================
	//Scaling experiments
	public static Boolean Scale = true;  
	public static int num_represented = 1000;

	//===================================
	/**
	 * Simulation Input Data pulled into WorldBuilder Class
	 */
	//===================================
	
	//***********
	//*Richard Test New Population (August 2019)
	//***********
	
	public static String popIDhashmapTestpath = "data/popIDhmTest.txt";
	//1 Commute Road Network
//	public static String roadsShape = "data/Richard_Test/Clean_Com_Road_NW_Richard.shp";
	//2 Commute Census Tracts
	public static String censusShape = "data/censusNYcommute.shp";
	//3 Water
	public static String waterShape = "data/NYCWater.shp";
	//4 Outer work places point
	public static String outerwrkfile = "data/Richard_Test/Rich_wrkid_out_May.csv";
//	//5 Work Places and RID Full Study Area
//	public static String workfile = "data/Richard_Test/Rich_wrk_rid_May.csv";
	//6 Schools and RID Full Study Area
	public static String schoolfile = "data/Richard_Test/Rich_Edu_school_rid_may.csv";
	//7 DayCare and RID Full Study Area
	public static String daycarefile = "data/Richard_Test/Rich_Edu_daycare_rid_May.csv";
//	//8 Sample Population Size:2300 in Commute region Richard August Data
//	public static String popfile = "data/Richard_Test/Rich_sample_2300_May.csv";
//	
//	//9 Commuter Road ID for verification of the Road Network
//	public static String rdIDfile = "data/Richard_Test/Clean_Rich_commute_rid.csv";
	
	//==========Validation
//	//x.1 validation dummy road
	static String roadsShape = "data/Richard_Test/Vali_Traffic/Test_Long_Road_Richard_1.shp";
	//x.2  Validation dummy population
	public static String popfile = "data/Richard_Test/Vali_Traffic/Test_Agent_1.csv";
	//x.3 validation dummy WorkID
	public static String workfile = "data/Richard_Test/Vali_Traffic/Test_work_10.csv";
	//dummy validation road ID
	public static String rdIDfile = "data/Richard_Test/Vali_Traffic/Test_Long_road_Richard_1_ID.csv";
	//==========
	
	//===================================
	/**
	 * World Parameters
	 */
	//===================================
	
	// Trace & data storage
	// viz data for Pat's visualization
	public static boolean exportVizData = true;	// to record or not
	public static String exportVizDataFilename = ("data/TrafficVali_scale_1000.csv");
	public static int startVizData = 5*60; // 5*60 = 5AM
	public static int stopVizData = 18*60; // 18*60;  // 18*60 = 6PM
	
	// collect agent loc data in trace  for plots of multiple runs	
	public static boolean saveAgentLocations= false;	// write data to file  NOTE: FILE NAME a constant in World.jave
	public static int 	  saveLocationsStart= 300;		// when to start saving agent locations data
	public static int     saveLocationsStop = 660;		// one hour after boom

	
	//===================================
	/**
	 * Agent Parameters
	 */
	//===================================
		
	// Work hours 
	// shift assignments
	public static Boolean 	shiftWork 	= false;	// yes/no to calc distribution of shifts
	public static Double 	swingShift 	= 0.15;	// 15% work swing shift
	public static Double 	nightShift 	= 0.05;	//  5% work night shift
	public static Double 	dayShift 	= 1 - swingShift - nightShift;	// 80% work day shift
	// shift work: day shift presumed to be 8-5, swing 4-midnight, night midnight to 8am
	public static int 		dayStart	=  480 - 30;	// m 480 = 8am
	public static int 		dayEnd		= 1020 - 30;	// m1020 = 5pm
	public static int 		swingStart	= 1020 - 30;	// m1020 = 5pm
	public static int 		swingEnd	= 1440 - 30;	// m1440 = midnight
	public static int 		nightStart	= 1440 - 30;	// m1440 = midnight
	public static int 		nightEnd	=  480 - 30;	// m 480 = 8pm
	
	// Commuting
	public static Boolean randomizeCommute = true;	// yes/no to calc commuter variation
	public static int commuteDuration = 90;	// duration in minutes of randomized commuting time
	
	// Trace
	public static boolean traceDecisions 	= false;	// agent decisions	NOTE: FILE NAME a constant in World.java
	
	// Social Behavior Parameters
	// indicates whether agents form groups -- either through the carPool method or individual nonroutine
	//public static Boolean Grouping = true;  
	//public static int maxGrpSize = 8;  // group size limits

	// Post-Impact
	// after impact, probability (in %) agents will shelter when at home or work
	public static int chanceShelteratHome = 100;
	public static int chanceShelteratWork = 100;
	
	//===================================
	/**
	 * Effects Parameters
	 */
	//===================================
	
	
	
}

