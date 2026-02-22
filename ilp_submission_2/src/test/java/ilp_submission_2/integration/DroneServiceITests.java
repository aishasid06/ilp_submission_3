package ilp_submission_2.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class DroneServiceITests {
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("JUnit integration test for distanceTo operation")
    void distanceToTest() throws Exception {
        String jsonRequest = """
            {
                "position1": {"lng": -3.192473, "lat": 55.946233},
                "position2": {"lng": -3.192473, "lat": 55.942617}
            }
        """;

        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("0.003616")));
    }

    @Test
    @DisplayName("JUnit integration test for isCloseTo operation")
    void isCloseToTest() throws Exception {
        String jsonRequest = """
            {
                "position1": {"lng": -3.192473, "lat": 55.946233},
                "position2": {"lng": -3.192471, "lat": 55.946233}
            }
        """;

        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("true")));
    }

    @Test
    @DisplayName("JUnit integration test for isInRegion operation")
    void isInRegionTest() throws Exception {
        String jsonRequest = """
            {
                "position": {"lng": -3.189123, "lat": 55.945123},
                "region": {
                    "name": "some region",
                    "vertices": [
                        {"lng": -3.192473, "lat": 55.946233},
                        {"lng": -3.192473, "lat": 55.942617},
                        {"lng": -3.184319, "lat": 55.942617},
                        {"lng": -3.184319, "lat": 55.946233},
                        {"lng": -3.192473, "lat": 55.946233}
                    ]
                }
            }
        """;

        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("true")));
    }

    @Test
    @DisplayName("JUnit integration test for nextPosition operation")
    void nextPositionTest() throws Exception {
        String jsonRequest = """
            {
                "start": {"lng": -3.192473, "lat": 55.946233},
                "angle": 45
            }
        """;

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.lng").value(closeTo(-3.192366934, 0.00015)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lat").value(closeTo(55.94633907, 0.00015)));
    }
}
