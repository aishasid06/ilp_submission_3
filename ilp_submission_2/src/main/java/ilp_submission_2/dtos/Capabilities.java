package ilp_submission_2.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Capabilities {
    @JsonProperty("cooling")
    private Boolean cooling;

    @JsonProperty("heating")
    private Boolean heating;

    @JsonProperty("capacity")
    private Double capacity;

    @JsonProperty("maxMoves")
    private Integer maxMoves;

    @JsonProperty("costPerMove")
    private Double costPerMove;

    @JsonProperty("costInitial")
    private Double costInitial;

    @JsonProperty("costFinal")
    private Double costFinal;
}
