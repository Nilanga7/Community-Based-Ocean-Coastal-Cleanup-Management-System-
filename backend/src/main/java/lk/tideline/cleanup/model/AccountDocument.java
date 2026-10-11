package lk.tideline.cleanup.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

/** A file uploaded at registration — a diving certificate, a licence, or proof of appointment.
 *  The file itself lives in the upload directory. */
@Entity
@Table(name = "account_documents")
@Getter
@Setter
public class AccountDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    /** Documents saved before this was recorded are diving certificates. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 20)
    private DocumentKind kind = DocumentKind.CERTIFICATE;

    /** The uploader's file name, for display only — never used to build a path. */
    @Column(nullable = false, length = 150)
    private String originalName;

    @Column(nullable = false, unique = true)
    private String storedName;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private long sizeBytes;

    @Column(nullable = false)
    private Instant uploadedAt = Instant.now();
}
