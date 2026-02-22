package ilp_submission_2.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FlightPathInputMCP {
    @JsonProperty("medicine")
    private Medicine medicine;
}
