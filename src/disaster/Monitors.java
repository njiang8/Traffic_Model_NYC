/**
	 *  Disaster ABM in MASON
	 *  @author Annetta Burger & Bill Kennedy
	 *  2018-19
	 *
	 */

	package disaster;

	import java.util.ArrayList;
	import java.util.HashMap;
	
	
	//===========================
	/** 
	 * Monitors Class to manage model output variables
	 */
	//===========================

	public class Monitors {

		// routine behavior agent monitors
	    //public static ArrayList<Agent> agentList = new ArrayList<Agent>();
	    public static ArrayList<Indv> indvList = new ArrayList<Indv>();
	    //public static ArrayList<Group> grpList = new ArrayList<Group>();

		public static int badagent;  // used to count number of bad agents created at data input
		public static int badhomeNode; // used to test data at initialization
		public static int agentpopulation;  // initially set in WorldBuilder
		public static int grouppopulation;  // initially set in WorldBuilder
		public static int stayAtHome; // counts the number of individual agents stay-at-home
	    public static int firstResponders;	// count of all responders  initially set in WorldBuilder
		public static int toSchoolDaycare; // agents who go to school or daycare
	    public static int atHomeCount; // agents at home at any point in time
	    public static int atWorkCount; // agents at work at any point in time
	    public static int onCommuteCount; // agents commuting
	    public static double avg_tCommute; // average agent commute time
	    public static double avg_dCommute; // average agent commute distance
	    
	    
	    // post detonation monitors
	    public static int indvDeaths; // number of agents killed, initialized to 0 at agent Start
	    public static int inZoneFirstResponders;  // initial exposed first inZoneFirstResponders
	    public static int agentsSheltering; // agents sheltering post impact
	    // IDPs are identified when the agents try to go to a work or home location with null values
	    public static int IDPhome;  // Internally Displaced Person (IDP) without a home
	    public static int IDPwork;  // Internally Displaced Person (IDP) without a workplace -- i.e. work from home designation

	    public static int firstRespZone1;	// first responders in zone 1 (dead)
	    public static int firstRespZone2;	// first responders in zone 2 (injured)
	    public static int firstRespZone3;	// first responders in zone 3 (healthy)
	    public static int firstRespZone4;	// first responders in zone 4 (outside damage area)

	    public static int affectedFleeing;  // 
	    public static int affectedInCare;  // agents getting first aid, rescue, or long-term victimeInCare
	    public static int affectedInMorgue;  // reported dead or dies after some victimeInCare
	    public static int affectedReleased;  // treated (long or short term) and released to go atHomeCount
	    public static int affectedHeadedHome;  // victims that didn't get care and left w/o victimeInCare

	    public static int agentsBlocked;  // agents at water's edge or entered damage area and stopped
	    public static int inTreatment;  // agents being treated
	    public static int agentsTreated;  // agents haven been agentsTreated
	    public static int popZone2;
	    public static int popZone3;
	    public static int[] healthCat = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0};

	    public static int bad09 = 0;  // bad agentsBlocked
	    
	    // building agent monitors
	    public static ArrayList<Building> bldgList = new ArrayList<Building>();
	    public static int bldgcount;  // initially set in WorldBuilder

	    
		//===========================
	    /**
	     * Method to initialize model monitors
	     */
		//===========================
	    
		public void initialize_monitors() {

		// Clear/set environmental monitors
		indvList.clear();
		//grpList.clear();
		bldgcount = 0;
		bldgList.clear();
			
		// agent stats -- pre-event
		badagent = 0;  // used to count number of bad agents created at data input
		badhomeNode = 0;  // used to test data at initialization
		agentpopulation = 0;
		agentpopulation = 0;  // initially set in WorldBuilder
		grouppopulation = 0;  // initially set in WorldBuilder
	    firstResponders = 0;  // count of all responders  initially set in WorldBuilder
		stayAtHome = 0;  // stay-at-home agents
		atHomeCount = 0; // agents at home at any point in time
		atWorkCount = 0; // agents at work at any point in time
		toSchoolDaycare = 0;  // agents who go to school or daycare
		onCommuteCount = 0; // agents commuting
		avg_tCommute = 0; // average agent commute time
		avg_dCommute = 0; // average agent commute distance
		
		// agent stats -- post-event
	    indvDeaths = 0; // number of agents killed, initialized to 0 at agent Start
	    agentsSheltering = 0; // agents sheltering post impact
	    popZone2 = 0;
	    popZone3 = 0;
	    IDPhome = 0;  // Internally Displaced Person (IDP) without a home
	    IDPwork = 0;  // Internally Displaced Person (IDP) without a workplace -- i.e. work from home designation
	    
	    agentsBlocked = 0;  // agents at water's edge or entered damage area and stopped
	    inTreatment = 0;  // agents being treated
	    agentsTreated = 0;  // agents haven been agentsTreated
	    
	    affectedFleeing = 0;
	    affectedInCare = 0;  // agents getting first aid, rescue, or long-term victimeInCare
	    affectedInMorgue = 0;  // reported dead or dies after some victimeInCare
	    affectedReleased = 0;  // treated (long or short term) and released to go atHomeCount
	    affectedHeadedHome = 0;  // victims that didn't get care and left w/o victimeInCare

	    inZoneFirstResponders = 0;  // initial exposed first inZoneFirstResponders
	    firstRespZone1 = 0;	// first responders in zone 1 (dead)
	    firstRespZone2 = 0;	// first responders in zone 2 (injured)
	    firstRespZone3 = 0;	// first responders in zone 3 (healthy)
	    firstRespZone4 = 0;	// first responders in zone 4 (outside damage area)
	    
	}
}
