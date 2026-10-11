package lk.tideline.cleanup;

import lk.tideline.cleanup.dto.ProjectDtos.EquipmentLine;
import lk.tideline.cleanup.dto.ProjectDtos.ProjectResponse;
import lk.tideline.cleanup.dto.ProjectDtos.ResourcesRequest;
import lk.tideline.cleanup.dto.ReportDtos.AuthorityDecisionRequest;
import lk.tideline.cleanup.dto.ReportDtos.ReportResponse;
import lk.tideline.cleanup.model.*;
import lk.tideline.cleanup.repository.AlertRepository;
import lk.tideline.cleanup.repository.PollutionReportRepository;
import lk.tideline.cleanup.repository.UserRepository;
import lk.tideline.cleanup.service.ProjectService;
import lk.tideline.cleanup.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The officer's approval note guides administrators, who assign the resources a project needs. */
@SpringBootTest
@TestPropertySource(properties = {"tideline.seed-demo-data=false", "tideline.uploads.directory=target/test-uploads"})
class ProjectResourcesTests {

    private static final String NOTE = "Needs 20 volunteers, 3 divers for the reef edge, and a boat.";

    @Autowired
    private ReportService reportService;

    @Autowired
    private ProjectService projects;

    @Autowired
    private PollutionReportRepository reports;

    @Autowired
    private UserRepository users;

    @Autowired
    private AlertRepository alerts;

    @Test
    void theApprovalNoteGoesToAdministratorsAsAHighPriorityAlertButNotToTheOwner() {
        User admin = user(Role.ADMIN);
        User owner = user(Role.CITIZEN);
        ReportResponse decided = approve(owner);

        assertThat(alerts.findByRecipientOrderByCreatedAtDesc(admin))
                .filteredOn(alert -> alert.getType() == AlertType.RESOURCES_NEEDED)
                .singleElement()
                .satisfies(alert -> {
                    assertThat(alert.isCritical()).isTrue();
                    assertThat(alert.getBody()).contains(NOTE);
                    assertThat(alert.getProjectId()).isEqualTo(decided.projectId());
                });

        assertThat(alerts.findByRecipientOrderByCreatedAtDesc(owner))
                .extracting(Alert::getTitle)
                .contains("Your report is now a project");
        assertThat(alerts.findByRecipientOrderByCreatedAtDesc(owner))
                .noneMatch(alert -> alert.getBody().contains(NOTE));

        assertThat(reportService.view(decided.id(), owner).authorityComment()).isNull();
        assertThat(reportService.view(decided.id(), admin).authorityComment()).isEqualTo(NOTE);
        assertThat(projects.view(decided.projectId(), owner).approval()).isNull();
        assertThat(projects.view(decided.projectId(), admin).approval().comment()).isEqualTo(NOTE);
    }

    @Test
    void resourcesStayADraftUntilAnAdministratorFinalizesThem() {
        User admin = user(Role.ADMIN);
        User owner = user(Role.CITIZEN);
        Long projectId = approve(owner).projectId();
        List<EquipmentLine> kit = List.of(new EquipmentLine("Gloves", 40), new EquipmentLine("Boat", 1));

        ProjectResponse draft = projects.updateResources(projectId, new ResourcesRequest(20, 3, kit, false), admin);
        assertThat(draft.resources().finalized()).isFalse();
        assertThat(projects.view(projectId, owner).resources()).as("owners don't see drafts").isNull();

        ProjectResponse published = projects.updateResources(projectId, new ResourcesRequest(20, 3, kit, true), admin);
        assertThat(published.resources().finalized()).isTrue();

        ProjectResponse seenByOwner = projects.view(projectId, owner);
        assertThat(seenByOwner.resources().volunteersNeeded()).isEqualTo(20);
        assertThat(seenByOwner.resources().diversNeeded()).isEqualTo(3);
        assertThat(seenByOwner.resources().equipment()).extracting(EquipmentLine::name).containsExactly("Gloves", "Boat");
        assertThat(alerts.findByRecipientOrderByCreatedAtDesc(owner))
                .extracting(Alert::getTitle)
                .contains("Resources assigned to " + seenByOwner.reference());
    }

    @Test
    void onlyAdministratorsAssignResourcesAndAnEmptyPlanCannotBeFinalized() {
        User admin = user(Role.ADMIN);
        Long projectId = approve(user(Role.CITIZEN)).projectId();
        ResourcesRequest plan = new ResourcesRequest(5, 0, List.of(), true);

        assertThatThrownBy(() -> projects.updateResources(projectId, plan, user(Role.AUTHORITY)))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> projects.updateResources(projectId, new ResourcesRequest(0, 0, List.of(), true), admin))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private ReportResponse approve(User reporter) {
        PollutionReport report = new PollutionReport();
        report.setReference("T-" + UUID.randomUUID());
        report.setTitle("Ghost nets");
        report.setDescription("Nets on the reef.");
        report.setSeverity(Severity.HIGH);
        report.setStatus(ReportStatus.ESCALATED);
        report.setAdminDecision(ReviewDecision.APPROVED);
        report.setAuthorityDecision(ReviewDecision.PENDING);
        report.setLocationName("Kalpitiya");
        report.setProvince("North Western Province");
        report.setLatitude(8.23);
        report.setLongitude(79.76);
        report.setReporter(reporter);
        reports.save(report);
        return reportService.decideAsAuthority(report.getId(),
                new AuthorityDecisionRequest(ReviewDecision.APPROVED, NOTE), user(Role.AUTHORITY));
    }

    private User user(Role role) {
        User user = new User();
        user.setFullName(role.name() + " " + UUID.randomUUID().toString().substring(0, 6));
        user.setEmail(UUID.randomUUID() + "@test.lk");
        user.setPasswordHash("x");
        user.setRole(role);
        return users.save(user);
    }
}
