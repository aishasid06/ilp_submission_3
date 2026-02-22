package ilp_submission_2.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * Defines a point using longitude and latitude.
 */
@Getter
@Setter
@Builder
public class Point {
    /**
     * A point's longitude.
     * Must be between -180 and 180 degrees inclusive.
     * */
    @NotNull(message = "Longitude is required")
    @Min(value = -180, message = "Longitude must be >= -180")
    @Max(value = 180, message = "Longitude must be <= 180")
    @JsonProperty("lng")
    private Double lng;

    /**
     * A point's latitude.
     * Must be between -90 and 90 degrees inclusive.
     * */
    @NotNull(message = "Latitude is required")
    @Min(value = -90, message = "Latitude must be >= -90")
    @Max(value = 90, message = "Latitude must be <= 90")
    @JsonProperty("lat")
    private Double lat;


    private static final double EPSILON = 1e-6;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Point other)) return false;
        return Math.abs(this.lat - other.lat) < EPSILON &&
                Math.abs(this.lng - other.lng) < EPSILON;
    }

    @Override
    public int hashCode() {
        double roundedLat = Math.round(lat / EPSILON) * EPSILON;
        double roundedLng = Math.round(lng / EPSILON) * EPSILON;
        return Objects.hash(roundedLat, roundedLng);
    }

    @Override
    public String toString() {
        return String.format("Point(lat=%.6f, lng=%.6f)", lat, lng);
    }


}




