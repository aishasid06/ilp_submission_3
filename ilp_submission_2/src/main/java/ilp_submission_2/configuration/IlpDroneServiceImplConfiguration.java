package ilp_submission_2.configuration;

import ilp_submission_2.repository.OrderRepository;
import ilp_submission_2.service.DroneService;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class for initializing the DroneService bean.
 * <p>
 * Uses a configurable implementation class name to dynamically instantiate the service.
 */
@Configuration
public class IlpDroneServiceImplConfiguration {
    @Value("${ilp.drone_service.implementation:ilp_submission_2.service.impl.DroneServiceImpl}")
    private String drone_service_implementation;

    /**
     * Creates and returns an instance of the configured DroneService implementation.
     * <p>
     * The class name is resolved from the {@code ilp.drone_service.implementation} property.
     * If not specified, it defaults to {@code ilp_submission_1.service.impl.DroneServiceImpl}.
     * <p>
     * The implementation class must have a public no-argument constructor.
     *
     * @return a DroneService instance
     * @throws RuntimeException if the class cannot be loaded or instantiated
     */
    @Bean
    public DroneService getIlpService(RestTemplate restTemplate, String getIlpEndpoint, RestTemplate getRestTemplate, OrderRepository orderRepository) {
        try {
            return (DroneService) Class.forName(drone_service_implementation)
                    .getDeclaredConstructor(RestTemplate.class, String.class, OrderRepository.class)
                    .newInstance(getRestTemplate, getIlpEndpoint, orderRepository);
        } catch (Exception e) {
            throw new RuntimeException("error creating instance of class: ", e);
        }
    }
}
