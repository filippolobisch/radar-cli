package tosca;

import java.util.ArrayList;
import java.util.HashMap;
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

import restassured.riskpattern.CloudModel.CloudEnvironment;
import runtime.EMFModelLoad;
import runtime.RuntimeModelLogic;

@RestController
public class ToscaImporter {

	@Autowired
	EMFModelLoad loader;
	@Autowired
	RuntimeModelLogic runtimeModelLogic;

	@RequestMapping(path = "/tosca/import/", method = RequestMethod.POST, consumes = "*/*; charset=UTF-8")
	@ResponseBody
	public String importTosca(@RequestBody(required = true) String yamlInput) {
		try {
			System.out.println("Imported YAML String\n" + yamlInput);
			String json = createJsonOutOfYaml(yamlInput);
			System.out.println("Imported and converted JSON String\n" + json.toString());

			CloudEnvironment cloudEnvironment;
			cloudEnvironment = (CloudEnvironment) loader.loadEObjectFromString(json);
			long id = loader.setCloudEnvironmentsMonitored(cloudEnvironment, 0);
			System.out.println("Loaded model after Tosca Import event as:\n"
					+ runtimeModelLogic.returnentiremodelasjson(cloudEnvironment));
			return String.valueOf(id);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "200";
	}

	private String createJsonOutOfYaml(String yamlInput) {
		JsonObject root = new JsonObject();
		root.addProperty("type", "CloudEnvironment");
		root.addProperty("@id", "/");

		JsonArray tosca_nodes_root = new JsonArray();

		Yaml yaml = new Yaml();
		Map<String, Object> toscaYamlRepresentation = yaml.load(yamlInput);
		Map<String, Object> topology_template = (Map<String, Object>) toscaYamlRepresentation.get("topology_template");
		Map<String, Object> node_templates = (Map<String, Object>) topology_template.get("node_templates");
		int id = 1;

		// Create Nodes with static attributes, type, name, id ...
		for (Map.Entry<String, Object> entry : node_templates.entrySet()) {
			String key = entry.getKey(); // Instance name
			Map<String, Object> value = (Map<String, Object>) entry.getValue(); // Object

			String type = value.get("type").toString();
			Map<String, Object> attributes;
			if (value.containsKey("attributes")) {
				attributes = (Map<String, Object>) value.get("attributes");
			} else {
				attributes = new HashMap<String, Object>();
			}

			JsonObject node = new JsonObject();
			node.addProperty("@id", String.valueOf(id));
			node.addProperty("id", id);
			node.addProperty("name", key);
			if (type.toLowerCase().contains("compute")) {
				// create a compute node in the json string
				node.addProperty("type", "Compute");
				for (Map.Entry<String, Object> attribute : attributes.entrySet()) {
					if (attribute.getKey().equals("jurisdiction")) {
						node.addProperty("jurisdiction", attribute.getValue().toString());
					}
				}
			} else if (type.toLowerCase().contains("dbms")) {
				node.addProperty("type", "DBMS");
			} else if (type.toLowerCase().contains("database")) {
				node.addProperty("type", "Database");
			} else if (type.toLowerCase().contains("storeddataset")) {
				node.addProperty("type", "StoredDataSet");
			} else if (type.toLowerCase().contains("dataflow")) {
				node.addProperty("type", "DataFlow");
				for (Map.Entry<String, Object> attribute : attributes.entrySet()) {
					if (attribute.getKey().equals("disabled")) {
						node.addProperty("disabled", Boolean.valueOf(attribute.getValue().toString()));
					}
				}
			} else if (type.toLowerCase().contains("record")) {
				node.addProperty("type", "Record");
				for (Map.Entry<String, Object> attribute : attributes.entrySet()) {
					if (attribute.getKey().equals("sensitive")) {
						node.addProperty("sensitive", Boolean.valueOf(attribute.getValue().toString()));
					}
				}
			} else if (type.toLowerCase().contains("datasubject")) {
				node.addProperty("type", "DataSubject");
				for (Map.Entry<String, Object> attribute : attributes.entrySet()) {
					if (attribute.getKey().equals("location")) {
						node.addProperty("location", attribute.getValue().toString());
					}
				}
			}
			id++;
			tosca_nodes_root.add(node);
		}

		// Create Relationship between objects
		for (Map.Entry<String, Object> entry : node_templates.entrySet()) {
			String key = entry.getKey(); // Instance name
			Map<String, Object> value = (Map<String, Object>) entry.getValue(); // Object

			String type = value.get("type").toString();
			Map<String, Object> capabilities;
			if (value.containsKey("capabilities")) {
				capabilities = (Map<String, Object>) value.get("capabilities");
			} else {
				capabilities = new HashMap<String, Object>();
			}
			Map<String, Object> requirements;
			if (value.containsKey("requirements")) {
				requirements = (Map<String, Object>) value.get("requirements");
			} else {
				requirements = new HashMap<String, Object>();
			}

			JsonObject relationFrom = null;
			for (JsonElement temp : tosca_nodes_root) {
				if (temp.getAsJsonObject().get("name").getAsString().equals(key)) {
					relationFrom = (JsonObject) temp;
				}
			}
			if (relationFrom == null) {
				System.err.println("Object relationFrom does not exist!");
			}

			if (type.toLowerCase().contains("compute")) {
				for (Map.Entry<String, Object> capability : capabilities.entrySet()) {
					if (capability.getKey().equals("host")) {
						JsonArray hosts = new JsonArray();
						if (capability.getValue().getClass().isAssignableFrom(ArrayList.class)) {
							ArrayList<String> list = (ArrayList<String>) capability.getValue();
							for (String s : list) {
								JsonObject relationTo = null;
								for (JsonElement temp : tosca_nodes_root) {
									if (temp.getAsJsonObject().get("name").getAsString().equals(s)) {
										relationTo = (JsonObject) temp;
									}
								}
								if (relationTo == null) {
									System.err.println("Object relationTo does not exist!");
								}
								JsonObject toAdd = new JsonObject();
								toAdd.addProperty("type", relationTo.get("type").getAsString());
								toAdd.addProperty("referencedObjectID", relationTo.get("@id").getAsString());
								hosts.add(toAdd);
							}
						} else {
							JsonObject relationTo = null;
							for (JsonElement temp : tosca_nodes_root) {
								if (temp.getAsJsonObject().get("name").getAsString().equals(capability.getValue())) {
									relationTo = (JsonObject) temp;
								}
							}
							if (relationTo == null) {
								System.err.println("Object relationTo does not exist!");
							}
							JsonObject toAdd = new JsonObject();
							toAdd.addProperty("type", relationTo.get("type").getAsString());
							toAdd.addProperty("referencedObjectID", relationTo.get("@id").getAsString());
							hosts.add(toAdd);
						}
						relationFrom.add("hosts", hosts);
					}
				}
				for (Map.Entry<String, Object> requirement : requirements.entrySet()) {
					// There is no requirements tab at the moment
				}
			} else if (type.toLowerCase().contains("dbms")) {
				for (Map.Entry<String, Object> capability : capabilities.entrySet()) {
					if (capability.getKey().equals("host")) {
						JsonArray hosts = new JsonArray();
						if (capability.getValue().getClass().isAssignableFrom(ArrayList.class)) {
							ArrayList<String> list = (ArrayList<String>) capability.getValue();
							for (String s : list) {
								JsonObject relationTo = null;
								for (JsonElement temp : tosca_nodes_root) {
									if (temp.getAsJsonObject().get("name").getAsString().equals(s)) {
										relationTo = (JsonObject) temp;
									}
								}
								if (relationTo == null) {
									System.err.println("Object relationTo does not exist!");
								}
								JsonObject toAdd = new JsonObject();
								toAdd.addProperty("type", relationTo.get("type").getAsString());
								toAdd.addProperty("referencedObjectID", relationTo.get("@id").getAsString());
								hosts.add(toAdd);
							}
						} else {
							JsonObject relationTo = null;
							for (JsonElement temp : tosca_nodes_root) {
								if (temp.getAsJsonObject().get("name").getAsString().equals(capability.getValue())) {
									relationTo = (JsonObject) temp;
								}
							}
							if (relationTo == null) {
								System.err.println("Object relationTo does not exist!");
							}
							JsonObject toAdd = new JsonObject();
							toAdd.addProperty("type", relationTo.get("type").getAsString());
							toAdd.addProperty("referencedObjectID", relationTo.get("@id").getAsString());
							hosts.add(toAdd);
						}
						relationFrom.add("hosts", hosts);
					}
				}
				for (Map.Entry<String, Object> requirement : requirements.entrySet()) {
					if (requirement.getKey().equals("host")) {
						if (requirement.getValue().getClass().isAssignableFrom(ArrayList.class)) {
							ArrayList<String> list = (ArrayList<String>) requirement.getValue();
							for (String s : list) {
								JsonObject relationTo = null;
								for (JsonElement temp : tosca_nodes_root) {
									if (temp.getAsJsonObject().get("name").getAsString().equals(s)) {
										relationTo = (JsonObject) temp;
									}
								}
								if (relationTo == null) {
									System.err.println("Object relationTo does not exist!");
								}
								JsonObject toAdd = new JsonObject();
								toAdd.addProperty("type", relationTo.get("type").getAsString());
								toAdd.addProperty("referencedObjectID", relationTo.get("@id").getAsString());
								relationFrom.add("hostedOn", toAdd);
							}
						} else {
							JsonObject relationTo = null;
							for (JsonElement temp : tosca_nodes_root) {
								if (temp.getAsJsonObject().get("name").getAsString().equals(requirement.getValue())) {
									relationTo = (JsonObject) temp;
								}
							}
							if (relationTo == null) {
								System.err.println("Object relationTo does not exist!");
							}
							JsonObject toAdd = new JsonObject();
							toAdd.addProperty("type", relationTo.get("type").getAsString());
							toAdd.addProperty("referencedObjectID", relationTo.get("@id").getAsString());
							relationFrom.add("hostedOn", toAdd);
						}
					}
				}
			} else if (type.toLowerCase().contains("database")) {
				for (Map.Entry<String, Object> capability : capabilities.entrySet()) {
					if (capability.getKey().equals("stores")) {
						JsonArray stores = new JsonArray();
						if (capability.getValue().getClass().isAssignableFrom(ArrayList.class)) {
							ArrayList<String> list = (ArrayList<String>) capability.getValue();
							for (String s : list) {
								JsonObject relationTo = null;
								for (JsonElement temp : tosca_nodes_root) {
									if (temp.getAsJsonObject().get("name").getAsString().equals(s)) {
										relationTo = (JsonObject) temp;
									}
								}
								if (relationTo == null) {
									System.err.println("Object relationTo does not exist!");
								}
								JsonObject toAdd = new JsonObject();
								toAdd.addProperty("type", relationTo.get("type").getAsString());
								toAdd.addProperty("referencedObjectID", relationTo.get("@id").getAsString());
								stores.add(toAdd);
							}
						} else {
							JsonObject relationTo = null;
							for (JsonElement temp : tosca_nodes_root) {
								if (temp.getAsJsonObject().get("name").getAsString().equals(capability.getValue())) {
									relationTo = (JsonObject) temp;
								}
							}
							if (relationTo == null) {
								System.err.println("Object relationTo does not exist!");
							}
							JsonObject toAdd = new JsonObject();
							toAdd.addProperty("type", relationTo.get("type").getAsString());
							toAdd.addProperty("referencedObjectID", relationTo.get("@id").getAsString());
							stores.add(toAdd);
						}
						relationFrom.add("stores", stores);
					}
					if (capability.getKey().equals("streamsFrom")) {
						JsonArray streamsFrom = new JsonArray();
						if (capability.getValue().getClass().isAssignableFrom(ArrayList.class)) {
							ArrayList<String> list = (ArrayList<String>) capability.getValue();
							for (String s : list) {
								JsonObject relationTo = null;
								for (JsonElement temp : tosca_nodes_root) {
									if (temp.getAsJsonObject().get("name").getAsString().equals(s)) {
										relationTo = (JsonObject) temp;
									}
								}
								if (relationTo == null) {
									System.err.println("Object relationTo does not exist!");
								}
								JsonObject toAdd = new JsonObject();
								toAdd.addProperty("type", relationTo.get("type").getAsString());
								toAdd.addProperty("referencedObjectID", relationTo.get("@id").getAsString());
								streamsFrom.add(toAdd);
							}
						} else {
							JsonObject relationTo = null;
							for (JsonElement temp : tosca_nodes_root) {
								if (temp.getAsJsonObject().get("name").getAsString().equals(capability.getValue())) {
									relationTo = (JsonObject) temp;
								}
							}
							if (relationTo == null) {
								System.err.println("Object relationTo does not exist!");
							}
							JsonObject toAdd = new JsonObject();
							toAdd.addProperty("type", relationTo.get("type").getAsString());
							toAdd.addProperty("referencedObjectID", relationTo.get("@id").getAsString());
							streamsFrom.add(toAdd);
						}
						relationFrom.add("streamsFrom", streamsFrom);
					}
				}
				for (Map.Entry<String, Object> requirement : requirements.entrySet()) {
					if (requirement.getKey().equals("host")) {
						if (requirement.getValue().getClass().isAssignableFrom(ArrayList.class)) {
							ArrayList<String> list = (ArrayList<String>) requirement.getValue();
							for (String s : list) {
								JsonObject relationTo = null;
								for (JsonElement temp : tosca_nodes_root) {
									if (temp.getAsJsonObject().get("name").getAsString().equals(s)) {
										relationTo = (JsonObject) temp;
									}
								}
								if (relationTo == null) {
									System.err.println("Object relationTo does not exist!");
								}
								JsonObject toAdd = new JsonObject();
								toAdd.addProperty("type", relationTo.get("type").getAsString());
								toAdd.addProperty("referencedObjectID", relationTo.get("@id").getAsString());
								relationFrom.add("hostedOn", toAdd);
							}
						} else {
							JsonObject relationTo = null;
							for (JsonElement temp : tosca_nodes_root) {
								if (temp.getAsJsonObject().get("name").getAsString().equals(requirement.getValue())) {
									relationTo = (JsonObject) temp;
								}
							}
							if (relationTo == null) {
								System.err.println("Object relationTo does not exist!");
							}
							JsonObject toAdd = new JsonObject();
							toAdd.addProperty("type", relationTo.get("type").getAsString());
							toAdd.addProperty("referencedObjectID", relationTo.get("@id").getAsString());
							relationFrom.add("hostedOn", toAdd);
						}
					}
				}
			} else if (type.toLowerCase().contains("storeddataset")) {
				for (Map.Entry<String, Object> capability : capabilities.entrySet()) {
					if (capability.getKey().equals("belongsTo")) {
						if (capability.getValue().getClass().isAssignableFrom(ArrayList.class)) {
							ArrayList<String> list = (ArrayList<String>) capability.getValue();
							for (String s : list) {
								JsonObject relationTo = null;
								for (JsonElement temp : tosca_nodes_root) {
									if (temp.getAsJsonObject().get("name").getAsString().equals(s)) {
										relationTo = (JsonObject) temp;
									}
								}
								if (relationTo == null) {
									System.err.println("Object relationTo does not exist!");
								}
								JsonObject toAdd = new JsonObject();
								toAdd.addProperty("type", relationTo.get("type").getAsString());
								toAdd.addProperty("referencedObjectID", relationTo.get("@id").getAsString());
								relationFrom.add("belongsTo", toAdd);
							}
						} else {
							JsonObject relationTo = null;
							for (JsonElement temp : tosca_nodes_root) {
								if (temp.getAsJsonObject().get("name").getAsString().equals(capability.getValue())) {
									relationTo = (JsonObject) temp;
								}
							}
							if (relationTo == null) {
								System.err.println("Object relationTo does not exist!");
							}
							JsonObject toAdd = new JsonObject();
							toAdd.addProperty("type", relationTo.get("type").getAsString());
							toAdd.addProperty("referencedObjectID", relationTo.get("@id").getAsString());
							relationFrom.add("belongsTo", toAdd);
						}
					}
				}
				for (Map.Entry<String, Object> requirement : requirements.entrySet()) {
					if (requirement.getKey().equals("consistsOf")) {
						JsonArray consistsOf = new JsonArray();
						if (requirement.getValue().getClass().isAssignableFrom(ArrayList.class)) {
							ArrayList<String> list = (ArrayList<String>) requirement.getValue();
							for (String s : list) {
								JsonObject relationTo = null;
								for (JsonElement temp : tosca_nodes_root) {
									if (temp.getAsJsonObject().get("name").getAsString().equals(s)) {
										relationTo = (JsonObject) temp;
									}
								}
								if (relationTo == null) {
									System.err.println("Object relationTo does not exist!");
								}
								JsonObject toAdd = new JsonObject();
								toAdd.addProperty("type", relationTo.get("type").getAsString());
								toAdd.addProperty("referencedObjectID", relationTo.get("@id").getAsString());
								consistsOf.add(toAdd);
							}
						} else {
							JsonObject relationTo = null;
							for (JsonElement temp : tosca_nodes_root) {
								if (temp.getAsJsonObject().get("name").getAsString().equals(requirement.getValue())) {
									relationTo = (JsonObject) temp;
								}
							}
							if (relationTo == null) {
								System.err.println("Object relationTo does not exist!");
							}
							JsonObject toAdd = new JsonObject();
							toAdd.addProperty("type", relationTo.get("type").getAsString());
							toAdd.addProperty("referencedObjectID", relationTo.get("@id").getAsString());
							consistsOf.add(toAdd);
						}
						relationFrom.add("consistsOf", consistsOf);
					}
					if (requirement.getKey().equals("storedIn")) {
						if (requirement.getValue().getClass().isAssignableFrom(ArrayList.class)) {
							ArrayList<String> list = (ArrayList<String>) requirement.getValue();
							for (String s : list) {
								JsonObject relationTo = null;
								for (JsonElement temp : tosca_nodes_root) {
									if (temp.getAsJsonObject().get("name").getAsString().equals(s)) {
										relationTo = (JsonObject) temp;
									}
								}
								if (relationTo == null) {
									System.err.println("Object relationTo does not exist!");
								}
								JsonObject toAdd = new JsonObject();
								toAdd.addProperty("type", relationTo.get("type").getAsString());
								toAdd.addProperty("referencedObjectID", relationTo.get("@id").getAsString());
								relationFrom.add("storedIn", toAdd);
							}
						} else {
							JsonObject relationTo = null;
							for (JsonElement temp : tosca_nodes_root) {
								if (temp.getAsJsonObject().get("name").getAsString().equals(requirement.getValue())) {
									relationTo = (JsonObject) temp;
								}
							}
							if (relationTo == null) {
								System.err.println("Object relationTo does not exist!");
							}
							JsonObject toAdd = new JsonObject();
							toAdd.addProperty("type", relationTo.get("type").getAsString());
							toAdd.addProperty("referencedObjectID", relationTo.get("@id").getAsString());
							relationFrom.add("storedIn", toAdd);
						}
					}
				}
			} else if (type.toLowerCase().contains("dataflow")) {
				for (Map.Entry<String, Object> capability : capabilities.entrySet()) {
					if (capability.getKey().equals("belongsTo")) {
						if (capability.getValue().getClass().isAssignableFrom(ArrayList.class)) {
							ArrayList<String> list = (ArrayList<String>) capability.getValue();
							for (String s : list) {
								JsonObject relationTo = null;
								for (JsonElement temp : tosca_nodes_root) {
									if (temp.getAsJsonObject().get("name").getAsString().equals(s)) {
										relationTo = (JsonObject) temp;
									}
								}
								if (relationTo == null) {
									System.err.println("Object relationTo does not exist!");
								}
								JsonObject toAdd = new JsonObject();
								toAdd.addProperty("type", relationTo.get("type").getAsString());
								toAdd.addProperty("referencedObjectID", relationTo.get("@id").getAsString());
								relationFrom.add("belongsTo", toAdd);
							}
						} else {
							JsonObject relationTo = null;
							for (JsonElement temp : tosca_nodes_root) {
								if (temp.getAsJsonObject().get("name").getAsString().equals(capability.getValue())) {
									relationTo = (JsonObject) temp;
								}
							}
							if (relationTo == null) {
								System.err.println("Object relationTo does not exist!");
							}
							JsonObject toAdd = new JsonObject();
							toAdd.addProperty("type", relationTo.get("type").getAsString());
							toAdd.addProperty("referencedObjectID", relationTo.get("@id").getAsString());
							relationFrom.add("belongsTo", toAdd);
						}
					}
				}
				for (Map.Entry<String, Object> requirement : requirements.entrySet()) {
					if (requirement.getKey().equals("consistsOf")) {
						JsonArray consistsOf = new JsonArray();
						if (requirement.getValue().getClass().isAssignableFrom(ArrayList.class)) {
							ArrayList<String> list = (ArrayList<String>) requirement.getValue();
							for (String s : list) {
								JsonObject relationTo = null;
								for (JsonElement temp : tosca_nodes_root) {
									if (temp.getAsJsonObject().get("name").getAsString().equals(s)) {
										relationTo = (JsonObject) temp;
									}
								}
								if (relationTo == null) {
									System.err.println("Object relationTo does not exist!");
								}
								JsonObject toAdd = new JsonObject();
								toAdd.addProperty("type", relationTo.get("type").getAsString());
								toAdd.addProperty("referencedObjectID", relationTo.get("@id").getAsString());
								consistsOf.add(toAdd);
							}
						} else {
							JsonObject relationTo = null;
							for (JsonElement temp : tosca_nodes_root) {
								if (temp.getAsJsonObject().get("name").getAsString().equals(requirement.getValue())) {
									relationTo = (JsonObject) temp;
								}
							}
							if (relationTo == null) {
								System.err.println("Object relationTo does not exist!");
							}
							JsonObject toAdd = new JsonObject();
							toAdd.addProperty("type", relationTo.get("type").getAsString());
							toAdd.addProperty("referencedObjectID", relationTo.get("@id").getAsString());
							consistsOf.add(toAdd);
						}
						relationFrom.add("consistsOf", consistsOf);
					}
					if (requirement.getKey().equals("streamsTo")) {
						JsonArray streamsTo = new JsonArray();
						if (requirement.getValue().getClass().isAssignableFrom(ArrayList.class)) {
							ArrayList<String> list = (ArrayList<String>) requirement.getValue();
							for (String s : list) {
								JsonObject relationTo = null;
								for (JsonElement temp : tosca_nodes_root) {
									if (temp.getAsJsonObject().get("name").getAsString().equals(s)) {
										relationTo = (JsonObject) temp;
									}
								}
								if (relationTo == null) {
									System.err.println("Object relationTo does not exist!");
								}
								JsonObject toAdd = new JsonObject();
								toAdd.addProperty("type", relationTo.get("type").getAsString());
								toAdd.addProperty("referencedObjectID", relationTo.get("@id").getAsString());
								streamsTo.add(toAdd);
							}
						} else {
							JsonObject relationTo = null;
							for (JsonElement temp : tosca_nodes_root) {
								if (temp.getAsJsonObject().get("name").getAsString().equals(requirement.getValue())) {
									relationTo = (JsonObject) temp;
								}
							}
							if (relationTo == null) {
								System.err.println("Object relationTo does not exist!");
							}
							JsonObject toAdd = new JsonObject();
							toAdd.addProperty("type", relationTo.get("type").getAsString());
							toAdd.addProperty("referencedObjectID", relationTo.get("@id").getAsString());
							streamsTo.add(toAdd);
						}
						relationFrom.add("streamsTo", streamsTo);
					}
				}
			} else if (type.toLowerCase().contains("record")) {
				for (Map.Entry<String, Object> capability : capabilities.entrySet()) {
					// There is no capability at the moment
				}
				for (Map.Entry<String, Object> requirement : requirements.entrySet()) {
					if (requirement.getKey().equals("partOf")) {
						JsonArray partOf = new JsonArray();
						if (requirement.getValue().getClass().isAssignableFrom(ArrayList.class)) {
							ArrayList<String> list = (ArrayList<String>) requirement.getValue();
							for (String s : list) {
								JsonObject relationTo = null;
								for (JsonElement temp : tosca_nodes_root) {
									if (temp.getAsJsonObject().get("name").getAsString().equals(s)) {
										relationTo = (JsonObject) temp;
									}
								}
								if (relationTo == null) {
									System.err.println("Object relationTo does not exist!");
								}
								JsonObject toAdd = new JsonObject();
								toAdd.addProperty("type", relationTo.get("type").getAsString());
								toAdd.addProperty("referencedObjectID", relationTo.get("@id").getAsString());
								partOf.add(toAdd);
							}
						} else {
							JsonObject relationTo = null;
							for (JsonElement temp : tosca_nodes_root) {
								if (temp.getAsJsonObject().get("name").getAsString().equals(requirement.getValue())) {
									relationTo = (JsonObject) temp;
								}
							}
							if (relationTo == null) {
								System.err.println("Object relationTo does not exist!");
							}
							JsonObject toAdd = new JsonObject();
							toAdd.addProperty("type", relationTo.get("type").getAsString());
							toAdd.addProperty("referencedObjectID", relationTo.get("@id").getAsString());
							partOf.add(toAdd);
						}
						relationFrom.add("partOf", partOf);
					}
				}
			} else if (type.toLowerCase().contains("datasubject")) {
				for (Map.Entry<String, Object> capability : capabilities.entrySet()) {
					if (capability.getKey().equals("owns")) {
						JsonArray owns = new JsonArray();
						if (capability.getValue().getClass().isAssignableFrom(ArrayList.class)) {
							ArrayList<String> list = (ArrayList<String>) capability.getValue();
							for (String s : list) {
								JsonObject relationTo = null;
								for (JsonElement temp : tosca_nodes_root) {
									if (temp.getAsJsonObject().get("name").getAsString().equals(s)) {
										relationTo = (JsonObject) temp;
									}
								}
								if (relationTo == null) {
									System.err.println("Object relationTo does not exist!");
								}
								JsonObject toAdd = new JsonObject();
								toAdd.addProperty("type", relationTo.get("type").getAsString());
								toAdd.addProperty("referencedObjectID", relationTo.get("@id").getAsString());
								owns.add(toAdd);
							}
						} else {
							JsonObject relationTo = null;
							for (JsonElement temp : tosca_nodes_root) {
								if (temp.getAsJsonObject().get("name").getAsString().equals(capability.getValue())) {
									relationTo = (JsonObject) temp;
								}
							}
							if (relationTo == null) {
								System.err.println("Object relationTo does not exist!");
							}
							JsonObject toAdd = new JsonObject();
							toAdd.addProperty("type", relationTo.get("type").getAsString());
							toAdd.addProperty("referencedObjectID", relationTo.get("@id").getAsString());
							owns.add(toAdd);
						}
						relationFrom.add("owns", owns);
					}
				}
				for (Map.Entry<String, Object> requirement : requirements.entrySet()) {
					// there is no requirement at the moment
				}
			}

		}

		root.add("tosca_nodes_root", tosca_nodes_root);
		return root.toString();
	}
}
