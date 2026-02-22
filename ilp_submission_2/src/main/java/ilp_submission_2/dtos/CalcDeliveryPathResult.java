package ilp_submission_2.dtos;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class CalcDeliveryPathResult {
    @Builder.Default
    private Double totalCost = 0.0;

    @Builder.Default
    private Integer totalMoves = 0;

    @Builder.Default
    private List<DronePath> dronePaths = new ArrayList<>();
}
