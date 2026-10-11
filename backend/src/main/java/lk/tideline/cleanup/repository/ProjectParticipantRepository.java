package lk.tideline.cleanup.repository;

import lk.tideline.cleanup.model.CleanupProject;
import lk.tideline.cleanup.model.ParticipantRole;
import lk.tideline.cleanup.model.ProjectParticipant;
import lk.tideline.cleanup.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectParticipantRepository extends JpaRepository<ProjectParticipant, Long> {

    Optional<ProjectParticipant> findByProjectAndUser(CleanupProject project, User user);

    List<ProjectParticipant> findByUser(User user);

    List<ProjectParticipant> findByProjectOrderByJoinedAtAsc(CleanupProject project);

    long countByProjectAndParticipantRole(CleanupProject project, ParticipantRole participantRole);
}
