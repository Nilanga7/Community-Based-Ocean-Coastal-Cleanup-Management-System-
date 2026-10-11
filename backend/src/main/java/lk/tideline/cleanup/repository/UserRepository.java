package lk.tideline.cleanup.repository;

import lk.tideline.cleanup.model.AccountStatus;
import lk.tideline.cleanup.model.Role;
import lk.tideline.cleanup.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<User> findByAvailableForAlertsTrue();

    List<User> findByRole(Role role);

    List<User> findAllByOrderByCreatedAtDesc();

    List<User> findByAccountStatusAndRoleInOrderByCreatedAtAsc(AccountStatus accountStatus, List<Role> roles);

    long countByRoleIn(List<Role> roles);
}
