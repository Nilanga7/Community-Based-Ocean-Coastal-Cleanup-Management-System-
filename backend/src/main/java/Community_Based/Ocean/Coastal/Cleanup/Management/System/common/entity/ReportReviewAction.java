package Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity;

import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.enums.ReviewDecision;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.enums.ReviewStage;
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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "report_review_action")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class ReportReviewAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "action_id")
    @EqualsAndHashCode.Include
    @ToString.Include
    private Integer actionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_post_id", nullable = false)
    private CleanupRequestPost requestPost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_stage", nullable = false, length = 20)
    @ToString.Include
    private ReviewStage reviewStage;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false, length = 20)
    @ToString.Include
    private ReviewDecision decision;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "action_date", nullable = false)
    private LocalDateTime actionDate;
}
