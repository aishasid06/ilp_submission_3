package ilp_submission_2.dtos;

import lombok.Data;

import java.util.List;

@Data
public class MedInStock {
    private double capacity;
    private boolean cooling;
    private boolean heating;
    private List<Integer> servicePoints;
}
