package ilp_submission_2.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DroneForServicePoint {
    @JsonProperty("servicePointId")
    private Integer servicePointId;

    @JsonProperty("drones")
    private List<DronesAvailability> drones;
}
