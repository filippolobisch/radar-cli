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
				c udEnvironment = (CloudEnvironment) loader.loadEObjectFromString("{ \"type\" : \"CloudEnvironment\", \"@id\" : \"/\", \"tosca_nodes_root\" : [ { \"type\" : \"DataSubject\", \"@id\" : \"//@tosca_nodes_root.0\", \"name\" : \":DataSubject\", \"id\" : 0, \"trust\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.4\" }, { \"type\" : \"DataProcessor\", \"referencedObjectID\" : \"//@tosca_nodes_root.14\" }, { \"type\" : \"DataProcessor\", \"referencedObjectID\" : \"//@tosca_nodes_root.34\" }, { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.25\" }, { \"type\" : \"DataProcessor\", \"referencedObjectID\" : \"//@tosca_nodes_root.55\" }, { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.46\" }, { \"type\" : \"DataProcessor\", \"referencedObjectID\" : \"//@tosca_nodes_root.76\" }, { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.67\" }, { \"type\" : \"DataProcessor\", \"referencedObjectID\" : \"//@tosca_nodes_root.97\" }, { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.88\" }, { \"type\" : \"DataProcessor\", \"referencedObjectID\" : \"//@tosca_nodes_root.118\" }, { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.109\" }, { \"type\" : \"DataProcessor\", \"referencedObjectID\" : \"//@tosca_nodes_root.139\" }, { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.130\" }, { \"type\" : \"DataProcessor\", \"referencedObjectID\" : \"//@tosca_nodes_root.160\" }, { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.151\" }, { \"type\" : \"DataProcessor\", \"referencedObjectID\" : \"//@tosca_nodes_root.181\" }, { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.172\" }, { \"type\" : \"DataProcessor\", \"referencedObjectID\" : \"//@tosca_nodes_root.202\" }, { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.193\" }, { \"type\" : \"DataProcessor\", \"referencedObjectID\" : \"//@tosca_nodes_root.223\" }, { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.214\" }, { \"type\" : \"DataProcessor\", \"referencedObjectID\" : \"//@tosca_nodes_root.244\" }, { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.235\" }, { \"type\" : \"DataProcessor\", \"referencedObjectID\" : \"//@tosca_nodes_root.265\" }, { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.256\" }, { \"type\" : \"DataProcessor\", \"referencedObjectID\" : \"//@tosca_nodes_root.286\" }, { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.277\" }, { \"type\" : \"DataProcessor\", \"referencedObjectID\" : \"//@tosca_nodes_root.307\" }, { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.298\" }, { \"type\" : \"DataProcessor\", \"referencedObjectID\" : \"//@tosca_nodes_root.328\" }, { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.319\" } ], \"location\" : \"EU\", \"owns\" : [ { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.12\" }, { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.29\" }, { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.50\" }, { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.71\" }, { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.92\" }, { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.113\" }, { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.134\" }, { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.155\" }, { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.176\" }, { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.197\" }, { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.218\" }, { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.239\" }, { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.260\" }, { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.281\" }, { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.302\" }, { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.323\" } ] }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.1\", \"name\" : \"X :IaaSOperator\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.2\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.2\", \"name\" : \"A :Compute\", \"id\" : 0, \"hosts\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.3\" } ], \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.1\" } ], \"usageCostPerDay\" : 1.1136, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.3\", \"name\" : \"A :SoftwareComponent\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.2\" }, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.6\" } ], \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.4\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false }, { \"type\" : \"DataController\", \"@id\" : \"//@tosca_nodes_root.4\", \"name\" : \"A :DataController\", \"id\" : 0, \"location\" : \"BE\", \"controls\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.3\" }, { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.6\" } ] }, { \"type\" : \"Database\", \"@id\" : \"//@tosca_nodes_root.5\", \"name\" : \":Database\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.6\" }, \"stores\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.13\" } ], \"streamsFrom\" : [ { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"//@tosca_nodes_root.11\" } ], \"encrypted\" : false }, { \"type\" : \"DBMS\", \"@id\" : \"//@tosca_nodes_root.6\", \"name\" : \":DBMS\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.9\" }, \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.4\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false, \"hosts\" : [ { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.5\" } ], \"accessedBySoftwareComponent\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.3\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.8\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.17\" } ] }, { \"type\" : \"SaaSOperator\", \"@id\" : \"//@tosca_nodes_root.7\", \"name\" : \"Z :SaaSOperator\", \"id\" : 0, \"operates\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.8\" } ] }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.8\", \"name\" : \"C :SoftwareComponent\", \"id\" : 0, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.6\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false, \"operatedBySaaSOperator\" : { \"type\" : \"SaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.7\" } }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.9\", \"name\" : \":Compute\", \"id\" : 0, \"hosts\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.6\" } ], \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.10\" } ], \"usageCostPerDay\" : 1.1136, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.10\", \"name\" : \"W :IaaSOperator\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.9\" } ] }, { \"type\" : \"DataFlow\", \"@id\" : \"//@tosca_nodes_root.11\", \"name\" : \":DataFlow\", \"id\" : 0, \"consistsOf\" : [ { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.12\" } ], \"streamsTo\" : [ { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.5\" } ], \"disab\" : false, \"amountOfDataInGB\" : 0.0 }, { \"type\" : \"Record\", \"@id\" : \"//@tosca_nodes_root.12\", \"name\" : \":Record\", \"id\" : 0, \"partOf\" : [ { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"//@tosca_nodes_root.11\" }, { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.13\" } ], \"sensitive\" : true, \"encrypted\" : true, \"belongsTo\" : { \"type\" : \"DataSubject\", \"referencedObjectID\" : \"//@tosca_nodes_root.0\" } }, { \"type\" : \"StoredDataSet\", \"@id\" : \"//@tosca_nodes_root.13\", \"name\" : \":StoredDataSet\", \"id\" : 0, \"consistsOf\" : [ { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.12\" } ], \"storedIn\" : { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.5\" }, \"accessedBy\" : [ { \"type\" : \"DataProcessor\", \"referencedObjectID\" : \"//@tosca_nodes_root.14\" } ] }, { \"type\" : \"DataProcessor\", \"@id\" : \"//@tosca_nodes_root.14\", \"name\" : \":DataProcessor\", \"id\" : 0, \"location\" : \"BE\", \"accesses\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.13\" } ] }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.15\", \"name\" : \"Y :IaaSOperator\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.16\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.16\", \"name\" : \"B :Compute\", \"id\" : 0, \"jurisdiction\" : \"US\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.15\" } ], \"usageCostPerDay\" : 0.552, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.17\", \"name\" : \"B :SoftwareComponent\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.20\" }, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.6\" } ], \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.18\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false }, { \"type\" : \"DataController\", \"@id\" : \"//@tosca_nodes_root.18\", \"name\" : \"B :DataController\", \"id\" : 0, \"location\" : \"BE\", \"controls\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.17\" } ] }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.19\", \"name\" : \"V :IaaSOperator\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.20\" }, { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.21\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.20\", \"name\" : \"D :Compute\", \"id\" : 0, \"hosts\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.122\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.101\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.80\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.59\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.38\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.17\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.227\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.206\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.185\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.164\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.143\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.332\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.311\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.290\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.269\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.248\" } ], \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.19\" } ], \"usageCostPerDay\" : 0.552, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.21\", \"name\" : \"E :Compute\", \"id\" : 0, \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.19\" } ], \"usageCostPerDay\" : 2.2272, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.22\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.23\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.23\", \"id\" : 0, \"hosts\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.24\" } ], \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.22\" } ], \"usageCostPerDay\" : 1.1136, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.24\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.23\" }, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.31\" } ], \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.25\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false }, { \"type\" : \"DataController\", \"@id\" : \"//@tosca_nodes_root.25\", \"id\" : 0, \"location\" : \"BE\", \"controls\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.24\" }, { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.31\" } ] }, { \"type\" : \"SaaSOperator\", \"@id\" : \"//@tosca_nodes_root.26\", \"id\" : 0, \"operates\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.28\" } ] }, { \"type\" : \"DataFlow\", \"@id\" : \"//@tosca_nodes_root.27\", \"id\" : 0, \"consistsOf\" : [ { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.29\" } ], \"streamsTo\" : [ { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.30\" } ], \"disab\" : false, \"amountOfDataInGB\" : 0.0 }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.28\", \"id\" : 0, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.31\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false, \"operatedBySaaSOperator\" : { \"type\" : \"SaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.26\" } }, { \"type\" : \"Record\", \"@id\" : \"//@tosca_nodes_root.29\", \"id\" : 0, \"partOf\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.35\" }, { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"//@tosca_nodes_root.27\" } ], \"sensitive\" : true, \"encrypted\" : true, \"belongsTo\" : { \"type\" : \"DataSubject\", \"referencedObjectID\" : \"//@tosca_nodes_root.0\" } }, { \"type\" : \"Database\", \"@id\" : \"//@tosca_nodes_root.30\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.31\" }, \"stores\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.35\" } ], \"streamsFrom\" : [ { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"//@tosca_nodes_root.27\" } ], \"encrypted\" : false }, { \"type\" : \"DBMS\", \"@id\" : \"//@tosca_nodes_root.31\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.32\" }, \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.25\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false, \"hosts\" : [ { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.30\" } ], \"accessedBySoftwareComponent\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.38\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.28\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.24\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.32\", \"id\" : 0, \"hosts\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.31\" } ], \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.33\" } ], \"usageCostPerDay\" : 1.1136, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.33\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.32\" } ] }, { \"type\" : \"DataProcessor\", \"@id\" : \"//@tosca_nodes_root.34\", \"id\" : 0, \"location\" : \"BE\", \"accesses\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.35\" } ] }, { \"type\" : \"StoredDataSet\", \"@id\" : \"//@tosca_nodes_root.35\", \"id\" : 0, \"consistsOf\" : [ { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.29\" } ], \"storedIn\" : { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.30\" }, \"accessedBy\" : [ { \"type\" : \"DataProcessor\", \"referencedObjectID\" : \"//@tosca_nodes_root.34\" } ] }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.36\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.37\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.37\", \"id\" : 0, \"jurisdiction\" : \"US\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.36\" } ], \"usageCostPerDay\" : 0.552, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.38\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.20\" }, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.31\" } ], \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.39\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false }, { \"type\" : \"DataController\", \"@id\" : \"//@tosca_nodes_root.39\", \"id\" : 0, \"location\" : \"BE\", \"controls\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.38\" } ] }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.40\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.41\" }, { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.42\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.41\", \"id\" : 0, \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.40\" } ], \"usageCostPerDay\" : 0.552, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.42\", \"id\" : 0, \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.40\" } ], \"usageCostPerDay\" : 2.2272, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.43\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.44\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.44\", \"id\" : 0, \"hosts\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.45\" } ], \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.43\" } ], \"usageCostPerDay\" : 1.1136, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.45\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.44\" }, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.52\" } ], \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.46\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false }, { \"type\" : \"DataController\", \"@id\" : \"//@tosca_nodes_root.46\", \"id\" : 0, \"location\" : \"BE\", \"controls\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.45\" }, { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.52\" } ] }, { \"type\" : \"SaaSOperator\", \"@id\" : \"//@tosca_nodes_root.47\", \"id\" : 0, \"operates\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.49\" } ] }, { \"type\" : \"DataFlow\", \"@id\" : \"//@tosca_nodes_root.48\", \"id\" : 0, \"consistsOf\" : [ { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.50\" } ], \"streamsTo\" : [ { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.51\" } ], \"disab\" : false, \"amountOfDataInGB\" : 0.0 }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.49\", \"id\" : 0, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.52\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false, \"operatedBySaaSOperator\" : { \"type\" : \"SaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.47\" } }, { \"type\" : \"Record\", \"@id\" : \"//@tosca_nodes_root.50\", \"id\" : 0, \"partOf\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.56\" }, { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"//@tosca_nodes_root.48\" } ], \"sensitive\" : true, \"encrypted\" : true, \"belongsTo\" : { \"type\" : \"DataSubject\", \"referencedObjectID\" : \"//@tosca_nodes_root.0\" } }, { \"type\" : \"Database\", \"@id\" : \"//@tosca_nodes_root.51\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.52\" }, \"stores\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.56\" } ], \"streamsFrom\" : [ { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"//@tosca_nodes_root.48\" } ], \"encrypted\" : false }, { \"type\" : \"DBMS\", \"@id\" : \"//@tosca_nodes_root.52\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.53\" }, \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.46\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false, \"hosts\" : [ { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.51\" } ], \"accessedBySoftwareComponent\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.59\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.49\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.45\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.53\", \"id\" : 0, \"hosts\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.52\" } ], \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.54\" } ], \"usageCostPerDay\" : 1.1136, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.54\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.53\" } ] }, { \"type\" : \"DataProcessor\", \"@id\" : \"//@tosca_nodes_root.55\", \"id\" : 0, \"location\" : \"BE\", \"accesses\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.56\" } ] }, { \"type\" : \"StoredDataSet\", \"@id\" : \"//@tosca_nodes_root.56\", \"id\" : 0, \"consistsOf\" : [ { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.50\" } ], \"storedIn\" : { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.51\" }, \"accessedBy\" : [ { \"type\" : \"DataProcessor\", \"referencedObjectID\" : \"//@tosca_nodes_root.55\" } ] }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.57\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.58\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.58\", \"id\" : 0, \"jurisdiction\" : \"US\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.57\" } ], \"usageCostPerDay\" : 0.552, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.59\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.20\" }, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.52\" } ], \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.60\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false }, { \"type\" : \"DataController\", \"@id\" : \"//@tosca_nodes_root.60\", \"id\" : 0, \"location\" : \"BE\", \"controls\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.59\" } ] }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.61\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.62\" }, { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.63\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.62\", \"id\" : 0, \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.61\" } ], \"usageCostPerDay\" : 0.552, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.63\", \"id\" : 0, \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.61\" } ], \"usageCostPerDay\" : 2.2272, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.64\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.65\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.65\", \"id\" : 0, \"hosts\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.66\" } ], \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.64\" } ], \"usageCostPerDay\" : 1.1136, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.66\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.65\" }, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.73\" } ], \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.67\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false }, { \"type\" : \"DataController\", \"@id\" : \"//@tosca_nodes_root.67\", \"id\" : 0, \"location\" : \"BE\", \"controls\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.66\" }, { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.73\" } ] }, { \"type\" : \"SaaSOperator\", \"@id\" : \"//@tosca_nodes_root.68\", \"id\" : 0, \"operates\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.70\" } ] }, { \"type\" : \"DataFlow\", \"@id\" : \"//@tosca_nodes_root.69\", \"id\" : 0, \"consistsOf\" : [ { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.71\" } ], \"streamsTo\" : [ { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.72\" } ], \"disab\" : false, \"amountOfDataInGB\" : 0.0 }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.70\", \"id\" : 0, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.73\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false, \"operatedBySaaSOperator\" : { \"type\" : \"SaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.68\" } }, { \"type\" : \"Record\", \"@id\" : \"//@tosca_nodes_root.71\", \"id\" : 0, \"partOf\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.77\" }, { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"//@tosca_nodes_root.69\" } ], \"sensitive\" : true, \"encrypted\" : true, \"belongsTo\" : { \"type\" : \"DataSubject\", \"referencedObjectID\" : \"//@tosca_nodes_root.0\" } }, { \"type\" : \"Database\", \"@id\" : \"//@tosca_nodes_root.72\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.73\" }, \"stores\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.77\" } ], \"streamsFrom\" : [ { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"//@tosca_nodes_root.69\" } ], \"encrypted\" : false }, { \"type\" : \"DBMS\", \"@id\" : \"//@tosca_nodes_root.73\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.74\" }, \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.67\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false, \"hosts\" : [ { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.72\" } ], \"accessedBySoftwareComponent\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.80\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.70\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.66\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.74\", \"id\" : 0, \"hosts\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.73\" } ], \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.75\" } ], \"usageCostPerDay\" : 1.1136, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.75\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.74\" } ] }, { \"type\" : \"DataProcessor\", \"@id\" : \"//@tosca_nodes_root.76\", \"id\" : 0, \"location\" : \"BE\", \"accesses\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.77\" } ] }, { \"type\" : \"StoredDataSet\", \"@id\" : \"//@tosca_nodes_root.77\", \"id\" : 0, \"consistsOf\" : [ { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.71\" } ], \"storedIn\" : { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.72\" }, \"accessedBy\" : [ { \"type\" : \"DataProcessor\", \"referencedObjectID\" : \"//@tosca_nodes_root.76\" } ] }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.78\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.79\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.79\", \"id\" : 0, \"jurisdiction\" : \"US\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.78\" } ], \"usageCostPerDay\" : 0.552, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.80\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.20\" }, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.73\" } ], \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.81\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false }, { \"type\" : \"DataController\", \"@id\" : \"//@tosca_nodes_root.81\", \"id\" : 0, \"location\" : \"BE\", \"controls\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.80\" } ] }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.82\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.83\" }, { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.84\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.83\", \"id\" : 0, \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.82\" } ], \"usageCostPerDay\" : 0.552, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.84\", \"id\" : 0, \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.82\" } ], \"usageCostPerDay\" : 2.2272, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.85\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.86\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.86\", \"id\" : 0, \"hosts\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.87\" } ], \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.85\" } ], \"usageCostPerDay\" : 1.1136, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.87\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.86\" }, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.94\" } ], \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.88\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false }, { \"type\" : \"DataController\", \"@id\" : \"//@tosca_nodes_root.88\", \"id\" : 0, \"location\" : \"BE\", \"controls\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.87\" }, { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.94\" } ] }, { \"type\" : \"SaaSOperator\", \"@id\" : \"//@tosca_nodes_root.89\", \"id\" : 0, \"operates\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.91\" } ] }, { \"type\" : \"DataFlow\", \"@id\" : \"//@tosca_nodes_root.90\", \"id\" : 0, \"consistsOf\" : [ { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.92\" } ], \"streamsTo\" : [ { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.93\" } ], \"disab\" : false, \"amountOfDataInGB\" : 0.0 }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.91\", \"id\" : 0, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.94\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false, \"operatedBySaaSOperator\" : { \"type\" : \"SaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.89\" } }, { \"type\" : \"Record\", \"@id\" : \"//@tosca_nodes_root.92\", \"id\" : 0, \"partOf\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.98\" }, { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"//@tosca_nodes_root.90\" } ], \"sensitive\" : true, \"encrypted\" : true, \"belongsTo\" : { \"type\" : \"DataSubject\", \"referencedObjectID\" : \"//@tosca_nodes_root.0\" } }, { \"type\" : \"Database\", \"@id\" : \"//@tosca_nodes_root.93\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.94\" }, \"stores\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.98\" } ], \"streamsFrom\" : [ { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"//@tosca_nodes_root.90\" } ], \"encrypted\" : false }, { \"type\" : \"DBMS\", \"@id\" : \"//@tosca_nodes_root.94\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.95\" }, \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.88\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false, \"hosts\" : [ { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.93\" } ], \"accessedBySoftwareComponent\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.101\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.91\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.87\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.95\", \"id\" : 0, \"hosts\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.94\" } ], \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.96\" } ], \"usageCostPerDay\" : 1.1136, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.96\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.95\" } ] }, { \"type\" : \"DataProcessor\", \"@id\" : \"//@tosca_nodes_root.97\", \"id\" : 0, \"location\" : \"BE\", \"accesses\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.98\" } ] }, { \"type\" : \"StoredDataSet\", \"@id\" : \"//@tosca_nodes_root.98\", \"id\" : 0, \"consistsOf\" : [ { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.92\" } ], \"storedIn\" : { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.93\" }, \"accessedBy\" : [ { \"type\" : \"DataProcessor\", \"referencedObjectID\" : \"//@tosca_nodes_root.97\" } ] }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.99\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.100\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.100\", \"id\" : 0, \"jurisdiction\" : \"US\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.99\" } ], \"usageCostPerDay\" : 0.552, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.101\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.20\" }, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.94\" } ], \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.102\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false }, { \"type\" : \"DataController\", \"@id\" : \"//@tosca_nodes_root.102\", \"id\" : 0, \"location\" : \"BE\", \"controls\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.101\" } ] }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.103\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.104\" }, { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.105\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.104\", \"id\" : 0, \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.103\" } ], \"usageCostPerDay\" : 0.552, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.105\", \"id\" : 0, \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.103\" } ], \"usageCostPerDay\" : 2.2272, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.106\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.107\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.107\", \"id\" : 0, \"hosts\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.108\" } ], \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.106\" } ], \"usageCostPerDay\" : 1.1136, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.108\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.107\" }, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.115\" } ], \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.109\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false }, { \"type\" : \"DataController\", \"@id\" : \"//@tosca_nodes_root.109\", \"id\" : 0, \"location\" : \"BE\", \"controls\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.108\" }, { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.115\" } ] }, { \"type\" : \"SaaSOperator\", \"@id\" : \"//@tosca_nodes_root.110\", \"id\" : 0, \"operates\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.112\" } ] }, { \"type\" : \"DataFlow\", \"@id\" : \"//@tosca_nodes_root.111\", \"id\" : 0, \"consistsOf\" : [ { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.113\" } ], \"streamsTo\" : [ { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.114\" } ], \"disab\" : false, \"amountOfDataInGB\" : 0.0 }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.112\", \"id\" : 0, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.115\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false, \"operatedBySaaSOperator\" : { \"type\" : \"SaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.110\" } }, { \"type\" : \"Record\", \"@id\" : \"//@tosca_nodes_root.113\", \"id\" : 0, \"partOf\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.119\" }, { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"//@tosca_nodes_root.111\" } ], \"sensitive\" : true, \"encrypted\" : true, \"belongsTo\" : { \"type\" : \"DataSubject\", \"referencedObjectID\" : \"//@tosca_nodes_root.0\" } }, { \"type\" : \"Database\", \"@id\" : \"//@tosca_nodes_root.114\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.115\" }, \"stores\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.119\" } ], \"streamsFrom\" : [ { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"//@tosca_nodes_root.111\" } ], \"encrypted\" : false }, { \"type\" : \"DBMS\", \"@id\" : \"//@tosca_nodes_root.115\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.116\" }, \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.109\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false, \"hosts\" : [ { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.114\" } ], \"accessedBySoftwareComponent\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.122\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.112\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.108\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.116\", \"id\" : 0, \"hosts\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.115\" } ], \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.117\" } ], \"usageCostPerDay\" : 1.1136, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.117\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.116\" } ] }, { \"type\" : \"DataProcessor\", \"@id\" : \"//@tosca_nodes_root.118\", \"id\" : 0, \"location\" : \"BE\", \"accesses\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.119\" } ] }, { \"type\" : \"StoredDataSet\", \"@id\" : \"//@tosca_nodes_root.119\", \"id\" : 0, \"consistsOf\" : [ { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.113\" } ], \"storedIn\" : { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.114\" }, \"accessedBy\" : [ { \"type\" : \"DataProcessor\", \"referencedObjectID\" : \"//@tosca_nodes_root.118\" } ] }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.120\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.121\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.121\", \"id\" : 0, \"jurisdiction\" : \"US\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.120\" } ], \"usageCostPerDay\" : 0.552, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.122\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.20\" }, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.115\" } ], \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.123\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false }, { \"type\" : \"DataController\", \"@id\" : \"//@tosca_nodes_root.123\", \"id\" : 0, \"location\" : \"BE\", \"controls\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.122\" } ] }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.124\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.125\" }, { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.126\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.125\", \"id\" : 0, \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.124\" } ], \"usageCostPerDay\" : 0.552, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.126\", \"id\" : 0, \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.124\" } ], \"usageCostPerDay\" : 2.2272, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.127\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.128\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.128\", \"id\" : 0, \"hosts\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.129\" } ], \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.127\" } ], \"usageCostPerDay\" : 1.1136, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.129\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.128\" }, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.136\" } ], \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.130\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false }, { \"type\" : \"DataController\", \"@id\" : \"//@tosca_nodes_root.130\", \"id\" : 0, \"location\" : \"BE\", \"controls\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.129\" }, { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.136\" } ] }, { \"type\" : \"SaaSOperator\", \"@id\" : \"//@tosca_nodes_root.131\", \"id\" : 0, \"operates\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.133\" } ] }, { \"type\" : \"DataFlow\", \"@id\" : \"//@tosca_nodes_root.132\", \"id\" : 0, \"consistsOf\" : [ { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.134\" } ], \"streamsTo\" : [ { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.135\" } ], \"disab\" : false, \"amountOfDataInGB\" : 0.0 }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.133\", \"id\" : 0, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.136\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false, \"operatedBySaaSOperator\" : { \"type\" : \"SaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.131\" } }, { \"type\" : \"Record\", \"@id\" : \"//@tosca_nodes_root.134\", \"id\" : 0, \"partOf\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.140\" }, { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"//@tosca_nodes_root.132\" } ], \"sensitive\" : true, \"encrypted\" : true, \"belongsTo\" : { \"type\" : \"DataSubject\", \"referencedObjectID\" : \"//@tosca_nodes_root.0\" } }, { \"type\" : \"Database\", \"@id\" : \"//@tosca_nodes_root.135\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.136\" }, \"stores\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.140\" } ], \"streamsFrom\" : [ { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"//@tosca_nodes_root.132\" } ], \"encrypted\" : false }, { \"type\" : \"DBMS\", \"@id\" : \"//@tosca_nodes_root.136\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.137\" }, \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.130\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false, \"hosts\" : [ { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.135\" } ], \"accessedBySoftwareComponent\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.143\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.133\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.129\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.137\", \"id\" : 0, \"hosts\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.136\" } ], \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.138\" } ], \"usageCostPerDay\" : 1.1136, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.138\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.137\" } ] }, { \"type\" : \"DataProcessor\", \"@id\" : \"//@tosca_nodes_root.139\", \"id\" : 0, \"location\" : \"BE\", \"accesses\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.140\" } ] }, { \"type\" : \"StoredDataSet\", \"@id\" : \"//@tosca_nodes_root.140\", \"id\" : 0, \"consistsOf\" : [ { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.134\" } ], \"storedIn\" : { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.135\" }, \"accessedBy\" : [ { \"type\" : \"DataProcessor\", \"referencedObjectID\" : \"//@tosca_nodes_root.139\" } ] }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.141\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.142\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.142\", \"id\" : 0, \"jurisdiction\" : \"US\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.141\" } ], \"usageCostPerDay\" : 0.552, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.143\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.20\" }, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.136\" } ], \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.144\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false }, { \"type\" : \"DataController\", \"@id\" : \"//@tosca_nodes_root.144\", \"id\" : 0, \"location\" : \"BE\", \"controls\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.143\" } ] }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.145\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.146\" }, { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.147\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.146\", \"id\" : 0, \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.145\" } ], \"usageCostPerDay\" : 0.552, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.147\", \"id\" : 0, \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.145\" } ], \"usageCostPerDay\" : 2.2272, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.148\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.149\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.149\", \"id\" : 0, \"hosts\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.150\" } ], \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.148\" } ], \"usageCostPerDay\" : 1.1136, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.150\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.149\" }, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.157\" } ], \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.151\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false }, { \"type\" : \"DataController\", \"@id\" : \"//@tosca_nodes_root.151\", \"id\" : 0, \"location\" : \"BE\", \"controls\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.150\" }, { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.157\" } ] }, { \"type\" : \"SaaSOperator\", \"@id\" : \"//@tosca_nodes_root.152\", \"id\" : 0, \"operates\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.154\" } ] }, { \"type\" : \"DataFlow\", \"@id\" : \"//@tosca_nodes_root.153\", \"id\" : 0, \"consistsOf\" : [ { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.155\" } ], \"streamsTo\" : [ { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.156\" } ], \"disab\" : false, \"amountOfDataInGB\" : 0.0 }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.154\", \"id\" : 0, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.157\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false, \"operatedBySaaSOperator\" : { \"type\" : \"SaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.152\" } }, { \"type\" : \"Record\", \"@id\" : \"//@tosca_nodes_root.155\", \"id\" : 0, \"partOf\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.161\" }, { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"//@tosca_nodes_root.153\" } ], \"sensitive\" : true, \"encrypted\" : true, \"belongsTo\" : { \"type\" : \"DataSubject\", \"referencedObjectID\" : \"//@tosca_nodes_root.0\" } }, { \"type\" : \"Database\", \"@id\" : \"//@tosca_nodes_root.156\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.157\" }, \"stores\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.161\" } ], \"streamsFrom\" : [ { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"//@tosca_nodes_root.153\" } ], \"encrypted\" : false }, { \"type\" : \"DBMS\", \"@id\" : \"//@tosca_nodes_root.157\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.158\" }, \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.151\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false, \"hosts\" : [ { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.156\" } ], \"accessedBySoftwareComponent\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.164\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.154\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.150\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.158\", \"id\" : 0, \"hosts\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.157\" } ], \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.159\" } ], \"usageCostPerDay\" : 1.1136, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.159\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.158\" } ] }, { \"type\" : \"DataProcessor\", \"@id\" : \"//@tosca_nodes_root.160\", \"id\" : 0, \"location\" : \"BE\", \"accesses\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.161\" } ] }, { \"type\" : \"StoredDataSet\", \"@id\" : \"//@tosca_nodes_root.161\", \"id\" : 0, \"consistsOf\" : [ { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.155\" } ], \"storedIn\" : { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.156\" }, \"accessedBy\" : [ { \"type\" : \"DataProcessor\", \"referencedObjectID\" : \"//@tosca_nodes_root.160\" } ] }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.162\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.163\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.163\", \"id\" : 0, \"jurisdiction\" : \"US\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.162\" } ], \"usageCostPerDay\" : 0.552, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.164\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.20\" }, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.157\" } ], \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.165\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false }, { \"type\" : \"DataController\", \"@id\" : \"//@tosca_nodes_root.165\", \"id\" : 0, \"location\" : \"BE\", \"controls\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.164\" } ] }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.166\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.167\" }, { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.168\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.167\", \"id\" : 0, \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.166\" } ], \"usageCostPerDay\" : 0.552, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.168\", \"id\" : 0, \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.166\" } ], \"usageCostPerDay\" : 2.2272, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.169\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.170\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.170\", \"id\" : 0, \"hosts\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.171\" } ], \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.169\" } ], \"usageCostPerDay\" : 1.1136, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.171\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.170\" }, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.178\" } ], \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.172\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false }, { \"type\" : \"DataController\", \"@id\" : \"//@tosca_nodes_root.172\", \"id\" : 0, \"location\" : \"BE\", \"controls\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.171\" }, { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.178\" } ] }, { \"type\" : \"SaaSOperator\", \"@id\" : \"//@tosca_nodes_root.173\", \"id\" : 0, \"operates\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.175\" } ] }, { \"type\" : \"DataFlow\", \"@id\" : \"//@tosca_nodes_root.174\", \"id\" : 0, \"consistsOf\" : [ { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.176\" } ], \"streamsTo\" : [ { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.177\" } ], \"disab\" : false, \"amountOfDataInGB\" : 0.0 }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.175\", \"id\" : 0, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.178\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false, \"operatedBySaaSOperator\" : { \"type\" : \"SaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.173\" } }, { \"type\" : \"Record\", \"@id\" : \"//@tosca_nodes_root.176\", \"id\" : 0, \"partOf\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.182\" }, { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"//@tosca_nodes_root.174\" } ], \"sensitive\" : true, \"encrypted\" : true, \"belongsTo\" : { \"type\" : \"DataSubject\", \"referencedObjectID\" : \"//@tosca_nodes_root.0\" } }, { \"type\" : \"Database\", \"@id\" : \"//@tosca_nodes_root.177\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.178\" }, \"stores\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.182\" } ], \"streamsFrom\" : [ { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"//@tosca_nodes_root.174\" } ], \"encrypted\" : false }, { \"type\" : \"DBMS\", \"@id\" : \"//@tosca_nodes_root.178\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.179\" }, \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.172\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false, \"hosts\" : [ { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.177\" } ], \"accessedBySoftwareComponent\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.185\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.175\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.171\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.179\", \"id\" : 0, \"hosts\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.178\" } ], \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.180\" } ], \"usageCostPerDay\" : 1.1136, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.180\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.179\" } ] }, { \"type\" : \"DataProcessor\", \"@id\" : \"//@tosca_nodes_root.181\", \"id\" : 0, \"location\" : \"BE\", \"accesses\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.182\" } ] }, { \"type\" : \"StoredDataSet\", \"@id\" : \"//@tosca_nodes_root.182\", \"id\" : 0, \"consistsOf\" : [ { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.176\" } ], \"storedIn\" : { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.177\" }, \"accessedBy\" : [ { \"type\" : \"DataProcessor\", \"referencedObjectID\" : \"//@tosca_nodes_root.181\" } ] }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.183\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.184\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.184\", \"id\" : 0, \"jurisdiction\" : \"US\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.183\" } ], \"usageCostPerDay\" : 0.552, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.185\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.20\" }, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.178\" } ], \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.186\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false }, { \"type\" : \"DataController\", \"@id\" : \"//@tosca_nodes_root.186\", \"id\" : 0, \"location\" : \"BE\", \"controls\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.185\" } ] }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.187\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.188\" }, { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.189\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.188\", \"id\" : 0, \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.187\" } ], \"usageCostPerDay\" : 0.552, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.189\", \"id\" : 0, \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.187\" } ], \"usageCostPerDay\" : 2.2272, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.190\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.191\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.191\", \"id\" : 0, \"hosts\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.192\" } ], \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.190\" } ], \"usageCostPerDay\" : 1.1136, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.192\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.191\" }, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.199\" } ], \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.193\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false }, { \"type\" : \"DataController\", \"@id\" : \"//@tosca_nodes_root.193\", \"id\" : 0, \"location\" : \"BE\", \"controls\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.192\" }, { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.199\" } ] }, { \"type\" : \"SaaSOperator\", \"@id\" : \"//@tosca_nodes_root.194\", \"id\" : 0, \"operates\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.196\" } ] }, { \"type\" : \"DataFlow\", \"@id\" : \"//@tosca_nodes_root.195\", \"id\" : 0, \"consistsOf\" : [ { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.197\" } ], \"streamsTo\" : [ { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.198\" } ], \"disab\" : false, \"amountOfDataInGB\" : 0.0 }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.196\", \"id\" : 0, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.199\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false, \"operatedBySaaSOperator\" : { \"type\" : \"SaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.194\" } }, { \"type\" : \"Record\", \"@id\" : \"//@tosca_nodes_root.197\", \"id\" : 0, \"partOf\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.203\" }, { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"//@tosca_nodes_root.195\" } ], \"sensitive\" : true, \"encrypted\" : true, \"belongsTo\" : { \"type\" : \"DataSubject\", \"referencedObjectID\" : \"//@tosca_nodes_root.0\" } }, { \"type\" : \"Database\", \"@id\" : \"//@tosca_nodes_root.198\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.199\" }, \"stores\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.203\" } ], \"streamsFrom\" : [ { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"//@tosca_nodes_root.195\" } ], \"encrypted\" : false }, { \"type\" : \"DBMS\", \"@id\" : \"//@tosca_nodes_root.199\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.200\" }, \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.193\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false, \"hosts\" : [ { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.198\" } ], \"accessedBySoftwareComponent\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.206\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.196\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.192\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.200\", \"id\" : 0, \"hosts\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.199\" } ], \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.201\" } ], \"usageCostPerDay\" : 1.1136, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.201\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.200\" } ] }, { \"type\" : \"DataProcessor\", \"@id\" : \"//@tosca_nodes_root.202\", \"id\" : 0, \"location\" : \"BE\", \"accesses\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.203\" } ] }, { \"type\" : \"StoredDataSet\", \"@id\" : \"//@tosca_nodes_root.203\", \"id\" : 0, \"consistsOf\" : [ { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.197\" } ], \"storedIn\" : { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.198\" }, \"accessedBy\" : [ { \"type\" : \"DataProcessor\", \"referencedObjectID\" : \"//@tosca_nodes_root.202\" } ] }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.204\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.205\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.205\", \"id\" : 0, \"jurisdiction\" : \"US\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.204\" } ], \"usageCostPerDay\" : 0.552, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.206\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.20\" }, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.199\" } ], \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.207\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false }, { \"type\" : \"DataController\", \"@id\" : \"//@tosca_nodes_root.207\", \"id\" : 0, \"location\" : \"BE\", \"controls\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.206\" } ] }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.208\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.209\" }, { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.210\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.209\", \"id\" : 0, \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.208\" } ], \"usageCostPerDay\" : 0.552, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.210\", \"id\" : 0, \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.208\" } ], \"usageCostPerDay\" : 2.2272, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.211\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.212\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.212\", \"id\" : 0, \"hosts\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.213\" } ], \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.211\" } ], \"usageCostPerDay\" : 1.1136, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.213\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.212\" }, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.220\" } ], \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.214\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false }, { \"type\" : \"DataController\", \"@id\" : \"//@tosca_nodes_root.214\", \"id\" : 0, \"location\" : \"BE\", \"controls\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.213\" }, { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.220\" } ] }, { \"type\" : \"SaaSOperator\", \"@id\" : \"//@tosca_nodes_root.215\", \"id\" : 0, \"operates\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.217\" } ] }, { \"type\" : \"DataFlow\", \"@id\" : \"//@tosca_nodes_root.216\", \"id\" : 0, \"consistsOf\" : [ { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.218\" } ], \"streamsTo\" : [ { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.219\" } ], \"disab\" : false, \"amountOfDataInGB\" : 0.0 }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.217\", \"id\" : 0, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.220\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false, \"operatedBySaaSOperator\" : { \"type\" : \"SaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.215\" } }, { \"type\" : \"Record\", \"@id\" : \"//@tosca_nodes_root.218\", \"id\" : 0, \"partOf\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.224\" }, { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"//@tosca_nodes_root.216\" } ], \"sensitive\" : true, \"encrypted\" : true, \"belongsTo\" : { \"type\" : \"DataSubject\", \"referencedObjectID\" : \"//@tosca_nodes_root.0\" } }, { \"type\" : \"Database\", \"@id\" : \"//@tosca_nodes_root.219\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.220\" }, \"stores\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.224\" } ], \"streamsFrom\" : [ { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"//@tosca_nodes_root.216\" } ], \"encrypted\" : false }, { \"type\" : \"DBMS\", \"@id\" : \"//@tosca_nodes_root.220\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.221\" }, \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.214\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false, \"hosts\" : [ { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.219\" } ], \"accessedBySoftwareComponent\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.227\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.217\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.213\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.221\", \"id\" : 0, \"hosts\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.220\" } ], \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.222\" } ], \"usageCostPerDay\" : 1.1136, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.222\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.221\" } ] }, { \"type\" : \"DataProcessor\", \"@id\" : \"//@tosca_nodes_root.223\", \"id\" : 0, \"location\" : \"BE\", \"accesses\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.224\" } ] }, { \"type\" : \"StoredDataSet\", \"@id\" : \"//@tosca_nodes_root.224\", \"id\" : 0, \"consistsOf\" : [ { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.218\" } ], \"storedIn\" : { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.219\" }, \"accessedBy\" : [ { \"type\" : \"DataProcessor\", \"referencedObjectID\" : \"//@tosca_nodes_root.223\" } ] }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.225\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.226\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.226\", \"id\" : 0, \"jurisdiction\" : \"US\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.225\" } ], \"usageCostPerDay\" : 0.552, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.227\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.20\" }, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.220\" } ], \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.228\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false }, { \"type\" : \"DataController\", \"@id\" : \"//@tosca_nodes_root.228\", \"id\" : 0, \"location\" : \"BE\", \"controls\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.227\" } ] }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.229\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.230\" }, { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.231\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.230\", \"id\" : 0, \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.229\" } ], \"usageCostPerDay\" : 0.552, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.231\", \"id\" : 0, \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.229\" } ], \"usageCostPerDay\" : 2.2272, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.232\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.233\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.233\", \"id\" : 0, \"hosts\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.234\" } ], \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.232\" } ], \"usageCostPerDay\" : 1.1136, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.234\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.233\" }, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.241\" } ], \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.235\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false }, { \"type\" : \"DataController\", \"@id\" : \"//@tosca_nodes_root.235\", \"id\" : 0, \"location\" : \"BE\", \"controls\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.234\" }, { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.241\" } ] }, { \"type\" : \"SaaSOperator\", \"@id\" : \"//@tosca_nodes_root.236\", \"id\" : 0, \"operates\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.238\" } ] }, { \"type\" : \"DataFlow\", \"@id\" : \"//@tosca_nodes_root.237\", \"id\" : 0, \"consistsOf\" : [ { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.239\" } ], \"streamsTo\" : [ { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.240\" } ], \"disab\" : false, \"amountOfDataInGB\" : 0.0 }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.238\", \"id\" : 0, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.241\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false, \"operatedBySaaSOperator\" : { \"type\" : \"SaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.236\" } }, { \"type\" : \"Record\", \"@id\" : \"//@tosca_nodes_root.239\", \"id\" : 0, \"partOf\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.245\" }, { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"//@tosca_nodes_root.237\" } ], \"sensitive\" : true, \"encrypted\" : true, \"belongsTo\" : { \"type\" : \"DataSubject\", \"referencedObjectID\" : \"//@tosca_nodes_root.0\" } }, { \"type\" : \"Database\", \"@id\" : \"//@tosca_nodes_root.240\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.241\" }, \"stores\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.245\" } ], \"streamsFrom\" : [ { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"//@tosca_nodes_root.237\" } ], \"encrypted\" : false }, { \"type\" : \"DBMS\", \"@id\" : \"//@tosca_nodes_root.241\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.242\" }, \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.235\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false, \"hosts\" : [ { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.240\" } ], \"accessedBySoftwareComponent\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.248\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.238\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.234\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.242\", \"id\" : 0, \"hosts\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.241\" } ], \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.243\" } ], \"usageCostPerDay\" : 1.1136, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.243\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.242\" } ] }, { \"type\" : \"DataProcessor\", \"@id\" : \"//@tosca_nodes_root.244\", \"id\" : 0, \"location\" : \"BE\", \"accesses\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.245\" } ] }, { \"type\" : \"StoredDataSet\", \"@id\" : \"//@tosca_nodes_root.245\", \"id\" : 0, \"consistsOf\" : [ { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.239\" } ], \"storedIn\" : { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.240\" }, \"accessedBy\" : [ { \"type\" : \"DataProcessor\", \"referencedObjectID\" : \"//@tosca_nodes_root.244\" } ] }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.246\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.247\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.247\", \"id\" : 0, \"jurisdiction\" : \"US\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.246\" } ], \"usageCostPerDay\" : 0.552, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.248\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.20\" }, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.241\" } ], \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.249\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false }, { \"type\" : \"DataController\", \"@id\" : \"//@tosca_nodes_root.249\", \"id\" : 0, \"location\" : \"BE\", \"controls\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.248\" } ] }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.250\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.251\" }, { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.252\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.251\", \"id\" : 0, \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.250\" } ], \"usageCostPerDay\" : 0.552, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.252\", \"id\" : 0, \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.250\" } ], \"usageCostPerDay\" : 2.2272, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.253\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.254\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.254\", \"id\" : 0, \"hosts\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.255\" } ], \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.253\" } ], \"usageCostPerDay\" : 1.1136, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.255\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.254\" }, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.262\" } ], \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.256\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false }, { \"type\" : \"DataController\", \"@id\" : \"//@tosca_nodes_root.256\", \"id\" : 0, \"location\" : \"BE\", \"controls\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.255\" }, { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.262\" } ] }, { \"type\" : \"SaaSOperator\", \"@id\" : \"//@tosca_nodes_root.257\", \"id\" : 0, \"operates\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.259\" } ] }, { \"type\" : \"DataFlow\", \"@id\" : \"//@tosca_nodes_root.258\", \"id\" : 0, \"consistsOf\" : [ { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.260\" } ], \"streamsTo\" : [ { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.261\" } ], \"disab\" : false, \"amountOfDataInGB\" : 0.0 }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.259\", \"id\" : 0, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.262\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false, \"operatedBySaaSOperator\" : { \"type\" : \"SaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.257\" } }, { \"type\" : \"Record\", \"@id\" : \"//@tosca_nodes_root.260\", \"id\" : 0, \"partOf\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.266\" }, { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"//@tosca_nodes_root.258\" } ], \"sensitive\" : true, \"encrypted\" : true, \"belongsTo\" : { \"type\" : \"DataSubject\", \"referencedObjectID\" : \"//@tosca_nodes_root.0\" } }, { \"type\" : \"Database\", \"@id\" : \"//@tosca_nodes_root.261\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.262\" }, \"stores\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.266\" } ], \"streamsFrom\" : [ { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"//@tosca_nodes_root.258\" } ], \"encrypted\" : false }, { \"type\" : \"DBMS\", \"@id\" : \"//@tosca_nodes_root.262\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.263\" }, \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.256\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false, \"hosts\" : [ { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.261\" } ], \"accessedBySoftwareComponent\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.269\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.259\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.255\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.263\", \"id\" : 0, \"hosts\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.262\" } ], \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.264\" } ], \"usageCostPerDay\" : 1.1136, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.264\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.263\" } ] }, { \"type\" : \"DataProcessor\", \"@id\" : \"//@tosca_nodes_root.265\", \"id\" : 0, \"location\" : \"BE\", \"accesses\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.266\" } ] }, { \"type\" : \"StoredDataSet\", \"@id\" : \"//@tosca_nodes_root.266\", \"id\" : 0, \"consistsOf\" : [ { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.260\" } ], \"storedIn\" : { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.261\" }, \"accessedBy\" : [ { \"type\" : \"DataProcessor\", \"referencedObjectID\" : \"//@tosca_nodes_root.265\" } ] }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.267\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.268\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.268\", \"id\" : 0, \"jurisdiction\" : \"US\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.267\" } ], \"usageCostPerDay\" : 0.552, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.269\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.20\" }, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.262\" } ], \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.270\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false }, { \"type\" : \"DataController\", \"@id\" : \"//@tosca_nodes_root.270\", \"id\" : 0, \"location\" : \"BE\", \"controls\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.269\" } ] }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.271\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.272\" }, { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.273\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.272\", \"id\" : 0, \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.271\" } ], \"usageCostPerDay\" : 0.552, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.273\", \"id\" : 0, \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.271\" } ], \"usageCostPerDay\" : 2.2272, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.274\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.275\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.275\", \"id\" : 0, \"hosts\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.276\" } ], \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.274\" } ], \"usageCostPerDay\" : 1.1136, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.276\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.275\" }, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.283\" } ], \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.277\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false }, { \"type\" : \"DataController\", \"@id\" : \"//@tosca_nodes_root.277\", \"id\" : 0, \"location\" : \"BE\", \"controls\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.276\" }, { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.283\" } ] }, { \"type\" : \"SaaSOperator\", \"@id\" : \"//@tosca_nodes_root.278\", \"id\" : 0, \"operates\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.280\" } ] }, { \"type\" : \"DataFlow\", \"@id\" : \"//@tosca_nodes_root.279\", \"id\" : 0, \"consistsOf\" : [ { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.281\" } ], \"streamsTo\" : [ { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.282\" } ], \"disab\" : false, \"amountOfDataInGB\" : 0.0 }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.280\", \"id\" : 0, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.283\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false, \"operatedBySaaSOperator\" : { \"type\" : \"SaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.278\" } }, { \"type\" : \"Record\", \"@id\" : \"//@tosca_nodes_root.281\", \"id\" : 0, \"partOf\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.287\" }, { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"//@tosca_nodes_root.279\" } ], \"sensitive\" : true, \"encrypted\" : true, \"belongsTo\" : { \"type\" : \"DataSubject\", \"referencedObjectID\" : \"//@tosca_nodes_root.0\" } }, { \"type\" : \"Database\", \"@id\" : \"//@tosca_nodes_root.282\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.283\" }, \"stores\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.287\" } ], \"streamsFrom\" : [ { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"//@tosca_nodes_root.279\" } ], \"encrypted\" : false }, { \"type\" : \"DBMS\", \"@id\" : \"//@tosca_nodes_root.283\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.284\" }, \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.277\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false, \"hosts\" : [ { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.282\" } ], \"accessedBySoftwareComponent\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.290\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.280\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.276\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.284\", \"id\" : 0, \"hosts\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.283\" } ], \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.285\" } ], \"usageCostPerDay\" : 1.1136, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.285\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.284\" } ] }, { \"type\" : \"DataProcessor\", \"@id\" : \"//@tosca_nodes_root.286\", \"id\" : 0, \"location\" : \"BE\", \"accesses\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.287\" } ] }, { \"type\" : \"StoredDataSet\", \"@id\" : \"//@tosca_nodes_root.287\", \"id\" : 0, \"consistsOf\" : [ { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.281\" } ], \"storedIn\" : { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.282\" }, \"accessedBy\" : [ { \"type\" : \"DataProcessor\", \"referencedObjectID\" : \"//@tosca_nodes_root.286\" } ] }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.288\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.289\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.289\", \"id\" : 0, \"jurisdiction\" : \"US\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.288\" } ], \"usageCostPerDay\" : 0.552, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.290\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.20\" }, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.283\" } ], \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.291\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false }, { \"type\" : \"DataController\", \"@id\" : \"//@tosca_nodes_root.291\", \"id\" : 0, \"location\" : \"BE\", \"controls\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.290\" } ] }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.292\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.293\" }, { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.294\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.293\", \"id\" : 0, \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.292\" } ], \"usageCostPerDay\" : 0.552, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.294\", \"id\" : 0, \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.292\" } ], \"usageCostPerDay\" : 2.2272, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.295\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.296\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.296\", \"id\" : 0, \"hosts\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.297\" } ], \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.295\" } ], \"usageCostPerDay\" : 1.1136, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.297\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.296\" }, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.304\" } ], \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.298\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false }, { \"type\" : \"DataController\", \"@id\" : \"//@tosca_nodes_root.298\", \"id\" : 0, \"location\" : \"BE\", \"controls\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.297\" }, { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.304\" } ] }, { \"type\" : \"SaaSOperator\", \"@id\" : \"//@tosca_nodes_root.299\", \"id\" : 0, \"operates\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.301\" } ] }, { \"type\" : \"DataFlow\", \"@id\" : \"//@tosca_nodes_root.300\", \"id\" : 0, \"consistsOf\" : [ { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.302\" } ], \"streamsTo\" : [ { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.303\" } ], \"disab\" : false, \"amountOfDataInGB\" : 0.0 }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.301\", \"id\" : 0, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.304\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false, \"operatedBySaaSOperator\" : { \"type\" : \"SaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.299\" } }, { \"type\" : \"Record\", \"@id\" : \"//@tosca_nodes_root.302\", \"id\" : 0, \"partOf\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.308\" }, { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"//@tosca_nodes_root.300\" } ], \"sensitive\" : true, \"encrypted\" : true, \"belongsTo\" : { \"type\" : \"DataSubject\", \"referencedObjectID\" : \"//@tosca_nodes_root.0\" } }, { \"type\" : \"Database\", \"@id\" : \"//@tosca_nodes_root.303\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.304\" }, \"stores\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.308\" } ], \"streamsFrom\" : [ { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"//@tosca_nodes_root.300\" } ], \"encrypted\" : false }, { \"type\" : \"DBMS\", \"@id\" : \"//@tosca_nodes_root.304\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.305\" }, \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.298\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false, \"hosts\" : [ { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.303\" } ], \"accessedBySoftwareComponent\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.311\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.301\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.297\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.305\", \"id\" : 0, \"hosts\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.304\" } ], \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.306\" } ], \"usageCostPerDay\" : 1.1136, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.306\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.305\" } ] }, { \"type\" : \"DataProcessor\", \"@id\" : \"//@tosca_nodes_root.307\", \"id\" : 0, \"location\" : \"BE\", \"accesses\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.308\" } ] }, { \"type\" : \"StoredDataSet\", \"@id\" : \"//@tosca_nodes_root.308\", \"id\" : 0, \"consistsOf\" : [ { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.302\" } ], \"storedIn\" : { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.303\" }, \"accessedBy\" : [ { \"type\" : \"DataProcessor\", \"referencedObjectID\" : \"//@tosca_nodes_root.307\" } ] }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.309\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.310\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.310\", \"id\" : 0, \"jurisdiction\" : \"US\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.309\" } ], \"usageCostPerDay\" : 0.552, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.311\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.20\" }, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.304\" } ], \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.312\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false }, { \"type\" : \"DataController\", \"@id\" : \"//@tosca_nodes_root.312\", \"id\" : 0, \"location\" : \"BE\", \"controls\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.311\" } ] }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.313\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.314\" }, { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.315\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.314\", \"id\" : 0, \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.313\" } ], \"usageCostPerDay\" : 0.552, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.315\", \"id\" : 0, \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.313\" } ], \"usageCostPerDay\" : 2.2272, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.316\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.317\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.317\", \"id\" : 0, \"hosts\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.318\" } ], \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.316\" } ], \"usageCostPerDay\" : 1.1136, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.318\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.317\" }, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.325\" } ], \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.319\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false }, { \"type\" : \"DataController\", \"@id\" : \"//@tosca_nodes_root.319\", \"id\" : 0, \"location\" : \"BE\", \"controls\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.318\" }, { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.325\" } ] }, { \"type\" : \"SaaSOperator\", \"@id\" : \"//@tosca_nodes_root.320\", \"id\" : 0, \"operates\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.322\" } ] }, { \"type\" : \"DataFlow\", \"@id\" : \"//@tosca_nodes_root.321\", \"id\" : 0, \"consistsOf\" : [ { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.323\" } ], \"streamsTo\" : [ { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.324\" } ], \"disab\" : false, \"amountOfDataInGB\" : 0.0 }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.322\", \"id\" : 0, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.325\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false, \"operatedBySaaSOperator\" : { \"type\" : \"SaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.320\" } }, { \"type\" : \"Record\", \"@id\" : \"//@tosca_nodes_root.323\", \"id\" : 0, \"partOf\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.329\" }, { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"//@tosca_nodes_root.321\" } ], \"sensitive\" : true, \"encrypted\" : true, \"belongsTo\" : { \"type\" : \"DataSubject\", \"referencedObjectID\" : \"//@tosca_nodes_root.0\" } }, { \"type\" : \"Database\", \"@id\" : \"//@tosca_nodes_root.324\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.325\" }, \"stores\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.329\" } ], \"streamsFrom\" : [ { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"//@tosca_nodes_root.321\" } ], \"encrypted\" : false }, { \"type\" : \"DBMS\", \"@id\" : \"//@tosca_nodes_root.325\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.326\" }, \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.319\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false, \"hosts\" : [ { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.324\" } ], \"accessedBySoftwareComponent\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.332\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.322\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.318\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.326\", \"id\" : 0, \"hosts\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.325\" } ], \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.327\" } ], \"usageCostPerDay\" : 1.1136, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.327\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.326\" } ] }, { \"type\" : \"DataProcessor\", \"@id\" : \"//@tosca_nodes_root.328\", \"id\" : 0, \"location\" : \"BE\", \"accesses\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"//@tosca_nodes_root.329\" } ] }, { \"type\" : \"StoredDataSet\", \"@id\" : \"//@tosca_nodes_root.329\", \"id\" : 0, \"consistsOf\" : [ { \"type\" : \"Record\", \"referencedObjectID\" : \"//@tosca_nodes_root.323\" } ], \"storedIn\" : { \"type\" : \"Database\", \"referencedObjectID\" : \"//@tosca_nodes_root.324\" }, \"accessedBy\" : [ { \"type\" : \"DataProcessor\", \"referencedObjectID\" : \"//@tosca_nodes_root.328\" } ] }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.330\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.331\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.331\", \"id\" : 0, \"jurisdiction\" : \"US\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.330\" } ], \"usageCostPerDay\" : 0.552, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"//@tosca_nodes_root.332\", \"id\" : 0, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.20\" }, \"accessDBMS\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"//@tosca_nodes_root.325\" } ], \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"//@tosca_nodes_root.333\" } ], \"neededCapacity\" : 0, \"hasToBeDeployedOnFogNode\" : false }, { \"type\" : \"DataController\", \"@id\" : \"//@tosca_nodes_root.333\", \"id\" : 0, \"location\" : \"BE\", \"controls\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"//@tosca_nodes_root.332\" } ] }, { \"type\" : \"IaaSOperator\", \"@id\" : \"//@tosca_nodes_root.334\", \"id\" : 0, \"operatesCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.335\" }, { \"type\" : \"Compute\", \"referencedObjectID\" : \"//@tosca_nodes_root.336\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.335\", \"id\" : 0, \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.334\" } ], \"usageCostPerDay\" : 0.552, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false }, { \"type\" : \"Compute\", \"@id\" : \"//@tosca_nodes_root.336\", \"id\" : 0, \"jurisdiction\" : \"EU\", \"operatedByIaaSOperator\" : [ { \"type\" : \"IaaSOperator\", \"referencedObjectID\" : \"//@tosca_nodes_root.334\" } ], \"usageCostPerDay\" : 2.2272, \"capacity\" : 0, \"transferCostPerGB\" : 0.0, \"costIncurred\" : false } ] }");
			}  h (IOException e1) {
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
			// 
				// Auto-generated catch block
				e printStackTrace();
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
			String[] data1 = {String.valueOf((double)(timestamp1 - timestampZERO) / 1_000_000_000.0) ,"337", "0", "48", "36.1872"};
			writeDataLineByLine(insertNumberBeforeFileEnding(filepathToNewTest, maxSeconds), data1);

			int amountOfPCPInstanceInjections = 0;
			//weniger events, damit mehr Ruhepausen sind
			for (int i = 1; i <= 5; i++) {
				cloudEnvironment = (CloudEnvironment) loader.getCloudEnvironmentsMonitored().get(1L);
				double probability = Math.random();
				if(probability>=0.80) {
					amountOfPCPInstanceInjections = ThreadLocalRandom.current().nextInt(15, 21);
				}else {
					amountOfPCPInstanceInjections = ThreadLocalRandom.current().nextInt(1, 6);
				}

				//Inject random PCP
				String[] rulesToInject = {"CreateUnauthorizedAccess" , "StoreDataOutsideEU"};
				for(int j = 0; j<= amountOfPCPInstanceInjections; j++) {
					//Bei wenigen PCP Instanzen erklären, dass es dann sehr schnell geht
					int ruleToInject = ThreadLocalRandom.current().nextInt(0, 2);
					injectPCPInstances(rulesToInject[ruleToInject]);
				}
				datas = riskFinder.startRadar(id, algorithm, String.valueOf(337), i,
						maxSeconds, cloudEnvironment, timestampZERO);
				for (String[] data : datas) {
					writeDataLineByLine(insertNumberBeforeFileEnding(filepathToNewTest, maxSeconds), data);
				}
					try {
						if(!datas.getLast()[2].equals("0")) {
							Thread.sleep(5000);
							cloudEnvironment = (CloudEnvironment) loader.getCloudEnvironmentsMonitored().get(1L);
							datas = riskFinder.startRadar(id, algorithm, String.valueOf(337), i,
									maxSeconds, cloudEnvironment, timestampZERO);
							for (String[] data : datas) {
								writeDataLineByLine(insertNumberBeforeFileEnding(filepathToNewTest, maxSeconds), data);
							}
						}
						//Nur schlafen, wenn keine PCP Instanzen vorhanden sind!
						int timeToSleep = ThreadLocalRandom.current().nextInt(120000, 180000 + 1);
						Thread.sleep(timeToSleep);
						long timestamp = System.nanoTime();
						String[] data = {String.valueOf((double)(timestamp - timestampZERO) / 1_000_000_000.0) ,datas.getLast()[1], datas.getLast()[2], datas.getLast()[3], datas.getLast()[4]};
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
//			System.out.println("Loaded model after registration event as:\n" + runtimeModelLogic.returnentiremodelasjson(cloudEnvironment));
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
//		String model = "{ \"type\":\"CloudEnvironment\", \"@id\":\"/\", \"tosca_nodes_root\":[ { \"type\":\"Compute\", \"@id\":\"1\", \"name\":\"DBServer_1\", \"id\":1, \"hosts\":[ { \"type\":\"DBMS\", \"referencedObjectID\":\"8\" }, { \"type\":\"SoftwareComponent\", \"referencedObjectID\":\"35\" } ], \"connectedToNetwork\":[ { \"type\":\"Network_Network\", \"referencedObjectID\":\"2\" } ], \"jurisdiction\":\"DE\" }, { \"type\":\"Network_Network\", \"@id\":\"2\", \"name\":\"DataLAN\", \"id\":2, \"connectedToCompute\":[ { \"type\":\"Compute\", \"referencedObjectID\":\"1\" }, { \"type\":\"Compute\", \"referencedObjectID\":\"17\" }, { \"type\":\"Compute\", \"referencedObjectID\":\"32\" } ], \"connectedTo\":{ \"type\":\"Network_Network\", \"referencedObjectID\":\"4\" } }, { \"type\":\"Compute\", \"@id\":\"3\", \"name\":\"QueryServer\", \"id\":3, \"hosts\":[ { \"type\":\"Gateway\", \"referencedObjectID\":\"16\" } ], \"connectedToNetwork\":[ { \"type\":\"Network_Network\", \"referencedObjectID\":\"4\" } ], \"jurisdiction\":\"DE\" }, { \"type\":\"Network_Network\", \"@id\":\"4\", \"name\":\"ServiceLAN\", \"id\":4, \"connectedToCompute\":[ { \"type\":\"Compute\", \"referencedObjectID\":\"3\" }, { \"type\":\"Compute\", \"referencedObjectID\":\"5\" } ], \"connectedToOpposite\":{ \"type\":\"Network_Network\", \"referencedObjectID\":\"2\" } }, { \"type\":\"Compute\", \"@id\":\"5\", \"name\":\"KGPortalServer\", \"id\":5, \"hosts\":[ { \"type\":\"WebServer\", \"referencedObjectID\":\"6\" } ], \"connectedToNetwork\":[ { \"type\":\"Network_Network\", \"referencedObjectID\":\"4\" } ], \"jurisdiction\":\"DE\" }, { \"type\":\"WebServer\", \"@id\":\"6\", \"name\":\"Webserver-KGPortalServer\", \"id\":6, \"hostedOn\":{ \"type\":\"Compute\", \"referencedObjectID\":\"5\" }, \"hosts\":[ { \"type\":\"WebApplication\", \"referencedObjectID\":\"7\" } ] }, { \"type\":\"WebApplication\", \"@id\":\"7\", \"name\":\"KnowGoPortal\", \"id\":7, \"hostedOn\":{ \"type\":\"WebServer\", \"referencedObjectID\":\"6\" }, \"usedBySoftwareComponent\":[ { \"type\":\"SoftwareComponent\", \"referencedObjectID\":\"14\" } ], \"usesGateway\":[ { \"type\":\"Gateway\", \"referencedObjectID\":\"16\" } ], \"usesSoftwareComponent\":[ { \"type\":\"SoftwareComponent\", \"referencedObjectID\":\"35\" }, { \"type\":\"SoftwareComponent\", \"referencedObjectID\":\"36\" } ] }, { \"type\":\"DBMS\", \"@id\":\"8\", \"name\":\"DBMS-DBServer_1\", \"id\":8, \"hostedOn\":{ \"type\":\"Compute\", \"referencedObjectID\":\"1\" }, \"hosts\":[ { \"type\":\"Database\", \"referencedObjectID\":\"9\" } ], \"uses\":[ { \"type\":\"Gateway\", \"referencedObjectID\":\"16\" } ] }, { \"type\":\"Database\", \"@id\":\"9\", \"name\":\"KnowGoDB_1\", \"id\":9, \"hostedOn\":{ \"type\":\"DBMS\", \"referencedObjectID\":\"8\" }, \"stores\":[ { \"type\":\"StoredDataSet\", \"referencedObjectID\":\"11\" }, { \"type\":\"StoredDataSet\", \"referencedObjectID\":\"21\" } ], \"streamsFrom\":[ { \"type\":\"DataFlow\", \"referencedObjectID\":\"23\" }, { \"type\":\"DataFlow\", \"referencedObjectID\":\"22\" } ] }, { \"type\":\"DataSubject\", \"@id\":\"10\", \"name\":\"Driver\", \"id\":10, \"location\":\"DE\", \"owns\":[ { \"type\":\"StoredDataSet\", \"referencedObjectID\":\"11\" }, { \"type\":\"DataFlow\", \"referencedObjectID\":\"22\" }, { \"type\":\"DataFlow\", \"referencedObjectID\":\"23\" }, { \"type\":\"StoredDataSet\", \"referencedObjectID\":\"21\" }, { \"type\":\"StoredDataSet\", \"referencedObjectID\":\"33\" }, { \"type\":\"StoredDataSet\", \"referencedObjectID\":\"24\" } ], \"manages\":[ { \"type\":\"MobileHost\", \"referencedObjectID\":\"26\" }, { \"type\":\"MobileHost\", \"referencedObjectID\":\"27\" } ] }, { \"type\":\"StoredDataSet\", \"@id\":\"11\", \"name\":\"DataSet-KnowGoDB-BiometricData\", \"id\":11, \"consistsOf\":[ { \"type\":\"Record\", \"referencedObjectID\":\"12\" } ], \"belongsTo\":{ \"type\":\"DataSubject\", \"referencedObjectID\":\"10\" }, \"storedIn\":{ \"type\":\"Database\", \"referencedObjectID\":\"9\" } }, { \"type\":\"Record\", \"@id\":\"12\", \"name\":\"BiometricData\", \"id\":12, \"partOf\":[ { \"type\":\"StoredDataSet\", \"referencedObjectID\":\"11\" }, { \"type\":\"DataFlow\", \"referencedObjectID\":\"22\" } ], \"sensitive\":true }, { \"type\":\"Record\", \"@id\":\"13\", \"name\":\"CarData\", \"id\":13, \"partOf\":[ { \"type\":\"StoredDataSet\", \"referencedObjectID\":\"21\" }, { \"type\":\"DataFlow\", \"referencedObjectID\":\"23\" } ] }, { \"type\":\"SoftwareComponent\", \"@id\":\"14\", \"name\":\"AccessBrowser\", \"id\":14, \"hostedOn\":{ \"type\":\"Compute\", \"referencedObjectID\":\"15\" }, \"controlledBy\":[ { \"type\":\"DataController\", \"referencedObjectID\":\"20\" } ], \"usesWebApplication\":[ { \"type\":\"WebApplication\", \"referencedObjectID\":\"7\" } ] }, { \"type\":\"Compute\", \"@id\":\"15\", \"name\":\"AccessPC\", \"id\":15, \"hosts\":[ { \"type\":\"SoftwareComponent\", \"referencedObjectID\":\"14\" } ], \"jurisdiction\":\"CH\" }, { \"type\":\"Gateway\", \"@id\":\"16\", \"name\":\"QueryGW\", \"id\":16, \"hostedOn\":{ \"type\":\"Compute\", \"referencedObjectID\":\"3\" }, \"usedByWebApplication\":[ { \"type\":\"WebApplication\", \"referencedObjectID\":\"7\" } ], \"usedBy\":[ { \"type\":\"DBMS\", \"referencedObjectID\":\"8\" }, { \"type\":\"DBMS\", \"referencedObjectID\":\"18\" } ], \"usedByGateway\":[ { \"type\":\"Gateway\", \"referencedObjectID\":\"31\" } ] }, { \"type\":\"Compute\", \"@id\":\"17\", \"name\":\"DBServer_2\", \"id\":17, \"hosts\":[ { \"type\":\"DBMS\", \"referencedObjectID\":\"18\" }, { \"type\":\"SoftwareComponent\", \"referencedObjectID\":\"36\" } ], \"connectedToNetwork\":[ { \"type\":\"Network_Network\", \"referencedObjectID\":\"2\" } ], \"jurisdiction\":\"AT\" }, { \"type\":\"DBMS\", \"@id\":\"18\", \"name\":\"DBMS-DBServer_2\", \"id\":18, \"hostedOn\":{ \"type\":\"Compute\", \"referencedObjectID\":\"17\" }, \"hosts\":[ { \"type\":\"Database\", \"referencedObjectID\":\"19\" } ], \"uses\":[ { \"type\":\"Gateway\", \"referencedObjectID\":\"16\" } ] }, { \"type\":\"Database\", \"@id\":\"19\", \"name\":\"KnowGoDB_2\", \"id\":19, \"hostedOn\":{ \"type\":\"DBMS\", \"referencedObjectID\":\"18\" } }, { \"type\":\"DataController\", \"@id\":\"20\", \"name\":\"AccessingParty\", \"id\":20, \"location\":\"CH\", \"controls\":[ { \"type\":\"SoftwareComponent\", \"referencedObjectID\":\"14\" } ] }, { \"type\":\"StoredDataSet\", \"@id\":\"21\", \"name\":\"DataSet-KnowGoDB-CarData\", \"id\":21, \"consistsOf\":[ { \"type\":\"Record\", \"referencedObjectID\":\"13\" } ], \"belongsTo\":{ \"type\":\"DataSubject\", \"referencedObjectID\":\"10\" }, \"storedIn\":{ \"type\":\"Database\", \"referencedObjectID\":\"9\" } }, { \"type\":\"DataFlow\", \"@id\":\"22\", \"name\":\"DataFlow-KnowGoDB-BiometricData\", \"id\":22, \"consistsOf\":[ { \"type\":\"Record\", \"referencedObjectID\":\"12\" } ], \"belongsTo\":{ \"type\":\"DataSubject\", \"referencedObjectID\":\"10\" }, \"streamsTo\":[ { \"type\":\"Database\", \"referencedObjectID\":\"9\" } ] }, { \"type\":\"DataFlow\", \"@id\":\"23\", \"name\":\"DataFlow-KnowGoDB-CarData\", \"id\":23, \"consistsOf\":[ { \"type\":\"Record\", \"referencedObjectID\":\"13\" } ], \"belongsTo\":{ \"type\":\"DataSubject\", \"referencedObjectID\":\"10\" }, \"streamsTo\":[ { \"type\":\"Database\", \"referencedObjectID\":\"9\" } ] }, { \"type\":\"StoredDataSet\", \"@id\":\"24\", \"name\":\"DataSet-KnowGoDB-SensorData\", \"id\":24, \"consistsOf\":[ { \"type\":\"Record\", \"referencedObjectID\":\"25\" } ], \"belongsTo\":{ \"type\":\"DataSubject\", \"referencedObjectID\":\"10\" }, \"storedInMobileHost\":{ \"type\":\"MobileHost\", \"referencedObjectID\":\"26\" }, \"createdBy\":{ \"type\":\"MobileHost\", \"referencedObjectID\":\"27\" } }, { \"type\":\"Record\", \"@id\":\"25\", \"name\":\"SensorData\", \"id\":25, \"partOf\":[ { \"type\":\"StoredDataSet\", \"referencedObjectID\":\"24\" } ], \"sensitive\":true }, { \"type\":\"MobileHost\", \"@id\":\"26\", \"name\":\"DriverPhone\", \"id\":26, \"jurisdiction\":\"DE\", \"managedBy\":{ \"type\":\"DataSubject\", \"referencedObjectID\":\"10\" }, \"hostsContainerApplication\":[ { \"type\":\"Container_Application\", \"referencedObjectID\":\"28\" } ], \"stores\":{ \"type\":\"StoredDataSet\", \"referencedObjectID\":\"24\" } }, { \"type\":\"MobileHost\", \"@id\":\"27\", \"name\":\"Car\", \"id\":27, \"jurisdiction\":\"DE\", \"managedBy\":{ \"type\":\"DataSubject\", \"referencedObjectID\":\"10\" }, \"hostsContainerApplication\":[ { \"type\":\"Container_Application\", \"referencedObjectID\":\"29\" } ], \"creates\":[ { \"type\":\"StoredDataSet\", \"referencedObjectID\":\"24\" } ] }, { \"type\":\"Container_Application\", \"@id\":\"28\", \"name\":\"KnowGoApp\", \"id\":28, \"hostedBy\":{ \"type\":\"MobileHost\", \"referencedObjectID\":\"26\" }, \"sendsThrough\":{ \"type\":\"DataFlow\", \"referencedObjectID\":\"30\" }, \"usedBy\":[ { \"type\":\"Container_Application\", \"referencedObjectID\":\"29\" } ] }, { \"type\":\"Container_Application\", \"@id\":\"29\", \"name\":\"CarDataMarshall\", \"id\":29, \"hostedBy\":{ \"type\":\"MobileHost\", \"referencedObjectID\":\"27\" }, \"uses\":[ { \"type\":\"Container_Application\", \"referencedObjectID\":\"28\" } ] }, { \"type\":\"DataFlow\", \"@id\":\"30\", \"name\":\"DataFlow-KnowGoApp-DataGW\", \"id\":30, \"sendsFrom\":{ \"type\":\"Container_Application\", \"referencedObjectID\":\"28\" }, \"sendsTo\":{ \"type\":\"Gateway\", \"referencedObjectID\":\"31\" } }, { \"type\":\"Gateway\", \"@id\":\"31\", \"name\":\"DataGW\", \"id\":31, \"hostedOn\":{ \"type\":\"Compute\", \"referencedObjectID\":\"32\" }, \"receivesFrom\":[ { \"type\":\"DataFlow\", \"referencedObjectID\":\"30\" } ], \"usesGateway\":[ { \"type\":\"Gateway\", \"referencedObjectID\":\"16\" } ] }, { \"type\":\"Compute\", \"@id\":\"32\", \"name\":\"DataGWServer\", \"id\":32, \"hosts\":[ { \"type\":\"Gateway\", \"referencedObjectID\":\"31\" } ], \"connectedToNetwork\":[ { \"type\":\"Network_Network\", \"referencedObjectID\":\"2\" } ], \"jurisdiction\":\"DE\" }, { \"type\":\"StoredDataSet\", \"@id\":\"33\", \"name\":\"DataSet-KGAnalytics-RiskData\", \"id\":33, \"consistsOf\":[ { \"type\":\"Record\", \"referencedObjectID\":\"34\" } ], \"belongsTo\":{ \"type\":\"DataSubject\", \"referencedObjectID\":\"10\" }, \"createdBySoftwareComponent\":{ \"type\":\"SoftwareComponent\", \"referencedObjectID\":\"35\" } }, { \"type\":\"Record\", \"@id\":\"34\", \"name\":\"RiskData\", \"id\":34, \"partOf\":[ { \"type\":\"StoredDataSet\", \"referencedObjectID\":\"33\" } ] }, { \"type\":\"SoftwareComponent\", \"@id\":\"36\", \"name\":\"KGAnalytics_2\", \"id\":36, \"hostedOn\":{ \"type\":\"Compute\", \"referencedObjectID\":\"17\" }, \"usedByWebApp\":[ { \"type\":\"WebApplication\", \"referencedObjectID\":\"7\" } ] }, { \"type\":\"SoftwareComponent\", \"@id\":\"35\", \"name\":\"KGAnalytics_1\", \"id\":35, \"hostedOn\":{ \"type\":\"Compute\", \"referencedObjectID\":\"1\" }, \"creates\":[ { \"type\":\"StoredDataSet\", \"referencedObjectID\":\"33\" } ], \"usedByWebApp\":[ { \"type\":\"WebApplication\", \"referencedObjectID\":\"7\" } ] } ] }";
		String model = "{ \"type\" : \"CloudEnvironment\", \"@id\" : \"/\", \"tosca_nodes_root\" : [ { \"type\" : \"Compute\", \"@id\" : \"1\", \"name\" : \"DBServer_1\", \"id\" : 1, \"hosts\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"8\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"35\" } ], \"connectedToNetwork\" : [ { \"type\" : \"Network_Network\", \"referencedObjectID\" : \"2\" } ], \"jurisdiction\" : \"DE\" }, { \"type\" : \"Network_Network\", \"@id\" : \"2\", \"name\" : \"DataLAN\", \"id\" : 2, \"connectedToCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"1\" }, { \"type\" : \"Compute\", \"referencedObjectID\" : \"17\" }, { \"type\" : \"Compute\", \"referencedObjectID\" : \"32\" } ], \"connectedTo\" : { \"type\" : \"Network_Network\", \"referencedObjectID\" : \"4\" } }, { \"type\" : \"Compute\", \"@id\" : \"3\", \"name\" : \"QueryServer\", \"id\" : 3, \"hosts\" : [ { \"type\" : \"Gateway\", \"referencedObjectID\" : \"16\" } ], \"connectedToNetwork\" : [ { \"type\" : \"Network_Network\", \"referencedObjectID\" : \"4\" } ], \"jurisdiction\" : \"DE\" }, { \"type\" : \"Network_Network\", \"@id\" : \"4\", \"name\" : \"ServiceLAN\", \"id\" : 4, \"connectedToCompute\" : [ { \"type\" : \"Compute\", \"referencedObjectID\" : \"3\" }, { \"type\" : \"Compute\", \"referencedObjectID\" : \"5\" } ], \"connectedToOpposite\" : { \"type\" : \"Network_Network\", \"referencedObjectID\" : \"2\" } }, { \"type\" : \"Compute\", \"@id\" : \"5\", \"name\" : \"KGPortalServer\", \"id\" : 5, \"hosts\" : [ { \"type\" : \"WebServer\", \"referencedObjectID\" : \"6\" } ], \"connectedToNetwork\" : [ { \"type\" : \"Network_Network\", \"referencedObjectID\" : \"4\" } ], \"jurisdiction\" : \"DE\" }, { \"type\" : \"WebServer\", \"@id\" : \"6\", \"name\" : \"Webserver-KGPortalServer\", \"id\" : 6, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"5\" }, \"hosts\" : [ { \"type\" : \"WebApplication\", \"referencedObjectID\" : \"7\" } ] }, { \"type\" : \"WebApplication\", \"@id\" : \"7\", \"name\" : \"KnowGoPortal\", \"id\" : 7, \"hostedOn\" : { \"type\" : \"WebServer\", \"referencedObjectID\" : \"6\" }, \"usedBySoftwareComponent\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"14\" } ], \"usesGateway\" : [ { \"type\" : \"Gateway\", \"referencedObjectID\" : \"16\" } ], \"usesSoftwareComponent\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"35\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"36\" } ] }, { \"type\" : \"DBMS\", \"@id\" : \"8\", \"name\" : \"DBMS-DBServer_1\", \"id\" : 8, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"1\" }, \"hosts\" : [ { \"type\" : \"Database\", \"referencedObjectID\" : \"9\" } ], \"uses\" : [ { \"type\" : \"Gateway\", \"referencedObjectID\" : \"16\" } ] }, { \"type\" : \"Database\", \"@id\" : \"9\", \"name\" : \"KnowGoDB_1\", \"id\" : 9, \"hostedOn\" : { \"type\" : \"DBMS\", \"referencedObjectID\" : \"8\" }, \"stores\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"11\" }, { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"21\" } ], \"streamsFrom\" : [ { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"23\" }, { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"22\" } ] }, { \"type\" : \"DataSubject\", \"@id\" : \"10\", \"name\" : \"Driver\", \"id\" : 10, \"location\" : \"DE\", \"owns\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"11\" }, { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"22\" }, { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"23\" }, { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"21\" }, { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"33\" }, { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"24\" } ], \"manages\" : [ { \"type\" : \"MobileHost\", \"referencedObjectID\" : \"26\" }, { \"type\" : \"MobileHost\", \"referencedObjectID\" : \"27\" } ] }, { \"type\" : \"StoredDataSet\", \"@id\" : \"11\", \"name\" : \"DataSet-KnowGoDB-BiometricData_1\", \"id\" : 11, \"consistsOf\" : [ { \"type\" : \"Record\", \"referencedObjectID\" : \"12\" } ], \"belongsTo\" : { \"type\" : \"DataSubject\", \"referencedObjectID\" : \"10\" }, \"storedIn\" : { \"type\" : \"Database\", \"referencedObjectID\" : \"9\" } }, { \"type\" : \"Record\", \"@id\" : \"12\", \"name\" : \"BiometricData_1\", \"id\" : 12, \"partOf\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"11\" }, { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"22\" } ], \"sensitive\" : true }, { \"type\" : \"Record\", \"@id\" : \"13\", \"name\" : \"CarData_1\", \"id\" : 13, \"partOf\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"21\" }, { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"23\" } ] }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"14\", \"name\" : \"AccessBrowser\", \"id\" : 14, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"15\" }, \"controlledBy\" : [ { \"type\" : \"DataController\", \"referencedObjectID\" : \"20\" } ], \"usesWebApplication\" : [ { \"type\" : \"WebApplication\", \"referencedObjectID\" : \"7\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"15\", \"name\" : \"AccessPC\", \"id\" : 15, \"hosts\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"14\" } ], \"jurisdiction\" : \"CH\" }, { \"type\" : \"Gateway\", \"@id\" : \"16\", \"name\" : \"QueryGW\", \"id\" : 16, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"3\" }, \"usedByWebApplication\" : [ { \"type\" : \"WebApplication\", \"referencedObjectID\" : \"7\" } ], \"usedBy\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"8\" }, { \"type\" : \"DBMS\", \"referencedObjectID\" : \"18\" } ], \"usedByGateway\" : [ { \"type\" : \"Gateway\", \"referencedObjectID\" : \"31\" } ], \"gatewayUsedBySoftwareComponent\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"36\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"35\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"17\", \"name\" : \"DBServer_2\", \"id\" : 17, \"hosts\" : [ { \"type\" : \"DBMS\", \"referencedObjectID\" : \"18\" }, { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"36\" } ], \"connectedToNetwork\" : [ { \"type\" : \"Network_Network\", \"referencedObjectID\" : \"2\" } ], \"jurisdiction\" : \"AT\" }, { \"type\" : \"DBMS\", \"@id\" : \"18\", \"name\" : \"DBMS-DBServer_2\", \"id\" : 18, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"17\" }, \"hosts\" : [ { \"type\" : \"Database\", \"referencedObjectID\" : \"19\" } ], \"uses\" : [ { \"type\" : \"Gateway\", \"referencedObjectID\" : \"16\" } ] }, { \"type\" : \"Database\", \"@id\" : \"19\", \"name\" : \"KnowGoDB_2\", \"id\" : 19, \"hostedOn\" : { \"type\" : \"DBMS\", \"referencedObjectID\" : \"18\" } }, { \"type\" : \"DataController\", \"@id\" : \"20\", \"name\" : \"AccessingParty\", \"id\" : 20, \"location\" : \"CH\", \"controls\" : [ { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"14\" } ] }, { \"type\" : \"StoredDataSet\", \"@id\" : \"21\", \"name\" : \"DataSet-KnowGoDB-CarData_1\", \"id\" : 21, \"consistsOf\" : [ { \"type\" : \"Record\", \"referencedObjectID\" : \"13\" } ], \"belongsTo\" : { \"type\" : \"DataSubject\", \"referencedObjectID\" : \"10\" }, \"storedIn\" : { \"type\" : \"Database\", \"referencedObjectID\" : \"9\" } }, { \"type\" : \"DataFlow\", \"@id\" : \"22\", \"name\" : \"DataFlow-BiometricData-KnowGoApp-KnowGoDB\", \"id\" : 22, \"consistsOf\" : [ { \"type\" : \"Record\", \"referencedObjectID\" : \"12\" } ], \"belongsTo\" : { \"type\" : \"DataSubject\", \"referencedObjectID\" : \"10\" }, \"streamsTo\" : [ { \"type\" : \"Database\", \"referencedObjectID\" : \"9\" } ] }, { \"type\" : \"DataFlow\", \"@id\" : \"23\", \"name\" : \"DataFlow-CarData-KnowGoApp-KnowGoDB\", \"id\" : 23, \"consistsOf\" : [ { \"type\" : \"Record\", \"referencedObjectID\" : \"13\" } ], \"belongsTo\" : { \"type\" : \"DataSubject\", \"referencedObjectID\" : \"10\" }, \"streamsTo\" : [ { \"type\" : \"Database\", \"referencedObjectID\" : \"9\" } ] }, { \"type\" : \"StoredDataSet\", \"@id\" : \"24\", \"name\" : \"DataSet-SensorData-DriverPhone\", \"id\" : 24, \"consistsOf\" : [ { \"type\" : \"Record\", \"referencedObjectID\" : \"25\" } ], \"belongsTo\" : { \"type\" : \"DataSubject\", \"referencedObjectID\" : \"10\" }, \"storedInMobileHost\" : { \"type\" : \"MobileHost\", \"referencedObjectID\" : \"26\" }, \"createdBy\" : { \"type\" : \"MobileHost\", \"referencedObjectID\" : \"27\" } }, { \"type\" : \"Record\", \"@id\" : \"25\", \"name\" : \"SensorData\", \"id\" : 25, \"partOf\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"24\" } ], \"sensitive\" : true }, { \"type\" : \"MobileHost\", \"@id\" : \"26\", \"name\" : \"DriverPhone\", \"id\" : 26, \"jurisdiction\" : \"DE\", \"managedBy\" : { \"type\" : \"DataSubject\", \"referencedObjectID\" : \"10\" }, \"hostsContainerApplication\" : [ { \"type\" : \"Container_Application\", \"referencedObjectID\" : \"28\" } ], \"stores\" : { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"24\" } }, { \"type\" : \"MobileHost\", \"@id\" : \"27\", \"name\" : \"Car\", \"id\" : 27, \"jurisdiction\" : \"DE\", \"managedBy\" : { \"type\" : \"DataSubject\", \"referencedObjectID\" : \"10\" }, \"hostsContainerApplication\" : [ { \"type\" : \"Container_Application\", \"referencedObjectID\" : \"29\" } ], \"creates\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"24\" } ] }, { \"type\" : \"Container_Application\", \"@id\" : \"28\", \"name\" : \"KnowGoApp\", \"id\" : 28, \"hostedBy\" : { \"type\" : \"MobileHost\", \"referencedObjectID\" : \"26\" }, \"sendsThrough\" : { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"30\" }, \"usedBy\" : [ { \"type\" : \"Container_Application\", \"referencedObjectID\" : \"29\" } ] }, { \"type\" : \"Container_Application\", \"@id\" : \"29\", \"name\" : \"CarDataMarshall\", \"id\" : 29, \"hostedBy\" : { \"type\" : \"MobileHost\", \"referencedObjectID\" : \"27\" }, \"uses\" : [ { \"type\" : \"Container_Application\", \"referencedObjectID\" : \"28\" } ] }, { \"type\" : \"DataFlow\", \"@id\" : \"30\", \"name\" : \"DataFlow-KnowGoApp-DataGW\", \"id\" : 30, \"sendsFrom\" : { \"type\" : \"Container_Application\", \"referencedObjectID\" : \"28\" }, \"sendsTo\" : { \"type\" : \"Gateway\", \"referencedObjectID\" : \"31\" } }, { \"type\" : \"Gateway\", \"@id\" : \"31\", \"name\" : \"DataGW\", \"id\" : 31, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"32\" }, \"receivesFrom\" : [ { \"type\" : \"DataFlow\", \"referencedObjectID\" : \"30\" } ], \"usesGateway\" : [ { \"type\" : \"Gateway\", \"referencedObjectID\" : \"16\" } ] }, { \"type\" : \"Compute\", \"@id\" : \"32\", \"name\" : \"DataGWServer\", \"id\" : 32, \"hosts\" : [ { \"type\" : \"Gateway\", \"referencedObjectID\" : \"31\" } ], \"connectedToNetwork\" : [ { \"type\" : \"Network_Network\", \"referencedObjectID\" : \"2\" } ], \"jurisdiction\" : \"DE\" }, { \"type\" : \"StoredDataSet\", \"@id\" : \"33\", \"name\" : \"DataSet-KnowGoDB-RiskData\", \"id\" : 33, \"consistsOf\" : [ { \"type\" : \"Record\", \"referencedObjectID\" : \"34\" } ], \"belongsTo\" : { \"type\" : \"DataSubject\", \"referencedObjectID\" : \"10\" }, \"createdBySoftwareComponent\" : { \"type\" : \"SoftwareComponent\", \"referencedObjectID\" : \"35\" } }, { \"type\" : \"Record\", \"@id\" : \"34\", \"name\" : \"RiskData\", \"id\" : 34, \"partOf\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"33\" } ] }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"36\", \"name\" : \"KGAnalytics_2\", \"id\" : 36, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"17\" }, \"usedByWebApp\" : [ { \"type\" : \"WebApplication\", \"referencedObjectID\" : \"7\" } ], \"softwareComponentUsesGateway\" : [ { \"type\" : \"Gateway\", \"referencedObjectID\" : \"16\" } ] }, { \"type\" : \"SoftwareComponent\", \"@id\" : \"35\", \"name\" : \"KGAnalytics_1\", \"id\" : 35, \"hostedOn\" : { \"type\" : \"Compute\", \"referencedObjectID\" : \"1\" }, \"creates\" : [ { \"type\" : \"StoredDataSet\", \"referencedObjectID\" : \"33\" } ], \"usedByWebApp\" : [ { \"type\" : \"WebApplication\", \"referencedObjectID\" : \"7\" } ], \"softwareComponentUsesGateway\" : [ { \"type\" : \"Gateway\", \"referencedObjectID\" : \"16\" } ] } ] }";
		long id = 0;
		// load model from JSON String to EMF
		CloudEnvironment cloudEnvironment = null;
		try {
			cloudEnvironment = (CloudEnvironment) loader.loadEObjectFromString(model);
			id = loader.setCloudEnvironmentsMonitored(cloudEnvironment, 1);
//			System.out.println("Loaded model after restart event as:\n" + runtimeModelLogic.returnentiremodelasjson(cloudEnvironment));
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
//		System.out.println(returner);
		return returner;
	}
}
