package ilp_submission_2.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Defines a point and a region.
 * Used for checking whether a given point lies within the specified region.
 */
@Getter
@Setter
@Builder
public class RegionRequest {
    /**
     * Position to check.
     * */
    @Valid
    @NotNull(message = "position is required")
    @JsonProperty("position")
    private Point position;

    /**
     * Region in which to check for position.
     * */
    @Valid
    @NotNull(message = "region is required")
    @JsonProperty("region")
    private Region region;
}
