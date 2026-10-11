package lk.tideline.cleanup.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * An administrator or authority officer asks the reporter for more information. The reporter's
 * answer (a description and photos) is private to reviewers, attached under the original report.
 */
@Entity
@Table(name = "info_requests")
@Getter
@Setter
public class InfoRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_id")
    private PollutionReport report;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_by_id")
    private User requestedBy;

    @Column(nullable = false, length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private InfoRequestStatus status = InfoRequestStatus.OPEN;

    @Column(length = 2000)
    private String responseText;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    private Instant respondedAt;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<InfoAttachment> attachments = new ArrayList<>();
}
