package runtime;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import restassured.riskpattern.CloudModel.CloudEnvironment;
import restassured.riskpattern.CloudModel.tosca_nodes_Root;

@Component
public class RuntimeModelLogic {

	@Autowired
	private EMFModelLoad loader;


	public RuntimeModelLogic() {
		EPackage.Registry.INSTANCE.getEPackage("www.CloudModel.com");
	}

	// Returns the instance for id "i" as a JSON Represenation
	public String returnentiremodelasjson(long i) {
		// Accessing the model information
		String jsonString = "";
		try {
			jsonString = loader.getMapper().writerWithDefaultPrettyPrinter()
					.writeValueAsString(loader.getCloudEnvironmentsMonitored().get(i));
			jsonString = jsonString.replaceAll("#", "");
		} catch (JsonProcessingException e) {
			jsonString = "FAILURE";
			e.printStackTrace();
		}
//		System.out.println(jsonString);
		return jsonString;
	}

	// Returns a JSON Representation of a .cloudmodel file
	public String returnentiremodelasjsonFromFile(String jsonModelFileName) {
		// Accessing the model information
		String jsonString = "";
		try {
			CloudEnvironment ce = loader.loadJSON(jsonModelFileName);
			jsonString = loader.getMapper().writerWithDefaultPrettyPrinter()
					.writeValueAsString(ce);
			jsonString = jsonString.replaceAll("#", "");
		} catch (JsonProcessingException e) {
			jsonString = "FAILURE";
			e.printStackTrace();
		}
//		System.out.println(jsonString);
		return jsonString;
	}

	// Returns the CloudEnvironment object as a JSON Represenation
	public String returnentiremodelasjson(CloudEnvironment proposal) {
		// Accessing the model information
		String jsonString = "";
		try {
			jsonString = loader.getMapper().writerWithDefaultPrettyPrinter().writeValueAsString(proposal);
			jsonString = jsonString.replaceAll("#", "");
		} catch (JsonProcessingException e) {
			jsonString = "FAILURE";
			e.printStackTrace();
		}
//			System.out.println(jsonString);
		return jsonString;
	}

	// Returns every instance from the set of monitored CloudEnvironments
	public String returnentiremodelasjsonforall() {
		String everyInstance = "";
		for (Long i : loader.getCloudEnvironmentsMonitored().keySet()) {
			everyInstance += "Instance with id " + i + ":\n" + this.returnentiremodelasjson(i) + "\n\n";
		}
		return everyInstance;
	}

	// Post Request to given URL with given String as Body
	public int makeRequest(String url, String requestBody) throws IOException {
//		System.out.println(requestBody);
		HttpURLConnection request;
		URL restUrl = new URL(url);
		request = (HttpURLConnection) restUrl.openConnection();
		request.setRequestMethod("POST");
		request.setDoOutput(true);
		Charset encoding = Charset.forName("UTF-8");
		request.setRequestProperty("Content-Type", "application/json");
		request.getOutputStream().write(requestBody.getBytes(encoding));
//		System.out.println("Request sent");
		int responseCode = request.getResponseCode();
		return responseCode;
	}

	public EMFModelLoad getLoader() {
		return loader;
	}

	public void delete(long i) {
		loader.deleteCloundEnvironmentMonitored(i, this);
	}

	// Used to find an object by its EMF generated id (@id in json) in a given
	// CloudEnvironment graph
	public EObject searchForObjectInGivenModel(CloudEnvironment cloudEnvironment, String id) {
		EList<tosca_nodes_Root> nodes = cloudEnvironment.getTosca_nodes_root();
		for (tosca_nodes_Root element : nodes) {
			Resource resource = element.eResource();
			String toTest = ((XMIResource) resource).getURIFragment(element);
			if (toTest.equals(id)) {
				return element;
			}
		}
		return null;
	}

	// Deprecated
	// -------------------------------------------------------------------------------------------------------------------------

	// Used to create readable JsonStrings - Not used anymore because mapper can do
	// the same
	public String prettifyJson(String jsonString) {
		JsonParser parser = new JsonParser();
		JsonObject json = parser.parse(jsonString).getAsJsonObject();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String prettyJson = gson.toJson(json);
		return prettyJson;
	}
}
