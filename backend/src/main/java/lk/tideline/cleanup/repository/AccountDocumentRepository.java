package lk.tideline.cleanup.repository;

import lk.tideline.cleanup.model.AccountDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountDocumentRepository extends JpaRepository<AccountDocument, Long> {
}
