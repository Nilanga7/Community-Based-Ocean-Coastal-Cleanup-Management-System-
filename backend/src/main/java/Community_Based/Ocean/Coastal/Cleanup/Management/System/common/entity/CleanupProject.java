package Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity;

import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.enums.ProjectStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cleanup_project")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class CleanupProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    @EqualsAndHashCode.Include
    @ToString.Include
    private Integer projectId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_post_id", nullable = false, unique = true)
    private CleanupRequestPost requestPost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @ToString.Include
    private ProjectStatus status;

    @Column(name = "divers_needed", nullable = false)
    private Integer diversNeeded;

    @Column(name = "participants_needed", nullable = false)
    private Integer participantsNeeded;

    @Column(name = "minimum_participants", nullable = false)
    private Integer minimumParticipants;

    @Column(name = "specialists_needed", length = 255)
    private String specialistsNeeded;

    @Column(name = "equipment_required", length = 255)
    private String equipmentRequired;

    @Column(name = "current_alert_radius_km", nullable = false)
    private Integer currentAlertRadiusKm;

    @Column(name = "initiation_date")
    private LocalDate initiationDate;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "finalized_date")
    private LocalDateTime finalizedDate;

    @Builder.Default
    @OneToMany(mappedBy = "project")
    private List<ProjectAlert> alerts = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "project")
    private List<ProjectParticipant> participants = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "project")
    private List<ProjectProgressUpdate> progressUpdates = new ArrayList<>();
}
