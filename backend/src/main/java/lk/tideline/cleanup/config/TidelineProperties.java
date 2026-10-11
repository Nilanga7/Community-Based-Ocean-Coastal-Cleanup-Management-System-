package lk.tideline.cleanup.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "tideline")
@Getter
@Setter
public class TidelineProperties {

    private Verification verification = new Verification();
    private Alerts alerts = new Alerts();
    private Security security = new Security();
    private Cors cors = new Cors();
    private Uploads uploads = new Uploads();
    private boolean seedDemoData = false;
    /** Where the web app runs, for links in emails (password reset). */
    private String frontendUrl = "http://localhost:5173";
    private Mail mail = new Mail();

    @Getter
    @Setter
    public static class Mail {
        private String from = "Tideline <no-reply@tideline.lk>";
    }

    @Getter
    @Setter
    public static class Uploads {
        private String directory = "uploads";
    }

    @Getter
    @Setter
    public static class Verification {
        private int thresholdPercent = 75;
        /** Confirmations needed before a report can pass, on top of the trust percentage. */
        private int minimumConfirmations = 5;
    }

    @Getter
    @Setter
    public static class Alerts {
        private double initialRadiusKm = 5;
    }

    @Getter
    @Setter
    public static class Security {
        private String jwtSecret;
        private long jwtExpiryMinutes = 720;
        /** Session length when "Remember me" is ticked. */
        private long rememberMeDays = 30;
    }

    @Getter
    @Setter
    public static class Cors {
        private List<String> allowedOrigins = List.of("http://localhost:5173");
    }
}
