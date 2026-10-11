package lk.tideline.cleanup.config;

import lk.tideline.cleanup.repository.AlertRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/** Moves "new registration to verify" notices saved under the old alert type onto their own type. */
@Configuration
public class AlertTypeMigration {

    @Bean
    @Order(0)
    public ApplicationRunner retypeApplicationNotices(AlertRepository alerts) {
        return args -> alerts.retypeApplicationNotices();
    }
}
