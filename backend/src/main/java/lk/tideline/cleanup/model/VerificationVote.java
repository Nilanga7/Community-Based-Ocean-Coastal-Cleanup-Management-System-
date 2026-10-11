package lk.tideline.cleanup.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "verification_votes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"report_id", "voter_id"})
)
@Getter
@Setter
public class VerificationVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_id")
    private PollutionReport report;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "voter_id")
    private User voter;

    /** true = the voter confirms the report is genuine. */
    @Column(nullable = false)
    private boolean confirmed;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}
