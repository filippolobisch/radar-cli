package runtime;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Gateway {

	@Autowired
	private RuntimeModelLogic runtimeModelLogic;
	@Autowired
	private RiskFinder riskFinder;

	/**
	 * This maps ServiceInstance ids to the URLs that can be used to access that
	 * ServiceInstance�s ExecutionAdapter.
	 */
	public Map<Long, String> instanceIdToExecutionRestUrl;
	/**
	 * Executor to make REST calls concurrently.
	 */
	public ScheduledExecutorService executor;

	/**
	 * This maps ServiceInstance ids to the URLs that can be used to access that
	 * ServiceInstance�s Model. If the value is null we will monitor passive not
	 * active
	 */
	public HashMap<Long, String> instanceIdToMonitoringRestUrl;

	public HashMap<Long, LinkedList<ExecutionAdapter>> instanceIdToExecutionAdapters;

	public Gateway() {
		this.instanceIdToExecutionRestUrl = new HashMap<Long, String>();
		this.instanceIdToMonitoringRestUrl = new HashMap<Long, String>();
		this.executor = Executors.newScheduledThreadPool(1);
		this.instanceIdToExecutionAdapters = new HashMap<Long, LinkedList<ExecutionAdapter>>();
	}

	public void startMonitoring(long serviceInstanceId, String monitoringRestUrl, String executionRestUrl) {
		Runnable gatewayAdapterPingTask = new MonitoringAdapter(this, serviceInstanceId, executionRestUrl, 0,
				monitoringRestUrl, runtimeModelLogic, riskFinder);
		executor.execute(gatewayAdapterPingTask);
	}

	public ScheduledExecutorService getExecutor() {
		return executor;
	}

	public void setExecutor(ScheduledExecutorService executor) {
		this.executor = executor;
	}

	public void registerAdapterForVaporWebServer(long id, String monitoringURL, String executionURL) {
		instanceIdToExecutionRestUrl.put(id, executionURL);
		instanceIdToMonitoringRestUrl.put(id, monitoringURL);
		LinkedList<ExecutionAdapter> executionAdapters = new LinkedList<ExecutionAdapter>();
		executionAdapters.add(new ExecutionAdaptorVapor(runtimeModelLogic));
		instanceIdToExecutionAdapters.put(id, executionAdapters);
	}

	// Stops Monitoring for ServiceInstanceID and also deletes the instance in the
	// runtimeModel
	public void forgetAdapter(long serviceInstanceId) {
		System.out.println("ExecutionGatewayAdapter with id " + serviceInstanceId + " and address "
				+ instanceIdToExecutionRestUrl.get(serviceInstanceId) + " forgotten!");
		instanceIdToExecutionRestUrl.remove(serviceInstanceId);
		instanceIdToMonitoringRestUrl.remove(serviceInstanceId);
		runtimeModelLogic.delete(serviceInstanceId);
		instanceIdToExecutionAdapters.remove(serviceInstanceId);
	}

	public Map<Long, String> getInstanceIdToExecutionRestUrl() {
		return instanceIdToExecutionRestUrl;
	}

	public void setInstanceIdToExecutionRestUrl(Map<Long, String> instanceIdToExecutionRestUrl) {
		this.instanceIdToExecutionRestUrl = instanceIdToExecutionRestUrl;
	}

	public HashMap<Long, LinkedList<ExecutionAdapter>> getInstanceIdToExecutionAdapters() {
		return instanceIdToExecutionAdapters;
	}

	public void setInstanceIdToExecutionAdapters(
			HashMap<Long, LinkedList<ExecutionAdapter>> instanceIdToExecutionAdapters) {
		this.instanceIdToExecutionAdapters = instanceIdToExecutionAdapters;
	}

}
