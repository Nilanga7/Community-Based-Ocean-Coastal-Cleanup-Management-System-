package lk.tideline.cleanup.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** A photo the reporter attached when answering an information request. */
@Entity
@Table(name = "info_attachments")
@Getter
@Setter
public class InfoAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_id")
    private InfoRequest request;

    @Column(nullable = false, unique = true, length = 80)
    private String storedName;

    @Column(nullable = false, length = 150)
    private String originalName;

    @Column(nullable = false, length = 100)
    private String contentType;

    @Column(nullable = false)
    private long sizeBytes;
}
