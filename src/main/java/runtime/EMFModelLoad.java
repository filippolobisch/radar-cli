package runtime;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.henshin.interpreter.EGraph;
import org.eclipse.emf.henshin.interpreter.impl.EGraphImpl;
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;
import org.emfjson.jackson.annotations.EcoreReferenceInfo;
import org.emfjson.jackson.annotations.EcoreTypeInfo;
import org.emfjson.jackson.databind.EMFContext;
import org.emfjson.jackson.module.EMFModule;
import org.emfjson.jackson.resource.JsonResourceFactory;
import org.emfjson.jackson.utils.ValueReader;
import org.emfjson.jackson.utils.ValueWriter;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;

import restassured.riskpattern.CloudModel.CloudEnvironment;
import restassured.riskpattern.CloudModel.impl.CloudModelPackageImpl;
import utility.Constants;

@Component
public class EMFModelLoad {
	private HashMap<Long, CloudEnvironment> cloudEnvironmentsMonitored = new HashMap<Long, CloudEnvironment>();
	private ObjectMapper mapper;
	private HenshinResourceSet resourceSet;
	private long countInstances;

	public HenshinResourceSet getResourceSet() {
		return resourceSet;
	}

	public void setResourceSet(HenshinResourceSet resourceSet) {
		this.resourceSet = resourceSet;
	}

	// Prepares ResourceSet and Mapper and initializes attributes
	public EMFModelLoad() {
		HenshinResourceSet resourceSet = new HenshinResourceSet(Constants.RUNTIME_MODEL_PATH);
		resourceSet.getPackageRegistry().put(CloudModelPackageImpl.eNS_URI, CloudModelPackageImpl.eINSTANCE);
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("cloudmodel", new XMIResourceFactoryImpl());

		ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

		EMFModule module = new EMFModule();
		module.configure(EMFModule.Feature.OPTION_USE_ID, true);
		module.configure(EMFModule.Feature.OPTION_SERIALIZE_DEFAULT_VALUE, true);
		ValueReader<String, EClass> valueReader = new ValueReader<String, EClass>() {
			public EClass readValue(String value, DeserializationContext context) {
				return (EClass) CloudModelPackageImpl.eINSTANCE.getEClassifier(value);
			}
		};
		ValueWriter<EClass, String> valueWriter = new ValueWriter<EClass, String>() {

			public String writeValue(EClass value, SerializerProvider context) {
				// Auto-generated method stub
				return value.getName();
			}
		};
		module.setTypeInfo(new EcoreTypeInfo("type", valueReader, valueWriter));
		module.setReferenceInfo(new EcoreReferenceInfo("referencedObjectID"));

		mapper.registerModule(module);
		JsonResourceFactory factory = new JsonResourceFactory(mapper);
		mapper = factory.getMapper();

		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("json", factory);

		this.mapper = mapper;
		this.resourceSet = resourceSet;
		this.countInstances = 0;
	}

	// load JSON from file and return CloudEnvironment object
	public CloudEnvironment loadJSON(String jsonModelFileName) {
		EGraph graph = new EGraphImpl(resourceSet.getResource(jsonModelFileName));
		System.out.println(graph.getRoots().get(0));
		CloudEnvironment cloudEnvironment = (CloudEnvironment) graph.getRoots().get(0);
		return cloudEnvironment;
	}

	// Used to create an EObject out of a JSON Representation - needs to fit with
	// our meta model
	public EObject loadEObjectFromString(String model) throws IOException {
		JsonResourceFactory factory = new JsonResourceFactory(mapper);
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION, factory);
		resourceSet.getPackageRegistry().put(CloudModelPackageImpl.eNS_URI, CloudModelPackageImpl.eINSTANCE);
		Resource resource = resourceSet.createResource(URI.createURI("cloudmodel"));
		InputStream stream = new ByteArrayInputStream(model.getBytes(StandardCharsets.UTF_8));
		resource.load(stream, null);
		stream.close();
		return resource.getContents().get(0);
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// The cloudmodel file, generated by this method, does not contain any
	// relations.
	// Please check this on your end as well.

	// Saves an EObject (root of a Tree) as a File (represented in XMI Format)
	public void saveEObjectToXMI(EObject toSave, long id) {
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("cloudmodel",
				new XMIResourceFactoryImpl());
		resourceSet.saveEObject(toSave, "ServiceInstance" + id + ".cloudmodel");
		;
	}

	public void saveEObjectToXMI(EObject toSave, String name) {
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("cloudmodel",
				new XMIResourceFactoryImpl());
		resourceSet.saveEObject(toSave, name + ".cloudmodel");
		;
	}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public ObjectMapper getMapper() {
		return mapper;
	}

	public void setMapper(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	public HashMap<Long, CloudEnvironment> getCloudEnvironmentsMonitored() {
		return cloudEnvironmentsMonitored;
	}

	// Saves a cloudEnvironmentObject (Root of EMF Tree)
	// If id is "0" a new ID will be created otherwise the existing object will be
	// overwritten
	public long setCloudEnvironmentsMonitored(CloudEnvironment cloudEnvironmentMonitored, long id) {
		if (id == 0) {
			countInstances++;
			this.cloudEnvironmentsMonitored.put(countInstances, cloudEnvironmentMonitored);
			this.saveEObjectToXMI(cloudEnvironmentMonitored, countInstances);
//			new ModelAdapter().saveGraph(new EGraphImpl(cloudEnvironmentMonitored), 2);
			return countInstances;
		} else {
			this.cloudEnvironmentsMonitored.put(id, cloudEnvironmentMonitored);
			this.saveEObjectToXMI(cloudEnvironmentMonitored, id);
			return id;
		}
	}

	// Deletes Instance out of Map and in the FileSystem
	public void deleteCloundEnvironmentMonitored(long id, RuntimeModelLogic logic) {
		this.cloudEnvironmentsMonitored.remove(id);
		File file = new File("models/runtime_models/ServiceInstance" + id + ".cloudmodel");
		if (!file.delete()) {
			System.out.println("File with id " + id + " doesn't exist");
		}
	}

}
