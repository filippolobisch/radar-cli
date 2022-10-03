package runtime;

import java.io.IOException;

import restassured.riskpattern.CloudModel.CloudEnvironment;

public class ExecutionAdapterTestApplication extends ExecutionAdapter {

	private long serviceInstanceId;
	private Gateway gateway;
	private RuntimeModelLogic runtimeModelLogic;
	
	public ExecutionAdapterTestApplication(long serviceInstanceId, Gateway gateway, RuntimeModelLogic runtimeModelLogic) {
		// TODO Auto-generated constructor stub
		this.serviceInstanceId = serviceInstanceId;
		this.gateway = gateway;
		this.runtimeModelLogic = runtimeModelLogic;
	}
	
	@Override
	public boolean execute(CloudEnvironment proposal, int[] adaptationCaseNumber) {
		// TODO Auto-generated method stub
		// Sends a JSON Representation of actual Instance to the application
		try {
			String send = runtimeModelLogic.returnentiremodelasjson(proposal);
			runtimeModelLogic.makeRequest(gateway.getInstanceIdToExecutionRestUrl().get(serviceInstanceId),
					send);
			System.out.println("Executed proposal.");
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("Can't Connect to Application");
			return false;
		}
	}

}
