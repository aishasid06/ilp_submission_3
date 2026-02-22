package ilp_submission_2.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import ilp_submission_2.dtos.*;

import java.util.List;

/**
 * Core Service interface that defines various operations needed for drone navigation.
 * */
public interface DroneService {
    /**
     * Calculates the Euclidean distance between two geographic points.
     *
     * @param points the request containing two points
     * @return {@code double} representing the distance between the two points
     * */
    double getDistance(PointRequest points);

    /**
     * Checks whether two points are within a predefined proximity threshold.
     *
     * @param points the request containing two points
     * @return {@code boolean} true if the points are close, otherwise false
     * */
    boolean areClose(PointRequest points);

    /**
     * Determines whether a point lies inside a specified polygonal region.
     *
     * @param pointRegion the request containing a point and a region
     * @return {@code boolean} true if the point is inside the region, otherwise false
     * */
    boolean positionInRegion(RegionRequest pointRegion);

    /**
     * Computes the next point based on a starting position and a direction angle.
     *
     * @param startPoint the request containing a starting point and a direction angle
     * @return {@code Point} the next calculated point
     * */
    Point getnextPoint(NextPointRequest startPoint);

    List<String> getDronesWithCooling(boolean state);
    Drone getDroneDetails(String id);
    List<String> getAvailableDrones(List<Medicine> medDispatchRec);
    List<String> getDronesWithAttribute(String attributeName, String attributeValue);
    List<String> getQueryAttributesDrones(List<QueryAttributes> queryAttributes);
    CalcDeliveryPathResult calcDeliveryPath(List<Medicine> medDispatchRec);
    ObjectNode calcDeliveryPathAsGeoJson(List<Medicine> medDispatchRec);
    OrderResponse tryPlacingOrder(Medicine medicine);
    ObjectNode showFlightPath(FlightPathInputMCP flightPathInputMCP);
    boolean deliveryLocationAccessible(Point point);
}
