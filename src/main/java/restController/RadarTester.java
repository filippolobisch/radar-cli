package restController;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;

import org.eclipse.emf.henshin.interpreter.EGraph;
import org.eclipse.emf.henshin.interpreter.Engine;
import org.eclipse.emf.henshin.interpreter.UnitApplication;
import org.eclipse.emf.henshin.interpreter.impl.EGraphImpl;
import org.eclipse.emf.henshin.interpreter.impl.EngineImpl;
import org.eclipse.emf.henshin.interpreter.impl.UnitApplicationImpl;
import org.eclipse.emf.henshin.model.Module;
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.opencsv.CSVWriter;

import restassured.riskpattern.CloudModel.CloudEnvironment;
import riskpatternfinder.AdaptationFinderToMitigateRisks;
import riskpatternfinder.AdaptationFinderToMitigateRisks.AdaptationAlgorithm;
import riskpatternfinder.AdaptationFinderToMitigateRisks.RandomizedAdaptationRule;
import runtime.EMFModelLoad;
import runtime.RiskFinder;
import runtime.RuntimeModelLogic;
import utility.Constants;

import helpers.*;

@RestController
public class RadarTester {

	@Autowired
	RuntimeModelLogic logic;
	@Autowired
	EMFModelLoad loader;
	@Autowired
	RuntimeModelLogic runtimeModelLogic;
	@Autowired
	runtime.Gateway gateway;
	@Autowired
	private RiskFinder riskFinder;
	private Engine engine = new EngineImpl();

	public final String filepathToCSV = new File(System.getProperty("user.dir")).getAbsolutePath() + File.separator
			+ "performanceTest" + File.separator + "output.csv";
	public final String filepathToCSVAverage = new File(System.getProperty("user.dir")).getAbsolutePath()
			+ File.separator + "performanceTest" + File.separator + "outputAverage.csv";
	public final String filepathToDiagramFormat = new File(System.getProperty("user.dir")).getAbsolutePath()
			+ File.separator + "performanceTest" + File.separator + "diagramFormat.csv";
	public final String filepathToNewTest = new File(System.getProperty("user.dir")).getAbsolutePath() + File.separator
			+ "performanceTest" + File.separator + "newTest.csv";

	// Which model do you want to use?
	static final String fileName = "CloudExample.cloudmodel";

	// Do you want to measure time?
	public boolean performanceTest = true;
	public LinkedList<Long> performanceTimeCollection = new LinkedList<Long>();
	private ArrayList<RandomizedAdaptationRule> randomizedAdaptationRules;
	boolean oldEnlargementMethod;

	private void injectPCPInstances(String henshinRuleName) {
		HenshinResourceSet resourceSet = new HenshinResourceSet(Constants.ADAPTATIONS_PATH);
		Module module = resourceSet.getModule("PCPInjection.henshin", false);
		EGraph graph = new EGraphImpl(
				new EGraphImpl(runtimeModelLogic.getLoader().getCloudEnvironmentsMonitored().get(1l)));
		UnitApplication app = new UnitApplicationImpl(engine);
		app.setEGraph(graph);
		app.setUnit(module.getUnit(henshinRuleName));
		try {
			if (!app.execute(null)) {
				System.err.println("Failed to execute rule: " + henshinRuleName);
			} else {
				System.out.println("Executed rule: " + henshinRuleName);
			}
		} catch (NullPointerException e) {
			System.err.println("Failure NullPointer");
		}
		CloudEnvironment newModel = (CloudEnvironment) graph.getRoots().get(0);
		runtimeModelLogic.getLoader().setCloudEnvironmentsMonitored(newModel, 1L);
	}

