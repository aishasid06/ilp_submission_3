package ilp_submission_2.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Builder
public class Medicine {
    @JsonProperty("id")
    @NotNull(message = "id is required")
    private Integer id;

    @JsonProperty("date")
    private LocalDate date;

    @JsonProperty("time")
    private LocalTime time;

    @Valid
    @JsonProperty("requirements")
    @NotNull(message = "requirements is required")
    private MedRequirements requirements;

    @Valid
    @JsonProperty("delivery")
    @NotNull(message = "delivery is required")
    private Point delivery;
}
