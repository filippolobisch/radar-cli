package restController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import restassured.riskpattern.CloudModel.CloudEnvironment;
import restassured.riskpattern.CloudModel.Record;
import riskpatternfinder.AdaptationFinderToMitigateRisks.AdaptationAlgorithm;
import runtime.RiskFinder;
import runtime.RuntimeModelLogic;

@RestController
public class FogExampleRadarAdapter {

	@Autowired
	RuntimeModelLogic runtimeModelLogic;
	@Autowired
	private RiskFinder riskFinder;

	@RequestMapping(path = "/radar/toggleSensitivityOfCustomerData", method = RequestMethod.POST, consumes = "*/*; charset=UTF-8")
	@ResponseBody
	public ResponseEntity monitorCustomerData(@RequestBody(required = true) String body) {
		ResponseEntity response;
		boolean value = true;
		if (body.equalsIgnoreCase("true")) {
			value = true;
		} else if (body.equalsIgnoreCase("false")) {
			value = false;
		} else {
			System.err.println("Body does not match true or false");
			response = new ResponseEntity(HttpStatus.BAD_REQUEST);
		}
		CloudEnvironment ce = runtimeModelLogic.getLoader().getCloudEnvironmentsMonitored().get(1l);
		try {
			//ID of customerData is 28!
			Record customerData = (Record) runtimeModelLogic.searchForObjectInGivenModel(ce, "//@tosca_nodes_root.28");
			try {
				customerData.setSensitive(value);
				System.out.println("Monitoring event: CustomerData now set to " + value + ".");
				response = new ResponseEntity(HttpStatus.ACCEPTED);
				// Now start search for risks
				runtimeModelLogic.getLoader().setCloudEnvironmentsMonitored(ce, 1L);
				System.out.println("Successfully updated model after monitoring event.");
				riskFinder.startRadar(1l, AdaptationAlgorithm.BestFirstSearch, "", 0, 10, null, 0); //TODO PROBLEM
			} catch (Exception e) {
				System.err.println("Could not set sensitivity");
				response = new ResponseEntity(HttpStatus.BAD_REQUEST);
			}
		} catch (ClassCastException e) {
			System.err.println("Can't find the needed object");
			e.printStackTrace();
			response = new ResponseEntity(HttpStatus.BAD_REQUEST);
		}
		// returns ResponseCode
		return response;
	}

}
