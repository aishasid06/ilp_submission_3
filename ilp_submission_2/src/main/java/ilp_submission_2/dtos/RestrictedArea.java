package ilp_submission_2.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RestrictedArea {
    @JsonProperty("name")
    private String name;

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("limits")
    private Limits limits;

    @JsonProperty("vertices")
    private List<LngLatAlt> vertices;
}
