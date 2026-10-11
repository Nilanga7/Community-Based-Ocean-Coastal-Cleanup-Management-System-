package lk.tideline.cleanup.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "diver_profiles")
@Getter
@Setter
public class DiverProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private CertificationLevel certificationLevel;

    private Integer experienceYears;

    @Column(length = 500)
    private String equipment;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "diver_preferred_regions", joinColumns = @JoinColumn(name = "diver_profile_id"))
    @Column(name = "region")
    private List<String> preferredRegions = new ArrayList<>();

    /** Cleanup projects completed, used as the diver's portfolio signal. */
    @Column(nullable = false)
    private int completedProjects = 0;
}
