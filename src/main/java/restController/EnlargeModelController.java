package restController;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EGenericType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.EEnumImpl;
import org.eclipse.emf.henshin.interpreter.EGraph;
import org.eclipse.emf.henshin.interpreter.Engine;
import org.eclipse.emf.henshin.interpreter.UnitApplication;
import org.eclipse.emf.henshin.interpreter.impl.EGraphImpl;
import org.eclipse.emf.henshin.interpreter.impl.EngineImpl;
import org.eclipse.emf.henshin.interpreter.impl.UnitApplicationImpl;
import org.eclipse.emf.henshin.model.Module;
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonMappingException.Reference;

import restassured.riskpattern.CloudModel.CloudEnvironment;
import restassured.riskpattern.CloudModel.CloudModelFactory;
import restassured.riskpattern.CloudModel.CloudModelPackage;
import restassured.riskpattern.CloudModel.Compute;
import restassured.riskpattern.CloudModel.DataSpecificRole;
import restassured.riskpattern.CloudModel.Database;
import restassured.riskpattern.CloudModel.IaaSOperator;
import restassured.riskpattern.CloudModel.Jurisdictions;
import restassured.riskpattern.CloudModel.Record;
import restassured.riskpattern.CloudModel.tosca_nodes_Root;
import restassured.riskpattern.CloudModel.impl.ComputeImpl;
import restassured.riskpattern.CloudModel.impl.tosca_nodes_RootImpl;
import runtime.EMFModelLoad;
import runtime.RiskFinder;
import runtime.RuntimeModelLogic;
import utility.Constants;

@RestController
public class EnlargeModelController {

	@Autowired
	private RuntimeModelLogic runtimeModelLogic;
	private Engine engine = new EngineImpl();
	@Autowired
	private RiskFinder riskFinder;

	private int globalNameInteger;
	
	@Autowired
	EMFModelLoad loader;
	static final String fileName = "CloudExample.cloudmodel";

	@RequestMapping(path = "/radar/enlargeCloudExample", method = RequestMethod.POST, consumes = "*/*; charset=UTF-8")
	@ResponseBody
	public ResponseEntity enlargeCloudExample(@RequestBody(required = true) String body) {
		ResponseEntity response = new ResponseEntity(HttpStatus.BAD_REQUEST);
		int amount = Integer.valueOf(body);
		HenshinResourceSet resourceSet = new HenshinResourceSet(Constants.ADAPTATIONS_PATH);
		Module module = resourceSet.getModule("enlargeCloudExample.henshin", false);
		EGraph graph = new EGraphImpl(
				new EGraphImpl(runtimeModelLogic.getLoader().getCloudEnvironmentsMonitored().get(1l)));
		UnitApplication app = new UnitApplicationImpl(engine);
		app.setEGraph(graph);
		app.setUnit(module.getUnit("enlargeCloudExample"));
		for (int i = 0; i < amount; i++) {
			try {
				if (!app.execute(null)) {
					System.err.println("Failure");
					response = new ResponseEntity(HttpStatus.BAD_REQUEST);
				} else {
					System.out.println("Enlarged");
					response = new ResponseEntity(HttpStatus.ACCEPTED);
				}
			} catch (NullPointerException e) {
				System.err.println("Failure NullPointer");
				response = new ResponseEntity(HttpStatus.BAD_REQUEST);
			}
		}
		CloudEnvironment enlargedModel = (CloudEnvironment) graph.getRoots().get(0);
		runtimeModelLogic.getLoader().setCloudEnvironmentsMonitored(enlargedModel, 1L);
		return response;
	}