	@RequestMapping(path = "/performanceTest", method = RequestMethod.GET)
	public String performanceTests() {
		// Which enlargement method do you want to use?
		oldEnlargementMethod = false;
		// After how many seconds the algorithm should stop searching for a better
		// solution?
		int[] maxSecondsArray = { 10 };
		for (int maxSeconds : maxSecondsArray) {
			// adding header to csv
			String[] header = { "Timestamp", "Nr. of Nodes", "Amount of PCP instances", "Functionality", "Costs" };
			writeDataLineByLine(insertNumberBeforeFileEnding(filepathToNewTest, maxSeconds), header);

			AdaptationAlgorithm algorithm = AdaptationAlgorithm.BestFirstSearch;

			randomizedAdaptationRules = new ArrayList<AdaptationFinderToMitigateRisks.RandomizedAdaptationRule>();

			long id = 0;
			// load model from JSON String to EMF
			CloudEnvironment cloudEnvironment = null;
			try {
				String modelObject = new JsonConverter().getJSONInStringFormat();
				cloudEnvironment = (CloudEnvironment) loader.loadEObjectFromString(modelObject);
			} catch (IOException e1) {
				// Auto-generated catch block
				e1.printStackTrace();
			}
			id = loader.setCloudEnvironmentsMonitored(cloudEnvironment, 1);
			long timestampZERO = System.nanoTime();
			cloudEnvironment = (CloudEnvironment) loader.getCloudEnvironmentsMonitored().get(1L);
			LinkedList<String[]> datas = riskFinder.startRadar(id, algorithm, String.valueOf(337), 0,
					maxSeconds, cloudEnvironment, timestampZERO);
			for (String[] data : datas) {
				writeDataLineByLine(insertNumberBeforeFileEnding(filepathToNewTest, maxSeconds), data);
			}

			try {
				Thread.sleep(20000);
			} catch (InterruptedException e1) {
				// Auto-generated catch block
				e1.printStackTrace();
			}
			long timestamp1 = System.nanoTime();
			String[] data1 = { String.valueOf((double) (timestamp1 - timestampZERO) / 1_000_000_000.0), "337", "0",
					"48", "36.1872" };
			writeDataLineByLine(insertNumberBeforeFileEnding(filepathToNewTest, maxSeconds), data1);

			int amountOfPCPInstanceInjections = 0;
			// weniger events, damit mehr Ruhepausen sind
			for (int i = 1; i <= 5; i++) {
				cloudEnvironment = (CloudEnvironment) loader.getCloudEnvironmentsMonitored().get(1L);
				double probability = Math.random();
				if (probability >= 0.80) {
					amountOfPCPInstanceInjections = ThreadLocalRandom.current().nextInt(15, 21);
				} else {
					amountOfPCPInstanceInjections = ThreadLocalRandom.current().nextInt(1, 6);
				}

				// Inject random PCP
				int pcpRuleIndex = PCPTypes.STOREDATAOUTSIDEEU;
				String[] rulesToInject = new PCPChooser().getRulesToInject(pcpRuleIndex);

				for (int j = 0; j <= amountOfPCPInstanceInjections; j++) {
					// Bei wenigen PCP Instanzen erklären, dass es dann sehr schnell geht
					int ruleToInject = ThreadLocalRandom.current().nextInt(0, 2);
					injectPCPInstances(rulesToInject[ruleToInject]);
				}
				datas = riskFinder.startRadar(id, algorithm, String.valueOf(337), i,
						maxSeconds, cloudEnvironment, timestampZERO);
				for (String[] data : datas) {
					writeDataLineByLine(insertNumberBeforeFileEnding(filepathToNewTest, maxSeconds), data);
				}
				try {
					if (!datas.getLast()[2].equals("0")) {
						Thread.sleep(5000);
						cloudEnvironment = (CloudEnvironment) loader.getCloudEnvironmentsMonitored().get(1L);
						datas = riskFinder.startRadar(id, algorithm, String.valueOf(337), i,
								maxSeconds, cloudEnvironment, timestampZERO);
						for (String[] data : datas) {
							writeDataLineByLine(insertNumberBeforeFileEnding(filepathToNewTest, maxSeconds), data);
						}
					}
					// Nur schlafen, wenn keine PCP Instanzen vorhanden sind!
					int timeToSleep = ThreadLocalRandom.current().nextInt(120000, 180000 + 1);
					Thread.sleep(timeToSleep);
					long timestamp = System.nanoTime();
					String[] data = { String.valueOf((double) (timestamp - timestampZERO) / 1_000_000_000.0),
							datas.getLast()[1], datas.getLast()[2], datas.getLast()[3], datas.getLast()[4] };
					writeDataLineByLine(insertNumberBeforeFileEnding(filepathToNewTest, maxSeconds), data);
				} catch (InterruptedException e) {
					// Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		System.out.println("Finished Experiment");
		return "Done";

	}

	public double calculatePCPInstancesLeft(String string) {
		String[] values = string.split("[|]");
		return values.length - 1;
	}

	@RequestMapping(path = "/loadInitialInstance", method = RequestMethod.GET)
	public String index() {
		// Insert EMF Model String here
		String model = runtimeModelLogic.returnentiremodelasjsonFromFile(fileName);
		long id = 0;
		// load model from JSON String to EMF
		CloudEnvironment cloudEnvironment = null;
		try {
			cloudEnvironment = (CloudEnvironment) loader.loadEObjectFromString(model);
			id = loader.setCloudEnvironmentsMonitored(cloudEnvironment, 0);
			// System.out.println("Loaded model after registration event as:\n" +
			// runtimeModelLogic.returnentiremodelasjson(cloudEnvironment));
			System.out.println("Loaded model after registration event.");
		} catch (IOException e) {
			System.err.println("Can't create EMF instance");
		}
		if (cloudEnvironment != null) {
			riskFinder.startRadar(id, AdaptationAlgorithm.BestFirstSearch, "", 0, 10, cloudEnvironment, 0);
		}

		return "Done";
	}

	@RequestMapping(path = "/restart", method = RequestMethod.GET)
	public String restart() {
		// String model = "{ \"type\":\"CloudEnvironment\", \"@id\":\"/\",
		// \"tosca_nodes_root\":[ { \"type\":\"Compute\", \"@id\":\"1\",
		// \"name\":\"DBServer_1\", \"id\":1, \"hosts\":[ { \"type\":\"DBMS\",
		// \"referencedObjectID\":\"8\" }, { \"type\":\"SoftwareComponent\",
		// \"referencedObjectID\":\"35\" } ], \"connectedToNetwork\":[ {
		// \"type\":\"Network_Network\", \"referencedObjectID\":\"2\" } ],
		// \"jurisdiction\":\"DE\" }, { \"type\":\"Network_Network\", \"@id\":\"2\",
		// \"name\":\"DataLAN\", \"id\":2, \"connectedToCompute\":[ {
		// \"type\":\"Compute\", \"referencedObjectID\":\"1\" }, { \"type\":\"Compute\",
		// \"referencedObjectID\":\"17\" }, { \"type\":\"Compute\",
		// \"referencedObjectID\":\"32\" } ], \"connectedTo\":{
		// \"type\":\"Network_Network\", \"referencedObjectID\":\"4\" } }, {
		// \"type\":\"Compute\", \"@id\":\"3\", \"name\":\"QueryServer\", \"id\":3,
		// \"hosts\":[ { \"type\":\"Gateway\", \"referencedObjectID\":\"16\" } ],
		// \"connectedToNetwork\":[ { \"type\":\"Network_Network\",
		// \"referencedObjectID\":\"4\" } ], \"jurisdiction\":\"DE\" }, {
		// \"type\":\"Network_Network\", \"@id\":\"4\", \"name\":\"ServiceLAN\",
		// \"id\":4, \"connectedToCompute\":[ { \"type\":\"Compute\",
		// \"referencedObjectID\":\"3\" }, { \"type\":\"Compute\",
		// \"referencedObjectID\":\"5\" } ], \"connectedToOpposite\":{
		// \"type\":\"Network_Network\", \"referencedObjectID\":\"2\" } }, {
		// \"type\":\"Compute\", \"@id\":\"5\", \"name\":\"KGPortalServer\", \"id\":5,
		// \"hosts\":[ { \"type\":\"WebServer\", \"referencedObjectID\":\"6\" } ],
		// \"connectedToNetwork\":[ { \"type\":\"Network_Network\",
		// \"referencedObjectID\":\"4\" } ], \"jurisdiction\":\"DE\" }, {
		// \"type\":\"WebServer\", \"@id\":\"6\", \"name\":\"Webserver-KGPortalServer\",
		// \"id\":6, \"hostedOn\":{ \"type\":\"Compute\", \"referencedObjectID\":\"5\"
		// }, \"hosts\":[ { \"type\":\"WebApplication\", \"referencedObjectID\":\"7\" }
		// ] }, { \"type\":\"WebApplication\", \"@id\":\"7\", \"name\":\"KnowGoPortal\",
		// \"id\":7, \"hostedOn\":{ \"type\":\"WebServer\", \"referencedObjectID\":\"6\"
		// }, \"usedBySoftwareComponent\":[ { \"type\":\"SoftwareComponent\",
		// \"referencedObjectID\":\"14\" } ], \"usesGateway\":[ { \"type\":\"Gateway\",
		// \"referencedObjectID\":\"16\" } ], \"usesSoftwareComponent\":[ {
		// \"type\":\"SoftwareComponent\", \"referencedObjectID\":\"35\" }, {
		// \"type\":\"SoftwareComponent\", \"referencedObjectID\":\"36\" } ] }, {
		// \"type\":\"DBMS\", \"@id\":\"8\", \"name\":\"DBMS-DBServer_1\", \"id\":8,
		// \"hostedOn\":{ \"type\":\"Compute\", \"referencedObjectID\":\"1\" },
		// \"hosts\":[ { \"type\":\"Database\", \"referencedObjectID\":\"9\" } ],
		// \"uses\":[ { \"type\":\"Gateway\", \"referencedObjectID\":\"16\" } ] }, {
		// \"type\":\"Database\", \"@id\":\"9\", \"name\":\"KnowGoDB_1\", \"id\":9,
		// \"hostedOn\":{ \"type\":\"DBMS\", \"referencedObjectID\":\"8\" },
		// \"stores\":[ { \"type\":\"StoredDataSet\", \"referencedObjectID\":\"11\" }, {
		// \"type\":\"StoredDataSet\", \"referencedObjectID\":\"21\" } ],
		// \"streamsFrom\":[ { \"type\":\"DataFlow\", \"referencedObjectID\":\"23\" }, {
		// \"type\":\"DataFlow\", \"referencedObjectID\":\"22\" } ] }, {
		// \"type\":\"DataSubject\", \"@id\":\"10\", \"name\":\"Driver\", \"id\":10,
		// \"location\":\"DE\", \"owns\":[ { \"type\":\"StoredDataSet\",
		// \"referencedObjectID\":\"11\" }, { \"type\":\"DataFlow\",
		// \"referencedObjectID\":\"22\" }, { \"type\":\"DataFlow\",
		// \"referencedObjectID\":\"23\" }, { \"type\":\"StoredDataSet\",
		// \"referencedObjectID\":\"21\" }, { \"type\":\"StoredDataSet\",
		// \"referencedObjectID\":\"33\" }, { \"type\":\"StoredDataSet\",
		// \"referencedObjectID\":\"24\" } ], \"manages\":[ { \"type\":\"MobileHost\",
		// \"referencedObjectID\":\"26\" }, { \"type\":\"MobileHost\",
		// \"referencedObjectID\":\"27\" } ] }, { \"type\":\"StoredDataSet\",
		// \"@id\":\"11\", \"name\":\"DataSet-KnowGoDB-BiometricData\", \"id\":11,
		// \"consistsOf\":[ { \"type\":\"Record\", \"referencedObjectID\":\"12\" } ],
		// \"belongsTo\":{ \"type\":\"DataSubject\", \"referencedObjectID\":\"10\" },
		// \"storedIn\":{ \"type\":\"Database\", \"referencedObjectID\":\"9\" } }, {
		// \"type\":\"Record\", \"@id\":\"12\", \"name\":\"BiometricData\", \"id\":12,
		// \"partOf\":[ { \"type\":\"StoredDataSet\", \"referencedObjectID\":\"11\" }, {
		// \"type\":\"DataFlow\", \"referencedObjectID\":\"22\" } ], \"sensitive\":true
		// }, { \"type\":\"Record\", \"@id\":\"13\", \"name\":\"CarData\", \"id\":13,
		// \"partOf\":[ { \"type\":\"StoredDataSet\", \"referencedObjectID\":\"21\" }, {
		// \"type\":\"DataFlow\", \"referencedObjectID\":\"23\" } ] }, {
		// \"type\":\"SoftwareComponent\", \"@id\":\"14\", \"name\":\"AccessBrowser\",
		// \"id\":14, \"hostedOn\":{ \"type\":\"Compute\", \"referencedObjectID\":\"15\"
		// }, \"controlledBy\":[ { \"type\":\"DataController\",
		// \"referencedObjectID\":\"20\" } ], \"usesWebApplication\":[ {
		// \"type\":\"WebApplication\", \"referencedObjectID\":\"7\" } ] }, {
		// \"type\":\"Compute\", \"@id\":\"15\", \"name\":\"AccessPC\", \"id\":15,
		// \"hosts\":[ { \"type\":\"SoftwareComponent\", \"referencedObjectID\":\"14\" }
		// ], \"jurisdiction\":\"CH\" }, { \"type\":\"Gateway\", \"@id\":\"16\",
		// \"name\":\"QueryGW\", \"id\":16, \"hostedOn\":{ \"type\":\"Compute\",
		// \"referencedObjectID\":\"3\" }, \"usedByWebApplication\":[ {
		// \"type\":\"WebApplication\", \"referencedObjectID\":\"7\" } ], \"usedBy\":[ {
		// \"type\":\"DBMS\", \"referencedObjectID\":\"8\" }, { \"type\":\"DBMS\",
		// \"referencedObjectID\":\"18\" } ], \"usedByGateway\":[ {
		// \"type\":\"Gateway\", \"referencedObjectID\":\"31\" } ] }, {
		// \"type\":\"Compute\", \"@id\":\"17\", \"name\":\"DBServer_2\", \"id\":17,
		// \"hosts\":[ { \"type\":\"DBMS\", \"referencedObjectID\":\"18\" }, {
		// \"type\":\"SoftwareComponent\", \"referencedObjectID\":\"36\" } ],
		// \"connectedToNetwork\":[ { \"type\":\"Network_Network\",
		// \"referencedObjectID\":\"2\" } ], \"jurisdiction\":\"AT\" }, {
		// \"type\":\"DBMS\", \"@id\":\"18\", \"name\":\"DBMS-DBServer_2\", \"id\":18,
		// \"hostedOn\":{ \"type\":\"Compute\", \"referencedObjectID\":\"17\" },
		// \"hosts\":[ { \"type\":\"Database\", \"referencedObjectID\":\"19\" } ],
		// \"uses\":[ { \"type\":\"Gateway\", \"referencedObjectID\":\"16\" } ] }, {
		// \"type\":\"Database\", \"@id\":\"19\", \"name\":\"KnowGoDB_2\", \"id\":19,
		// \"hostedOn\":{ \"type\":\"DBMS\", \"referencedObjectID\":\"18\" } }, {
		// \"type\":\"DataController\", \"@id\":\"20\", \"name\":\"AccessingParty\",
		// \"id\":20, \"location\":\"CH\", \"controls\":[ {
		// \"type\":\"SoftwareComponent\", \"referencedObjectID\":\"14\" } ] }, {
		// \"type\":\"StoredDataSet\", \"@id\":\"21\",
		// \"name\":\"DataSet-KnowGoDB-CarData\", \"id\":21, \"consistsOf\":[ {
		// \"type\":\"Record\", \"referencedObjectID\":\"13\" } ], \"belongsTo\":{
		// \"type\":\"DataSubject\", \"referencedObjectID\":\"10\" }, \"storedIn\":{
		// \"type\":\"Database\", \"referencedObjectID\":\"9\" } }, {
		// \"type\":\"DataFlow\", \"@id\":\"22\",
		// \"name\":\"DataFlow-KnowGoDB-BiometricData\", \"id\":22, \"consistsOf\":[ {
		// \"type\":\"Record\", \"referencedObjectID\":\"12\" } ], \"belongsTo\":{
		// \"type\":\"DataSubject\", \"referencedObjectID\":\"10\" }, \"streamsTo\":[ {
		// \"type\":\"Database\", \"referencedObjectID\":\"9\" } ] }, {
		// \"type\":\"DataFlow\", \"@id\":\"23\",
		// \"name\":\"DataFlow-KnowGoDB-CarData\", \"id\":23, \"consistsOf\":[ {
		// \"type\":\"Record\", \"referencedObjectID\":\"13\" } ], \"belongsTo\":{
		// \"type\":\"DataSubject\", \"referencedObjectID\":\"10\" }, \"streamsTo\":[ {
		// \"type\":\"Database\", \"referencedObjectID\":\"9\" } ] }, {
		// \"type\":\"StoredDataSet\", \"@id\":\"24\",
		// \"name\":\"DataSet-KnowGoDB-SensorData\", \"id\":24, \"consistsOf\":[ {
		// \"type\":\"Record\", \"referencedObjectID\":\"25\" } ], \"belongsTo\":{
		// \"type\":\"DataSubject\", \"referencedObjectID\":\"10\" },
		// \"storedInMobileHost\":{ \"type\":\"MobileHost\",
		// \"referencedObjectID\":\"26\" }, \"createdBy\":{ \"type\":\"MobileHost\",
		// \"referencedObjectID\":\"27\" } }, { \"type\":\"Record\", \"@id\":\"25\",
		// \"name\":\"SensorData\", \"id\":25, \"partOf\":[ {
		// \"type\":\"StoredDataSet\", \"referencedObjectID\":\"24\" } ],
		// \"sensitive\":true }, { \"type\":\"MobileHost\", \"@id\":\"26\",
		// \"name\":\"DriverPhone\", \"id\":26, \"jurisdiction\":\"DE\", \"managedBy\":{
		// \"type\":\"DataSubject\", \"referencedObjectID\":\"10\" },
		// \"hostsContainerApplication\":[ { \"type\":\"Container_Application\",
		// \"referencedObjectID\":\"28\" } ], \"stores\":{ \"type\":\"StoredDataSet\",
		// \"referencedObjectID\":\"24\" } }, { \"type\":\"MobileHost\", \"@id\":\"27\",
		// \"name\":\"Car\", \"id\":27, \"jurisdiction\":\"DE\", \"managedBy\":{
		// \"type\":\"DataSubject\", \"referencedObjectID\":\"10\" },
		// \"hostsContainerApplication\":[ { \"type\":\"Container_Application\",
		// \"referencedObjectID\":\"29\" } ], \"creates\":[ {
		// \"type\":\"StoredDataSet\", \"referencedObjectID\":\"24\" } ] }, {
		// \"type\":\"Container_Application\", \"@id\":\"28\", \"name\":\"KnowGoApp\",
		// \"id\":28, \"hostedBy\":{ \"type\":\"MobileHost\",
		// \"referencedObjectID\":\"26\" }, \"sendsThrough\":{ \"type\":\"DataFlow\",
		// \"referencedObjectID\":\"30\" }, \"usedBy\":[ {
		// \"type\":\"Container_Application\", \"referencedObjectID\":\"29\" } ] }, {
		// \"type\":\"Container_Application\", \"@id\":\"29\",
		// \"name\":\"CarDataMarshall\", \"id\":29, \"hostedBy\":{
		// \"type\":\"MobileHost\", \"referencedObjectID\":\"27\" }, \"uses\":[ {
		// \"type\":\"Container_Application\", \"referencedObjectID\":\"28\" } ] }, {
		// \"type\":\"DataFlow\", \"@id\":\"30\",
		// \"name\":\"DataFlow-KnowGoApp-DataGW\", \"id\":30, \"sendsFrom\":{
		// \"type\":\"Container_Application\", \"referencedObjectID\":\"28\" },
		// \"sendsTo\":{ \"type\":\"Gateway\", \"referencedObjectID\":\"31\" } }, {
		// \"type\":\"Gateway\", \"@id\":\"31\", \"name\":\"DataGW\", \"id\":31,
		// \"hostedOn\":{ \"type\":\"Compute\", \"referencedObjectID\":\"32\" },
		// \"receivesFrom\":[ { \"type\":\"DataFlow\", \"referencedObjectID\":\"30\" }
		// ], \"usesGateway\":[ { \"type\":\"Gateway\", \"referencedObjectID\":\"16\" }
		// ] }, { \"type\":\"Compute\", \"@id\":\"32\", \"name\":\"DataGWServer\",
		// \"id\":32, \"hosts\":[ { \"type\":\"Gateway\", \"referencedObjectID\":\"31\"
		// } ], \"connectedToNetwork\":[ { \"type\":\"Network_Network\",
		// \"referencedObjectID\":\"2\" } ], \"jurisdiction\":\"DE\" }, {
		// \"type\":\"StoredDataSet\", \"@id\":\"33\",
		// \"name\":\"DataSet-KGAnalytics-RiskData\", \"id\":33, \"consistsOf\":[ {
		// \"type\":\"Record\", \"referencedObjectID\":\"34\" } ], \"belongsTo\":{
		// \"type\":\"DataSubject\", \"referencedObjectID\":\"10\" },
		// \"createdBySoftwareComponent\":{ \"type\":\"SoftwareComponent\",
		// \"referencedObjectID\":\"35\" } }, { \"type\":\"Record\", \"@id\":\"34\",
		// \"name\":\"RiskData\", \"id\":34, \"partOf\":[ { \"type\":\"StoredDataSet\",
		// \"referencedObjectID\":\"33\" } ] }, { \"type\":\"SoftwareComponent\",
		// \"@id\":\"36\", \"name\":\"KGAnalytics_2\", \"id\":36, \"hostedOn\":{
		// \"type\":\"Compute\", \"referencedObjectID\":\"17\" }, \"usedByWebApp\":[ {
		// \"type\":\"WebApplication\", \"referencedObjectID\":\"7\" } ] }, {
		// \"type\":\"SoftwareComponent\", \"@id\":\"35\", \"name\":\"KGAnalytics_1\",
		// \"id\":35, \"hostedOn\":{ \"type\":\"Compute\", \"referencedObjectID\":\"1\"
		// }, \"creates\":[ { \"type\":\"StoredDataSet\", \"referencedObjectID\":\"33\"
		// } ], \"usedByWebApp\":[ { \"type\":\"WebApplication\",
		// \"referencedObjectID\":\"7\" } ] } ] }";
		String model = "{ \"type\" : \"CloudEnvironment\", \"@id\" : \"/\", \"tosca_nodes_root\" : [ { \"type\" : \"Compute\", \"@id\" : \"1\", \"name\" : \"DBServer_1\", \"id\" : 1, \"hosts\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"8\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"35\" } ], \"connectedToNetwork\" : [ { \"type\" : \"Network_Network\", \"referencedObjectID\" : \"2\" } ], \"jurisdiction\" : \"DE\" }, { \"type\" : \"Network_Network\", \"@id\" : \"2\", \"name\" : \"DataLAN\", \"id\" : 2, \"connectedToCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"1\" }, { \"type\" : \"Compute\", \"referencedObjectID\" : \"17\" }, { \"type\" : \"Compute\", \"referencedObjectID\" : \"32\" } ], \"connectedTo\" : { \"type\" : \"Network_Network\", \"referencedObjectID\" : \"4\" } }, { \"type\" : \"Compute\", \"@id\" : \"3\", \"name\" : \"QueryServer\", \"id\" : 3, \"hosts\" : [ { \"type\" : \"Gateway\", \"referencedObjectID\" : \"16\" } ], \"connectedToNetwork\" : [ { \"type\" : \"Network_Network\", \"referencedObjectID\" : \"4\" } ], \"jurisdiction\" : \"DE\" }, { \"type\" : \"Network_Network\", \"@id\" : \"4\", \"name\" : \"ServiceLAN\", \"id\" : 4, \"connectedToCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"3\" }, { \"type\" : \"Compute\", \"referencedObjectID\" : \"5\" } ], \"connectedToOpposite\" : { \"type\" : \"Network_Network\", \"referencedObjectID\" : \"2\" } }, { \"type\" : \"Compute\", \"@id\" : \"5\", \"name\" : \"KGPortalServer\", \"id\" : 5, \"hosts\" : [ { \"type\" : \"WebServer\", \"referencedObjectID\" : \"6\" } ], \"connectedToNetwork\" : [ { \"type\" : \"Network_Network\", \"referencedObjectID\" : \"4\" } ], \"jurisdiction\" : \"DE\" }, { \"type\" : \"WebServer\", \"@id\" : \"6\", \"name\" : \"Webserver-KGPortalServer\", \"id\" : 6, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"5\" }, \"hosts\" : [ { \"type\" : \"WebApplication\", \"referencedObjectID\" : \"7\" } ] }, { \"type\" : \"WebApplication\", \"@id\" : \"7\", \"name\" : \"KnowGoPortal\", \"id\" : 7, \"hostedOn\" : { \"type\" : \"WebServer\", \"referencedObjectID\" : \"6\" }, \"usedBySoftwareComponent\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"14\" } ], \"usesGateway\" : [ { \"type\" : \"Gateway\", \"referencedObjectID\" : \"16\" } ], \"usesSoftwareComponent\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"35\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"36\" } ] }, { \"type\" : \"DBMS\", \"@id\" : \"8\", \"name\" : \"DBMS-DBServer_1\", \"id\" : 8, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"1\" }, \"hosts\" : [ { \"type\" : \"Database\", \"referencedObjectID\" : \"9\" } ], \"uses\" : [ { \"type\" : \"Gateway\", \"referencedObjectID\" : \"16\" } ] }, { \"type\" : \"Database\", \"@id\" : \"9\", \"name\" : \"KnowGoDB_1\", \"id\" : 9, \"hostedOn\" : { \"type\" : \"DBMS\", \"referencedObjectID\" : \"8\" }, \"stores\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"11\" }, { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"21\" } ], \"streamsFrom\" : [ { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"23\" }, { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"22\" } ] }, { \"type\" : \"DataSubject\", \"@id\" : \"10\", \"name\" : \"Driver\", \"id\" : 10, \"location\" : \"DE\", \"owns\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"11\" }, { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"22\" }, { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"23\" }, { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"21\" }, { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"33\" }, { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"24\" } ], \"manages\" : [ { \"type\" : \"MobileHost\", \"referencedObjectID\" : \"26\" }, { \"type\" : \"MobileHost\", \"referencedObjectID\" : \"27\" } ] }, { \"type\" : \"StoredDataSet\", \"@id\" : \"11\", \"name\" : \"DataSet-KnowGoDB-BiometricData_1\", \"id\" : 11, \"consistsOf\" : [ { \"type\" : \"Record\", \"referencedObjectID\" : \"12\" } ], \"belongsTo\" : { \"type\" : \"DataSubject\", \"referencedObjectID\" : \"10\" }, \"storedIn\" : { \"type\" : \"Database\", \"referencedObjectID\" : \"9\" } }, { \"type\" : \"Record\", \"@id\" : \"12\", \"name\" : \"BiometricData_1\", \"id\" : 12, \"partOf\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"11\" }, { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"22\" } ], \"sensitive\" : true }, { \"type\" : \"Record\", \"@id\" : \"13\", \"name\" : \"CarData_1\", \"id\" : 13, \"partOf\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"21\" }, { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"23\" } ] }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"14\", \"name\" : \"AccessBrowser\", \"id\" : 14, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"15\" }, \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"20\" } ], \"usesWebApplication\" : [ { \"type\" : \"WebApplication\", \"referencedObjectID\" : \"7\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"15\", \"name\" : \"AccessPC\", \"id\" : 15, \"hosts\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"14\" } ], \"jurisdiction\" : \"CH\" }, { \"type\" : \"Gateway\", \"@id\" : \"16\", \"name\" : \"QueryGW\", \"id\" : 16, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"3\" }, \"usedByWebApplication\" : [ { \"type\" : \"WebApplication\", \"referencedObjectID\" : \"7\" } ], \"usedBy\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"8\" }, { \"type\" : \"DBMS\", \"referencedObjectID\" : \"18\" } ], \"usedByGateway\" : [ { \"type\" : \"Gateway\", \"referencedObjectID\" : \"31\" } ], \"gatewayUsedBySoftwareComponent\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"36\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"35\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"17\", \"name\" : \"DBServer_2\", \"id\" : 17, \"hosts\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"18\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"36\" } ], \"connectedToNetwork\" : [ { \"type\" : \"Network_Network\", \"referencedObjectID\" : \"2\" } ], \"jurisdiction\" : \"AT\" }, { \"type\" : \"DBMS\", \"@id\" : \"18\", \"name\" : \"DBMS-DBServer_2\", \"id\" : 18, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"17\" }, \"hosts\" : [ { \"type\" : \"Database\", \"referencedObjectID\" : \"19\" } ], \"uses\" : [ { \"type\" : \"Gateway\", \"referencedObjectID\" : \"16\" } ] }, { \"type\" : \"Database\", \"@id\" : \"19\", \"name\" : \"KnowGoDB_2\", \"id\" : 19, \"hostedOn\" : { \"type\" : \"DBMS\", \"referencedObjectID\" : \"18\" } }, { \"type\" : \"DataController\", \"@id\" : \"20\", \"name\" : \"AccessingParty\", \"id\" : 20, \"location\" : \"CH\", \"controls\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"14\" } ] }, { \"type\" : \"StoredDataSet\", \"@id\" : \"21\", \"name\" : \"DataSet-KnowGoDB-CarData_1\", \"id\" : 21, \"consistsOf\" : [ { \"type\" : \"Record\", \"referencedObjectID\" : \"13\" } ], \"belongsTo\" : { \"type\" : \"DataSubject\", \"referencedObjectID\" : \"10\" }, \"storedIn\" : { \"type\" : \"Database\", \"referencedObjectID\" : \"9\" } }, { \"type\" : \"DataFlow\", \"@id\" : \"22\", \"name\" : \"DataFlow-BiometricData-KnowGoApp-KnowGoDB\", \"id\" : 22, \"consistsOf\" : [ { \"type\" : \"Record\", \"referencedObjectID\" : \"12\" } ], \"belongsTo\" : { \"type\" : \"DataSubject\", \"referencedObjectID\" : \"10\" }, \"streamsTo\" : [ { \"type\" : \"Database\", \"referencedObjectID\" : \"9\" } ] }, { \"type\" : \"DataFlow\", \"@id\" : \"23\", \"name\" : \"DataFlow-CarData-KnowGoApp-KnowGoDB\", \"id\" : 23, \"consistsOf\" : [ { \"type\" : \"Record\", \"referencedObjectID\" : \"13\" } ], \"belongsTo\" : { \"type\" : \"DataSubject\", \"referencedObjectID\" : \"10\" }, \"streamsTo\" : [ { \"type\" : \"Database\", \"referencedObjectID\" : \"9\" } ] }, { \"type\" : \"StoredDataSet\", \"@id\" : \"24\", \"name\" : \"DataSet-SensorData-DriverPhone\", \"id\" : 24, \"consistsOf\" : [ { \"type\" : \"Record\", \"referencedObjectID\" : \"25\" } ], \"belongsTo\" : { \"type\" : \"DataSubject\", \"referencedObjectID\" : \"10\" }, \"storedInMobileHost\" : { \"type\" : \"MobileHost\", \"referencedObjectID\" : \"26\" }, \"createdBy\" : { \"type\" : \"MobileHost\", \"referencedObjectID\" : \"27\" } }, { \"type\" : \"Record\", \"@id\" : \"25\", \"name\" : \"SensorData\", \"id\" : 25, \"partOf\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"24\" } ], \"sensitive\" : true }, { \"type\" : \"MobileHost\", \"@id\" : \"26\", \"name\" : \"DriverPhone\", \"id\" : 26, \"jurisdiction\" : \"DE\", \"managedBy\" : { \"type\" : \"DataSubject\", \"referencedObjectID\" : \"10\" }, \"hostsContainerApplication\" : [ { \"type\" : \"Container_Application\", \"referencedObjectID\" : \"28\" } ], \"stores\" : { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"24\" } }, { \"type\" : \"MobileHost\", \"@id\" : \"27\", \"name\" : \"Car\", \"id\" : 27, \"jurisdiction\" : \"DE\", \"managedBy\" : { \"type\" : \"DataSubject\", \"referencedObjectID\" : \"10\" }, \"hostsContainerApplication\" : [ { \"type\" : \"Container_Application\", \"referencedObjectID\" : \"29\" } ], \"creates\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"24\" } ] }, { \"type\" : \"Container_Application\", \"@id\" : \"28\", \"name\" : \"KnowGoApp\", \"id\" : 28, \"hostedBy\" : { \"type\" : \"MobileHost\", \"referencedObjectID\" : \"26\" }, \"sendsThrough\" : { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"30\" }, \"usedBy\" : [ { \"type\" : \"Container_Application\", \"referencedObjectID\" : \"29\" } ] }, { \"type\" : \"Container_Application\", \"@id\" : \"29\", \"name\" : \"CarDataMarshall\", \"id\" : 29, \"hostedBy\" : { \"type\" : \"MobileHost\", \"referencedObjectID\" : \"27\" }, \"uses\" : [ { \"type\" : \"Container_Application\", \"referencedObjectID\" : \"28\" } ] }, { \"type\" : \"DataFlow\", \"@id\" : \"30\", \"name\" : \"DataFlow-KnowGoApp-DataGW\", \"id\" : 30, \"sendsFrom\" : { \"type\" : \"Container_Application\", \"referencedObjectID\" : \"28\" }, \"sendsTo\" : { \"type\" : \"Gateway\", \"referencedObjectID\" : \"31\" } }, { \"type\" : \"Gateway\", \"@id\" : \"31\", \"name\" : \"DataGW\", \"id\" : 31, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"32\" }, \"receivesFrom\" : [ { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"30\" } ], \"usesGateway\" : [ { \"type\" : \"Gateway\", \"referencedObjectID\" : \"16\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"32\", \"name\" : \"DataGWServer\", \"id\" : 32, \"hosts\" : [ { \"type\" : \"Gateway\", \"referencedObjectID\" : \"31\" } ], \"connectedToNetwork\" : [ { \"type\" : \"Network_Network\", \"referencedObjectID\" : \"2\" } ], \"jurisdiction\" : \"DE\" }, { \"type\" : \"StoredDataSet\", \"@id\" : \"33\", \"name\" : \"DataSet-KnowGoDB-RiskData\", \"id\" : 33, \"consistsOf\" : [ { \"type\" : \"Record\", \"referencedObjectID\" : \"34\" } ], \"belongsTo\" : { \"type\" : \"DataSubject\", \"referencedObjectID\" : \"10\" }, \"createdBySoftwareComponent\" : { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"35\" } }, { \"type\" : \"Record\", \"@id\" : \"34\", \"name\" : \"RiskData\", \"id\" : 34, \"partOf\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"33\" } ] }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"36\", \"name\" : \"KGAnalytics_2\", \"id\" : 36, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"17\" }, \"usedByWebApp\" : [ { \"type\" : \"WebApplication\", \"referencedObjectID\" : \"7\" } ], \"softwareComponentUsesGateway\" : [ { \"type\" : \"Gateway\", \"referencedObjectID\" : \"16\" } ] }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"35\", \"name\" : \"KGAnalytics_1\", \"id\" : 35, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"1\" }, \"creates\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"33\" } ], \"usedByWebApp\" : [ { \"type\" : \"WebApplication\", \"referencedObjectID\" : \"7\" } ], \"softwareComponentUsesGateway\" : [ { \"type\" : \"Gateway\", \"referencedObjectID\" : \"16\" } ] } ] }";
		long id = 0;
		// load model from JSON String to EMF
		CloudEnvironment cloudEnvironment = null;
		try {
			cloudEnvironment = (CloudEnvironment) loader.loadEObjectFromString(model);
			id = loader.setCloudEnvironmentsMonitored(cloudEnvironment, 1);
			// System.out.println("Loaded model after restart event as:\n" +
			// runtimeModelLogic.returnentiremodelasjson(cloudEnvironment));
			System.out.println("Loaded model after restart event.");
		} catch (IOException e) {
			System.err.println("Can't create EMF instance");
		}
		if (cloudEnvironment != null) {
			riskFinder.startRadar(id, AdaptationAlgorithm.BestFirstSearch, "", 0, 10, cloudEnvironment, 0);
		}

		return "Done";
	}

	public ArrayList<RandomizedAdaptationRule> getRandomizedAdaptationRules() {
		return randomizedAdaptationRules;
	}

	public static void writeDataLineByLine(String filePath, String[] data) {
		// first create file object for file placed at location
		// specified by filepath
		File file = new File(filePath);
		try {
			// create FileWriter object with file as parameter
			FileWriter outputfile = new FileWriter(file, true);

			// create CSVWriter object filewriter object as parameter
			CSVWriter writer = new CSVWriter(outputfile);

			writer.writeNext(data);

			// closing writer connection
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String insertNumberBeforeFileEnding(String fileName, int number) {
		String firstPart = fileName.split("[.]csv")[0];
		String returner = firstPart + number + ".csv";
		// System.out.println(returner);
		return returner;
	}
}
