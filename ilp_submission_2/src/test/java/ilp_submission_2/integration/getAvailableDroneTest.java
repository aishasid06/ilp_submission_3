package ilp_submission_2.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class getAvailableDroneTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Delivery Date & Time Not Specified")
    public void noDeliveryDateAndTime() throws Exception {
        String jsonRequest = """
            [
                {
                    "id": 1,
                    "requirements": {
                        "capacity": 2,
                        "cooling": true,
                        "heating": false,
                        "maxCost": 150
                    },
                    "delivery": {
                        "lng": -3.1785,
                        "lat": 55.9435
                    }
                }
            ]
        """;

        mockMvc.perform(post("/api/v1/queryAvailableDrones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.containsInAnyOrder(
                        "1", "5", "8", "9"
                )));
    }
}
