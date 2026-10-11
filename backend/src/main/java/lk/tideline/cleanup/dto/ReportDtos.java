package lk.tideline.cleanup.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lk.tideline.cleanup.model.CleanupProject;
import lk.tideline.cleanup.model.CommentReaction;
import lk.tideline.cleanup.model.InfoRequestStatus;
import lk.tideline.cleanup.model.PollutionReport;
import lk.tideline.cleanup.model.ReactionType;
import lk.tideline.cleanup.model.ReportComment;
import lk.tideline.cleanup.model.ReportStatus;
import lk.tideline.cleanup.model.ReviewDecision;
import lk.tideline.cleanup.model.Severity;
import lk.tideline.cleanup.model.User;

import java.time.Instant;
import java.util.List;

public final class ReportDtos {

    private ReportDtos() {
    }

    public record CreateReportRequest(
            @NotBlank @Size(max = 120) String title,
            @NotBlank @Size(max = 2000) String description,
            @NotNull Severity severity,
            @NotBlank String locationName,
            String province,
            @NotNull Double latitude,
            @NotNull Double longitude,
            List<String> photoUrls
    ) {
    }

    public record ReportResponse(
            Long id,
            String reference,
            String title,
            String description,
            Severity severity,
            ReportStatus status,
            String locationName,
            String province,
            Double latitude,
            Double longitude,
            UserDtos.UserSummary reporter,
            List<String> photoUrls,
            /** The same evidence with its type, so clients can show videos as videos. */
            List<EvidenceResponse> evidence,
            int confirmVotes,
            int disputeVotes,
            int trustPercentage,
            int thresholdPercent,
            int minimumConfirmations,
            /** Status of the latest request for more information, or null if none was made. */
            InfoRequestStatus infoRequestStatus,
            ReviewDecision adminDecision,
            String moderationComment,
            Instant adminReviewedAt,
            /** Null until an administrator sends the report to the authority. */
            ReviewDecision authorityDecision,
            String authorityComment,
            UserDtos.UserSummary authorityOfficer,
            Instant decidedAt,
            /** The project this report became once the authority approved it. */
            Long projectId,
            String projectReference,
            Instant createdAt,
            Instant verifiedAt,
            Instant escalatedAt,
            /** The signed-in viewer's vote: true confirmed, false disputed, null none. */
            Boolean myVote
    ) {
        public static ReportResponse from(PollutionReport report, int thresholdPercent, int minimumConfirmations,
                                          CleanupProject project, InfoRequestStatus infoRequestStatus, Boolean myVote,
                                          boolean hideAuthorityComment) {
            return new ReportResponse(
                    report.getId(),
                    report.getReference(),
                    report.getTitle(),
                    report.getDescription(),
                    report.getSeverity(),
                    report.getStatus(),
                    report.getLocationName(),
                    report.getProvince(),
                    report.getLatitude(),
                    report.getLongitude(),
                    UserDtos.UserSummary.from(report.getReporter()),
                    report.getPhotos().stream().map(photo -> photo.getUrl()).toList(),
                    report.getPhotos().stream()
                            .map(photo -> new EvidenceResponse(photo.getUrl(), photo.getContentType()))
                            .toList(),
                    report.getConfirmVotes(),
                    report.getDisputeVotes(),
                    report.getTrustPercentage(),
                    thresholdPercent,
                    minimumConfirmations,
                    infoRequestStatus,
                    report.getAdminDecision(),
                    report.getModerationComment(),
                    report.getAdminReviewedAt(),
                    report.getAuthorityDecision(),
                    hideAuthorityComment ? null : report.getAuthorityComment(),
                    UserDtos.UserSummary.from(report.getAuthorityOfficer()),
                    report.getDecidedAt(),
                    project == null ? null : project.getId(),
                    project == null ? null : project.getReference(),
                    report.getCreatedAt(),
                    report.getVerifiedAt(),
                    report.getEscalatedAt(),
                    myVote);
        }
    }

    public record EvidenceResponse(String url, String contentType) {
    }
}
