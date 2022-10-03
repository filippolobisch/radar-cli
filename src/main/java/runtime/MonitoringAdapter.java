package runtime;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil.EqualityHelper;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.henshin.interpreter.EGraph;
import org.eclipse.emf.henshin.interpreter.impl.EGraphImpl;
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import restassured.riskpattern.CloudModel.CloudEnvironment;
import riskpatternfinder.AdaptationFinderToMitigateRisks.AdaptationAlgorithm;
import utility.Constants;

/**
 * This class should make periodic calls to an ExecutionAdapter�s REST interface
 * to make sure that the ExecutionAdapter is still running.
 */

public class MonitoringAdapter implements Runnable {

	protected Gateway gateway;
	protected long serviceInstanceId;
	protected String pingAddress;
	protected long missedPings;
	protected String monitoringAdress;
	protected RuntimeModelLogic runtimeModelLogic;
	protected RiskFinder riskFinder;

	public MonitoringAdapter(Gateway gateway, long serviceInstanceId, String pingAddress, long missedPings,
			String monitoringAdress, RuntimeModelLogic runtimeModelLogic, RiskFinder riskFinder) {
		super();
		this.gateway = gateway;
		this.pingAddress = pingAddress;
		this.serviceInstanceId = serviceInstanceId;
		this.missedPings = missedPings;
		this.monitoringAdress = monitoringAdress;
		this.runtimeModelLogic = runtimeModelLogic;
		this.riskFinder = riskFinder;
	}

	public void run() {
		try {
			URL url;
			try {
				url = new URL(pingAddress);
				HttpURLConnection request = (HttpURLConnection) url.openConnection();
				request.setRequestMethod("GET");
				request.setDoOutput(false);
				if (request.getResponseCode() == 200) {
					missedPings = 0;
					if (somethingChanged()) {
						// RADAR + Execution
						System.out.println("Something has changed!!!!");
						CloudEnvironment ce = runtimeModelLogic.getLoader().getCloudEnvironmentsMonitored().get(serviceInstanceId);
						riskFinder.lookForRisks(ce, serviceInstanceId, AdaptationAlgorithm.BestFirstSearch, "", 0, 10);
					}else {
						System.out.println("Nothing has changed!!!!");
					}
					gateway.getExecutor().schedule(new MonitoringAdapter(gateway, serviceInstanceId, pingAddress,
							missedPings, monitoringAdress, runtimeModelLogic, riskFinder), 10, TimeUnit.SECONDS);

				} else {
					missedPing();
				}
			} catch (IOException e) {
				missedPing();
			}
		} catch (Exception ee) {
			System.err.println(ee);
		}
	}

	//After 3 missed Pings Adapter will be deleted
	public void missedPing() {
		System.out.println("Ping Missed to " + pingAddress);
		if (++missedPings > 2) {
			gateway.forgetAdapter(serviceInstanceId);
		} else {
			gateway.getExecutor().schedule(new MonitoringAdapter(gateway, serviceInstanceId, pingAddress, missedPings,
					monitoringAdress, runtimeModelLogic, riskFinder), 1, TimeUnit.SECONDS);
		}
	}

	//Searches for changes between actual instance and monitored system - true if something changed
	public boolean somethingChanged() {
		String model = "";
		try {
			URL url = new URL(monitoringAdress);
			HttpURLConnection request = (HttpURLConnection) url.openConnection();
			request.setRequestMethod("GET");
			long timestamp = System.currentTimeMillis();

			BufferedReader bodyReader = new BufferedReader(new InputStreamReader(request.getInputStream()));
			String body = "";
			String temp;
			while ((temp = bodyReader.readLine()) != null) {
				body += temp;
			}
//			System.out.println("Body: " + body);
			String decodedWithEqualsSign;
			decodedWithEqualsSign = URLDecoder.decode(body, "UTF-8");
			String decoded = StringUtils.strip(decodedWithEqualsSign, "=");
			model = decoded;
//			System.out.println("Responsetime in sec: " + (System.currentTimeMillis() - timestamp) / 1000f);
		} catch (Exception e) {
			System.out.println("Problem to get the model from Application");
			System.err.println(e);
		}
		if(!model.equals("")) { //Received Model
			CloudEnvironment newInstance = null;
			HenshinResourceSet resourceSet = new HenshinResourceSet(Constants.RUNTIME_MODEL_PATH);
			Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("cloudmodel", new XMIResourceFactoryImpl());
			try {
				newInstance = (CloudEnvironment) runtimeModelLogic.getLoader().loadEObjectFromString(model);
				resourceSet.saveEObject(newInstance, "temp.cloudmodel");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(newInstance != null) {
				EGraph graphActual = new EGraphImpl(resourceSet.getResource("ServiceInstance" + serviceInstanceId + ".cloudmodel"));
				EGraph graphNew = new EGraphImpl(resourceSet.getResource("temp.cloudmodel"));			
				if(!new EqualityHelper().equals(graphActual.getRoots().get(0), graphNew.getRoots().get(0))) {
					File file = new File("models/runtime_models/temp.cloudmodel");
					file.delete();
					runtimeModelLogic.getLoader().setCloudEnvironmentsMonitored(newInstance, serviceInstanceId);
					return true;
				}else {
					File file = new File("models/runtime_models/temp.cloudmodel");
					file.delete();
				}
			}else {System.out.println("Wasn't able to load cloudenvironment out of String");}
		}
		return false;
	}
}