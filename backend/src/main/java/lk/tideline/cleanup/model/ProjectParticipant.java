package lk.tideline.cleanup.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(
        name = "project_participants",
        uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "user_id"})
)
@Getter
@Setter
public class ProjectParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id")
    private CleanupProject project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false)
    private ParticipantRole participantRole = ParticipantRole.VOLUNTEER;

    /** Mark given by the project owner once the cleanup is done (0-5). */
    private Integer contributionMark;

    @Column(nullable = false)
    private Instant joinedAt = Instant.now();
}
