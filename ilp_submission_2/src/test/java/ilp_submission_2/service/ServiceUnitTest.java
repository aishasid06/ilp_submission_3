package ilp_submission_2.service;

import ilp_submission_2.dtos.*;
import ilp_submission_2.repository.OrderRepository;
import ilp_submission_2.service.impl.DroneServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ServiceUnitTest {
    private DroneServiceImpl droneService;

    @BeforeEach
    public void setup() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        OrderRepository orderRepository = mock(OrderRepository.class);
        String ilpEndPoint = "https://ilp-rest-2025-bvh6e9hschfagrgy.ukwest-01.azurewebsites.net/";
        droneService = new DroneServiceImpl(restTemplate, ilpEndPoint, orderRepository);
    }

    @Test
    @DisplayName("JUnit test for getDistance operation")
    public void calculateDistanceBtwTwoLngLatPairs() {
        Point position1 = Point.builder().lng(-3.192473).lat(55.946233).build();
        Point position2 = Point.builder().lng(-3.192473).lat(55.942617).build();

        PointRequest req = PointRequest.builder().position1(position1).position2(position2).build();

        assertEquals(0.003616, droneService.getDistance(req), 0.00015);
    }

    @Test
    @DisplayName("JUnit test for areClose operation on two LngLat pairs close to each other")
    public void checkIfTwoLngLatPairsAreClose() {
        Point position1 = Point.builder().lng(-3.192473).lat(55.946233).build();
        Point position2 = Point.builder().lng(-3.192471).lat(55.946233).build();

        PointRequest req = PointRequest.builder().position1(position1).position2(position2).build();

        assertTrue(droneService.areClose(req));
    }

    @Test
    @DisplayName("JUnit test for areClose operation on two LngLat pairs not close to each other")
    public void checkIfTwoLngLatPairsAreNotClose() {
        Point position1 = Point.builder().lng(-3.192473).lat(55.946233).build();
        Point position2 = Point.builder().lng(-3.192473).lat(55.942617).build();

        PointRequest req = PointRequest.builder().position1(position1).position2(position2).build();

        assertFalse(droneService.areClose(req));
    }

    @Test
    @DisplayName("JUnit test for positionInRegion operation where a LngLat pair is in a convex region")
    public void checkIfLngLatPairInConvexRegion() {
        Point position = Point.builder().lng(-3.189123).lat(55.945123).build();

        List<Point> vertices = List.of(
                Point.builder().lng(-3.192473).lat(55.946233).build(),
                Point.builder().lng(-3.192473).lat(55.942617).build(),
                Point.builder().lng(-3.184319).lat(55.942617).build(),
                Point.builder().lng(-3.184319).lat(55.946233).build(),
                Point.builder().lng(-3.192473).lat(55.946233).build()
        );

        Region region = Region.builder().name("No-fly Zone A").vertices(vertices).build();

        RegionRequest req = RegionRequest.builder().position(position).region(region).build();

        assertTrue(droneService.positionInRegion(req));
    }

    @Test
    @DisplayName("JUnit test for positionInRegion operation where a LngLat pair is not in a convex region")
    public void checkIfLngLatPairNotInConvexRegion() {
        Point position = Point.builder().lng(-3.189123).lat(55.946300).build();

        List<Point> vertices = List.of(
                Point.builder().lng(-3.192473).lat(55.946233).build(),
                Point.builder().lng(-3.192473).lat(55.942617).build(),
                Point.builder().lng(-3.184319).lat(55.942617).build(),
                Point.builder().lng(-3.184319).lat(55.946233).build(),
                Point.builder().lng(-3.192473).lat(55.946233).build()
        );

        Region region = Region.builder().name("No-fly Zone A").vertices(vertices).build();

        RegionRequest req = RegionRequest.builder().position(position).region(region).build();

        assertFalse(droneService.positionInRegion(req));
    }

    @Test
    @DisplayName("JUnit test for positionInRegion operation where a LngLat pair is in a concave region")
    public void checkIfLngLatPairInConcaveRegion() {
        Point position = Point.builder().lng(-3.189).lat(55.944).build();

        List<Point> vertices = List.of(
                Point.builder().lng(-3.192473).lat(55.946233).build(),
                Point.builder().lng(-3.192473).lat(55.942617).build(),
                Point.builder().lng(-3.188319).lat(55.944123).build(),
                Point.builder().lng(-3.184319).lat(55.942617).build(),
                Point.builder().lng(-3.184319).lat(55.946233).build(),
                Point.builder().lng(-3.192473).lat(55.946233).build()
        );

        Region region = Region.builder().name("No-fly Zone A").vertices(vertices).build();

        RegionRequest req = RegionRequest.builder().position(position).region(region).build();

        assertTrue(droneService.positionInRegion(req));
    }

    @Test
    @DisplayName("JUnit test for positionInRegion operation where a LngLat pair is not in a concave region")
    public void checkIfLngLatPairNotInConcaveRegion() {
        Point position = Point.builder().lng(-3.188).lat(55.944).build();

        List<Point> vertices = List.of(
                Point.builder().lng(-3.192473).lat(55.946233).build(),
                Point.builder().lng(-3.192473).lat(55.942617).build(),
                Point.builder().lng(-3.188319).lat(55.944123).build(),
                Point.builder().lng(-3.184319).lat(55.942617).build(),
                Point.builder().lng(-3.184319).lat(55.946233).build(),
                Point.builder().lng(-3.192473).lat(55.946233).build()
        );

        Region region = Region.builder().name("No-fly Zone A").vertices(vertices).build();

        RegionRequest req = RegionRequest.builder().position(position).region(region).build();

        assertFalse(droneService.positionInRegion(req));
    }

    @Test
    @DisplayName("JUnit test for positionInRegion operation where a LngLat pair is in a complex concave region")
    public void checkIfLngLatPairInComplexConcaveRegion() {
        Point position = Point.builder().lng(-3.185).lat(55.943).build();

        List<Point> vertices = List.of(
                Point.builder().lng(-3.192473).lat(55.946233).build(),
                Point.builder().lng(-3.192473).lat(55.942617).build(),
                Point.builder().lng(-3.188319).lat(55.944123).build(),
                Point.builder().lng(-3.184319).lat(55.942617).build(),
                Point.builder().lng(-3.184319).lat(55.946233).build(),
                Point.builder().lng(-3.187001).lat(55.940001).build(),
                Point.builder().lng(-3.192473).lat(55.946233).build()
        );

        Region region = Region.builder().name("No-fly Zone A").vertices(vertices).build();

        RegionRequest req = RegionRequest.builder().position(position).region(region).build();

        assertTrue(droneService.positionInRegion(req));
    }

    @Test
    @DisplayName("JUnit test for positionInRegion operation where a LngLat pair is not in a complex concave region")
    public void checkIfLngLatPairNotInComplexConcaveRegion() {
        Point position = Point.builder().lng(-3.184).lat(55.943).build();

        List<Point> vertices = List.of(
                Point.builder().lng(-3.192473).lat(55.946233).build(),
                Point.builder().lng(-3.192473).lat(55.942617).build(),
                Point.builder().lng(-3.188319).lat(55.944123).build(),
                Point.builder().lng(-3.184319).lat(55.942617).build(),
                Point.builder().lng(-3.184319).lat(55.946233).build(),
                Point.builder().lng(-3.187001).lat(55.940001).build(),
                Point.builder().lng(-3.192473).lat(55.946233).build()
        );

        Region region = Region.builder().name("No-fly Zone A").vertices(vertices).build();

        RegionRequest req = RegionRequest.builder().position(position).region(region).build();

        assertFalse(droneService.positionInRegion(req));
    }

    @Test
    @DisplayName("JUnit test for positionInRegion operation where a LngLat pair is on an edge")
    public void checkIfLngLatPairOnEdgeInRegion() {
        Point position = Point.builder().lng(-3.18).lat(55.90).build();

        List<Point> vertices = List.of(
                Point.builder().lng(-3.19).lat(55.95).build(),
                Point.builder().lng(-3.16).lat(55.80).build(),
                Point.builder().lng(-3.17).lat(55.75).build(),
                Point.builder().lng(-3.19).lat(55.95).build()
        );

        Region region = Region.builder().name("No-fly Zone A").vertices(vertices).build();

        RegionRequest req = RegionRequest.builder().position(position).region(region).build();

        assertTrue(droneService.positionInRegion(req));
    }

    @Test
    @DisplayName("JUnit test for positionInRegion operation where a LngLat pair is close to an edge")
    public void checkIfLngLatPairCloseToEdgeNotInRegion() {
        Point position = Point.builder().lng(-3.18).lat(55.9003).build();

        List<Point> vertices = List.of(
                Point.builder().lng(-3.19).lat(55.95).build(),
                Point.builder().lng(-3.16).lat(55.80).build(),
                Point.builder().lng(-3.17).lat(55.75).build(),
                Point.builder().lng(-3.19).lat(55.95).build()
        );

        Region region = Region.builder().name("No-fly Zone A").vertices(vertices).build();

        RegionRequest req = RegionRequest.builder().position(position).region(region).build();

        assertFalse(droneService.positionInRegion(req));
    }

    @Test
    @DisplayName("JUnit test for getNextPoint at 0° (East)")
    public void findNextPointAtZeroDegrees() {
        Point start = Point.builder().lng(-3.192473).lat(55.946233).build();
        NextPointRequest req = NextPointRequest.builder().start(start).angle(0.0).build();

        Point nextPoint = droneService.getnextPoint(req);
        Point expected = Point.builder().lng(-3.192323).lat(55.946233).build();

        assertEquals(expected.getLng(), nextPoint.getLng(), 0.00015);
        assertEquals(expected.getLat(), nextPoint.getLat(), 0.00015);
    }

    @Test
    @DisplayName("JUnit test for getNextPoint at 45° (Northeast)")
    public void findNextPointAtFortyFiveDegrees() {
        Point start = Point.builder().lng(-3.192473).lat(55.946233).build();

        NextPointRequest req = NextPointRequest.builder().start(start).angle(45.0).build();

        Point nextPoint = droneService.getnextPoint(req);
        Point expectedNextPoint = Point.builder().lng(-3.192366934).lat(55.94633907).build();

        assertEquals(expectedNextPoint.getLng(), nextPoint.getLng(), 0.00015);
        assertEquals(expectedNextPoint.getLat(), nextPoint.getLat(), 0.00015);
    }

    @Test
    @DisplayName("JUnit test for getNextPoint at 90° (North)")
    public void findNextPointAtNinetyDegrees() {
        Point start = Point.builder().lng(-3.192473).lat(55.946233).build();
        NextPointRequest req = NextPointRequest.builder().start(start).angle(90.0).build();

        Point nextPoint = droneService.getnextPoint(req);
        Point expected = Point.builder().lng(-3.192473).lat(55.946383).build();

        assertEquals(expected.getLng(), nextPoint.getLng(), 0.00015);
        assertEquals(expected.getLat(), nextPoint.getLat(), 0.00015);
    }

    @Test
    @DisplayName("JUnit test for getNextPoint at 135° (Northwest)")
    public void findNextPointAtOneThirtyFiveDegrees() {
        Point start = Point.builder().lng(-3.192473).lat(55.946233).build();
        NextPointRequest req = NextPointRequest.builder().start(start).angle(135.0).build();

        Point nextPoint = droneService.getnextPoint(req);
        Point expected = Point.builder().lng(-3.192579).lat(55.946339).build();

        assertEquals(expected.getLng(), nextPoint.getLng(), 0.00015);
        assertEquals(expected.getLat(), nextPoint.getLat(), 0.00015);
    }

    @Test
    @DisplayName("JUnit test for getNextPoint at 180° (West)")
    public void findNextPointAtOneEightyDegrees() {
        Point start = Point.builder().lng(-3.192473).lat(55.946233).build();
        NextPointRequest req = NextPointRequest.builder().start(start).angle(180.0).build();

        Point nextPoint = droneService.getnextPoint(req);
        Point expected = Point.builder().lng(-3.192623).lat(55.946233).build();

        assertEquals(expected.getLng(), nextPoint.getLng(), 0.00015);
        assertEquals(expected.getLat(), nextPoint.getLat(), 0.00015);
    }

    @Test
    @DisplayName("JUnit test for getNextPoint at 270° (South)")
    public void findNextPointAtTwoSeventyDegrees() {
        Point start = Point.builder().lng(-3.192473).lat(55.946233).build();
        NextPointRequest req = NextPointRequest.builder().start(start).angle(270.0).build();

        Point nextPoint = droneService.getnextPoint(req);
        Point expected = Point.builder().lng(-3.192473).lat(55.946083).build();

        assertEquals(expected.getLng(), nextPoint.getLng(), 0.00015);
        assertEquals(expected.getLat(), nextPoint.getLat(), 0.00015);
    }

    @Test
    @DisplayName("JUnit test for getNextPoint at 315° (Southeast)")
    public void findNextPointAtThreeFifteenDegrees() {
        Point start = Point.builder().lng(-3.192473).lat(55.946233).build();
        NextPointRequest req = NextPointRequest.builder().start(start).angle(315.0).build();

        Point nextPoint = droneService.getnextPoint(req);
        Point expected = Point.builder().lng(-3.192366).lat(55.946126).build();

        assertEquals(expected.getLng(), nextPoint.getLng(), 0.00015);
        assertEquals(expected.getLat(), nextPoint.getLat(), 0.00015);
    }

    // Testing for R1.1.1 begins here
    @Test
    @DisplayName("Candidate Point Lies on the Edge of a No-Fly Zone")
    public void nextPointOnEdge() {
        Point current = Point.builder().lng(-3.1865001).lat(55.94149995).build();
        Point candidate = Point.builder().lng(-3.1865).lat(55.9435).build();

        List<Point> vertices = List.of(
                Point.builder().lng(-3.1925).lat(55.9435).build(),
                Point.builder().lng(-3.1925).lat(55.9415).build(),
                Point.builder().lng(-3.1865).lat(55.9415).build(),
                Point.builder().lng(-3.1865).lat(55.9435).build(),
                Point.builder().lng(-3.1925).lat(55.9435).build()
        );

        Region region = Region.builder().name("No-fly Zone A").vertices(vertices).build();
        RegionRequest req = RegionRequest.builder().position(candidate).region(region).build();

        assertTrue(droneService.positionInRegionCheckForAStar(current, req));
    }

    @Test
    @DisplayName("Candidate Point Lies Inside a No-Fly Zone")
    public void nextPointInside() {
        Point current = Point.builder().lng(-3.1865001).lat(55.94149995).build();
        Point candidate = Point.builder().lng(-3.18655).lat(55.9417).build();

        List<Point> vertices = List.of(
                Point.builder().lng(-3.1925).lat(55.9435).build(),
                Point.builder().lng(-3.1925).lat(55.9415).build(),
                Point.builder().lng(-3.1865).lat(55.9415).build(),
                Point.builder().lng(-3.1865).lat(55.9435).build(),
                Point.builder().lng(-3.1925).lat(55.9435).build()
        );

        Region region = Region.builder().name("No-fly Zone A").vertices(vertices).build();
        RegionRequest req = RegionRequest.builder().position(candidate).region(region).build();

        assertTrue(droneService.positionInRegionCheckForAStar(current, req));
    }

    @Test
    @DisplayName("Segment Intersects Corner of a No-Fly Zone")
    public void segmentIntersectsCorner() {
        Point current = Point.builder().lng(-3.1865001).lat(55.94149995).build();
        Point candidate = Point.builder().lng(-3.186394034).lat(55.94160602).build();

        List<Point> vertices = List.of(
                Point.builder().lng(-3.1925).lat(55.9435).build(),
                Point.builder().lng(-3.1925).lat(55.9415).build(),
                Point.builder().lng(-3.1865).lat(55.9415).build(),
                Point.builder().lng(-3.1865).lat(55.9435).build(),
                Point.builder().lng(-3.1925).lat(55.9435).build()
        );

        Region region = Region.builder().name("No-fly Zone A").vertices(vertices).build();
        RegionRequest req = RegionRequest.builder().position(candidate).region(region).build();

        assertTrue(droneService.positionInRegionCheckForAStar(current, req));
    }

    @Test
    @DisplayName("Valid Segment")
    public void validSegment() {
        Point current = Point.builder().lng(-3.1865001).lat(55.94149995).build();
        Point candidate = Point.builder().lng(-3.18649).lat(55.9414).build();

        List<Point> vertices = List.of(
                Point.builder().lng(-3.1925).lat(55.9435).build(),
                Point.builder().lng(-3.1925).lat(55.9415).build(),
                Point.builder().lng(-3.1865).lat(55.9415).build(),
                Point.builder().lng(-3.1865).lat(55.9435).build(),
                Point.builder().lng(-3.1925).lat(55.9435).build()
        );

        Region region = Region.builder().name("No-fly Zone A").vertices(vertices).build();
        RegionRequest req = RegionRequest.builder().position(candidate).region(region).build();

        assertFalse(droneService.positionInRegionCheckForAStar(current, req));
    }

}
