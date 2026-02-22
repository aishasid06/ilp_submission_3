package ilp_submission_2.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QueryAttributes {
    @JsonProperty("attribute")
    private String attribute;

    @JsonProperty("operator")
    private String operator;

    @JsonProperty("value")
    private String value;
}
