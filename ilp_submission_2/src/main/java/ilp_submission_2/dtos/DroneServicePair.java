package ilp_submission_2.dtos;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DroneServicePair {
    private final int droneId;
    private final int servicePointId;
}
