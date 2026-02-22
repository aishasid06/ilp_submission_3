package ilp_submission_2.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Defines a region using name and a list of points.
 */
@Getter
@Setter
@Builder
public class Region {
    /**
     * A region's name.
     * */
    @NotNull(message = "region must have a name")
    @JsonProperty("name")
    private String name;

    /**
     * A region's vertices.
     * Must have at least 4 points for polygon to close so minimum 3 vertices.
     * */
    @Valid
    @Size(min = 4, message = "region must have at least 4 points so min 3 vertices")
    @JsonProperty("vertices")
    private List<Point> vertices;
}
