package ilp_submission_2.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ilp_submission_2.dtos.MedInStock;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Map;

@Component
public class MedStockService {
    private Map<String, MedInStock> store;

    @PostConstruct
    public void loadRequirements() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = getClass().getResourceAsStream("/medicine_requirements.json");

            store = mapper.readValue(is, new TypeReference<Map<String, MedInStock>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to load medicine requirements", e);
        }
    }

    public boolean exists(String name) {
        return store.containsKey(name.toLowerCase());
    }

    public MedInStock get(String name) {
        return store.get(name.toLowerCase());
    }

    public Map<String, MedInStock> getAll() {
        return store;
    }
}
