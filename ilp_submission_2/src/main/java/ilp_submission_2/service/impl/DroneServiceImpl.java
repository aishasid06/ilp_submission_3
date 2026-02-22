package ilp_submission_2.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ilp_submission_2.dtos.*;
import ilp_submission_2.entity.Order;
import ilp_submission_2.repository.OrderRepository;
import ilp_submission_2.service.DroneService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.awt.geom.Line2D;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.*;
import java.time.LocalTime;
import java.util.stream.Collectors;

/**
 * Implementation of the {@link DroneService} interface.
 * <p>
 * Provides logic for operations that help with drone navigation such as
 * calculating the distance between two geographic points,
 * checking proximity between two points,
 * determining if a point lies within a polygonal region,
 * and computing the next point based on a starting position and direction angle.
 * */

@Service
public class DroneServiceImpl implements DroneService {
    /**
     * Fixed step size used for movement calculations.
     * */
    private final double stepSize = 0.00015;
    private final RestTemplate restTemplate;
    private final String ilpEndPoint;
    private final OrderRepository orderRepository;
    private Map<String, Integer> mapDroneToService;         // map drone ID to service point ID
    private Map<Integer, Point> mapServiceIdToServicePoint; // map service point ID to service point
    private static final Logger logger = LoggerFactory.getLogger(DroneServiceImpl.class);

    private record AvailabilityContext(
            List<String> availableDroneIds,
            Map<String, Integer> droneToService,
            Map<Integer, Point> serviceIdToPoint
    ) {}

