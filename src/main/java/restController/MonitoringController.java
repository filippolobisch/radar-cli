package restController;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import restassured.riskpattern.CloudModel.CloudEnvironment;
import runtime.EMFModelLoad;
import runtime.Gateway;
import runtime.RiskFinder;
import runtime.RuntimeModelLogic;

@RestController
public class MonitoringController {

	@Autowired
	private EMFModelLoad loader;
	@Autowired
	private Gateway gateway;
	@Autowired
	private RuntimeModelLogic runtimeModelLogic;
	@Autowired
	private RiskFinder riskFinder;

	/*
	 * Used to register a new ServiceInstance with the RestAssured application: This
	 * web service allows the monitoring adapter of a system to register with the
	 * adaptation component. The request body needs to contain a JSON representation
	 * of a ServiceInstance to be persisted in the run-time model, the URL address
	 * of the monitored system’s execution adapter, as well as the id of the service
	 * that the ServiceInstance should be attached to. This web service returns the
	 * id of the altered Service in the response body, or -1 if the request failed
	 * for any reason.
	 */
	// First Part of the Body is "model" and second Part is "URL Monitoring" and
	// third Part is "URL Execution"
	// Parts are separated by &

	@RequestMapping(path = "/registerManagedSystem", method = RequestMethod.POST, consumes = "*/*; charset=UTF-8")
	@ResponseBody
	public long registerManagedSystem(@RequestBody(required = true) String jsonServiceInstance) {
		String decodedWithEqualsSign;
		long returner = -1;
		try {
			decodedWithEqualsSign = URLDecoder.decode(jsonServiceInstance, StandardCharsets.UTF_8);
			String[] url = decodedWithEqualsSign.split("&");
			String model = StringUtils.strip(url[0], "=");
			String monitoringRestUrl = StringUtils.strip(url[1], "=");
			String executionRestUrl = StringUtils.strip(url[2], "=");

			CloudEnvironment cloudEnvironment = (CloudEnvironment) loader.loadEObjectFromString(model);

			long monitoredEnvironmentId = loader.setCloudEnvironmentsMonitored(cloudEnvironment, 0);
			gateway.registerAdapterForVaporWebServer(monitoredEnvironmentId, monitoringRestUrl, executionRestUrl);

			riskFinder.runExperiment(model, cloudEnvironment, monitoredEnvironmentId, runtimeModelLogic);
		} catch (UnsupportedEncodingException e) {
			System.err.println(e);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return returner; // should return the id of the instance
	}

	/*
	 * This web service allows the monitoring adapter to replace a ServiceInstance
	 * that is attached to a given Service with another ServiceInstance. The fidg in
	 * the path should be the id of the Service whose ServiceInstance should be
	 * replaced. The replacement ServiceInstance should be supplied in the request
	 * body as a JSON object. This web service returns the id of the altered Service
	 * in the response body, or -1 if the request failed for any reason.
	 */
	@RequestMapping(path = "/updateRuntimeModel/{id}", method = RequestMethod.PUT, consumes = "*/*; charset=UTF-8")
	@ResponseBody
	public long updateRuntimeModel(@PathVariable("id") long serviceInstanceId,
			@RequestBody(required = true) String jsonServiceInstance) {
		String decodedWithEqualsSign;
		long returner = -1;
		try {
			decodedWithEqualsSign = URLDecoder.decode(jsonServiceInstance, "UTF-8");
			String decoded = StringUtils.strip(decodedWithEqualsSign, "=");
			System.out.println("JSON-service submitted: " + decoded);
			// Create Object out of JSON
			CloudEnvironment cloudEnvironment = (CloudEnvironment) loader.loadEObjectFromString(decoded);
			String jsonString = loader.getMapper().writerWithDefaultPrettyPrinter()
					.writeValueAsString(cloudEnvironment);
			System.out.println("Loaded:" + jsonString);
			returner = loader.setCloudEnvironmentsMonitored(cloudEnvironment, serviceInstanceId);
			loader.saveEObjectToXMI(loader.getCloudEnvironmentsMonitored().get(returner), returner);
		} catch (UnsupportedEncodingException e) {
			System.err.println(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return returner; // should return the id of the instance
	}
}
