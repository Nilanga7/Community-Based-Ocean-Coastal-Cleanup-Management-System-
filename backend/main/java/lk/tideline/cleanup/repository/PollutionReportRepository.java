package lk.tideline.cleanup.repository;

import lk.tideline.cleanup.model.PollutionReport;
import lk.tideline.cleanup.model.ReportStatus;
import lk.tideline.cleanup.model.Severity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface PollutionReportRepository extends JpaRepository<PollutionReport, Long> {

    @Query("""
            select r from PollutionReport r
            where ((:status is null and r.status not in :hidden) or r.status = :status)
              and (:severity is null or r.severity = :severity)
              and (:province is null or lower(r.province) = lower(:province))
            order by r.createdAt desc
            """)
    Page<PollutionReport> search(@Param("status") ReportStatus status,
                                 @Param("hidden") Collection<ReportStatus> hidden,
                                 @Param("severity") Severity severity,
                                 @Param("province") String province,
                                 Pageable pageable);

    @Query("""
            select r from PollutionReport r
            where r.status in :statuses
              and (:severity is null or r.severity = :severity)
              and (:province is null or lower(r.province) = lower(:province))
            order by r.createdAt desc
            """)
    Page<PollutionReport> searchIn(@Param("statuses") Collection<ReportStatus> statuses,
                                   @Param("severity") Severity severity,
                                   @Param("province") String province,
                                   Pageable pageable);

    List<PollutionReport> findByStatus(ReportStatus status);

    long countByStatus(ReportStatus status);

    long countByReporterId(Long reporterId);

    long countByStatusNotIn(Collection<ReportStatus> statuses);

    @Query("select r.province, count(r) from PollutionReport r group by r.province order by count(r) desc")
    List<Object[]> countGroupedByProvince();

    @Query("select r.locationName, count(r) from PollutionReport r group by r.locationName order by count(r) desc")
    List<Object[]> countGroupedByLocation();
}
