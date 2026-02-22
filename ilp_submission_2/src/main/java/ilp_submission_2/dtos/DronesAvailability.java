package ilp_submission_2.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DronesAvailability {
    @JsonProperty("id")
    private String id;

    @JsonProperty("availability")
    private List<Availability> availability;
}
