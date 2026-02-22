package ilp_submission_2.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Defines a point and an angle.
 * Used for calculating the next position from a starting point and a direction angle.
 */
@Getter
@Setter
@Builder
public class NextPointRequest {
    /**
     * Starting point.
     * */
    @Valid
    @NotNull(message = "a start position is required")
    @JsonProperty("start")
    private Point start;

    /**
     * Direction angle.
     * */
    @NotNull(message = "an angle is required")
    @Min(value = 0, message = "angle must be >= 0")
    @Max(value = 360, message = "angle must be <= 360")
    @JsonProperty("angle")
    private Double angle;
}