	public void newEnlargerMethodForCloudExample(int amountOfNewNodes, CloudEnvironment cloudEnvironment) {
		globalNameInteger = 0;
		for (int i = 0; i < amountOfNewNodes; i++) {
			ArrayList<String> listWithPossibleNodeTypes = new ArrayList<String>();
			listWithPossibleNodeTypes = fillListWithPossibleNodes(listWithPossibleNodeTypes);
			int randomNodeTypeInt = (int) (Math.random() * (listWithPossibleNodeTypes.size() - 1));
			String nodeType = listWithPossibleNodeTypes.get(randomNodeTypeInt);
			EObject randomNode = this.createNewEObject(nodeType);

			
			this.setAttributes(randomNode);

			cloudEnvironment.getTosca_nodes_root().add((tosca_nodes_Root) randomNode);
			int amountOfNodesAddedByConstraints = executePossibleConstraints(randomNode, cloudEnvironment);
			i = i + amountOfNodesAddedByConstraints;
			List<ReferenceToEObject> newPossibleReferences = new ArrayList<ReferenceToEObject>();
			for (EReference r1 : randomNode.eClass().getEAllReferences()) {
				EObject o1 = this.findObjectFromSpecificType(cloudEnvironment, r1);
				if (o1 != null) {
					ReferenceToEObject rto = new ReferenceToEObject(r1, randomNode, o1);
					newPossibleReferences.add(rto);
				}
			}
			int randomAmountOfRelationships = (int) (Math.random() * newPossibleReferences.size());
			for (int j = 0; j < randomAmountOfRelationships; j++) {
				int randomEObject = (int) (Math.random() * (newPossibleReferences.size() - 1));
				ReferenceToEObject rto = newPossibleReferences.get(randomEObject);
				EStructuralFeature feature = rto.getSourceObject().eClass()
						.getEStructuralFeature(rto.getReference().getName());
				if (rto.getReference().getUpperBound() == 1) {
					EObject relation = (EObject) randomNode.eGet(feature);
					if (relation == null) {
						randomNode.eSet(feature, rto.getTargetObject());
					}
				} else {
					EList<EObject> relations = (EList<EObject>) rto.getSourceObject().eGet(feature);
					relations.add(rto.getTargetObject());
					newPossibleReferences.remove(rto);
				}
			}
			System.out.println("Finished " + i);
		}
		loader.setCloudEnvironmentsMonitored(cloudEnvironment, 1);
	}

	private void setAttributes(EObject randomNode) {
		// NAME ==> always generated
		EStructuralFeature attributeFeature = randomNode.eClass().getEStructuralFeature("name");
		randomNode.eSet(attributeFeature, "Auto-Generated_Node_" + globalNameInteger);
		globalNameInteger++;

		// Compute
		// costs ==> Randomly chosen from possible realistic costs
		// Jurisdiction ==> Randomly chosen from enum
		Compute computeNode = (Compute) createNewEObject("Compute");
		if (computeNode.eClass().isSuperTypeOf(randomNode.eClass())) {
			attributeFeature = randomNode.eClass().getEStructuralFeature("usageCostPerDay");
			ArrayList<Double> possibleCosts = new ArrayList<Double>();
			possibleCosts.add(0.552);
			possibleCosts.add(1.1136);
			possibleCosts.add(2.2272);
			double costs = possibleCosts.get((int) (Math.random() * (possibleCosts.size() - 1)));
			randomNode.eSet(attributeFeature, costs);

			attributeFeature = randomNode.eClass().getEStructuralFeature("jurisdiction");
			Jurisdictions jurisdictions = Jurisdictions.get((int) (Math.random() * (Jurisdictions.VALUES.size() - 1)));
			randomNode.eSet(attributeFeature, jurisdictions);
		}

		// Database
		// Encrypted ==> Randomly chosen
		if (((Database) createNewEObject("Database")).eClass().isSuperTypeOf(randomNode.eClass())) {
			attributeFeature = randomNode.eClass().getEStructuralFeature("encrypted");
			randomNode.eSet(attributeFeature, new Random().nextBoolean());
		}

		// Record
		// Sensitive ==> Randomly chosen
		// Encrypted ==> Randomly chosen
		if (((Record) createNewEObject("Record")).eClass().isSuperTypeOf(randomNode.eClass())) {
			attributeFeature = randomNode.eClass().getEStructuralFeature("sensitive");
			randomNode.eSet(attributeFeature, new Random().nextBoolean());
			attributeFeature = randomNode.eClass().getEStructuralFeature("encrypted");
			randomNode.eSet(attributeFeature, new Random().nextBoolean());
		}

		// DataSpecificRole
		// location ==> Randomly chosen from Enum
		EClass eclassDataSpecificRole = (EClass) CloudModelPackage.eINSTANCE.getEClassifier("DataSpecificRole");
		if (eclassDataSpecificRole.isSuperTypeOf(randomNode.eClass())) {
			attributeFeature = randomNode.eClass().getEStructuralFeature("location");
			Jurisdictions jurisdictions = Jurisdictions.get((int) (Math.random() * (Jurisdictions.VALUES.size() - 1)));
			randomNode.eSet(attributeFeature, jurisdictions);
		}

	}