    public DroneServiceImpl(RestTemplate getRestTemplate, String getIlpEndpoint, OrderRepository orderRepository) {
        this.restTemplate = getRestTemplate;
        this.ilpEndPoint = getIlpEndpoint;
        this.orderRepository = orderRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getDistance(PointRequest points) {
        Point position1 = points.getPosition1();
        Point position2 = points.getPosition2();

        double deltaX = position2.getLng() - position1.getLng();
        double deltaY = position2.getLat() - position1.getLat();

        return Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));
    }

    /**
     * {@inheritDoc}
     * */
    @Override
    public boolean areClose(PointRequest points) {
        double distance = getDistance(points);
        return distance < stepSize;
    }

    /**
     * Determines whether a point lies inside a polygonal region.
     * <p>
     * The Ray Casting algorithm is used.
     * Assumes the region is explicitly closed (i.e., the first and last vertices are equal).
     * A horizontal ray is cast from the point, and the number of edge crossings is counted.
     * An odd number of crossings indicates the point is inside the region while even means it's outside.
     *
     * <p>
     * Works for both convex and concave polygons.
     *
     * @param pointRegion the request containing a point and a region
     * @return {@code boolean} true if the point is inside the region, otherwise false
     * */
    @Override
    public boolean positionInRegion(RegionRequest pointRegion) {
        Region region = pointRegion.getRegion();
        List<Point> vertices = region.getVertices();

        Point position = pointRegion.getPosition();
        double xp = position.getLng();
        double yp = position.getLat();

        int count = 0;

        for (int i = 0; i < vertices.size() - 1; i++) {
            // getting the vertices of the current edge
            double x1 = vertices.get(i).getLng();
            double y1 = vertices.get(i).getLat();

            double x2 = vertices.get(i + 1).getLng();
            double y2 = vertices.get(i + 1).getLat();

            // Check if the horizontal ray from (xp, yp) crosses the edge
            // A crossing occurs if the point's y is between y1 and y2,
            // and the x of the intersection is to the right of the point.

            if (((yp <= y1) != (yp <= y2)) && (xp <= (x1 + ((yp-y1)/(y2-y1))*(x2-x1)))) {
                count++;
            }
        }

        return (count % 2 == 1);
    }

    /**
     * Computes the next point based on a starting position and a direction angle.
     *<p>
     * The direction angle is converted from degrees to radians.
     * The movement is calculated using a fixed step size of {@code 0.00015} degrees.
     * The resulting point is offset from the original using cosine (longitude) and sine (latitude) components.
     *
     * @param startPoint the request containing a starting point and a direction angle
     * @return {@code Point} the next calculated point
     * */
    @Override
    public Point getnextPoint(NextPointRequest startPoint) {
        Point start = startPoint.getStart();
        double radAngle = Math.toRadians(startPoint.getAngle());

        return Point.builder()
                .lng(start.getLng() + Math.cos(radAngle) * stepSize)
                .lat(start.getLat() + Math.sin(radAngle) * stepSize)
                .build();
    }

    @Override
    public List<String> getDronesWithCooling(boolean state) {
        Drone[] drones = restTemplate.getForObject(ilpEndPoint + "/drones", Drone[].class);

        if  (drones == null) {
            return List.of();
        }

        return Arrays.stream(drones)
                .filter(drone -> {
                    Boolean cooling = drone.getCapability().getCooling();
                    return Boolean.TRUE.equals(cooling) == state;
                })
                .map(Drone::getId).
                toList();
    }

    @Override
    public Drone getDroneDetails(String id) {
        Drone[] drones = restTemplate.getForObject(ilpEndPoint + "/drones", Drone[].class);

        if (drones == null) {
            return null;
        }

        return Arrays.stream(drones)
                .filter(drone -> Objects.equals(drone.getId(), id))
                .findFirst().
                orElse(null);
    }





    private boolean droneAvailableForMedicine(Medicine medicine, List<Availability> droneAvailableTimes) {
        if (medicine.getDate() == null && medicine.getTime() == null) {
            return true;
        }

        for (Availability availability : droneAvailableTimes) {
            LocalTime fromTime = availability.getFrom();
            LocalTime toTime = availability.getUntil();

            if (medicine.getDate() != null) {
                if (medicine.getDate().getDayOfWeek().equals(availability.getDayOfWeek()) &&
                        (medicine.getTime() == null ||
                                (!medicine.getTime().isBefore(fromTime) && !medicine.getTime().isAfter(toTime)))) {
                    return true;
                }
            } else {
                if (!medicine.getTime().isBefore(fromTime) && !medicine.getTime().isAfter(toTime)) {
                    return true;
                }
            }
        }
//        logger.info("Day or time not met by drone.");
        return false;
    }

    private boolean droneCapabilitiesMeetingMedicine(MedRequirements medRequirements, Capabilities droneCap, double costPerDelivery) {
        Boolean reqCooling = medRequirements.getCooling();
        Boolean droneCooling = droneCap.getCooling();

        // If medicine requires cooling, the drone must have cooling. O/w, we don't care
        if (Boolean.TRUE.equals(reqCooling) && !Boolean.TRUE.equals(droneCooling)) {
//            logger.info("Cooling requirement not met by drone.");
            return false;
        }

        Boolean reqHeating = medRequirements.getHeating();
        Boolean droneHeating = droneCap.getHeating();

        // If medicine requires heating, the drone must have heating. O/w, we don't care
        if (Boolean.TRUE.equals(reqHeating) && !Boolean.TRUE.equals(droneHeating)) {
//            logger.info("Heating requirement not met by drone.");
            return false;
        }

        if (medRequirements.getMaxCost() != null) {
//            logger.info("Estimated CostPerDelivery : '{}', Medicine's MaxCost: '{}'", costPerDelivery, medRequirements.getMaxCost());
            return costPerDelivery <= medRequirements.getMaxCost();
        }

        return true;
    }

    private Point getServicePoint(int id, DroneServicePoint[] servicePoints) {
        if (servicePoints == null) {
            return null;
        }

        DroneServicePoint matchedPoint = Arrays.stream(servicePoints)
                .filter(servicePoint -> Objects.equals(servicePoint.getId(), id))
                .findFirst()
                .orElse(null);

        if (matchedPoint == null || matchedPoint.getLocation() == null) {
            return null;
        }

        LngLatAlt location = matchedPoint.getLocation();
        return Point.builder().lng(location.getLng()).lat(location.getLat()).build();
    }

    private int estTotalMovesFromServiceToAllDeliveries(Point servicePoint, List<Medicine> medDispatchRec) {
        int totalMoves = 0;
        for (Medicine medicine : medDispatchRec) {
            Point deliveryPoint = medicine.getDelivery();
            // inputs guaranteed to have delivery so this is a harmless if condition
            if (deliveryPoint == null) {
                continue;
            }
            PointRequest points = PointRequest.builder().position1(servicePoint).position2(deliveryPoint).build();
            totalMoves += (int) Math.round(getDistance(points) / stepSize);
        }
        return totalMoves;
    }

    @Override
    public List<String> getAvailableDrones(List<Medicine> medDispatchRec) {
        AvailabilityContext result = getAvailableDronesLogic(medDispatchRec);
        return result.availableDroneIds();
    }

    private AvailabilityContext getAvailableDronesLogic(List<Medicine> medDispatchRec) {
        DroneForServicePoint[] dronesForServicePoint =
                restTemplate.getForObject(ilpEndPoint + "/drones-for-service-points", DroneForServicePoint[].class);

        if (dronesForServicePoint == null) {
            return new AvailabilityContext(List.of(), Map.of(), Map.of());
        }

        List<String> availableDrones = new ArrayList<>();	// list of possible drone IDs
        int numberOfMedDispatch = medDispatchRec.size();	// no. of prescriptions to be delivered

        double totalCapacityRequired = medDispatchRec.stream()	// needed as a single drone must have enough capacity for all deliveries
                .mapToDouble(med -> med.getRequirements().getCapacity())
                .sum();

        // mapDroneToService = new HashMap<>();			    // just maps the drone ID to a service point ID to indicate that the drone can be found at that specific service point
        // mapServiceIdToServicePoint = new HashMap<>();		// using a map to maintain the location of each service point for O(1) lookup while calculating delivery paths

        // fix for concurrent stuff
        Map<String, Integer> droneToService =  new HashMap<>();
        Map<Integer, Point> serviceIdToPoint = new HashMap<>();

        DroneServicePoint[] servicePoints = restTemplate.getForObject(ilpEndPoint + "/service-points", DroneServicePoint[].class);	// calling this endpoint only once to get the locations of all the service points

        for (DroneForServicePoint droneForServicePoint : dronesForServicePoint) {
            int servicePointID = droneForServicePoint.getServicePointId();
            Point service = getServicePoint(servicePointID, servicePoints);
            // mapServiceIdToServicePoint.put(servicePointID, service);

            // fix for concurrent stuff
            serviceIdToPoint.put(servicePointID, service);

            int estTotalMoves = estTotalMovesFromServiceToAllDeliveries(service, medDispatchRec);
            // logger.info("Service point '{}' being considered", servicePointID);

            for (DronesAvailability dronesAvailability : droneForServicePoint.getDrones()) {
                String droneId = dronesAvailability.getId();
                // drone may be available at another service
                // point and so may have been already checked before
                // edit: might be unnecessary honestly but leaving it here is harmless
                if (availableDrones.contains(droneId)) {
                    continue;
                }

                // unnecessary check because droneDetails will be populated
                // but again, having this here is harmless
                Drone droneDetails = getDroneDetails(droneId);
                if (droneDetails == null) {
                    continue;
                }

//                logger.info("Drone ID '{}' being considered.", droneId);

                Capabilities droneCap = droneDetails.getCapability();

                if (totalCapacityRequired > droneCap.getCapacity()) {
//                    logger.info("Capacity requirements not met by drone.");
                    continue;
                }

                double totalCostToCompleteAllDeliveries = (estTotalMoves * droneCap.getCostPerMove()) + droneCap.getCostFinal() + droneCap.getCostInitial();
                double costPerDelivery = totalCostToCompleteAllDeliveries / numberOfMedDispatch;

                boolean droneAvailableForAllMeds = true;	// flag will be set to false if any medicine's conditions are not met

                for (Medicine medicine : medDispatchRec) {
                    boolean availableForThisMedicine = droneAvailableForMedicine(medicine, dronesAvailability.getAvailability());
                    if (!availableForThisMedicine) {
                        droneAvailableForAllMeds = false;
                        break;
                    }

                    MedRequirements medRequirements = medicine.getRequirements();
                    boolean capabilitiesMatchThisMedicine = droneCapabilitiesMeetingMedicine(medRequirements, droneCap, costPerDelivery);
                    if (!capabilitiesMatchThisMedicine) {
                        droneAvailableForAllMeds = false;
                        break;
                    }
                }

                if (droneAvailableForAllMeds) {
                    availableDrones.add(droneId);
                    // mapDroneToService.put(droneId, servicePointID);
                    // fix for concurrent stuff
                    droneToService.put(droneId, servicePointID);
                    // logger.info("Drone ID '{}' accepted.", droneId);
                }
            }
        }
        return new AvailabilityContext(availableDrones, droneToService, serviceIdToPoint);
    }





    private boolean hasMatchingAttribute(Drone drone, String attributeName, String attributeValue) {
        try {
            Field field = drone.getCapability().getClass().getDeclaredField(attributeName);
            field.setAccessible(true);
            Object fieldValue = field.get(drone.getCapability());

            if (attributeName.equals("heating") || attributeName.equals("cooling")) {
                boolean actual = fieldValue != null && (Boolean) fieldValue;
                boolean expected = Boolean.parseBoolean(attributeValue);
                return actual == expected;
            } else {

                if (fieldValue == null) {
                    return false;
                }

                double actualValue = Double.parseDouble(fieldValue.toString());
                double expectedValue = Double.parseDouble(attributeValue);
                return expectedValue == actualValue;
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return false;
        }
    }


    @Override
    public List<String> getDronesWithAttribute(String attributeName, String attributeValue) {
        Drone[] drones = restTemplate.getForObject(ilpEndPoint + "/drones", Drone[].class);

        if  (drones == null) {
            return List.of();
        }

        return Arrays.stream(drones)
                .filter(drone -> hasMatchingAttribute(drone, attributeName, attributeValue))
                .map(Drone::getId).
                toList();
    }

    private boolean performOperation(double fieldValue, double value, String operator) {
        return switch (operator) {
            case "=" -> fieldValue == value;
            case "!=" -> fieldValue != value;
            case "<" -> fieldValue < value;
            case ">" -> fieldValue > value;
            case ">=" -> fieldValue >= value;
            case "<=" -> fieldValue <= value;
            case null, default -> false;
        };
    }

    private boolean hasQueryAttributes(Drone drone, List<QueryAttributes> queryAttributes) {
        boolean result;
        for (QueryAttributes queryAttribute : queryAttributes) {
            String attributeName = queryAttribute.getAttribute();
            String attributeValue = queryAttribute.getValue();
            String attributeOperator =  queryAttribute.getOperator();
            try {
                Field field = drone.getCapability().getClass().getDeclaredField(attributeName);
                field.setAccessible(true);
                Object fieldValue = field.get(drone.getCapability());

                if (attributeName.equals("cooling") || attributeName.equals("heating")) {
                    boolean actual = fieldValue != null && (Boolean) fieldValue;
                    boolean expected = Boolean.parseBoolean(attributeValue);
                    result = actual == expected;
                } else {
                    if (fieldValue == null) {
                        return false;
                    }

                    double actualValue = Double.parseDouble(fieldValue.toString());
                    double expectedValue = Double.parseDouble(attributeValue);
                    result = performOperation(actualValue, expectedValue, attributeOperator);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                return false;
            }

            if (!result) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<String> getQueryAttributesDrones(List<QueryAttributes> queryAttributes) {
        Drone[] drones = restTemplate.getForObject(ilpEndPoint + "/drones", Drone[].class);

        if (drones == null) {
            return List.of();
        }

        return Arrays.stream(drones)
                .filter(drone -> hasQueryAttributes(drone, queryAttributes))
                .map(Drone::getId).
                toList();
    }


    // used to check if a path under consideration is not crossing over a no-fly region
    private boolean segmentsIntersect(Point p1, Point p2, Point q1, Point q2) {
        return Line2D.linesIntersect(
                p1.getLng(), p1.getLat(),
                p2.getLng(), p2.getLat(),
                q1.getLng(), q1.getLat(),
                q2.getLng(), q2.getLat()
        );
    }

    // used to check if a candidate point lies on the edge or boundary of a no-fly region
    private boolean pointOnSegment(Point p, Point a, Point b) {
        double epsilon = 1e-9;
        // Vector AB
        double abx = b.getLng() - a.getLng();
        double aby = b.getLat() - a.getLat();

        // Vector AP
        double apx = p.getLng() - a.getLng();
        double apy = p.getLat() - a.getLat();

        // check collinearity using cross product
        double cross = abx * apy - aby * apx;
        if (Math.abs(cross) > epsilon) {
            return false;   // Not collinear → cannot be on segment
        }

        // dot product to check if P lies between A and B
        double dot = abx * apx + aby * apy;
        if (dot < 0) {
            return false;   // P is behind A
        }

        double lenSq = abx * abx + aby * aby;

        if (dot > lenSq) {
            return false;   // P is beyond B
        }

        // otherwise, P is on the segment (including endpoints)
        return true;
    }

    private boolean pointOnEdge(Point p, List<Point> vertices) {
        for (int i = 0; i < vertices.size() - 1; i++) {
            Point a = vertices.get(i);
            Point b = vertices.get(i + 1);

            if (pointOnSegment(p, a, b)) {
                return true;   // boundary considered inside
            }
        }
        return false;
    }

    public boolean positionInRegionCheckForAStar(Point current, RegionRequest pointRegion) {
        Region region = pointRegion.getRegion();
        List<Point> vertices = region.getVertices();

        Point candidate = pointRegion.getPosition();

        // checking if candidate lies on a boundary or edge
        if (pointOnEdge(candidate, vertices)) {
            return true;
        }

        // ray-casting algorithm to check if candidate is in the rectangle
        double xp = candidate.getLng();
        double yp = candidate.getLat();

        int count = 0;

        for (int i = 0; i < vertices.size() - 1; i++) {
            double x1 = vertices.get(i).getLng();
            double y1 = vertices.get(i).getLat();
            double x2 = vertices.get(i + 1).getLng();
            double y2 = vertices.get(i + 1).getLat();

            double dy = y2 - y1;
            if (Math.abs(dy) < 1e-12) continue;

            boolean cond = (yp <= y1) != (yp <= y2);
            if (cond) {
                double xIntersect = x1 + (yp - y1) * (x2 - x1) / dy;
                if (xIntersect >= xp) count++;
            }
        }

        if (count % 2 == 1) {
            return true;
        }

        // check if path between current and candidate does not intersect rectangle edges
        for (int i = 0; i < vertices.size() - 1; i++) {
            Point a = vertices.get(i);
            Point b = vertices.get(i + 1);

            if (segmentsIntersect(current, candidate, a, b)) {
                return true;
            }
        }

        return false;
    }

    private List<Point> getNeighbours(Point current, List<Region> restrictedAreas) {
        List<Point> neighbours = new ArrayList<>();
        for (double direction = 0.0; direction < 360.0; direction += 22.5) {
            NextPointRequest nextPointRequest = NextPointRequest.builder().start(current).angle(direction).build();
            Point candidate = getnextPoint(nextPointRequest);

            boolean safe = true;

            for (Region area : restrictedAreas) {
                RegionRequest regionRequest = RegionRequest.builder()
                        .position(candidate)
                        .region(area)
                        .build();

                if (positionInRegionCheckForAStar(current, regionRequest)) {
                    safe = false;
                    break;
                }
            }

            if (safe) {
                neighbours.add(candidate);
            }

        }
        return neighbours;
    }

    private List<Point> reconstructPath(AStarNode end) {
        List<Point> path = new ArrayList<>();
        AStarNode current = end;

        while (current != null) {
            path.add(current.getPoint());
            current = current.getParent();
        }

        Collections.reverse(path);
        return path;
    }

    private double heuristic(Point from, Point to) {
        PointRequest req = PointRequest.builder()
                .position1(from)
                .position2(to)
                .build();
        return getDistance(req) / stepSize;
    }

    private boolean isGoal(Point p, Point goal) {
        PointRequest req = PointRequest.builder()
                .position1(p)
                .position2(goal)
                .build();
        return getDistance(req) < stepSize;
    }


    private List<Point> aStar(Point start, Point goal, List<Region> restrictedAreas) {
        PriorityQueue<AStarNode> openList = new PriorityQueue<>(); // uses compareTo(f-cost)
        Set<Point> closedList = new HashSet<>();

        Map<Point, AStarNode> allNodes = new HashMap<>();

        AStarNode startNode = new AStarNode(start);
        startNode.setGCost(0.0);                         // start.g = 0
        startNode.setHCost(heuristic(start, goal));      // start.h = heuristic(start, goal)
        startNode.setParent(null);                       // start.parent = null

        openList.add(startNode);
        allNodes.put(start, startNode);

        // while openList is not empty:
        while (!openList.isEmpty()) {
            // current = node in openList with lowest f value
            AStarNode current = openList.poll();
            Point currentPoint = current.getPoint();

            // if current = goal: return reconstruct_path(current)
            if (isGoal(currentPoint, goal)) {
                return reconstructPath(current);  // your existing helper
            }

            // Move current from open to closed
            closedList.add(currentPoint);

            // for each neighbor of current:
            for (Point neighborPoint : getNeighbours(currentPoint, restrictedAreas)) {
                // if neighbor in closedList: continue
                if (closedList.contains(neighborPoint)) {
                    continue;
                }

                // tentative_g = current.g + distance(current, neighbor)
                double tentativeG =
                        current.getGCost() + 1;

                // Lookup or create neighbor node
                AStarNode neighbor = allNodes.get(neighborPoint);

                if (neighbor == null) {
                    // neighbor not in openList
                    neighbor = new AStarNode(neighborPoint);
                    allNodes.put(neighborPoint, neighbor);
                    // We will add it to openList after we set costs below

                } else if (tentativeG >= neighbor.getGCost()) {
                    // else if tentative_g >= neighbor.g: continue
                    // This path is not better
                    continue;
                }

                // This path is the best so far:
                // neighbor.parent = current
                neighbor.setParent(current);
                // neighbor.g = tentative_g
                neighbor.setGCost(tentativeG);
                // neighbor.h = heuristic(neighbor, goal)
                neighbor.setHCost(heuristic(neighborPoint, goal));
                // neighbor.f = neighbor.g + neighbor.h  (implicit via getFCost/compareTo)

                // If neighbor not in openList, or if we improved it, ensure it's in openList
                openList.add(neighbor);
            }
        }
        // return failure because no path exists
        return Collections.emptyList();
    }

    public List<Region> getRestrictedAreas() {
        RestrictedArea[] restrictedAreas = restTemplate.getForObject(ilpEndPoint + "/restricted-areas", RestrictedArea[].class);

        if (restrictedAreas == null) {
            return List.of();
        }

        List<Region> regions = new ArrayList<>();

        for (RestrictedArea area : restrictedAreas) {
            List<Point> regionVertices = area.getVertices().stream()
                    .map(v -> Point.builder()
                            .lng(v.getLng())
                            .lat(v.getLat())
                            .build())
                    .collect(Collectors.toList());

            Region region = Region.builder()
                    .name(area.getName())
                    .vertices(regionVertices)
                    .build();

            regions.add(region);
        }
        return regions;
    }


    private Map<Medicine, List<DroneServicePair>> getClosestServicePointsToDeliveries(
            AvailabilityContext availabilityContext,
            List<Medicine> medicines
    ) {
        Map<Medicine, List<DroneServicePair>> result = new HashMap<>();

        for (Medicine medicine : medicines) {

            Point deliveryPoint = medicine.getDelivery();
            List<DroneServicePair> rankedList = new ArrayList<>();

            // Build list of all (droneId, servicePointId) pairs
            for (String droneIdStr : availabilityContext.availableDroneIds()) {

                int droneId = Integer.parseInt(droneIdStr);
                int servicePointId = availabilityContext.droneToService().get(droneIdStr);

                rankedList.add(DroneServicePair.builder().droneId(droneId).servicePointId(servicePointId).build());
            }

            // Sort by distance from service point → delivery point
            rankedList.sort(Comparator.comparingDouble(pair -> {
                Point sp = availabilityContext.serviceIdToPoint().get(pair.getServicePointId());
                return getDistance(
                        PointRequest.builder()
                                .position1(sp)
                                .position2(deliveryPoint)
                                .build()
                );
            }));

            result.put(medicine, rankedList);
        }
        return result;
    }

    @Override
    public ObjectNode calcDeliveryPathAsGeoJson(List<Medicine> medDispatchRec) {
        CalcDeliveryPathResult path = calcDeliveryPathSingleFlight(new ArrayList<>(medDispatchRec));
        List<DronePath> dronePaths = path.getDronePaths();

        if (dronePaths.isEmpty()) {
            return getEmptyGeoJsonNode();
        }

        List<Delivery> deliveries = dronePaths.getFirst().getDeliveries();

        String droneID = dronePaths.getFirst().getDroneId();
        int totalMoves = path.getTotalMoves();
        double totalCost = path.getTotalCost();
        List<Point> route = new ArrayList<>();

        for (Delivery delivery : deliveries) {
            List<Point> flight = delivery.getFlightPath();
            route.addAll(flight.subList(0, flight.size() - 1));             // if we don't do this we'd end up with the delivery point occurring 3 times
        }

        List<Point> lastFlight = deliveries.getLast().getFlightPath();      // b/c of the above logic, need to specifically add the service point
        route.add(lastFlight.getLast());

        return getGeoJsonNode(route, droneID, totalMoves, totalCost);
    }

    private static ObjectNode getEmptyGeoJsonNode() {
        ObjectMapper mapper = new ObjectMapper();

        ObjectNode featureCollection = mapper.createObjectNode();
        featureCollection.put("type", "FeatureCollection");
        featureCollection.set("features", mapper.createArrayNode());

        return featureCollection;
    }

    private static ObjectNode getGeoJsonNode(List<Point> route, String droneID, int totalMoves, double totalCost) {
        ObjectMapper mapper = new ObjectMapper();

        ObjectNode featureCollection = mapper.createObjectNode();
        featureCollection.put("type", "FeatureCollection");

        ArrayNode featuresArray = mapper.createArrayNode();

        ObjectNode feature = mapper.createObjectNode();
        feature.put("type", "Feature");

        ObjectNode props = mapper.createObjectNode();
        props.put("droneID", droneID);
        props.put("totalMoves", totalMoves);
        props.put("totalCost", totalCost);
        feature.set("properties", props);

        ObjectNode geometry = mapper.createObjectNode();
        geometry.put("type", "LineString");

        ArrayNode coordinates = mapper.createArrayNode();
        for (Point p : route) {
            ArrayNode coordinate = mapper.createArrayNode();
            coordinate.add(p.getLng());
            coordinate.add(p.getLat());
            coordinates.add(coordinate);
        }

        geometry.set("coordinates", coordinates);
        feature.set("geometry", geometry);
        featuresArray.add(feature);
        featureCollection.set("features", featuresArray);

        return featureCollection;
    }

     private boolean isCostMetByAllMeds(List<Medicine> medDispatchRec, int totalMoves, Capabilities droneCap) {
        double totalCost = totalMoves * droneCap.getCostPerMove() + droneCap.getCostFinal() + droneCap.getCostInitial();
        double costPerDelivery = totalCost / medDispatchRec.size();

        for (Medicine medicine : medDispatchRec) {
            MedRequirements medReq = medicine.getRequirements();
            if (medReq.getMaxCost() != null && medReq.getMaxCost() < costPerDelivery) {
//                logger.info("Medicine '{}' can't afford this drone.", medicine.getId());
                return false;
            }
        }
        return true;
    }

    private CalcDeliveryPathResult calcDeliveryPathSingleFlight(List<Medicine> medDispatchRec) {
        // List<String> availableDroneIDs = getAvailableDrones(medDispatchRec);
        AvailabilityContext availabilityContext = getAvailableDronesLogic(medDispatchRec);

        if (availabilityContext.availableDroneIds().isEmpty()) {
            logger.info("No drone can do these deliveries in one flight sequence");
            return CalcDeliveryPathResult.builder().build();
        }

        medDispatchRec.sort(Comparator.comparing(
                        Medicine::getTime,
                        Comparator.nullsLast(Comparator.naturalOrder())));

        Map<Integer, List<Delivery>> pathFromServicePoint = new HashMap<>();
        List<Region> restrictedRegions = getRestrictedAreas();
        double totalCapacityRequired = medDispatchRec.stream()
                .mapToDouble(med -> med.getRequirements().getCapacity())
                .sum();

        for (String droneID: availabilityContext.availableDroneIds()) {
            logger.info("Trying drone '{}' for completing all deliveries in one go.", droneID);
            Drone drone = getDroneDetails(droneID);
            Capabilities droneCap = drone.getCapability();

            if (totalCapacityRequired > droneCap.getCapacity()) {
                continue;
            }

            int servicePointID = availabilityContext.droneToService().get(droneID);
            Point servicePoint = availabilityContext.serviceIdToPoint().get(servicePointID);

            if (pathFromServicePoint.containsKey(servicePointID)) {
                List<Delivery> deliveries = pathFromServicePoint.get(servicePointID);
                int totalMoves = deliveries.stream()
                        .mapToInt(d -> d.getFlightPath().size())
                        .sum();
                if (totalMoves <= droneCap.getMaxMoves() && isCostMetByAllMeds(medDispatchRec, totalMoves, droneCap)) {
                    DronePath dronePath = DronePath.builder().droneId(droneID).deliveries(deliveries).build();
                    double totalCost = (totalMoves * droneCap.getCostPerMove()) + droneCap.getCostInitial() + droneCap.getCostFinal();
                    return CalcDeliveryPathResult.builder().totalCost(totalCost).totalMoves(totalMoves).dronePaths(List.of(dronePath)).build();
                }
            } else {
                List<Delivery> deliveriesByDrone = new ArrayList<>();
                int totalMoves = 0;

                Point from = servicePoint;
                Point to;
                boolean atLeastOneMedNotDelivered = false;
                for (Medicine medicine : medDispatchRec) {
                    to = medicine.getDelivery();
                    List<Point> path = aStar(from, to, restrictedRegions);

                    if (path.isEmpty()) {
                        logger.info("Medicine '{}' can't be delivered", medicine.getId());
                        atLeastOneMedNotDelivered = true;
                        break;
                    }

                    path.add(path.getLast());
                    totalMoves += path.size() - 1;
                    Delivery delivery = Delivery.builder().deliveryId(medicine.getId()).flightPath(path).build();
                    deliveriesByDrone.add(delivery);
                    from = path.getLast();
                }

                if (atLeastOneMedNotDelivered) {
                    continue;
                }

                to = servicePoint;
                List<Point> returnPath = aStar(from, to, restrictedRegions);
                if (returnPath.isEmpty()) {
                    logger.info("No return path exists from the last delivery. Checking new drone.");
                    continue;
                } else {
                    // no hover needed after returning to the service point
                    totalMoves += returnPath.size() - 1;
                    Delivery delivery = Delivery.builder().deliveryId(null).flightPath(returnPath).build();
                    deliveriesByDrone.add(delivery);
                }

                if (totalMoves <= droneCap.getMaxMoves() && isCostMetByAllMeds(medDispatchRec, totalMoves, droneCap)) {
                    DronePath dronePath = DronePath.builder().droneId(droneID).deliveries(deliveriesByDrone).build();
                    double totalCost = (totalMoves * droneCap.getCostPerMove()) + droneCap.getCostInitial() + droneCap.getCostFinal();
                    return CalcDeliveryPathResult.builder().totalCost(totalCost).totalMoves(totalMoves).dronePaths(List.of(dronePath)).build();
                } else {
                    pathFromServicePoint.put(servicePointID, deliveriesByDrone);
                }

            }
        }

        logger.info("No drone can do these deliveries in one flight sequence");
        return CalcDeliveryPathResult.builder().build();
    }




    private Map<String, List<Medicine>> groupMedsByTime(List<Medicine> medicines) {
        Map<String, List<Medicine>> medsGroupedByTime = new HashMap<>();
        String slot;
        for (Medicine medicine : medicines) {
            LocalTime deliveryTime = medicine.getTime();
            if (deliveryTime == null) {
                slot = "AnyTime";
            } else if (deliveryTime.isBefore(LocalTime.NOON)) {
                slot = "Morning";
            } else if (deliveryTime.isBefore(LocalTime.of(17, 0, 0))) {
                slot = "Afternoon";
            } else {
                slot = "Evening";
            }
            medsGroupedByTime
                    .computeIfAbsent(slot, k -> new ArrayList<>())
                    .add(medicine);
        }
        return medsGroupedByTime;
    }

    private Map<Medicine, List<DroneServicePair>> availableDronesRankedForEachMed(List<Medicine> medicines) {
        Map<Medicine, List<DroneServicePair>> rankedDroneMap = new HashMap<>();
        for (Medicine medicine : medicines) {
            // List<String> availableDroneIDs = getAvailableDrones(List.of(medicine));
            AvailabilityContext availabilityContext = getAvailableDronesLogic(List.of(medicine));
            Map<Medicine, List<DroneServicePair>> rankedDronesForMed = getClosestServicePointsToDeliveries(availabilityContext, List.of(medicine));
            rankedDroneMap.put(medicine, rankedDronesForMed.get(medicine));
        }
        return rankedDroneMap;
    }

    @Override
    public CalcDeliveryPathResult calcDeliveryPath(List<Medicine> medDispatchRec) {

        double totalCost = 0.0;
        int totalMoves = 0;

        Map<String, List<Delivery>> deliveries = new HashMap<>();
        List<Region> restrictedAreas = getRestrictedAreas();

        // Group medicines by day
        medDispatchRec.sort(Comparator.comparing(
                Medicine::getDate,
                Comparator.nullsLast(Comparator.naturalOrder())));

        Map<LocalDate, List<Medicine>> medsByDate = medDispatchRec.stream()
                .collect(Collectors.groupingBy(m -> m.getDate() == null ? LocalDate.MAX : m.getDate()));

        for (Map.Entry<LocalDate, List<Medicine>> entry : medsByDate.entrySet()) {
            List<Medicine> dayMeds = new ArrayList<>(entry.getValue());
            dayMeds.sort(Comparator.comparing(
                    Medicine::getTime,
                    Comparator.nullsLast(Comparator.naturalOrder())));

            // grouping medicines by morning, afternoon, evening, or any time group
            Map<String, List<Medicine>> medsGroupedByTime = groupMedsByTime(dayMeds);

            // for each time window independently:
            for (Map.Entry<String, List<Medicine>> timeEntry : medsGroupedByTime.entrySet()) {
                List<Medicine> medicineTimeList = timeEntry.getValue();

                // fetch drones available ONLY for this time slot
                // List<String> availableDrones = getAvailableDrones(medicineTimeList);

                // fix for concurrent stuff
                AvailabilityContext availabilityContext = getAvailableDronesLogic(medicineTimeList);

                // if availableDrones is an empty list, call a method which for each medicine,
                // individually checks what drones satisfy it and rank these using the same logic
                // as getClosestServicePointsToDeliveries
                Map<Medicine, List<DroneServicePair>> rankedDroneMap;
                if (availabilityContext.availableDroneIds().isEmpty()) {
                    rankedDroneMap = availableDronesRankedForEachMed(medicineTimeList);
                } else {
                    rankedDroneMap = getClosestServicePointsToDeliveries(availabilityContext, medicineTimeList);
                }

                int i = 0;

                while (i < medicineTimeList.size()) {
                    Medicine startingMed = medicineTimeList.get(i);
                    List<DroneServicePair> candidateDrones = rankedDroneMap.get(startingMed);

                    boolean assigned = false;

                    for (DroneServicePair pair : candidateDrones) {
                        String droneId = String.valueOf(pair.getDroneId());
                        int servicePointId = pair.getServicePointId();

                        Drone drone = getDroneDetails(droneId);
                        Point base = availabilityContext.serviceIdToPoint().get(servicePointId);

                        int movesLeft = drone.getCapability().getMaxMoves();
                        double capacityLeft = drone.getCapability().getCapacity();

                        List<Delivery> thisFlightDeliveries = new ArrayList<>();
                        List<Medicine> thisFlightMeds = new ArrayList<>();
                        int forwardMoves = 0;

                        Point current = base;
                        int j = i;

                        while (j < medicineTimeList.size()) {
                            Medicine med = medicineTimeList.get(j);
                            double medCapacity = med.getRequirements().getCapacity();
                            Point dest = med.getDelivery();

                            List<Point> forwardPath = aStar(current, dest, restrictedAreas);
                            forwardPath.add(forwardPath.getLast());
                            int movesNeeded = forwardPath.size() - 1;

                            List<Point> returnPath = aStar(forwardPath.getLast(), base, restrictedAreas);
                            int returnMoves = returnPath.size() - 1;

                            if (movesNeeded + returnMoves <= movesLeft &&
                                    medCapacity <= capacityLeft) {

                                thisFlightDeliveries.add(
                                        Delivery.builder()
                                                .deliveryId(med.getId())
                                                .flightPath(forwardPath)
                                                .build()
                                );

                                thisFlightMeds.add(med);

                                movesLeft -= movesNeeded;
                                forwardMoves += movesNeeded;
                                capacityLeft -= medCapacity;

                                current = forwardPath.getLast();
                                j++;
                            } else {
                                break;
                            }
                        }

                        if (thisFlightMeds.isEmpty()) {
                            continue;
                        }

                        List<Point> ret = aStar(current, base, restrictedAreas);
                        thisFlightDeliveries.add(
                                Delivery.builder()
                                        .deliveryId(null)
                                        .flightPath(ret)
                                        .build()
                        );

                        int returnMoves = ret.size() - 1;

                        Capabilities cap = drone.getCapability();
                        int totalFlightMoves = forwardMoves + returnMoves;

                        double flightCost =
                                cap.getCostInitial()
                                        + (totalFlightMoves * cap.getCostPerMove())
                                        + cap.getCostFinal();

                        double costPerDelivery = flightCost / thisFlightMeds.size();

                        Medicine violatingMed = null;

                        for (Medicine deliveredMed : thisFlightMeds) {
                            Double max = deliveredMed.getRequirements().getMaxCost();
                            if (max != null && costPerDelivery > max) {
                                violatingMed = deliveredMed;
                                break;
                            }
                        }

                        if (violatingMed != null) {
                            rankedDroneMap.get(violatingMed)
                                    .removeIf(p -> p.getDroneId() == pair.getDroneId());
                            continue;
                        }

                        totalMoves += totalFlightMoves;
                        totalCost += flightCost;

                        deliveries.computeIfAbsent(droneId, k -> new ArrayList<>())
                                .addAll(thisFlightDeliveries);

                        i += thisFlightMeds.size();
                        assigned = true;
                        break;
                    }

                    if (!assigned) {
                        logger.info("This medicine can't be delivered - skipping it.");
                        i++;
                    }
                }
            }
        }

        List<DronePath> dronePaths = deliveries.entrySet().stream()
                .map(e -> DronePath.builder()
                        .droneId(e.getKey())
                        .deliveries(e.getValue())
                        .build())
                .toList();

        return CalcDeliveryPathResult.builder()
                .totalCost(totalCost)
                .totalMoves(totalMoves)
                .dronePaths(dronePaths)
                .build();
    }

    @Override
    public OrderResponse tryPlacingOrder(Medicine medicine){
        List<String> availableDrones = getAvailableDrones(List.of(medicine));
        if (availableDrones.isEmpty()) {
            return OrderResponse.failure(
                    "No drones can deliver this type of medicine at the required date and time. Choose a different date or time!"
            );
        }

        List<Order> allOrders = orderRepository.findAll();

        String selectedDrone = null;

        for (String droneId : availableDrones) {
            boolean alreadyBooked = allOrders.stream()
                    .anyMatch(order -> order.getDroneId().equals(droneId));

            if (!alreadyBooked) {
                selectedDrone = droneId;
                break;
            }
        }

        if (selectedDrone == null) {
            return OrderResponse.failure(
                    "All eligible drones are currently booked. Try again later!"
            );
        }

        Order newOrder = new Order();
        newOrder.setOrderID(String.valueOf(medicine.getId()));    // or use medicine.getName() if you have it
        newOrder.setLng(medicine.getDelivery().getLng());
        newOrder.setLat(medicine.getDelivery().getLat());
        newOrder.setOrderDate(medicine.getDate().toString());
        newOrder.setOrderTime(medicine.getTime().toString());
        newOrder.setDroneId(selectedDrone);
        newOrder.setStatus("PLACED");

        orderRepository.save(newOrder);

        return OrderResponse.success(selectedDrone);
    }

    @Override
    public ObjectNode showFlightPath(FlightPathInputMCP flightPathInputMCP) {
        Medicine medicine = flightPathInputMCP.getMedicine();
        return calcDeliveryPathAsGeoJson(new ArrayList<>(List.of(medicine)));
    }

    @Override
    public boolean deliveryLocationAccessible(Point point) {
        List<Region> restrictedAreas = getRestrictedAreas();
        boolean deliveryPossible = true;

        for (Region area : restrictedAreas) {
            RegionRequest regionRequest = RegionRequest.builder()
                    .position(point)
                    .region(area)
                    .build();

            if (positionInRegion(regionRequest)) {
                deliveryPossible = false;
                break;
            }
        }

        return deliveryPossible;
    }
}