package lk.tideline.cleanup.repository;

import lk.tideline.cleanup.model.PollutionReport;
import lk.tideline.cleanup.model.User;
import lk.tideline.cleanup.model.VerificationVote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationVoteRepository extends JpaRepository<VerificationVote, Long> {

    Optional<VerificationVote> findByReportAndVoter(PollutionReport report, User voter);

    long countByReportAndConfirmed(PollutionReport report, boolean confirmed);
}
