package lk.tideline.cleanup.service;

import lk.tideline.cleanup.dto.ProjectDtos.ParticipantResponse;
import lk.tideline.cleanup.dto.ProjectDtos.ProjectResponse;
import lk.tideline.cleanup.dto.ProjectDtos.ProjectUpdateRequest;
import lk.tideline.cleanup.model.*;
import lk.tideline.cleanup.repository.CleanupProjectRepository;
import lk.tideline.cleanup.repository.ProjectParticipantRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
public class ProjectService {

    private final CleanupProjectRepository projectRepository;
    private final ProjectParticipantRepository participantRepository;
    private final AlertService alertService;

    public ProjectService(CleanupProjectRepository projectRepository,
                          ProjectParticipantRepository participantRepository,
                          AlertService alertService) {
        this.projectRepository = projectRepository;
        this.participantRepository = participantRepository;
        this.alertService = alertService;
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> list(ProjectStatus status, Long reportId, User viewer) {
        List<CleanupProject> projects = reportId != null
                ? projectRepository.findByReportIdOrderByCreatedAtDesc(reportId)
                : status != null
                    ? projectRepository.findByStatusOrderByCreatedAtDesc(status)
                    : projectRepository.findAllByOrderByCreatedAtDesc();
        return projects.stream()
                .filter(project -> status == null || project.getStatus() == status)
                .map(project -> toResponse(project, viewer))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse view(Long id, User viewer) {
        return toResponse(get(id), viewer);
    }

    private CleanupProject get(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Project " + id + " was not found."));
    }

    private ProjectResponse toResponse(CleanupProject project, User viewer) {
        Boolean joined = viewer == null
                ? null
                : participantRepository.findByProjectAndUser(project, viewer).isPresent();

        // Participants' names are shown only to the people running the cleanup.
        boolean manages = viewer != null
                && (Objects.equals(project.getOwner().getId(), viewer.getId())
                    || viewer.getRole() == Role.ADMIN
                    || viewer.getRole() == Role.AUTHORITY);
        List<ParticipantResponse> participants = manages
                ? participantRepository.findByProjectOrderByJoinedAtAsc(project).stream()
                        .map(ParticipantResponse::from)
                        .toList()
                : null;

        boolean official = viewer != null && (viewer.getRole() == Role.ADMIN || viewer.getRole() == Role.AUTHORITY);
        return ProjectResponse.from(project,
                participantRepository.countByProjectAndParticipantRole(project, ParticipantRole.VOLUNTEER),
                participantRepository.countByProjectAndParticipantRole(project, ParticipantRole.DIVER),
                joined,
                participants,
                official);
    }

    @Transactional
    public ProjectResponse join(Long projectId, User user, ParticipantRole role) {
        CleanupProject project = get(projectId);

        if (project.getStatus() == ProjectStatus.COMPLETED) {
            throw new IllegalStateException("This cleanup is already complete.");
        }
        if (Objects.equals(project.getOwner().getId(), user.getId())) {
            throw new IllegalStateException("You are this project's owner, so you are already part of it.");
        }
        participantRepository.findByProjectAndUser(project, user).ifPresent(existing -> {
            throw new IllegalStateException("You have already joined this cleanup.");
        });

        ParticipantRole resolved = role != null ? role
                : (user.getRole() == Role.DIVER ? ParticipantRole.DIVER : ParticipantRole.VOLUNTEER);

        ProjectParticipant participant = new ProjectParticipant();
        participant.setProject(project);
        participant.setUser(user);
        participant.setParticipantRole(resolved);
        participantRepository.saveAndFlush(participant);

        if (project.getStatus() == ProjectStatus.PLANNED) {
            project.setStatus(ProjectStatus.ACTIVE);
            project.setStartedAt(Instant.now());
        }

        return toResponse(project, user);
    }

    /** Module 7 — progress evidence, completion percentage and recorded outcome. */
    @Transactional
    public ProjectResponse addUpdate(Long projectId, ProjectUpdateRequest request, User author) {
        CleanupProject project = get(projectId);

        // Progress is the project owner's responsibility; administrators and officers only follow it.
        if (!Objects.equals(project.getOwner().getId(), author.getId())) {
            throw new AccessDeniedException("Only the project owner can post progress updates.");
        }

        ProjectUpdate update = new ProjectUpdate();
        update.setProject(project);
        update.setAuthor(author);
        update.setStage(request.stage());
        update.setNote(request.note());
        update.setImageUrl(request.imageUrl());
        update.setCompletionPercentage(request.completionPercentage());
        project.getUpdates().add(update);

        if (request.completionPercentage() != null) {
            project.setCompletionPercentage(request.completionPercentage());
        }
        if (request.debrisRemovedKg() != null) {
            project.setDebrisRemovedKg(request.debrisRemovedKg());
        }
        if (project.getStatus() == ProjectStatus.PLANNED) {
            project.setStatus(ProjectStatus.ACTIVE);
            project.setStartedAt(Instant.now());
        }

        if (project.getCompletionPercentage() >= 100) {
            complete(project);
        }

        return toResponse(project, author);
    }

    private void complete(CleanupProject project) {
        project.setStatus(ProjectStatus.COMPLETED);
        project.setCompletionPercentage(100);
        project.setCompletedAt(Instant.now());

        PollutionReport report = project.getReport();
        if (report != null) {
            report.setStatus(ReportStatus.CLEANED);
            report.setUpdatedAt(Instant.now());
        }

        alertService.send(project.getOwner(), AlertType.PROJECT_UPDATE,
                "Your cleanup is complete",
                project.getReference() + " — " + project.getTitle() + " is marked complete"
                        + (project.getDebrisRemovedKg() != null
                                ? ", with " + project.getDebrisRemovedKg() + " kg of debris recorded." : ".")
                        + " The site you reported is now marked cleaned.",
                null, project.getId(), null);

        for (ProjectParticipant participant : project.getParticipants()) {
            DiverProfile profile = participant.getUser().getDiverProfile();
            if (profile != null) {
                profile.setCompletedProjects(profile.getCompletedProjects() + 1);
            }
            alertService.send(participant.getUser(), AlertType.PROJECT_UPDATE,
                    "Cleanup complete",
                    project.getTitle() + " is finished. Thank you for taking part.",
                    null, project.getId(), null);
        }
    }
}
