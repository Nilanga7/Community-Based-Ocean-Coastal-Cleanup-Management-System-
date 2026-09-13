package Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
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

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "organization")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Organization {

    @Id
    @Column(name = "user_id")
    @EqualsAndHashCode.Include
    @ToString.Include
    private Integer userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "organization_name", nullable = false, length = 255)
    @ToString.Include
    private String organizationName;

    @Column(name = "organization_type", length = 100)
    private String organizationType;

    @Column(name = "business_reg_num", length = 100)
    private String businessRegNum;

    @Builder.Default
    @OneToMany(mappedBy = "organization")
    private List<DiverOpportunity> opportunities = new ArrayList<>();
}
