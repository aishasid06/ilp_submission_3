package ilp_submission_2.controller;

import ilp_submission_2.dtos.CalcDeliveryPathResult;
import ilp_submission_2.dtos.Point;
import ilp_submission_2.service.DroneService;
import ilp_submission_2.service.MedStockService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ServiceController.class)
public class ControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DroneService droneService;

    @MockitoBean
    MedStockService medStockService;

    @Test
    @DisplayName("JUnit test for uid get requests")
    public void getUIDRequests() throws Exception {
        mockMvc.perform(get("/api/v1/uid"))
                .andExpect(status().isOk())
                .andExpect(content().string("s2508065"));
    }

    @Test
    @DisplayName("JUnit test for distanceTo post requests with valid inputs")
    public void distanceToPostRequestsWithValidInputs() throws Exception {
        Mockito.when(droneService.getDistance(Mockito.any())).thenReturn(0.003616);

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
                .andExpect(content().string("0.003616"));
    }

    @Test
    @DisplayName("JUnit test for distanceTo post requests with invalid data type in LngLat Pair")
    public void distanceToPostRequestsWithInvalidDataTypeInLngLatPair() throws Exception {
        Mockito.when(droneService.getDistance(Mockito.any())).thenReturn(0.003616);

        String jsonRequest = """
            {
                "position1": {"lng": "Hello", "lat": 55.946233},
                "position2": {"lng": -3.192473, "lat": 55.942617}
            }
        """;

        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("JUnit test for distanceTo post requests with empty LngLat pair")
    public void distanceToPostRequestsWithEmptyLngLatPair() throws Exception {
        Mockito.when(droneService.getDistance(Mockito.any())).thenReturn(0.003616);

        String jsonRequest = """
            {
                "position1": {},
                "position2": {"lng": -3.192473, "lat": 55.942617}
            }
        """;

        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("JUnit test for distanceTo post requests with a missing LngLat pair")
    public void distanceToPostRequestsWithMissingLngLatPair() throws Exception {
        Mockito.when(droneService.getDistance(Mockito.any())).thenReturn(0.003616);

        String jsonRequest = """
            {
                "position2": {"lng": -3.192473, "lat": 55.942617}
            }
        """;

        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("JUnit test for distanceTo post requests with a out of range Lng value")
    public void distanceToPostRequestsWithLngValOutOfRange() throws Exception {
        Mockito.when(droneService.getDistance(Mockito.any())).thenReturn(0.003616);

        String jsonRequest = """
            {
                "position1": {"lng": -180.192473, "lat": 55.946233},
                "position2": {"lng": -3.192473, "lat": 55.942617}
            }
        """;

        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("JUnit test for distanceTo post requests with a out of range Lat value")
    public void distanceToPostRequestsWithLatValOutOfRange() throws Exception {
        Mockito.when(droneService.getDistance(Mockito.any())).thenReturn(0.003616);

        String jsonRequest = """
            {
                "position1": {"lng": -3.192473, "lat": 55.946233},
                "position2": {"lng": -3.192473, "lat": 180.942617}
            }
        """;

        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("JUnit test for isCloseTo post requests with valid inputs")
    public void isCloseToPostRequestsWithValidInputs() throws Exception {
        Mockito.when(droneService.areClose(Mockito.any())).thenReturn(false);

        String jsonRequest = """
            {
                "position1": {"lng": -3.192473, "lat": 55.946233},
                "position2": {"lng": -3.192473, "lat": 55.942617}
            }
        """;

        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @DisplayName("JUnit test for isInRegion post requests with valid inputs")
    public void isInRegionPostRequestsWithValidInputs() throws Exception {
        Mockito.when(droneService.positionInRegion(Mockito.any())).thenReturn(true);

        String jsonRequest = """
            {
                "position": {"lng": 1.234, "lat": 1.222},
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
                .andExpect(content().string("true"));

    }

    @Test
    @DisplayName("JUnit test for isInRegion post requests with valid inputs")
    public void isInRegionPostRequestsWithUnclosedRegion() throws Exception {
        Mockito.when(droneService.positionInRegion(Mockito.any())).thenReturn(true);

        String jsonRequest = """
            {
                "position": {"lng": 1.234, "lat": 1.222},
                "region": {
                    "name": "some region",
                    "vertices": [
                        {"lng": -3.192473, "lat": 55.946233},
                        {"lng": -3.192473, "lat": 55.942617},
                        {"lng": -3.184319, "lat": 55.942617},
                        {"lng": -3.184319, "lat": 55.946233}
                    ]
                }
            }
        """;

        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("JUnit test for isInRegion post requests with missing position field")
    public void isInRegionPostRequestsWithMissingPositionField() throws Exception {
        Mockito.when(droneService.positionInRegion(Mockito.any())).thenReturn(true);

        String jsonRequest = """
            {
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
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("JUnit test for isInRegion post requests with an empty region object")
    public void isInRegionPostRequestsWithEmptyRegion() throws Exception {
        Mockito.when(droneService.positionInRegion(Mockito.any())).thenReturn(true);

        String jsonRequest = """
            {
                "position": {"lng": 1.234, "lat": 1.222},
                "region": {}
            }
        """;

        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("JUnit test for isInRegion post requests with missing region name")
    public void isInRegionPostRequestsWithMissingRegionName() throws Exception {
        Mockito.when(droneService.positionInRegion(Mockito.any())).thenReturn(true);

        String jsonRequest = """
            {
                "position": {"lng": 1.234, "lat": 1.222},
                "region": {
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
                .andExpect(status().isBadRequest());

    }

    @Test
    @DisplayName("JUnit test for isInRegion post requests with less than 4 vertices")
    public void isInRegionPostRequestsWithLessThanFourVertices() throws Exception {
        Mockito.when(droneService.positionInRegion(Mockito.any())).thenReturn(true);

        String jsonRequest = """
            {
                "position": {"lng": 1.234, "lat": 1.222},
                "region": {
                    "name": "some region",
                    "vertices": [
                        {"lng": -3.192473, "lat": 55.946233},
                        {"lng": -3.192473, "lat": 55.942617},
                        {"lng": -3.184319, "lat": 55.942617}
                    ]
                }
            }
        """;

        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());

    }

    @Test
    @DisplayName("JUnit test for isInRegion post requests with extra inputs")
    public void isInRegionPostRequestsWithExtraInputs() throws Exception {
        Mockito.when(droneService.positionInRegion(Mockito.any())).thenReturn(true);

        String jsonRequest = """
            {
                "position": {"lng": 1.234, "lat": 1.222},
                "region": {
                    "name": "some region",
                    "vertices": [
                        {"lng": -3.192473, "lat": 55.946233},
                        {"lng": -3.192473, "lat": 55.942617},
                        {"lng": -3.184319, "lat": 55.942617},
                        {"lng": -3.184319, "lat": 55.946233},
                        {"lng": -3.192473, "lat": 55.946233}
                    ]
                },
                "extra": "Hello"
            }
        """;

        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("JUnit test for nextPosition post requests with valid inputs")
    public void nextPositionPostRequestsWithValidInputs() throws Exception {
        Point p = Point.builder().lng(-3.15).lat(55.15).build();

        Mockito.when(droneService.getnextPoint(Mockito.any())).thenReturn(p);

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
                .andExpect(MockMvcResultMatchers.jsonPath("$.lng").value(-3.15))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lat").value(55.15));
    }

    @Test
    @DisplayName("JUnit test for nextPosition post requests with empty angle field")
    public void nextPositionPostRequestsWithEmptyAngleField() throws Exception {
        Point p = Point.builder().lng(-3.15).lat(55.15).build();

        Mockito.when(droneService.getnextPoint(Mockito.any())).thenReturn(p);

        String jsonRequest = """
            {
                "start": {"lng": -3.192473, "lat": 55.946233},
                "angle": {}
            }
        """;

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("JUnit test for nextPosition post requests with invalid angle")
    public void nextPositionPostRequestsWithInvalidAngle() throws Exception {
        Point p = Point.builder().lng(-3.15).lat(55.15).build();

        Mockito.when(droneService.getnextPoint(Mockito.any())).thenReturn(p);

        String jsonRequest = """
            {
                "start": {"lng": -3.192473, "lat": 55.946233},
                "angle": 55.0
            }
        """;

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("JUnit test for nextPosition post requests with missing start field")
    public void nextPositionPostRequestsWithMissingStartField() throws Exception {
        Point p = Point.builder().lng(-3.15).lat(55.15).build();

        Mockito.when(droneService.getnextPoint(Mockito.any())).thenReturn(p);

        String jsonRequest = """
            {
                "angle": 45.0
            }
        """;

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("JUnit test for nextPosition post requests with extra inputs")
    public void nextPositionPostRequestsWithExtraInputs() throws Exception {
        Point p = Point.builder().lng(-3.15).lat(55.15).build();

        Mockito.when(droneService.getnextPoint(Mockito.any())).thenReturn(p);

        String jsonRequest = """
            {
                "start": {"lng": -3.192473, "lat": 55.946233},
                "angle": 45,
                "extra": "This is extra"
            }
        """;

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.lng").value(-3.15))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lat").value(55.15));
    }


    // Testing for R1.2.3 begins here
    @Test
    @DisplayName("Valid Minimal Request")
    public void validMinimalRequest() throws Exception {
        Mockito.when(droneService.calcDeliveryPath(Mockito.any())).thenReturn(CalcDeliveryPathResult.builder().build());
        String jsonRequest = """
                [
                  {
                    "id": 123,
                    "requirements": {
                      "capacity": 4
                    },
                    "delivery": {
                      "lng": -3.18335807889864,
                      "lat": 55.9476806670849
                    }
                  }
                ]
                """;
        mockMvc.perform(post("/api/v1/calcDeliveryPath")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Valid Request Including Optional Fields")
    public void validRequestIncludingOptionalFields() throws Exception {
        Mockito.when(droneService.calcDeliveryPath(Mockito.any())).thenReturn(CalcDeliveryPathResult.builder().build());
        String jsonRequest = """
                [
                  {
                    "id": 123,
                    "date": "2025-12-22",
                    "time": "14:30",
                    "requirements": {
                      "capacity": 4,
                      "cooling": true,
                      "heating": true,
                      "maxCost": 200
                    },
                    "delivery": {
                      "lng": -3.18335807889864,
                      "lat": 55.9476806670849
                    }
                  }
                ]
                """;
        mockMvc.perform(post("/api/v1/calcDeliveryPath")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Missing Double Quotes On A Key-Value Pair")
    public void syntaxErrorRequest() throws Exception {
        Mockito.when(droneService.calcDeliveryPath(Mockito.any())).thenReturn(CalcDeliveryPathResult.builder().build());
        String jsonRequest = """
                [
                  {
                    id: 123,
                    "requirements": {
                      "capacity": 4
                    },
                    "delivery": {
                      "lng": -3.18335807889864,
                      "lat": 55.9476806670849
                    }
                  }
                ]
                """;
        mockMvc.perform(post("/api/v1/calcDeliveryPath")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Missing ID")
    public void missingID() throws Exception {
        Mockito.when(droneService.calcDeliveryPath(Mockito.any())).thenReturn(CalcDeliveryPathResult.builder().build());
        String jsonRequest = """
                [
                  {
                    "requirements": {
                      "capacity": 4
                    },
                    "delivery": {
                      "lng": -3.18335807889864,
                      "lat": 55.9476806670849
                    }
                  }
                ]
                """;
        mockMvc.perform(post("/api/v1/calcDeliveryPath")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Missing Requirements")
    public void missingRequirements() throws Exception {
        Mockito.when(droneService.calcDeliveryPath(Mockito.any())).thenReturn(CalcDeliveryPathResult.builder().build());
        String jsonRequest = """
                [
                  {
                    "id": 123,
                    "delivery": {
                      "lng": -3.18335807889864,
                      "lat": 55.9476806670849
                    }
                  }
                ]
                """;
        mockMvc.perform(post("/api/v1/calcDeliveryPath")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Missing Capacity")
    public void missingCapacity() throws Exception {
        Mockito.when(droneService.calcDeliveryPath(Mockito.any())).thenReturn(CalcDeliveryPathResult.builder().build());
        String jsonRequest = """
                [
                  {
                    "id": 123,
                    "requirements": {},
                    "delivery": {
                      "lng": -3.18335807889864,
                      "lat": 55.9476806670849
                    }
                  }
                ]
                """;
        mockMvc.perform(post("/api/v1/calcDeliveryPath")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Missing Delivery")
    public void missingDelivery() throws Exception {
        Mockito.when(droneService.calcDeliveryPath(Mockito.any())).thenReturn(CalcDeliveryPathResult.builder().build());
        String jsonRequest = """
                [
                  {
                    "id": 123,
                    "requirements": {
                      "capacity": 4
                    },
                    "delivery":
                  }
                ]
                """;
        mockMvc.perform(post("/api/v1/calcDeliveryPath")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Missing Longitude in Delivery")
    public void missingDeliveryLongitude() throws Exception {
        Mockito.when(droneService.calcDeliveryPath(Mockito.any())).thenReturn(CalcDeliveryPathResult.builder().build());
        String jsonRequest = """
                [
                  {
                    "id": 123,
                    "requirements": {
                      "capacity": 4
                    },
                    "delivery": {
                      "lng": ,
                      "lat": 55.9476806670849
                    }
                  }
                ]
                """;
        mockMvc.perform(post("/api/v1/calcDeliveryPath")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("String Capacity Instead of Double")
    public void stringCapacityNotDouble() throws Exception {
        Mockito.when(droneService.calcDeliveryPath(Mockito.any())).thenReturn(CalcDeliveryPathResult.builder().build());
        String jsonRequest = """
                [
                  {
                    "id": 123,
                    "requirements": {
                      "capacity": "hello"
                    },
                    "delivery": {
                      "lng": -3.18335807889864,
                      "lat": 55.9476806670849
                    }
                  }
                ]
                """;
        mockMvc.perform(post("/api/v1/calcDeliveryPath")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("Delivery Latitude String Instead of Double")
    public void stringLatitudeNotDouble() throws Exception {
        Mockito.when(droneService.calcDeliveryPath(Mockito.any())).thenReturn(CalcDeliveryPathResult.builder().build());
        String jsonRequest = """
                [
                  {
                    "id": 123,
                    "requirements": {
                      "capacity": 4
                    },
                    "delivery": {
                      "lng": -3.18335807889864,
                      "lat": "Oops"
                    }
                  }
                ]
                """;
        mockMvc.perform(post("/api/v1/calcDeliveryPath")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("String Cooling Instead of Boolean")
    public void stringCoolingNotBoolean() throws Exception {
        Mockito.when(droneService.calcDeliveryPath(Mockito.any())).thenReturn(CalcDeliveryPathResult.builder().build());
        String jsonRequest = """
                [
                  {
                    "id": 123,
                    "requirements": {
                      "capacity": 4,
                      "cooling": "notBoolean:("
                    },
                    "delivery": {
                      "lng": -3.18335807889864,
                      "lat": 55.9476806670849
                    }
                  }
                ]
                """;
        mockMvc.perform(post("/api/v1/calcDeliveryPath")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Valid Boundary Values for Lng/Lat")
    public void validBoundaryLatLng() throws Exception {
        Mockito.when(droneService.calcDeliveryPath(Mockito.any())).thenReturn(CalcDeliveryPathResult.builder().build());
        String jsonRequest = """
                [
                  {
                    "id": 123,
                    "requirements": {
                      "capacity": 4
                    },
                    "delivery": {
                      "lng": -180,
                      "lat": 90
                    }
                  }
                ]
                """;
        mockMvc.perform(post("/api/v1/calcDeliveryPath")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Lat Just Out of Boundary")
    public void invalidLat() throws Exception {
        Mockito.when(droneService.calcDeliveryPath(Mockito.any())).thenReturn(CalcDeliveryPathResult.builder().build());
        String jsonRequest = """
                [
                  {
                    "id": 123,
                    "requirements": {
                      "capacity": 4
                    },
                    "delivery": {
                      "lng": -180.0000001,
                      "lat": 90
                    }
                  }
                ]
                """;
        mockMvc.perform(post("/api/v1/calcDeliveryPath")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Lng Just Out of Boundary")
    public void invalidLng() throws Exception {
        Mockito.when(droneService.calcDeliveryPath(Mockito.any())).thenReturn(CalcDeliveryPathResult.builder().build());
        String jsonRequest = """
                [
                  {
                    "id": 123,
                    "requirements": {
                      "capacity": 4
                    },
                    "delivery": {
                      "lng": -170,
                      "lat": 90.0000001
                    }
                  }
                ]
                """;
        mockMvc.perform(post("/api/v1/calcDeliveryPath")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Lng/Lat Just Out of Boundary")
    public void invalidLngLat() throws Exception {
        Mockito.when(droneService.calcDeliveryPath(Mockito.any())).thenReturn(CalcDeliveryPathResult.builder().build());
        String jsonRequest = """
                [
                  {
                    "id": 123,
                    "requirements": {
                      "capacity": 4
                    },
                    "delivery": {
                      "lng": 180.0000001,
                      "lat": -90.0000001
                    }
                  }
                ]
                """;
        mockMvc.perform(post("/api/v1/calcDeliveryPath")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Unexpected Field")
    public void unexpectedField() throws Exception {
        Mockito.when(droneService.calcDeliveryPath(Mockito.any())).thenReturn(CalcDeliveryPathResult.builder().build());
        String jsonRequest = """
                [
                  {
                    "id": 123,
                    "name": "anonymous",
                    "requirements": {
                      "capacity": 4
                    },
                    "delivery": {
                      "lng": -3.18335807889864,
                      "lat": 55.9476806670849
                    }
                  }
                ]
                """;
        mockMvc.perform(post("/api/v1/calcDeliveryPath")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk());
    }

}
