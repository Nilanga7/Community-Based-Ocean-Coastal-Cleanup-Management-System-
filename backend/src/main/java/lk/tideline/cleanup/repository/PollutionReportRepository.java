package lk.tideline.cleanup.repository;

import lk.tideline.cleanup.model.PollutionReport;
import lk.tideline.cleanup.model.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface PollutionReportRepository extends JpaRepository<PollutionReport, Long> {

    long countByStatus(ReportStatus status);

    long countByReporterId(Long reporterId);

    long countByStatusNotIn(Collection<ReportStatus> statuses);

    @Query("select r.province, count(r) from PollutionReport r group by r.province order by count(r) desc")
    List<Object[]> countGroupedByProvince();

    @Query("select r.locationName, count(r) from PollutionReport r group by r.locationName order by count(r) desc")
    List<Object[]> countGroupedByLocation();
}
