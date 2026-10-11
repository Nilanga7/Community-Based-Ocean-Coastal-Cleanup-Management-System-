package lk.tideline.cleanup.dto;

import java.util.List;
import java.util.Map;

public final class AnalyticsDtos {

    private AnalyticsDtos() {
    }

    public record SummaryResponse(
            long reportedSites,
            long verifiedIncidents,
            long escalatedReports,
            long activeProjects,
            long completedProjects,
            long registeredVolunteers,
            Map<String, Long> reportsByStatus,
            List<RegionCount> topLocations,
            List<RegionCount> provinceBreakdown
    ) {
    }

    public record RegionCount(String name, long count) {
    }
}