	private EObject createNewEObject(String nodeType) {
		CloudModelFactory factory = CloudModelFactory.eINSTANCE;
		try {
			Method methodToCreateObject = factory.getClass().getMethod("create" + nodeType);
			EObject randomNode = (EObject) methodToCreateObject.invoke(factory);
			return randomNode;
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			e.printStackTrace();
			return null;
		}
	}

	private int executePossibleConstraints(EObject randomNode, CloudEnvironment cloudEnvironment) {
		int numberOfAddedNodes = 0;
		for (EReference r1 : randomNode.eClass().getEAllReferences()) {
			String nodeType = "";
			EClass ec = (EClass) r1.getEType();
			if (ec.isAbstract()) {
				// Target is abstract
				EList<EObject> list = CloudModelFactory.eINSTANCE.getEPackage().eContents();
				ArrayList<EObject> l = new ArrayList<EObject>();
				for (EObject o : list) {
					try {
						if (ec.isSuperTypeOf((EClass) o) && !((EClass) o).getName().equals(ec.getName())) {
							l.add(o);
						}
					} catch (ClassCastException e) {
//						System.err.println("Class Name in Catch Block: " + ((EEnumImpl) o).getName());
					}
				}
				int randomNodeFromL = (int) (Math.random() * (l.size() - 1));
				nodeType = ((EClass) l.get(randomNodeFromL)).getName();
			} else {
				nodeType = r1.getEType().getName();
			}

			if (r1.getLowerBound() == 1) {
				if (r1.getUpperBound() == 1) {
					EStructuralFeature feature = randomNode.eClass().getEStructuralFeature(r1.getName());
					EObject relation = (EObject) randomNode.eGet(feature);
					if (relation == null) {
						EObject objectToAdd = this.findObjectFromSpecificType(cloudEnvironment, r1);
						if (objectToAdd == null) {
							// We need a new Object
							objectToAdd = this.createNewEObject(nodeType);
							this.setAttributes(objectToAdd);
							cloudEnvironment.getTosca_nodes_root().add((tosca_nodes_Root) objectToAdd);
							numberOfAddedNodes += this.executePossibleConstraints(objectToAdd, cloudEnvironment);
							numberOfAddedNodes++;
						}
						randomNode.eSet(feature, objectToAdd);
					}
				} else {
					EStructuralFeature feature = randomNode.eClass().getEStructuralFeature(r1.getName());
					EList<EObject> relations = (EList<EObject>) randomNode.eGet(feature);
					if (relations.isEmpty()) {
						EObject objectToAdd = this.findObjectFromSpecificType(cloudEnvironment, r1);
						if (objectToAdd == null) {
							// We need a new Object
							objectToAdd = this.createNewEObject(nodeType);
							this.setAttributes(objectToAdd);
							cloudEnvironment.getTosca_nodes_root().add((tosca_nodes_Root) objectToAdd);
							numberOfAddedNodes += this.executePossibleConstraints(objectToAdd, cloudEnvironment);
							numberOfAddedNodes++;
						}
						relations.add(objectToAdd);
					}
				}
			}
		}

		return numberOfAddedNodes;
	}

