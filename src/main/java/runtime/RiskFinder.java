package runtime;

import java.util.LinkedList;
import java.util.Random;
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
import org.springframework.stereotype.Component;
import restController.RadarTester;
import restassured.riskpattern.CloudModel.CloudEnvironment;
import riskpatternfinder.AdaptationFinderToImproveSystem;
import riskpatternfinder.AdaptationFinderToMitigateRisks;
import riskpatternfinder.AdaptationFinderToMitigateRisks.AdaptationAlgorithm;
import utility.Adaptation;
import utility.AdaptationCombinationRepresentation;
import utility.AtomicAdaptation;
import utility.Constants;
import utility.MatchRepresentation;
import utility.Model;

@Component
public class RiskFinder {
	@Autowired
	private RuntimeModelLogic runtimeModelLogic;
	@Autowired
	RadarTester radarTester;
	private Engine engine = new EngineImpl();
	private long startTime = 0;
	private long endTime = 0;
	private long elapsedTime = 0;
	private long timestampZERO;

	public LinkedList<String[]> startRadar(long index, AdaptationAlgorithm aa, String enlargement, int testnumber,
			int maxSeconds, CloudEnvironment cloudEnvironment, long timestamptZERO) {
		this.timestampZERO = timestamptZERO;
		LinkedList<String[]> datas = this.lookForRisks(cloudEnvironment, index, aa, enlargement, testnumber,
				maxSeconds);

		// Not used for performance testing
		// this.lookForConstraints(runtimeModelLogic.getLoader().getCloudEnvironmentsMonitored().get(1l));
		// this.lookForImprovements(runtimeModelLogic.getLoader().getCloudEnvironmentsMonitored().get(1l),
		// index, aa, maxSeconds);
		return datas;
	}

	private void lookForConstraints(CloudEnvironment cloudEnvironment) {
		HenshinResourceSet resourceSet = new HenshinResourceSet(Constants.RISKPATTERNS_PATH);
		Module module = resourceSet.getModule("constraints.henshin", false);
		EGraph graph = new EGraphImpl(new EGraphImpl(cloudEnvironment));
		UnitApplication app = new UnitApplicationImpl(engine);
		app.setEGraph(graph);
		app.setUnit(module.getUnit("conditionalCheckSequence"));
		try {
			if (!app.execute(null)) {
				System.err.println("One condition was not fulfilled");
			} else {
				System.out.println("All conditions are fulfilled");
			}
		} catch (NullPointerException e) {
			System.err.println("One condition was not fulfilled");
		}

	}

	private void lookForImprovements(CloudEnvironment cloudEnvironment, long index, AdaptationAlgorithm aa,
			int maxSeconds) {
		AdaptationFinderToImproveSystem adaptationFinder = new AdaptationFinderToImproveSystem(engine);
		AdaptationCombinationRepresentation[] possibleAdaptations = adaptationFinder.generatePossibleAdaptations(
				cloudEnvironment, aa, radarTester.getRandomizedAdaptationRules(), maxSeconds, 0, null, "");
		if (possibleAdaptations == null || possibleAdaptations.length == 0) {
			System.out.println("No adaptations to improve the systems functionality found.");
		} else {
			System.out.println("Possible Adaptation(s) to improve system found.");
			int i = 1;
			// loop through every adaptation within the list, until one is accepted
			for (AdaptationCombinationRepresentation adaptation : possibleAdaptations) {
				// set proposal graph
				CloudEnvironment proposal = adaptation.getRuntimeModel();
				// System.out.println("Proposal created:\n" +
				// runtimeModelLogic.returnentiremodelasjson(proposal));
				System.out.println("Executed adaptation(s):");
				int[] adaptationCaseNumber = new int[adaptation.getAtomicAdaptationArray().length];
				int j = 0;
				for (AtomicAdaptation adap : adaptation.getAtomicAdaptationArray()) {
					System.out.println("Adaptation_" + adap.getAdaptationType() + "_" + adap.getAdaptationIndex());
					adaptationCaseNumber[j] = adap.getAdaptationType();
					j++;
				}
				// update run-time model
				LinkedList<String[]> datas = new LinkedList<String[]>();
				datas = executeAdaptations2(adaptation, datas, "0", 1);
				// save new runtime model as file for testing purposes
				this.saveGraph(new EGraphImpl(proposal), 1);
			}
			this.startRadar(1l, aa, "", 0, 10, cloudEnvironment, 0);
			// break out of method, because there is already an accepted solution
			return;
		}
	}

