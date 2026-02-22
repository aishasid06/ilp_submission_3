package ilp_submission_2.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;


@Getter
@Setter
public class Availability {
    @JsonProperty("dayOfWeek")
    private DayOfWeek dayOfWeek;

    @JsonProperty("from")
    private LocalTime from;

    @JsonProperty("until")
    private LocalTime until;
}