	private EObject findObjectFromSpecificType(CloudEnvironment cloudEnvironment, EReference r1) {
		for (EObject o1 : cloudEnvironment.eContents()) {
			EClass[] eClassList = new EClass[o1.eClass().getEAllSuperTypes().size() + 1];
			Object[] objectArray = (o1.eClass().getEAllSuperTypes().toArray());
			for (int x = 0; x < o1.eClass().getEAllSuperTypes().size(); x++) {
				eClassList[x] = (EClass) objectArray[x];
			}
			eClassList[eClassList.length - 1] = o1.eClass();
			for (EClass eclass : eClassList) {
				if (r1.getEType().getName().equals(eclass.getName())) {
					if (r1.getEOpposite() == null) {
						return o1;
					} else {
						if (r1.getEOpposite().getUpperBound() == 1) {
							EStructuralFeature feature = o1.eClass().getEStructuralFeature(r1.getEOpposite().getName());
							EObject relation = (EObject) o1.eGet(feature);
							if (relation == null) {
								return o1;
							}
						} else {
							return o1;
						}
					}
				}
			}
		}

		return null;
	}

	private ArrayList<String> fillListWithPossibleNodes(ArrayList<String> list) {
		list.add("Compute");
		list.add("PaaSOperator"); // needs Compute object 
		list.add("IaaSOperator"); // needs Compute object 
		list.add("SaaSOperator"); // needs SoftwareComponent object 
		list.add("DataSubject"); // needs Record object
		list.add("DataProcessor");
		list.add("DataController");
		list.add("SoftwareComponent");
		list.add("Database");
		list.add("DBMS");
		list.add("Record");
		list.add("StoredDataSet");
		list.add("DataFlow");
		return list;
	}

	@RequestMapping(path = "/radar/enlargeFogExample", method = RequestMethod.POST, consumes = "*/*; charset=UTF-8")
	@ResponseBody
	public ResponseEntity enlargeFogExample(@RequestBody(required = true) String body) {
		ResponseEntity response = new ResponseEntity(HttpStatus.BAD_REQUEST);
		int amount = Integer.valueOf(body);
		HenshinResourceSet resourceSet = new HenshinResourceSet(Constants.ADAPTATIONS_PATH);
		Module module = resourceSet.getModule("enlargeFogExample.henshin", false);
		EGraph graph = new EGraphImpl(
				new EGraphImpl(runtimeModelLogic.getLoader().getCloudEnvironmentsMonitored().get(1l)));
		UnitApplication app = new UnitApplicationImpl(engine);
		app.setEGraph(graph);
		app.setUnit(module.getUnit("enlargeFogExample"));
		for (int i = 0; i < amount; i++) {
			try {
				if (!app.execute(null)) {
					System.err.println("Failure");
					response = new ResponseEntity(HttpStatus.BAD_REQUEST);
				} else {
					System.out.println("Enlarged");
					response = new ResponseEntity(HttpStatus.ACCEPTED);
				}
			} catch (NullPointerException e) {
				System.err.println("Failure NullPointer");
				response = new ResponseEntity(HttpStatus.BAD_REQUEST);
			}
		}
		CloudEnvironment enlargedModel = (CloudEnvironment) graph.getRoots().get(0);
		runtimeModelLogic.getLoader().setCloudEnvironmentsMonitored(enlargedModel, 1L);
//		riskFinder.lookForRisksDEMO(1l);

		// returns ResponseCode
		return response;
	}

	private class ReferenceToEObject {
		private EReference reference;
		private EObject sourceObject, targetObject;

		public ReferenceToEObject(EReference reference, EObject sourceObject, EObject targetObject) {
			this.reference = reference;
			this.sourceObject = sourceObject;
			this.targetObject = targetObject;
		}

		public EReference getReference() {
			return reference;
		}

		public void setReference(EReference reference) {
			this.reference = reference;
		}

		public EObject getSourceObject() {
			return sourceObject;
		}

		public void setSourceObject(EObject sourceObject) {
			this.sourceObject = sourceObject;
		}

		public EObject getTargetObject() {
			return targetObject;
		}

		public void setTargetObject(EObject targetObject) {
			this.targetObject = targetObject;
		}

	}

}
