package lk.tideline.cleanup.repository;

import lk.tideline.cleanup.model.CommentReaction;
import lk.tideline.cleanup.model.PollutionReport;
import lk.tideline.cleanup.model.ReportComment;
import lk.tideline.cleanup.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentReactionRepository extends JpaRepository<CommentReaction, Long> {

    @Query("select r from CommentReaction r join fetch r.comment c where c.report = :report")
    List<CommentReaction> findByReport(@Param("report") PollutionReport report);

    List<CommentReaction> findByComment(ReportComment comment);

    Optional<CommentReaction> findByCommentAndUser(ReportComment comment, User user);
}
