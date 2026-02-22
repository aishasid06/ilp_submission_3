package ilp_submission_2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Drone REST Service Spring Boot application.
 * <p>
 * The annotation @SpringBootApplication, triggers the auto-configuration
 * of Spring Boot and scans for Spring components throughout this project.
 */
@SpringBootApplication
public class IlpSubmission2Application {

    /**
     * Starts the Spring Boot application.
     * <p>
     * This method initializes the application context and begins listening for incoming requests.
     *
     * @param args command-line arguments passed at startup
     */
    public static void main(String[] args) {
        SpringApplication.run(IlpSubmission2Application.class, args);
    }

}
