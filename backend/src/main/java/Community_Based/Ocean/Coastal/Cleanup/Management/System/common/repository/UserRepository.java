package Community_Based.Ocean.Coastal.Cleanup.Management.System.common.repository;

import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.User;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    // Used by Step 7c's bootstrap check: skip creating the first admin if one already exists.
    boolean existsByRole(UserRole role);
}
