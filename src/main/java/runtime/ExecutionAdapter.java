package runtime;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import restassured.riskpattern.CloudModel.CloudEnvironment;

public abstract class ExecutionAdapter {
	protected boolean isSystemResponsePositive;
	protected CountDownLatch countDownLatch;
	
	public boolean isResponseOfPAYDSystem() {
		return isSystemResponsePositive;
	}

	public void setResponseOfPAYDSystem(boolean responseOfPAYDSystem) {
		this.isSystemResponsePositive = responseOfPAYDSystem;
	}

	public CountDownLatch getCountDownLatch() {
		return countDownLatch;
	}
	
	public void setCountDownLatch(CountDownLatch countDownLatch) {
		this.countDownLatch = countDownLatch;
	}

	public abstract boolean execute(CloudEnvironment proposal, ArrayList<Integer> adaptationCaseNumber);
}
