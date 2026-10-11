package lk.tideline.cleanup.service;

import lk.tideline.cleanup.dto.UserDtos.AccountReviewRequest;
import lk.tideline.cleanup.dto.UserDtos.AccountReviewResponse;
import lk.tideline.cleanup.dto.UserDtos.AdminUserResponse;
import lk.tideline.cleanup.dto.UserDtos.DocumentDownload;
import lk.tideline.cleanup.dto.UserDtos.SuspensionRequest;
import lk.tideline.cleanup.model.AccountDocument;
import lk.tideline.cleanup.model.AccountStatus;
import lk.tideline.cleanup.model.AlertType;
import lk.tideline.cleanup.model.Role;
import lk.tideline.cleanup.model.User;
import lk.tideline.cleanup.repository.AccountDocumentRepository;
import lk.tideline.cleanup.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    /** Roles an existing administrator must approve before the account can sign in. */
    private static final List<Role> VERIFIED_ROLES = List.of(Role.DIVER, Role.ORGANIZATION, Role.AUTHORITY, Role.ADMIN);

    private final UserRepository userRepository;
    private final AccountDocumentRepository documentRepository;
    private final DocumentStorageService storage;
    private final AlertService alertService;

    public UserService(UserRepository userRepository,
                       AccountDocumentRepository documentRepository,
                       DocumentStorageService storage,
                       AlertService alertService) {
        this.userRepository = userRepository;
        this.documentRepository = documentRepository;
        this.storage = storage;
        this.alertService = alertService;
    }

    private User get(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User " + id + " was not found."));
    }

    /** Module 4 — account list for administrators, optionally filtered by name or email. */
    @Transactional(readOnly = true)
    public List<AdminUserResponse> listForAdmin(String query) {
        String needle = query == null ? "" : query.trim().toLowerCase();
        return userRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(user -> needle.isEmpty()
                        || user.getFullName().toLowerCase().contains(needle)
                        || user.getEmail().toLowerCase().contains(needle))
                .map(AdminUserResponse::from)
                .toList();
    }

    /** Module 4 — suspend or reinstate an abusive account. */
    @Transactional
    public AdminUserResponse setSuspension(Long userId, SuspensionRequest request) {
        User user = get(userId);

        if (user.getRole() == Role.ADMIN) {
            throw new IllegalStateException("Administrator accounts cannot be suspended here.");
        }
        if (request.suspended() && (request.reason() == null || request.reason().isBlank())) {
            throw new IllegalArgumentException("Give a reason for the suspension so other administrators can see why.");
        }

        user.setSuspended(request.suspended());
        user.setSuspensionReason(request.suspended() ? request.reason().trim() : null);
        return AdminUserResponse.from(userRepository.save(user));
    }

    /** Divers and organisations waiting for, or already through, administrator verification. */
    @Transactional(readOnly = true)
    public List<AccountReviewResponse> verifications(AccountStatus status) {
        return userRepository.findByAccountStatusAndRoleInOrderByCreatedAtAsc(status, VERIFIED_ROLES).stream()
                .map(AccountReviewResponse::from)
                .toList();
    }

    @Transactional
    public AccountReviewResponse reviewAccount(Long userId, AccountReviewRequest request) {
        User user = get(userId);

        if (!VERIFIED_ROLES.contains(user.getRole())) {
            throw new IllegalStateException("Community members don't need verification.");
        }
        if (user.getAccountStatus() == AccountStatus.APPROVED) {
            throw new IllegalStateException("This account is already verified.");
        }

        if (request.approved()) {
            user.setAccountStatus(AccountStatus.APPROVED);
            user.setAccountReviewNote(null);
            alertService.send(user, AlertType.ACCOUNT_REVIEW,
                    "Your account has been verified",
                    switch (user.getRole()) {
                        case DIVER -> "An administrator checked your certificates. You can now join cleanups and apply for diving work.";
                        case ADMIN -> "An administrator confirmed your appointment. You now have access to the administration console.";
                        case AUTHORITY -> "An administrator confirmed your appointment. You can now review reports in the government officer console.";
                        default -> "An administrator checked your organisation. You can now post opportunities for divers.";
                    },
                    null, null, null);
        } else {
            if (request.reason() == null || request.reason().isBlank()) {
                throw new IllegalArgumentException("Give a reason. The applicant sees it when they try to sign in.");
            }
            user.setAccountStatus(AccountStatus.REJECTED);
            user.setAccountReviewNote(request.reason().trim());
            // Kept in their alerts too, so the decision is still on record if they are approved later.
            alertService.send(user, AlertType.ACCOUNT_REVIEW,
                    "Your registration wasn't approved",
                    "An administrator reviewed your " + (switch (user.getRole()) {
                        case DIVER -> "certificates";
                        case ADMIN, AUTHORITY -> "appointment";
                        default -> "organisation";
                    })
                            + " and couldn't approve it: " + request.reason().trim(),
                    null, null, null);
        }

        return AccountReviewResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public DocumentDownload document(Long documentId) {
        AccountDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("That document was not found."));
        return new DocumentDownload(document.getOriginalName(), document.getContentType(), storage.read(document));
    }
}
