package Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity;

import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.enums.UserRole;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.enums.UserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    @EqualsAndHashCode.Include
    @ToString.Include
    private Integer userId;

    @Column(name = "first_name", nullable = false, length = 100)
    @ToString.Include
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    @ToString.Include
    private String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    @ToString.Include
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "phone", length = 30)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    @ToString.Include
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @ToString.Include
    private UserStatus status;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "address_line", length = 255)
    private String addressLine;

    @Column(name = "latitude", precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private VolunteerNonDiver volunteerNonDiver;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private VolunteerDiver volunteerDiver;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Organization organization;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private GovernmentOfficer governmentOfficer;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Admin admin;
}
