package ilp_submission_2.dtos;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OrderResponse {
    private String status;
    private String message;
    private String droneId;

    public static OrderResponse success(String droneId) {
        return OrderResponse.builder()
                .status("PLACED")
                .message("Order successfully created")
                .droneId(droneId)
                .build();
    }

    public static OrderResponse failure(String message) {
        return OrderResponse.builder()
                .status("FAILED")
                .message(message)
                .droneId(null)
                .build();
    }

}