	@SuppressWarnings(value = { "all" })
	public LinkedList<String[]> lookForRisks(CloudEnvironment cloudEnvironment, long index, AdaptationAlgorithm aa,
			String enlargement, int testnumber, int maxSeconds) {
		// BEGIN TO MEASURE
		LinkedList<String[]> datas = new LinkedList<String[]>();
		String[] data = null;
		if (radarTester.performanceTest == true) {
			System.out.println("Timer started");
			startTime = System.nanoTime();
			data = this.setData("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "");
		}

		System.out.println("Enlargement: " + String.valueOf(Integer.valueOf(enlargement) + 22));
		enlargement = String.valueOf(cloudEnvironment.eContents().size());
		System.out.println("Amount of nodes: " + enlargement);

		// look for possible adaptations in the runtime model
		AdaptationFinderToMitigateRisks adaptationFinder = new AdaptationFinderToMitigateRisks();
		AdaptationCombinationRepresentation[] effectiveAdaptations = adaptationFinder.generatePossibleAdaptations(
				cloudEnvironment, aa, radarTester.getRandomizedAdaptationRules(), maxSeconds, timestampZERO, datas,
				enlargement);
		long timestampAfterAdaptationSearch = System.nanoTime();
		String amountOfPCPInstances = "";
		if (adaptationFinder.getFirstFoundRisks() == null) {
			amountOfPCPInstances = "0";
		} else {
			amountOfPCPInstances = String.valueOf(adaptationFinder.getFirstFoundRisks().size());
		}
		String[] values = {
				String.valueOf((double) ((timestampAfterAdaptationSearch - timestampZERO) / 1_000_000_000.0)),
				enlargement, amountOfPCPInstances, String.valueOf(adaptationFinder.getMaxFuncInOriginModel()),
				String.valueOf(adaptationFinder.getCostInOriginModel()) };
		datas.add(values);
		// check if list with adaptations does exist
		if (effectiveAdaptations == null) {
			// there is no list, because there is no risk found within the runtime model
			// log into console, that there is no risk found
			System.out.println("No risks found.");

			// STOP MEASURE

			if (radarTester.performanceTest == true) {
				endTime = System.nanoTime();
				elapsedTime = endTime - startTime;
				radarTester.performanceTimeCollection.add(elapsedTime);
				System.out.println("elapsedTime: " + (elapsedTime / 1e9d));
				startTime = 0;
				endTime = 0;
				elapsedTime = 0;
				System.out.println("Timer stopped");
				// String[] values = {String.valueOf((double)((timestampAfterAdaptationSearch -
				// timestampZERO)/ 1_000_000_000.0)), enlargement, String.valueOf(0),
				// String.valueOf(adaptationFinder.getMaxFuncInOriginModel()),
				// String.valueOf(adaptationFinder.getCostInOriginModel())};
				// datas.add(values);
			}
		} else {
			// check if the list of adaptations is empty
			if (effectiveAdaptations.length == 0) {

				System.err.println("Risk(s) found without possible adaptation: ");
				String foundPCPInstances = "";
				for (MatchRepresentation matchRep : adaptationFinder.getFirstFoundRisks()) {
					System.err.println("     Risk: \"" + matchRep.getRuleName() + "\"");
					foundPCPInstances += "Risk: \"" + matchRep.getRuleName() + "\" | ";
				}
				Model nonPerfectSolution = adaptationFinder.lookForRisksInNonPerfectSolution();
				CloudEnvironment proposal = (CloudEnvironment) nonPerfectSolution.getRuntimeModel().getRoots().get(0);
				String remainingPCPInstances = "";
				for (MatchRepresentation matchRep : nonPerfectSolution.getRisks()) {
					remainingPCPInstances += "Risk: \"" + matchRep.getRuleName() + "\" | ";
				}

				String usedAdaptations = adaptationFinder.generatePathString(nonPerfectSolution);
				for (Adaptation adap : nonPerfectSolution.getAdaptations()) {
					System.out.println("     " + adap.getRiskName() + ": Adaptation_" + adap.getRule().getName());
					usedAdaptations += adap.getRiskName() + ": " + adap.getRule().getName() + " | ";
				}

				// STOP MEASURE

				if (radarTester.performanceTest == true) {
					endTime = System.nanoTime();
					elapsedTime = endTime - startTime;
					radarTester.performanceTimeCollection.add(elapsedTime);
					double elapsedTimeDouble = (elapsedTime / 1e9d);
					System.out.println("elapsedTime: " + elapsedTimeDouble);
					data = this.setData(aa.toString(), enlargement, String.valueOf(testnumber),
							String.valueOf(elapsedTimeDouble),
							String.valueOf((nonPerfectSolution.getCreatedAt() / 1000000000d)), foundPCPInstances,
							usedAdaptations, remainingPCPInstances,
							String.valueOf((adaptationFinder.getMaxFuncInOriginModel()
									- nonPerfectSolution.getWorkingFunctions())),
							String.valueOf(
									(nonPerfectSolution.getTotalCost() - adaptationFinder.getCostInOriginModel())),
							String.valueOf(adaptationFinder.getAmountOfHenshinCalls()),
							String.valueOf(adaptationFinder.getTimeToFindPCPInstances()),
							String.valueOf(adaptationFinder.getTimeToExecuteAdaptations()),
							String.valueOf(adaptationFinder.getTimeToCalculateWF()),
							String.valueOf(adaptationFinder.getTimeToCalculateCost()),
							String.valueOf(adaptationFinder.getTimeForEMFCompareMethod()),
							String.valueOf(adaptationFinder.getTimeForCompareRiskyModelsForBFS()));

					startTime = 0;
					endTime = 0;
					elapsedTime = 0;
					System.out.println("Timer stopped");
				}
				// the list is empty
				// no possible adaptations were found
				// risks still exist
				datas = executeAdaptations(nonPerfectSolution, datas, enlargement,
						Integer.valueOf(amountOfPCPInstances));
			} else {
				System.out.println("Risk(s) found with possible adaptation: ");
				String foundPCPInstances = "";
				for (MatchRepresentation matchRep : adaptationFinder.getFirstFoundRisks()) {
					System.out.println("     Risk: \"" + matchRep.getRuleName() + "\"");
					foundPCPInstances += "Risk: \"" + matchRep.getRuleName() + "\" | ";
				}
				// there are effective adaptations within the list

				// loop through every adaptation within the list, until one is accepted => THIS
				// LOOP IS NOT USED BECAUSE THE FIRST ONE WILL BE ACCEPTED
				for (AdaptationCombinationRepresentation adaptation : effectiveAdaptations) {
					// set proposal graph
					CloudEnvironment proposal = adaptation.getRuntimeModel();

					// System.out.println("Proposal created:\n" +
					// runtimeModelLogic.returnentiremodelasjson(proposal));

					System.out.println("Executed adaptation(s):");
					int[] adaptationCaseNumber = new int[adaptation.getAtomicAdaptationArray().length];
					int j = 0;
					String usedAdaptations = "";
					for (AtomicAdaptation adap : adaptation.getAtomicAdaptationArray()) {
						System.out.println("     " + adap.getRiskRuleName() + ": Adaptation_" + adap.getAdaptationType()
								+ "_" + adap.getAdaptationIndex());
						usedAdaptations += adap.getRiskRuleName() + ": Adaptation_" + adap.getAdaptationType() + "_"
								+ adap.getAdaptationIndex() + " | ";
						adaptationCaseNumber[j] = adap.getAdaptationType();
						j++;
					}

					System.out.println(adaptation.getCreatedAt() / 1000000000d);
					// STOP MEASURE

					if (radarTester.performanceTest == true) {
						endTime = System.nanoTime();
						elapsedTime = endTime - startTime;
						radarTester.performanceTimeCollection.add(elapsedTime);
						double elapsedTimeDouble = (elapsedTime / 1e9d);
						System.out.println("elapsedTime: " + elapsedTimeDouble);
						System.out.println("Timer stopped");
						data = setData(aa.toString(), enlargement, String.valueOf(testnumber),
								String.valueOf(elapsedTimeDouble),
								String.valueOf((adaptation.getCreatedAt() / 1000000000d)), foundPCPInstances,
								usedAdaptations, "none",
								String.valueOf((adaptationFinder.getMaxFuncInOriginModel()
										- adaptation.getAmountOfWorkingFunctions())),
								String.valueOf((adaptation.getTotalCost() - adaptationFinder.getCostInOriginModel())),
								String.valueOf(adaptationFinder.getAmountOfHenshinCalls()),
								String.valueOf(adaptationFinder.getTimeToFindPCPInstances()),
								String.valueOf(adaptationFinder.getTimeToExecuteAdaptations()),
								String.valueOf(adaptationFinder.getTimeToCalculateWF()),
								String.valueOf(adaptationFinder.getTimeToCalculateCost()),
								String.valueOf(adaptationFinder.getTimeForEMFCompareMethod()),
								String.valueOf(adaptationFinder.getTimeForCompareRiskyModelsForBFS()));
						System.out.println("MaxAmountOfFunctions " + adaptationFinder.getMaxFuncInOriginModel()
								+ ". Working Functions: " + adaptation.getAmountOfWorkingFunctions());
						startTime = 0;
						endTime = 0;
						elapsedTime = 0;
					}

					// update run-time model
					datas = executeAdaptations2(adaptation, datas, enlargement, Integer.valueOf(amountOfPCPInstances));
					// save new run-time model as file for testing purposes
					this.saveGraph(new EGraphImpl(proposal), 1);
					// this.lookForRisksDEMO(1l);
					// break out of method, because there is already an accepted solution
					return datas;
				}
			}
		}
		return datas;
	}

	private LinkedList<String[]> executeAdaptations(Model model, LinkedList<String[]> datas, String enlargement,
			int maxAmountOfPCPInstanes) {
		CloudEnvironment proposal = (CloudEnvironment) model.getRuntimeModel().getRoots().get(0);
		runtimeModelLogic.getLoader().setCloudEnvironmentsMonitored(proposal, 1L);
		Adaptation[] ar = model.getAdaptations();
		shuffleArray(ar);
		for (int i = 0; i < (maxAmountOfPCPInstanes - model.getRisks().length); i++) {
			try {
				if (ar[i].getRiskName().equals("OutsideEU")) {
					Thread.sleep(6000);
				} else {
					Thread.sleep(2000);
				}
				double random = Math.random();
				String newAmountOfPCPInstances = "";
				if (ar.length - i > 3 && random < 0.5) {
					newAmountOfPCPInstances = String.valueOf(maxAmountOfPCPInstanes - (i + 2));
					i++;
				} else {
					newAmountOfPCPInstances = String.valueOf(maxAmountOfPCPInstanes - (i + 1));
				}
				long timestamp = System.nanoTime();
				String[] values = { String.valueOf((double) ((timestamp - timestampZERO) / 1_000_000_000.0)),
						enlargement, newAmountOfPCPInstances,
						String.valueOf(model.getWorkingFunctions()), String.valueOf(model.getTotalCost()) };
				datas.add(values);
				System.out.println("PCP Instances = " + newAmountOfPCPInstances);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return datas;

	}

	static void shuffleArray(Adaptation[] ar) {
		// If running on Java 6 or older, use `new Random()` on RHS here
		Random rnd = ThreadLocalRandom.current();
		for (int i = ar.length - 1; i > 0; i--) {
			int index = rnd.nextInt(i + 1);
			// Simple swap
			Adaptation a = ar[index];
			ar[index] = ar[i];
			ar[i] = a;
		}
	}

	private LinkedList<String[]> executeAdaptations2(AdaptationCombinationRepresentation adaptation,
			LinkedList<String[]> datas, String enlargement, int maxAmountOfPCPInstanes) {
		CloudEnvironment proposal = (CloudEnvironment) adaptation.getRuntimeModel();
		runtimeModelLogic.getLoader().setCloudEnvironmentsMonitored(proposal, 1L);
		AtomicAdaptation[] ar = adaptation.getAtomicAdaptationArray();
		shuffleArray(ar);
		for (int i = 0; i < ar.length; i++) {
			try {
				if (ar[i].getRiskRuleName().equals("OutsideEU")) {
					Thread.sleep(10000);
				} else {
					Thread.sleep(4000);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			double random = Math.random();
			String newAmountOfPCPInstances = "";
			if (ar.length - i > 3 && random < 0.5) {
				newAmountOfPCPInstances = String.valueOf(maxAmountOfPCPInstanes - (i + 2));
				i++;
			} else {
				newAmountOfPCPInstances = String.valueOf(maxAmountOfPCPInstanes - (i + 1));
			}
			long timestamp = System.nanoTime();
			String[] values = { String.valueOf((double) ((timestamp - timestampZERO) / 1_000_000_000.0)), enlargement,
					newAmountOfPCPInstances, String.valueOf(adaptation.getWorkingFunctions()),
					String.valueOf(adaptation.getTotalCost()) };
			datas.add(values);
			System.out.println("PCP Instances = " + newAmountOfPCPInstances);
		}
		return datas;
	}

	static void shuffleArray(AtomicAdaptation[] ar) {
		// If running on Java 6 or older, use `new Random()` on RHS here
		Random rnd = ThreadLocalRandom.current();
		for (int i = ar.length - 1; i > 0; i--) {
			int index = rnd.nextInt(i + 1);
			// Simple swap
			AtomicAdaptation a = ar[index];
			ar[index] = ar[i];
			ar[i] = a;
		}
	}

	// output generation for testing purposes
	public void saveGraph(EGraph outputGraph, int index) {
		HenshinResourceSet resourceSet = new HenshinResourceSet(Constants.RUNTIME_MODEL_PATH);
		resourceSet.saveEObject(outputGraph.getRoots().get(0), "AdaptedModel" + index + ".cloudmodel");
	}

	private String[] setData(String algorithmusname, String modellvergroesserung, String versuchsnummer,
			String genutzteZeit, String genutzteZeitBisBesteLoesung, String erkanntePCPInstanzen,
			String angewendeteAdaptionen, String zurueckgebleiebenePCPInstanzen, String anzahlFunktionseinschraenkungen,
			String kosten, String henshinAufrufe, String zeitPCPInstanzSuche, String zeitAdaptionsAusführung,
			String zeitWFBerechnung, String zeitKostenBerechnung, String zeitEMFCompareMethode, String zeitBFSCompare) {
		String[] data = { algorithmusname, modellvergroesserung, versuchsnummer, genutzteZeit,
				genutzteZeitBisBesteLoesung, erkanntePCPInstanzen, angewendeteAdaptionen,
				zurueckgebleiebenePCPInstanzen, anzahlFunktionseinschraenkungen, kosten, henshinAufrufe,
				zeitPCPInstanzSuche, zeitAdaptionsAusführung, zeitWFBerechnung, zeitKostenBerechnung,
				zeitEMFCompareMethode, zeitBFSCompare };
		return data;
	}

}
