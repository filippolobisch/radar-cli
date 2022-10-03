package tosca;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.yaml.snakeyaml.Yaml;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import restassured.riskpattern.CloudModel.CloudEnvironment;
import runtime.EMFModelLoad;
import runtime.RuntimeModelLogic;

@RestController
public class ToscaExporter {
	@Autowired
	EMFModelLoad loader;
	@Autowired
	RuntimeModelLogic runtimeModelLogic;

	@RequestMapping(path = "/tosca/export/", method = RequestMethod.POST, consumes = "*/*; charset=UTF-8")
	@ResponseBody
	public String exportTosca(@RequestBody(required = true) String id) {
		try {
			String json = "";
			if (loader.getCloudEnvironmentsMonitored().get(Long.valueOf(id)) != null) {
				System.out.println("Export from run-time model with ID: " + id);
				json = runtimeModelLogic
						.returnentiremodelasjson(loader.getCloudEnvironmentsMonitored().get(Long.valueOf(id)));
			} else {
				System.out.println("Export from given test run-time model because ID does not exist!");
				json = "{\"type\":\"CloudEnvironment\",\"@id\":\"/\",\"tosca_nodes_root\":[{\"@id\":\"1\",\"id\":1,\"name\":\"DBServer_1\",\"type\":\"Compute\",\"jurisdiction\":\"DE\",\"hosts\":[{\"type\":\"DBMS\",\"referencedObjectID\":\"2\"}]},{\"@id\":\"2\",\"id\":2,\"name\":\"DBMS-DBServer_1\",\"type\":\"DBMS\",\"hosts\":[{\"type\":\"Database\",\"referencedObjectID\":\"3\"}],\"hostedOn\":{\"type\":\"Compute\",\"referencedObjectID\":\"1\"}},{\"@id\":\"3\",\"id\":3,\"name\":\"KnowGoDB_1\",\"type\":\"Database\",\"stores\":[{\"type\":\"StoredDataSet\",\"referencedObjectID\":\"4\"},{\"type\":\"StoredDataSet\",\"referencedObjectID\":\"5\"}],\"streamsFrom\":[{\"type\":\"DataFlow\",\"referencedObjectID\":\"6\"},{\"type\":\"DataFlow\",\"referencedObjectID\":\"7\"}],\"hostedOn\":{\"type\":\"DBMS\",\"referencedObjectID\":\"2\"}},{\"@id\":\"4\",\"id\":4,\"name\":\"DataSet-KnowGoDB-BiometricData_1\",\"type\":\"StoredDataSet\",\"belongsTo\":{\"type\":\"DataSubject\",\"referencedObjectID\":\"10\"},\"consistsOf\":[{\"type\":\"Record\",\"referencedObjectID\":\"8\"}],\"storedIn\":{\"type\":\"Database\",\"referencedObjectID\":\"3\"}},{\"@id\":\"5\",\"id\":5,\"name\":\"DataSet-KnowGoDB-CarData_1\",\"type\":\"StoredDataSet\",\"belongsTo\":{\"type\":\"DataSubject\",\"referencedObjectID\":\"10\"},\"consistsOf\":[{\"type\":\"Record\",\"referencedObjectID\":\"9\"}],\"storedIn\":{\"type\":\"Database\",\"referencedObjectID\":\"3\"}},{\"@id\":\"6\",\"id\":6,\"name\":\"DataFlow-BiometricData-KnowGoApp-KnowGoDB\",\"type\":\"DataFlow\",\"disabled\":false,\"belongsTo\":{\"type\":\"DataSubject\",\"referencedObjectID\":\"10\"},\"consistsOf\":[{\"type\":\"Record\",\"referencedObjectID\":\"8\"}],\"streamsTo\":[{\"type\":\"Database\",\"referencedObjectID\":\"3\"}]},{\"@id\":\"7\",\"id\":7,\"name\":\"DataFlow-CarData-KnowGoApp-KnowGoDB\",\"type\":\"DataFlow\",\"disabled\":false,\"belongsTo\":{\"type\":\"DataSubject\",\"referencedObjectID\":\"10\"},\"consistsOf\":[{\"type\":\"Record\",\"referencedObjectID\":\"9\"}],\"streamsTo\":[{\"type\":\"Database\",\"referencedObjectID\":\"3\"}]},{\"@id\":\"8\",\"id\":8,\"name\":\"BiometricData_1\",\"type\":\"Record\",\"sensitive\":true,\"partOf\":[{\"type\":\"DataFlow\",\"referencedObjectID\":\"6\"},{\"type\":\"StoredDataSet\",\"referencedObjectID\":\"4\"}]},{\"@id\":\"9\",\"id\":9,\"name\":\"CarData_1\",\"type\":\"Record\",\"sensitive\":false,\"partOf\":[{\"type\":\"DataFlow\",\"referencedObjectID\":\"7\"},{\"type\":\"StoredDataSet\",\"referencedObjectID\":\"5\"}]},{\"@id\":\"10\",\"id\":10,\"name\":\"Driver\",\"type\":\"DataSubject\",\"location\":\"DE\",\"owns\":[{\"type\":\"StoredDataSet\",\"referencedObjectID\":\"4\"},{\"type\":\"StoredDataSet\",\"referencedObjectID\":\"5\"},{\"type\":\"DataFlow\",\"referencedObjectID\":\"6\"},{\"type\":\"DataFlow\",\"referencedObjectID\":\"7\"}]}]}";
			}
			CloudEnvironment cloudEnvironment;
			cloudEnvironment = (CloudEnvironment) loader.loadEObjectFromString(json);
			System.out.println("Run-time model that will be exported:\n"
					+ runtimeModelLogic.returnentiremodelasjson(cloudEnvironment));

			JsonObject jsonToExport = new JsonParser().parse(json).getAsJsonObject();

			String yaml = createYamlOutOfJson(jsonToExport);
			System.out.println("Exported YAML as String\n" + yaml);

			BufferedWriter writer = new BufferedWriter(new FileWriter("exportedRunTimeModel.yaml"));
			writer.write(yaml);
			writer.close();

			return yaml;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "This shouln't be returned!!!";
	}

	private String createYamlOutOfJson(JsonObject jsonToExport) {
		Yaml yaml = new Yaml();

		Map<String, Object> yamlRoot = new HashMap<String, Object>();
		yamlRoot.put("tosca_definitions_version", "toscal_simple_yaml_1_0_0");
		yamlRoot.put("description", "\"Tosca simple profile with JDK.\"");
		yamlRoot.put("template_name", "jdk-type");
		yamlRoot.put("template_version", "1.0.0-SNAPSHOT");
		yamlRoot.put("template_author", "RestAssured");

		Map<String, Object> node_types = new HashMap<String, Object>();
		Map<String, Object> myTypesNodesRecord = new HashMap<String, Object>();
		myTypesNodesRecord.put("abstract", "# Optional");
		myTypesNodesRecord.put("derived_from", "tosca.nodes.root");
		myTypesNodesRecord.put("description", "# Optional");
		myTypesNodesRecord.put("tags", "# Optional");
		myTypesNodesRecord.put("properties", "# Optional");
		Map<String, Object> attributes1 = new HashMap<String, Object>();
		Map<String, Object> type1 = new HashMap<String, Object>();
		type1.put("type", "boolean");
		attributes1.put("sensitive", type1);
		myTypesNodesRecord.put("attributes", attributes1);
		Map<String, Object> requirements2 = new HashMap<String, Object>();
		Map<String, Object> type2 = new HashMap<String, Object>();
		type2.put("type", "my.types.nodes.dataset");
		requirements2.put("partOf", type2);
		myTypesNodesRecord.put("requirements", requirements2);
		myTypesNodesRecord.put("capabilitites", "# Optional");
		myTypesNodesRecord.put("interfaces", "# Optional");
		node_types.put("my.types.nodes.record", myTypesNodesRecord);

		Map<String, Object> capability_types = new HashMap<String, Object>();

		Map<String, Object> relationship_types = new HashMap<String, Object>();
		Map<String, Object> myTypesRelationshipsConsistsOf = new HashMap<String, Object>();
		myTypesRelationshipsConsistsOf.put("derived_from", "tosca.relationships.Root");
		myTypesRelationshipsConsistsOf.put("valid_target_types", "my.types.nodes.record");
		relationship_types.put("my.types.relationships.consistsOf", myTypesRelationshipsConsistsOf);

		Map<String, Object> topology_template = new HashMap<String, Object>();
		Map<String, Object> node_templates = new HashMap<String, Object>();
		topology_template.put("node_templates", node_templates);

		// loading things from json starts now
		JsonArray toscaNodesRoot = (JsonArray) jsonToExport.get("tosca_nodes_root");
		for (JsonElement objects : toscaNodesRoot) {
			String type = objects.getAsJsonObject().get("type").getAsString();
			String name = objects.getAsJsonObject().get("name").getAsString();
			Map<String, Object> objectToInsert = new HashMap<String, Object>();
			if (type.equals("Compute")) {
				objectToInsert.put("type", "my.types.nodes.mycompute");

				Map<String, Object> attributes = new HashMap<String, Object>();
				attributes.put("jurisdiction", objects.getAsJsonObject().get("jurisdiction").getAsString());
				objectToInsert.put("attributes", attributes);

				Map<String, Object> capabilities = new HashMap<String, Object>();
				JsonArray array = objects.getAsJsonObject().get("hosts").getAsJsonArray();
				for (JsonElement hosts : array) {
					String relationshipObjectId = hosts.getAsJsonObject().get("referencedObjectID").getAsString();
					for (JsonElement temp : toscaNodesRoot) {
						String id = temp.getAsJsonObject().get("id").getAsString();
						if (id.equals(relationshipObjectId)) {
							capabilities.put("host", temp.getAsJsonObject().get("name").getAsString());
						}
					}
				}
				objectToInsert.put("capabilities", capabilities);
			} else if (type.equals("DBMS")) {
				objectToInsert.put("type", "tosca.nodes.DBMS");

//				Map<String, Object> attributes = new HashMap<String, Object>();
//				attributes.put("jurisdiction", objects.getAsJsonObject().get("jurisdiction").getAsString());
//				objectToInsert.put("attributes", attributes);

				Map<String, Object> requirements = new HashMap<String, Object>();
				JsonObject obj = objects.getAsJsonObject().get("hostedOn").getAsJsonObject();
				String relationshipObjectId = obj.getAsJsonObject().get("referencedObjectID").getAsString();
				for (JsonElement temp : toscaNodesRoot) {
					String id = temp.getAsJsonObject().get("id").getAsString();
					if (id.equals(relationshipObjectId)) {
						requirements.put("host", temp.getAsJsonObject().get("name").getAsString());
					}
				}

				objectToInsert.put("requirements", requirements);

				Map<String, Object> capabilities = new HashMap<String, Object>();
				JsonArray array = objects.getAsJsonObject().get("hosts").getAsJsonArray();
				for (JsonElement hosts : array) {
					relationshipObjectId = hosts.getAsJsonObject().get("referencedObjectID").getAsString();
					for (JsonElement temp : toscaNodesRoot) {
						String id = temp.getAsJsonObject().get("id").getAsString();
						if (id.equals(relationshipObjectId)) {
							capabilities.put("host", temp.getAsJsonObject().get("name").getAsString());
						}
					}
				}
				objectToInsert.put("capabilities", capabilities);
			} else if (type.equals("Database")) {
				objectToInsert.put("type", "my.types.nodes.database");

//				Map<String, Object> attributes = new HashMap<String, Object>();
//				attributes.put("jurisdiction", objects.getAsJsonObject().get("jurisdiction").getAsString());
//				objectToInsert.put("attributes", attributes);

				Map<String, Object> requirements = new HashMap<String, Object>();
				JsonObject obj = objects.getAsJsonObject().get("hostedOn").getAsJsonObject();
				String relationshipObjectId = obj.getAsJsonObject().get("referencedObjectID").getAsString();
				for (JsonElement temp : toscaNodesRoot) {
					String id = temp.getAsJsonObject().get("id").getAsString();
					if (id.equals(relationshipObjectId)) {
						requirements.put("host", temp.getAsJsonObject().get("name").getAsString());
					}
				}

				objectToInsert.put("requirements", requirements);

				Map<String, Object> capabilities = new HashMap<String, Object>();
				LinkedList<String> insertList = new LinkedList<String>();
				JsonArray array = objects.getAsJsonObject().get("stores").getAsJsonArray();
				for (JsonElement hosts : array) {
					relationshipObjectId = hosts.getAsJsonObject().get("referencedObjectID").getAsString();
					for (JsonElement temp : toscaNodesRoot) {
						String id = temp.getAsJsonObject().get("id").getAsString();
						if (id.equals(relationshipObjectId)) {
							insertList.add(temp.getAsJsonObject().get("name").getAsString());
						}
					}
				}
				capabilities.put("stores", insertList);

				insertList = new LinkedList<String>();
				array = objects.getAsJsonObject().get("streamsFrom").getAsJsonArray();
				for (JsonElement hosts : array) {
					relationshipObjectId = hosts.getAsJsonObject().get("referencedObjectID").getAsString();
					for (JsonElement temp : toscaNodesRoot) {
						String id = temp.getAsJsonObject().get("id").getAsString();
						if (id.equals(relationshipObjectId)) {
							insertList.add(temp.getAsJsonObject().get("name").getAsString());
						}
					}
				}
				capabilities.put("streamsFrom", insertList);
				objectToInsert.put("capabilities", capabilities);
			} else if (type.equals("StoredDataSet")) {
				objectToInsert.put("type", "my.types.nodes.storeddataset");

//				Map<String, Object> attributes = new HashMap<String, Object>();
//				attributes.put("jurisdiction", objects.getAsJsonObject().get("jurisdiction").getAsString());
//				objectToInsert.put("attributes", attributes);

				Map<String, Object> requirements = new HashMap<String, Object>();
				JsonArray array = objects.getAsJsonObject().get("consistsOf").getAsJsonArray();
				for (JsonElement hosts : array) {
					String relationshipObjectId = hosts.getAsJsonObject().get("referencedObjectID").getAsString();
					for (JsonElement temp : toscaNodesRoot) {
						String id = temp.getAsJsonObject().get("id").getAsString();
						if (id.equals(relationshipObjectId)) {
							requirements.put("consistsOf", temp.getAsJsonObject().get("name").getAsString());
						}
					}
				}

				JsonObject obj = objects.getAsJsonObject().get("storedIn").getAsJsonObject();
				String relationshipObjectId = obj.getAsJsonObject().get("referencedObjectID").getAsString();
				for (JsonElement temp : toscaNodesRoot) {
					String id = temp.getAsJsonObject().get("id").getAsString();
					if (id.equals(relationshipObjectId)) {
						requirements.put("storedIn", temp.getAsJsonObject().get("name").getAsString());
					}
				}

				objectToInsert.put("requirements", requirements);

				Map<String, Object> capabilities = new HashMap<String, Object>();
				obj = objects.getAsJsonObject().get("belongsTo").getAsJsonObject();
				relationshipObjectId = obj.getAsJsonObject().get("referencedObjectID").getAsString();
				for (JsonElement temp : toscaNodesRoot) {
					String id = temp.getAsJsonObject().get("id").getAsString();
					if (id.equals(relationshipObjectId)) {
						capabilities.put("belongsTo", temp.getAsJsonObject().get("name").getAsString());
					}
				}
				objectToInsert.put("capabilities", capabilities);
			} else if (type.equals("DataFlow")) {
				objectToInsert.put("type", "my.types.nodes.dataflow");

				Map<String, Object> attributes = new HashMap<String, Object>();
				attributes.put("disabled", objects.getAsJsonObject().get("disabled").getAsBoolean());
				objectToInsert.put("attributes", attributes);

				Map<String, Object> requirements = new HashMap<String, Object>();
				JsonArray array = objects.getAsJsonObject().get("consistsOf").getAsJsonArray();
				for (JsonElement hosts : array) {
					String relationshipObjectId = hosts.getAsJsonObject().get("referencedObjectID").getAsString();
					for (JsonElement temp : toscaNodesRoot) {
						String id = temp.getAsJsonObject().get("id").getAsString();
						if (id.equals(relationshipObjectId)) {
							requirements.put("consistsOf", temp.getAsJsonObject().get("name").getAsString());
						}
					}
				}

				array = objects.getAsJsonObject().get("streamsTo").getAsJsonArray();
				for (JsonElement hosts : array) {
					String relationshipObjectId = hosts.getAsJsonObject().get("referencedObjectID").getAsString();
					for (JsonElement temp : toscaNodesRoot) {
						String id = temp.getAsJsonObject().get("id").getAsString();
						if (id.equals(relationshipObjectId)) {
							requirements.put("streamsTo", temp.getAsJsonObject().get("name").getAsString());
						}
					}
				}

				objectToInsert.put("requirements", requirements);

				Map<String, Object> capabilities = new HashMap<String, Object>();
				JsonObject obj = objects.getAsJsonObject().get("belongsTo").getAsJsonObject();
				String relationshipObjectId = obj.getAsJsonObject().get("referencedObjectID").getAsString();
				for (JsonElement temp : toscaNodesRoot) {
					String id = temp.getAsJsonObject().get("id").getAsString();
					if (id.equals(relationshipObjectId)) {
						capabilities.put("belongsTo", temp.getAsJsonObject().get("name").getAsString());
					}
				}
				objectToInsert.put("capabilities", capabilities);
			} else if (type.equals("Record")) {
				objectToInsert.put("type", "my.types.nodes.record");

				Map<String, Object> attributes = new HashMap<String, Object>();
				attributes.put("sensitive", objects.getAsJsonObject().get("sensitive").getAsBoolean());
				objectToInsert.put("attributes", attributes);

				Map<String, Object> requirements = new HashMap<String, Object>();
				LinkedList<String> insertList = new LinkedList<String>();
				JsonArray array = objects.getAsJsonObject().get("partOf").getAsJsonArray();
				for (JsonElement hosts : array) {
					String relationshipObjectId = hosts.getAsJsonObject().get("referencedObjectID").getAsString();
					for (JsonElement temp : toscaNodesRoot) {
						String id = temp.getAsJsonObject().get("id").getAsString();
						if (id.equals(relationshipObjectId)) {
							insertList.add(temp.getAsJsonObject().get("name").getAsString());
						}
					}
				}
				requirements.put("partOf", insertList);

				objectToInsert.put("requirements", requirements);

//				Map<String, Object> capabilities = new HashMap<String, Object>();
//				JsonObject obj = objects.getAsJsonObject().get("belongsTo").getAsJsonObject();
//				String relationshipObjectId = obj.getAsJsonObject().get("referencedObjectID").getAsString();
//				for (JsonElement temp : toscaNodesRoot) {
//					String id = temp.getAsJsonObject().get("id").getAsString();
//					if (id.equals(relationshipObjectId)) {
//						capabilities.put("belongsTo", temp.getAsJsonObject().get("name").getAsString());
//					}
//				}
//				objectToInsert.put("capabilities", capabilities);
			} else if (type.equals("DataSubject")) {
				objectToInsert.put("type", "my.types.nodes.datasubject");

				Map<String, Object> attributes = new HashMap<String, Object>();
				attributes.put("location", objects.getAsJsonObject().get("location").getAsString());
				objectToInsert.put("attributes", attributes);

//				Map<String, Object> requirements = new HashMap<String, Object>();
//				LinkedList<String> insertList = new LinkedList<String>();
//				JsonArray array = objects.getAsJsonObject().get("partOf").getAsJsonArray();
//				for (JsonElement hosts : array) {
//					String relationshipObjectId = hosts.getAsJsonObject().get("referencedObjectID").getAsString();
//					for (JsonElement temp : toscaNodesRoot) {
//						String id = temp.getAsJsonObject().get("id").getAsString();
//						if (id.equals(relationshipObjectId)) {
//							insertList.add(temp.getAsJsonObject().get("name").getAsString());
//						}
//					}
//				}
//				requirements.put("partOf", insertList);
//				
//				objectToInsert.put("requirements", requirements);

				Map<String, Object> capabilities = new HashMap<String, Object>();
				LinkedList<String> insertList = new LinkedList<String>();
				JsonArray array = objects.getAsJsonObject().get("owns").getAsJsonArray();
				for (JsonElement hosts : array) {
					String relationshipObjectId = hosts.getAsJsonObject().get("referencedObjectID").getAsString();
					for (JsonElement temp : toscaNodesRoot) {
						String id = temp.getAsJsonObject().get("id").getAsString();
						if (id.equals(relationshipObjectId)) {
							insertList.add(temp.getAsJsonObject().get("name").getAsString());
						}
					}
				}
				capabilities.put("owns", insertList);
				objectToInsert.put("capabilities", capabilities);
			}

			node_templates.put(name, objectToInsert);
		}

		yamlRoot.put("node_types", node_types);
		yamlRoot.put("capability_types", capability_types);
		yamlRoot.put("relationship_types", relationship_types);
		yamlRoot.put("topology_template", topology_template);

		return yaml.dumpAsMap(yamlRoot);
	}
}
