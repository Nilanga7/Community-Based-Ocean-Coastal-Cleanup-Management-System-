package Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity;

import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.enums.CommunityResult;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.enums.CurrentStage;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.enums.ReviewStatus;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cleanup_request_post")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class CleanupRequestPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_post_id")
    @EqualsAndHashCode.Include
    @ToString.Include
    private Integer requestPostId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by_user_id", nullable = false)
    private User submittedBy;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "incident_date_time", nullable = false)
    private LocalDateTime incidentDateTime;

    @Column(name = "latitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal longitude;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id")
    private District district;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_stage", nullable = false, length = 20)
    @ToString.Include
    private CurrentStage currentStage;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage_status", nullable = false, length = 30)
    @ToString.Include
    private ReviewStatus stageStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "community_result", nullable = false, length = 20)
    @ToString.Include
    private CommunityResult communityResult;

    @Column(name = "trust_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal trustPercentage;

    @Column(name = "submitted_date", nullable = false)
    private LocalDateTime submittedDate;

    @Column(name = "verification_deadline", nullable = false)
    private LocalDateTime verificationDeadline;

    @Builder.Default
    @OneToMany(mappedBy = "requestPost")
    private List<RequestPostMedia> media = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "requestPost")
    private List<ReportVote> votes = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "requestPost")
    private List<ReportComment> comments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "requestPost")
    private List<ReportReviewAction> reviewActions = new ArrayList<>();

    @OneToOne(mappedBy = "requestPost", fetch = FetchType.LAZY)
    private CleanupProject project;
}
