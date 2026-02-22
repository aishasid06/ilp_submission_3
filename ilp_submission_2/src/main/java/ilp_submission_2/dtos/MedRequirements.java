package ilp_submission_2.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MedRequirements {
    @JsonProperty("capacity")
    @NotNull(message = "capacity is required")
    private Double capacity;

    @JsonProperty("cooling")
    private Boolean cooling;

    @JsonProperty("heating")
    private Boolean heating;

    @JsonProperty("maxCost")
    private Double maxCost;
}
