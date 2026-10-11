package lk.tideline.cleanup;

import lk.tideline.cleanup.service.GeoUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestPropertySource(properties = "tideline.seed-demo-data=false")
class CleanupApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void distanceBetweenNegomboAndMountLaviniaIsAboutFortyKilometres() {
        double km = GeoUtils.distanceKm(7.2083, 79.8358, 6.8389, 79.8653);
        assertTrue(km > 38 && km < 46, "expected roughly 40 km but was " + km);
    }
}
