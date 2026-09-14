package Community_Based.Ocean.Coastal.Cleanup.Management.System.user.service;

import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.User;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.VerificationDocument;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.error.BusinessValidationException;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.error.ForbiddenOperationException;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.repository.UserRepository;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.repository.VerificationDocumentRepository;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.storage.FileStorageService;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.user.dto.VerificationDocumentResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
public class VerificationDocumentService {

    private final FileStorageService fileStorageService;
    private final VerificationDocumentRepository verificationDocumentRepository;
    private final UserRepository userRepository;
    private final RegistrationVerificationService registrationVerificationService;

    public VerificationDocumentService(
            FileStorageService fileStorageService,
            VerificationDocumentRepository verificationDocumentRepository,
            UserRepository userRepository,
            RegistrationVerificationService registrationVerificationService
    ) {
        this.fileStorageService = fileStorageService;
        this.verificationDocumentRepository = verificationDocumentRepository;
        this.userRepository = userRepository;
        this.registrationVerificationService = registrationVerificationService;
    }

    /**
     * The file write happens before this method's own @Transactional boundary starts (it's
     * triggered from within, but a disk write can't participate in a JDBC transaction/rollback),
     * so a failure saving the VerificationDocument row or initiating the verification leaves an
     * orphaned file on disk rather than a dangling DB row — an accepted simplification for this
     * pass, not a full save/DB two-phase guarantee.
     */
    @Transactional
    public VerificationDocumentResponse upload(Integer userId, String documentType, MultipartFile file) {
        if (documentType == null || documentType.isBlank()) {
            throw new BusinessValidationException("documentType is required");
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessValidationException("file is required and must not be empty");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("No such user: " + userId));

        String relativePath = fileStorageService.store(userId, file);

        VerificationDocument document = VerificationDocument.builder()
                .user(user)
                .documentType(documentType)
                .fileUrl(relativePath)
                .uploadedDate(LocalDateTime.now())
                .build();
        document = verificationDocumentRepository.save(document);

        registrationVerificationService.initiate(userId);

        return toResponse(document);
    }

    /**
     * @param pathOwnerId           the {id} from GET /users/{id}/verification-documents/{docId} —
     *                              must match the document's actual owner, or this behaves as if
     *                              the document doesn't exist under that user's collection at all
     * @param requesterId           the authenticated caller's userId
     * @param requesterIsPrivileged true if the caller holds ADMIN or GOVERNMENT_OFFICER
     */
    public Resource loadForDownload(
            Integer documentId, Integer pathOwnerId, Integer requesterId, boolean requesterIsPrivileged
    ) {
        VerificationDocument document = verificationDocumentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("No such verification document: " + documentId));

        Integer actualOwnerId = document.getUser().getUserId();
        if (!actualOwnerId.equals(pathOwnerId)) {
            throw new EntityNotFoundException("No such verification document: " + documentId);
        }

        boolean isOwner = actualOwnerId.equals(requesterId);
        if (!isOwner && !requesterIsPrivileged) {
            throw new ForbiddenOperationException("You do not have permission to access this document");
        }

        return fileStorageService.load(document.getFileUrl());
    }

    private VerificationDocumentResponse toResponse(VerificationDocument document) {
        return new VerificationDocumentResponse(
                document.getDocumentId(),
                document.getDocumentType(),
                document.getFileUrl(),
                document.getUploadedDate()
        );
    }
}
