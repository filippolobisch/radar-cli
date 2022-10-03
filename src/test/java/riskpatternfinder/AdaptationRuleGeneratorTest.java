//package riskpatternfinder;
//
//import static org.junit.Assert.*;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.Map;
//
//import org.eclipse.emf.common.util.EList;
//import org.eclipse.emf.common.util.URI;
//import org.eclipse.emf.ecore.EObject;
//import org.eclipse.emf.ecore.resource.Resource;
//import org.eclipse.emf.ecore.resource.ResourceSet;
//import org.eclipse.emf.ecore.resource.impl.ResourceFactoryRegistryImpl;
//import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
//import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
//import org.eclipse.emf.henshin.model.Rule;
//import org.junit.Test;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//
//import henshinTools.HenshinRuleCreator;
//import restassured.riskpattern.CloudModel.Atomic_component;
//import restassured.riskpattern.CloudModel.CloudEnvironment;
//import restassured.riskpattern.CloudModel.CloudModelFactory;
//import restassured.riskpattern.CloudModel.DBMS;
//import restassured.riskpattern.CloudModel.Data_subject;
//import restassured.riskpattern.CloudModel.Database;
//import restassured.riskpattern.CloudModel.PM;
//import restassured.riskpattern.CloudModel.PaaS_operator;
//import restassured.riskpattern.CloudModel.Record;
//import restassured.riskpattern.CloudModel.Stored_data_set;
//import restassured.riskpattern.CloudModel.VM;
//import restassured.riskpattern.CloudModel.impl.CloudModelPackageImpl;
//import runtime.EMFModelLoad;
//import utility.MatchRepresentation;
//import utility.NodeRepresentation;
//
//public class AdaptationRuleGeneratorTest
//{
//
//	@Test
//	public void testGetAdaptationRules()
//	{
//		AdaptationRuleGenerator generator = new AdaptationRuleGenerator();
//		CloudEnvironment cloudEnvironment = this.createCloudEnvironmentObject();
//		
//		String jsonString;
//		try
//		{
//			jsonString = new EMFModelLoad().getMapper().writerWithDefaultPrettyPrinter()
//					.writeValueAsString(cloudEnvironment);
//			System.out.println("Loaded:" + jsonString);
//		}
//		catch (JsonProcessingException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		
//		List<MatchRepresentation> matches = this.createMatchRepList();
//		int amountOfRulesCreated = generator.getAdaptationRules(matches, cloudEnvironment).length;
//		int expectedAmountOfRules = this.createRules().length;
//		assertEquals(amountOfRulesCreated, expectedAmountOfRules);
//	}
//	
//	private Rule[] createRules()
//	{
//		Rule[] rules = new Rule[8];
//		HenshinRuleCreator creator = new HenshinRuleCreator();
//		rules[0]=creator.createChangeRelationTargetRule(CloudModelPackageImpl.eINSTANCE.getVM(), CloudModelPackageImpl.eINSTANCE.getPM(), CloudModelPackageImpl.eINSTANCE.getVM_HostedByPM(), 1, 2, 1);
//		rules[1]=creator.createChangeRelationTargetRule(CloudModelPackageImpl.eINSTANCE.getVM(), CloudModelPackageImpl.eINSTANCE.getPM(), CloudModelPackageImpl.eINSTANCE.getVM_HostedByPM(), 2, 1, 2);
//		rules[2]=creator.createDeleteRelationRule(CloudModelPackageImpl.eINSTANCE.getVM(), CloudModelPackageImpl.eINSTANCE.getPM(), CloudModelPackageImpl.eINSTANCE.getVM_HostedByPM(), 1, 1);
//		rules[3]=creator.createDeleteRelationRule(CloudModelPackageImpl.eINSTANCE.getVM(), CloudModelPackageImpl.eINSTANCE.getPM(), CloudModelPackageImpl.eINSTANCE.getVM_HostedByPM(), 2, 2);
//		rules[4]=creator.createChangeAttributeRule(CloudModelPackageImpl.eINSTANCE.getRecord(), 1, CloudModelPackageImpl.eINSTANCE.getRecord_Encrypted(), "true");
//		rules[5]=creator.createChangeAttributeRule(CloudModelPackageImpl.eINSTANCE.getStored_data_set(), 1, CloudModelPackageImpl.eINSTANCE.getStored_data_set_Encrypted(), "true");
//		rules[6]=creator.createChangeAttributeRule(CloudModelPackageImpl.eINSTANCE.getDBMS(), 1, CloudModelPackageImpl.eINSTANCE.getDBMS_Encrypted(), "true");
//		return rules;
//	}
//	
//	private List<MatchRepresentation> createMatchRepList()
//	{
//		ArrayList<MatchRepresentation> matches = new ArrayList<MatchRepresentation>();
//		String ruleName ="unauthorizedAccess";
//		ArrayList<NodeRepresentation> nodes = new ArrayList<NodeRepresentation>();
//		nodes.add(new NodeRepresentation("Data_subject", 1));
//		nodes.add(new NodeRepresentation("Record", 1));
//		nodes.add(new NodeRepresentation("Stored_data_set", 1));
//		nodes.add(new NodeRepresentation("DBMS", 1));
//		nodes.add(new NodeRepresentation("Paas_operator", 1));
//		
//		String ruleName2 ="physicalDataLocation";
//		ArrayList<NodeRepresentation> nodes2 = new ArrayList<NodeRepresentation>();
//		nodes2.add(new NodeRepresentation("Data_subject", 1));
//		nodes2.add(new NodeRepresentation("Record", 1));
//		nodes2.add(new NodeRepresentation("Stored_data_set", 1));
//		nodes2.add(new NodeRepresentation("Database", 1));
//		nodes2.add(new NodeRepresentation("VM", 2));
//		nodes2.add(new NodeRepresentation("PM", 2));
//		nodes2.add(new NodeRepresentation("Atomic_component", 3));
//		
//		matches.add(new MatchRepresentation(ruleName, nodes));
//		matches.add(new MatchRepresentation(ruleName2, nodes2));
//		return matches;
//	}
//	
//	private CloudEnvironment createCloudEnvironmentObject()
//	{
//		//create nodes
//		CloudEnvironment cloudEnvironment = CloudModelFactory.eINSTANCE.createCloudEnvironment();
//		Data_subject dataSubject = CloudModelFactory.eINSTANCE.createData_subject();
//		Record record = CloudModelFactory.eINSTANCE.createRecord();
//		Stored_data_set storedDataSet = CloudModelFactory.eINSTANCE.createStored_data_set();
//		Database database = CloudModelFactory.eINSTANCE.createDatabase();
//		DBMS dbms = CloudModelFactory.eINSTANCE.createDBMS();
//		PaaS_operator paasOperator = CloudModelFactory.eINSTANCE.createPaaS_operator();
//		Atomic_component atomicComponent = CloudModelFactory.eINSTANCE.createAtomic_component();
//		PM pm = CloudModelFactory.eINSTANCE.createPM();
//		VM vm = CloudModelFactory.eINSTANCE.createVM();
//		PM pm2 = CloudModelFactory.eINSTANCE.createPM();
//		VM vm2 = CloudModelFactory.eINSTANCE.createVM();
//		
//		//set id for every node
//		dataSubject.setId(1);
//		record.setId(1);
//		storedDataSet.setId(1);
//		database.setId(1);
//		dbms.setId(1);
//		paasOperator.setId(1);
//		atomicComponent.setId(3);
//		pm.setId(1);
//		vm.setId(1);
//		pm2.setId(2);
//		vm2.setId(2);
//		
//		//set the other attributes
//		dataSubject.setCitizenOf("EU");
//		dataSubject.setLocation("US");
//		record.setSensitive(true);
//		record.setBiometricalData(true);
//		storedDataSet.setEncrypted(false);
//		storedDataSet.setSensitive(true);
//		dbms.setEncrypted(false);
//		dbms.setVm(vm2);
//		pm.setLocation("EU");
//		pm2.setLocation("US");
//
//		EList<EObject> actors = (EList<EObject>) cloudEnvironment.eGet(cloudEnvironment.eClass().getEStructuralFeature("actors"));
//		actors.add(dataSubject);
//		actors.add(paasOperator);
//		EList<EObject> infrastructure = (EList<EObject>) cloudEnvironment.eGet(cloudEnvironment.eClass().getEStructuralFeature("infrastructure"));
//		infrastructure.add(vm);
//		infrastructure.add(vm2);
//		infrastructure.add(pm);
//		infrastructure.add(pm2);
//		EList<EObject> middlewareElement = (EList<EObject>) cloudEnvironment.eGet(cloudEnvironment.eClass().getEStructuralFeature("middleware_element"));
//		middlewareElement.add(dbms);
//		EList<EObject> data = (EList<EObject>) cloudEnvironment.eGet(cloudEnvironment.eClass().getEStructuralFeature("data"));
//		data.add(record);
//		data.add(storedDataSet);
//		data.add(database);
//		EList<EObject> applications = (EList<EObject>) cloudEnvironment.eGet(cloudEnvironment.eClass().getEStructuralFeature("applications"));
//		applications.add(atomicComponent);
//		
//		dataSubject.getOwns().add(record);
//		record.setData_set(storedDataSet);
//		storedDataSet.setStoredIn(dbms);
//		storedDataSet.setStoredInDatabase(database);
//		dbms.setOperatedBy(paasOperator);
//		database.getAccessedByComponent().add(atomicComponent);
//		atomicComponent.setHostedByVM(vm2);
//		vm2.setHostedByPM(pm2);
//		vm.setHostedByPM(pm);
//		
//		Resource resource = this.createResource();
//		resource.getContents().add(cloudEnvironment);
//		saveResource(resource);
//		return cloudEnvironment;
//	}
//	
//	private Resource createResource()
//	{
//		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
//		Map<String, Object> map = reg.getExtensionToFactoryMap();
//		map.put("cloudmodel", new XMIResourceFactoryImpl());
//		
//		ResourceSet resSet = new ResourceSetImpl();
//		
//		Resource resource = resSet.createResource(URI.createURI("cloudmodel/test.cloudmodel"));
//		
//		return resource;
//	}
//	
//	public static void saveResource(Resource resource) 
//	{
//	     try 
//	     {
//	        resource.save(Collections.EMPTY_MAP);
//	     } 
//	     catch (IOException e) 
//	     {
//	        throw new RuntimeException(e);
//	     }
//	}
//}
