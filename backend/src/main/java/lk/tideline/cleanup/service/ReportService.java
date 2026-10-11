package lk.tideline.cleanup.service;

import lk.tideline.cleanup.config.TidelineProperties;
import lk.tideline.cleanup.dto.ReportDtos.*;
import lk.tideline.cleanup.model.*;
import lk.tideline.cleanup.repository.CleanupProjectRepository;
import lk.tideline.cleanup.repository.CommentReactionRepository;
import lk.tideline.cleanup.repository.PollutionReportRepository;
import lk.tideline.cleanup.repository.ReportCommentRepository;
import lk.tideline.cleanup.repository.UserRepository;
import lk.tideline.cleanup.repository.VerificationVoteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.UUID;

/**
 * Entities are mapped to DTOs inside the transaction — lazy associations such as the
 * reporter and the photo list cannot be read once the session has closed.
 */
@Service
public class ReportService {

    private final PollutionReportRepository reportRepository;
    private final VerificationVoteRepository voteRepository;
    private final ReportCommentRepository commentRepository;
    private final CommentReactionRepository reactionRepository;
    private final UserRepository userRepository;
    private final CleanupProjectRepository projectRepository;
    private final ProjectService projectService;
    private final AlertService alertService;
    private final TidelineProperties properties;
    private final DocumentStorageService storage;
    private final InfoRequestService infoRequests;

    public ReportService(PollutionReportRepository reportRepository,
                         VerificationVoteRepository voteRepository,
                         ReportCommentRepository commentRepository,
                         CommentReactionRepository reactionRepository,
                         UserRepository userRepository,
                         CleanupProjectRepository projectRepository,
                         ProjectService projectService,
                         AlertService alertService,
                         TidelineProperties properties,
                         DocumentStorageService storage,
                         InfoRequestService infoRequests) {
        this.reportRepository = reportRepository;
        this.voteRepository = voteRepository;
        this.commentRepository = commentRepository;
        this.reactionRepository = reactionRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.projectService = projectService;
        this.alertService = alertService;
        this.properties = properties;
        this.storage = storage;
        this.infoRequests = infoRequests;
    }

    private ReportResponse toResponse(PollutionReport report) {
        return toResponse(report, null);
    }

    /** With a viewer, includes how they voted so the page can show their choice. */
    private ReportResponse toResponse(PollutionReport report, User viewer) {
        CleanupProject project = projectRepository.findFirstByReportId(report.getId()).orElse(null);
        Boolean myVote = viewer == null ? null : voteRepository.findByReportAndVoter(report, viewer)
                .map(VerificationVote::isConfirmed)
                .orElse(null);
        // The officer's approval note is for administrators and officers only.
        boolean reviewer = viewer != null && (viewer.getRole() == Role.ADMIN || viewer.getRole() == Role.AUTHORITY);
        boolean hideNote = !reviewer && report.getAuthorityDecision() == ReviewDecision.APPROVED;
        return ReportResponse.from(report, thresholdPercent(), minimumConfirmations(), project,
                infoRequests.latestStatus(report), myVote, hideNote);
    }

    @Transactional(readOnly = true)
    public Page<ReportResponse> search(ReportStatus status, Severity severity, String province, Pageable pageable) {
        return search(status, severity, province, false, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ReportResponse> search(ReportStatus status, Severity severity, String province, boolean reviewQueue,
                                       Pageable pageable) {
        // Approved reports are projects now; they're found through the projects API instead.
        if (status != null && status.becameProject()) {
            return Page.empty(pageable);
        }
        if (reviewQueue) {
            // Reports reach the administrators only once the community has verified them.
            if (status != null && !ReportStatus.REVIEW_QUEUE.contains(status)) {
                return Page.empty(pageable);
            }
            return reportRepository.searchIn(status == null ? ReportStatus.REVIEW_QUEUE : List.of(status),
                    severity, province, pageable).map(this::toResponse);
        }
        return reportRepository.search(status, ReportStatus.BECAME_PROJECT, severity, province, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ReportResponse view(Long id) {
        return toResponse(get(id));
    }

    @Transactional(readOnly = true)
    public ReportResponse view(Long id, User viewer) {
        return toResponse(get(id), viewer);
    }

    private PollutionReport get(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Report " + id + " was not found."));
    }

    @Transactional
    public ReportResponse create(CreateReportRequest request, User reporter) {
        return create(request, List.of(), reporter);
    }

    @Transactional
    public ReportResponse create(CreateReportRequest request, List<MultipartFile> evidence, User reporter) {
        List<DocumentStorageService.CheckedFile> files = storage.checkEvidence(evidence);
        PollutionReport report = new PollutionReport();
        report.setReference("TMP-" + UUID.randomUUID());
        report.setTitle(request.title());
        report.setDescription(request.description());
        report.setSeverity(request.severity());
        report.setLocationName(request.locationName());
        report.setProvince(request.province());
        report.setLatitude(request.latitude());
        report.setLongitude(request.longitude());
        report.setReporter(reporter);
        report.setStatus(ReportStatus.PENDING);

        if (request.photoUrls() != null) {
            for (String url : request.photoUrls()) {
                ReportPhoto photo = new ReportPhoto();
                photo.setReport(report);
                photo.setUrl(url);
                report.getPhotos().add(photo);
            }
        }
        for (DocumentStorageService.CheckedFile file : files) {
            String storedName = storage.saveEvidence(file);
            ReportPhoto photo = new ReportPhoto();
            photo.setReport(report);
            photo.setStoredName(storedName);
            photo.setContentType(file.contentType());
            photo.setUrl("/api/reports/evidence/" + storedName);
            photo.setCaption(file.originalName());
            report.getPhotos().add(photo);
        }

        PollutionReport saved = reportRepository.saveAndFlush(report);
        saved.setReference("SR-" + (2400 + saved.getId()));

        double radius = properties.getAlerts().getInitialRadiusKm();
        saved.setAlertRadiusKm(radius);
        alertService.notifyNearby(saved, radius,
                AlertType.NEW_REPORT_NEARBY,
                "New pollution report near " + saved.getLocationName(),
                saved.getTitle() + " — the community can now confirm or dispute this report.");

        return toResponse(saved);
    }
}
