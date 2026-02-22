package ilp_submission_2.dtos;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class Delivery {
    private Integer deliveryId;
    private List<Point> flightPath;
}
