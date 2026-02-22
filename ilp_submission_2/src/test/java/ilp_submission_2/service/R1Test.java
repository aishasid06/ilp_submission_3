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
public class R1Test {
    private DroneServiceImpl droneService;

    @BeforeEach
    public void setup() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        OrderRepository orderRepository = mock(OrderRepository.class);
        String ilpEndPoint = "https://ilp-rest-2025-bvh6e9hschfagrgy.ukwest-01.azurewebsites.net/";
        droneService = new DroneServiceImpl(restTemplate, ilpEndPoint, orderRepository);
    }

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

    @Test
    @DisplayName("Candidate Point Lies on the Segment of a No-Fly Zone")
    public void nextPointOnSegment() {
        Point current = Point.builder().lng(-3.1865001).lat(55.94149995).build();
        Point candidate = Point.builder().lng(-3.1905).lat(55.9415).build();

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
    @DisplayName("Candidate Point Lies on the line passing through a segment but outside the No-Fly Zone")
    public void nextPointBeyondSegment() {
        Point current = Point.builder().lng(-3.1865001).lat(55.94149995).build();
        Point candidate = Point.builder().lng(-3.18645).lat(55.9415).build();

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

    @Test
    @DisplayName("Candidate Point Lies on the line passing through a segment but outside the No-Fly Zone")
    public void nextPointBeforeSegment() {
        Point current = Point.builder().lng(-3.1865001).lat(55.94149995).build();
        Point candidate = Point.builder().lng(-3.193).lat(55.9415).build();

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
