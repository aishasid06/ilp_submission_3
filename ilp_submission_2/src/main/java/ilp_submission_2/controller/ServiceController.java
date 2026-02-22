package ilp_submission_2.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import ilp_submission_2.dtos.*;
import ilp_submission_2.service.DroneService;
import ilp_submission_2.service.MedStockService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller class that handles various HTTP endpoints for the Drone REST Service.
 * <p>
 * Provides functionality for retrieving a static User ID,
 * finding the distance between two points,
 * checking if two points are close to each other,
 * determining whether a given point is in a specified region,
 * and calculating the next point based on a starting point
 * and a direction angle through POST requests.
 */
@RestController()
@RequestMapping("/api/v1")
public class ServiceController {
    @Autowired
    private MedStockService medStockService;
    /**
     * Attribute for storing the service layer.
     * */
    private final DroneService droneService;

    /**
     * Creates a new {@code ServiceController} with the specified {@link DroneService}.
     *
     * @param getIlpService the service used to implement the functionalities
     * */
    public ServiceController(DroneService getIlpService) {
        this.droneService = getIlpService;
    }

    /**
     * GET a static user ID
     *
     * @return {@code String} representing the user ID
     * */
    @GetMapping("/uid")
    public String uid() {
        return "s2508065";
    }

    @GetMapping("/dronesWithCooling/{state}")
    public ResponseEntity<List<String>> dronesWithCooling(@PathVariable boolean state) {
        return ResponseEntity.ok(droneService.getDronesWithCooling(state));
    }

    @GetMapping("/droneDetails/{id}")
    public ResponseEntity<Drone> droneDetails(@PathVariable String id) {
        Drone drone = droneService.getDroneDetails(id);
        if (drone == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(drone);
    }

    @GetMapping("/queryAsPath/{attributeName}/{attributeValue}")
    public ResponseEntity<List<String>> queryAsPath(@PathVariable String attributeName, @PathVariable String attributeValue) {
        return ResponseEntity.ok(droneService.getDronesWithAttribute(attributeName, attributeValue));
    }

    @GetMapping("/medicineRequirement/{name}")
    public ResponseEntity<MedInStock> getRequirement(@PathVariable String name) {
        if (!medStockService.exists(name)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(medStockService.get(name));
    }

    /**
     * POST with a JSON data structure in the request body.
     *
     * @param req the request containing two points
     * @return {@code ResponseEntity<Double>} distance between the points
     * */
    @PostMapping("/distanceTo")
    public ResponseEntity<Double> distanceTo(@Valid @RequestBody PointRequest req) {
        return ResponseEntity.ok(droneService.getDistance(req));
    }

    /**
     * POST with a JSON data structure in the request body.
     *
     * @param req the request containing two points
     * @return {@code ResponseEntity<Boolean>} true if the points are close, false otherwise
     * */
    @PostMapping("/isCloseTo")
    public ResponseEntity<Boolean> isCloseTo(@Valid @RequestBody PointRequest req) {
        return ResponseEntity.ok(droneService.areClose(req));
    }

    /**
     * POST with a JSON data structure in the request body.
     *
     * @param req the request contains a point and a region
     * @return {@code ResponseEntity<Boolean>} true if the point is in the region, false otherwise;
     * HTTP 400 if the region is not closed
     * */
    @PostMapping("/isInRegion")
    public ResponseEntity<Boolean> isInRegion(@Valid @RequestBody RegionRequest req) {
        List<Point> vertices = req.getRegion().getVertices();
        if (!vertices.getFirst().equals(vertices.getLast())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        return ResponseEntity.ok(droneService.positionInRegion(req));
    }

    /**
     * POST with a JSON data structure in the request body.
     *
     * @param req the request containing a starting point and direction angle
     * @return {@code ResponseEntity<Point>} representing the next position;
     * HTTP 400 if the angle is not a multiple of 22.5
     * */
    @PostMapping("/nextPosition")
    public ResponseEntity<Point> nextPosition(@Valid @RequestBody NextPointRequest req) {
        if (req.getAngle() % 22.5 != 0.0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        return ResponseEntity.ok(droneService.getnextPoint(req));
    }

    @PostMapping("/queryAvailableDrones")
    public ResponseEntity<List<String>> queryAvailableDrones(@Valid @RequestBody List<@Valid Medicine> medDispatchRec) {
        return ResponseEntity.ok(droneService.getAvailableDrones(medDispatchRec));
    }

    @PostMapping("/query")
    public ResponseEntity<List<String>> query(@Valid @RequestBody List<QueryAttributes> queryAttributes) {
        return ResponseEntity.ok(droneService.getQueryAttributesDrones(queryAttributes));
    }

    @PostMapping("/calcDeliveryPath")
    public ResponseEntity<CalcDeliveryPathResult> calcDeliveryPath(@Valid @RequestBody List<@Valid Medicine> medDispatchRec) {
        return ResponseEntity.ok(droneService.calcDeliveryPath(medDispatchRec));
    }

    @PostMapping("/calcDeliveryPathAsGeoJson")
    public ResponseEntity<ObjectNode> calcDeliveryPathAsGeoJson(@Valid @RequestBody List<@Valid Medicine> medDispatchRec) {
        return ResponseEntity.ok(droneService.calcDeliveryPathAsGeoJson(medDispatchRec));
    }

    @PostMapping("/placeOrder")
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody Medicine medicine) {
        return ResponseEntity.ok(droneService.tryPlacingOrder(medicine));
    }

    @PostMapping("/showFlightPath")
    public ResponseEntity<ObjectNode> showFlightPath(@RequestBody FlightPathInputMCP flightPathInputMCP) {
        return ResponseEntity.ok(droneService.showFlightPath(flightPathInputMCP));
    }

    @PostMapping("/deliveryLocationAccessible")
    public ResponseEntity<Boolean> deliveryLocationAccessible(@Valid @RequestBody Point point) {
        return ResponseEntity.ok(droneService.deliveryLocationAccessible(point));
    }

}
