package run;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import restController.RadarTester;
import runtime.LoggingInterface;

@SpringBootApplication(scanBasePackages = { "restController", "runtime", "tosca" })
public class Application {

    @Autowired
    LoggingInterface loggingInterface;

    public static void main(String[] args) {
        LoggingInterface.setLogToGraylog(false); // True = loggingToGraylogServer
        SpringApplication.run(Application.class, args);
    }
}
