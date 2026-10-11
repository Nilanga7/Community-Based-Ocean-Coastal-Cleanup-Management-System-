package lk.tideline.cleanup.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "report_comments")
@Getter
@Setter
public class ReportComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_id")
    private PollutionReport report;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id")
    private User author;

    /** The comment this one replies to. Replies are one level deep: a reply to a reply joins the same thread. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ReportComment parent;

    @Column(nullable = false, length = 1000)
    private String body;

    /** Comments from an admin or authority officer are shown as official. */
    @Column(nullable = false)
    private boolean official = false;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}
