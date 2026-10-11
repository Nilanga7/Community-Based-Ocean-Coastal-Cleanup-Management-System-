package lk.tideline.cleanup.service;

import lk.tideline.cleanup.dto.AnalyticsDtos.RegionCount;
import lk.tideline.cleanup.dto.AnalyticsDtos.SummaryResponse;
import lk.tideline.cleanup.model.ProjectStatus;
import lk.tideline.cleanup.model.ReportStatus;
import lk.tideline.cleanup.model.Role;
import lk.tideline.cleanup.repository.CleanupProjectRepository;
import lk.tideline.cleanup.repository.PollutionReportRepository;
import lk.tideline.cleanup.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

    private final PollutionReportRepository reportRepository;
    private final CleanupProjectRepository projectRepository;
    private final UserRepository userRepository;

    public AnalyticsService(PollutionReportRepository reportRepository,
                            CleanupProjectRepository projectRepository,
                            UserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public SummaryResponse summary() {
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (ReportStatus status : ReportStatus.values()) {
            if (status.becameProject()) {
                continue;
            }
            byStatus.put(status.name(), reportRepository.countByStatus(status));
        }

        return new SummaryResponse(
                reportRepository.countByStatusNotIn(ReportStatus.BECAME_PROJECT),
                reportRepository.countByStatus(ReportStatus.VERIFIED),
                reportRepository.countByStatus(ReportStatus.ESCALATED),
                projectRepository.countByStatus(ProjectStatus.PLANNED) + projectRepository.countByStatus(ProjectStatus.ACTIVE),
                projectRepository.countByStatus(ProjectStatus.COMPLETED),
                userRepository.countByRoleIn(List.of(Role.CITIZEN, Role.DIVER)),
                byStatus,
                toCounts(reportRepository.countGroupedByLocation()),
                toCounts(reportRepository.countGroupedByProvince()));
    }

    private List<RegionCount> toCounts(List<Object[]> rows) {
        return rows.stream()
                .filter(row -> row[0] != null)
                .map(row -> new RegionCount((String) row[0], ((Number) row[1]).longValue()))
                .toList();
    }
}
