package ilp_submission_2.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Defines a pair of points.
 * Each point is represented using longitude and latitude.
 */
@Getter
@Setter
@Builder
public class PointRequest {
    /**
     * First point in a pair of points.
     * */
    @Valid
    @NotNull(message = "position1 is required")
    @JsonProperty("position1")
    private Point position1;

    /**
     * Second point in a pair of points.
     * */
    @Valid
    @NotNull(message = "position2 is required")
    @JsonProperty("position2")
    private Point position2;
}
