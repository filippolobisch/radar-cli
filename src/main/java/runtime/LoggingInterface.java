package runtime;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.springframework.stereotype.Component;

@Component
public class LoggingInterface {

	private static boolean logToGraylog;

	public LoggingInterface() {

	}

	/**
	 * Logs a String message.
	 * 
	 * @param severity The severity of what triggered the logging operation. This is
	 *                 completely arbitrary.
	 * @param message  The message to be logged.
	 */
//	private static void logPrivate(int severity, String message) {
//		if(severity<50) {
//			System.out.println(message);
//		}else{
//			System.err.println(message);
//		}
//	}
	private void logPrivate(String message) {
		System.out.println(message);

	}

	/**
	 * Logs a String message, as well as the object that triggered the logging
	 * operation.
	 * 
	 * @param severity The severity of what triggered the logging operation. This is
	 *                 completely arbitrary.
	 * @param message  The message to be logged.
	 * @param caller   The object that triggered the logging operation.
	 */
//	public static void log(int severity, String message) {
//		if(logToGraylog == true) {
//			try {
//				logToGraylog(message);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				System.err.println("Failed logging to Graylog Server");
//			}
//		}else {
//			logPrivate(severity, message);
//		}
//		
//	}
	public void log(String message) {
		if (logToGraylog == true) {
			try {
				logToGraylog(message);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.err.println("Failed logging to Graylog Server");
			}
		} else {
			logPrivate(message);
		}

	}

	/**
	 * Logs an exception.
	 * 
	 * @param e      The exception to be logged.
	 * @param caller The object that triggered the logging operation.
	 */
//	public static void logException(Exception e, Object caller) {
//		StringWriter out = new StringWriter();
//		PrintWriter writer = new PrintWriter(out);
//		e.printStackTrace(writer);
//		log(100, "Exception thrown by " + caller + ":\n" + out.toString(), caller);
//	}

	private static void logToGraylog(String message) throws IOException {
		try {
			String json = "{\"version\":\"1.1\",\"host\":\"test.restassuredh2020.eu\",\"short_message\":\"" + message
					+ "\",\"full_message\":\"Log Message from Adaptation:" + message
					+ "\",\"level\":5,\"_userId\":\"test@ra.com\",\"_sessionId\":\"sErKwASqausCIIYa0zjoj\",\"_custom_field\":\"Custom Field Value\"}";
			String body = post("https://ra-logs.it-innovation-deployments.cf/gelf", json);
			System.out.println(body);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

	}

	public static String post(String postUrl, String data) throws IOException {
		URL url = new URL(postUrl);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");

		con.setDoOutput(true);

		sendData(con, data);

		return read(con.getInputStream());
	}

	protected static void sendData(HttpURLConnection con, String data) throws IOException {
		DataOutputStream wr = null;
		try {
			wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(data);
			wr.flush();
			wr.close();
		} catch (IOException exception) {
			throw exception;
		} finally {
			closeQuietly(wr);
		}
	}

	private static String read(InputStream is) throws IOException {
		BufferedReader in = null;
		String inputLine;
		StringBuilder body;
		try {
			in = new BufferedReader(new InputStreamReader(is));

			body = new StringBuilder();

			while ((inputLine = in.readLine()) != null) {
				body.append(inputLine);
			}
			in.close();

			return body.toString();
		} catch (IOException ioe) {
			throw ioe;
		} finally {
			closeQuietly(in);
		}
	}

	protected static void closeQuietly(Closeable closeable) {
		try {
			if (closeable != null) {
				closeable.close();
			}
		} catch (IOException ex) {

		}
	}

	public static boolean isLogToGraylog() {
		return logToGraylog;
	}

	public static void setLogToGraylog(boolean logToGraylog) {
		LoggingInterface.logToGraylog = logToGraylog;
	}

}
