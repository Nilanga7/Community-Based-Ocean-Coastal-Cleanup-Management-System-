package Community_Based.Ocean.Coastal.Cleanup.Management.System.user.controller;

import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.config.RequestAuthorization;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.user.dto.VerificationDocumentResponse;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.user.service.VerificationDocumentService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Upload is self-only (path {id} must match the caller — you can only upload your own
 * verification documents). Download streams the file through this controller (never as static
 * content off the upload directory) and is restricted to the owning user, an ADMIN, or a
 * GOVERNMENT_OFFICER — see VerificationDocumentService.loadForDownload, which also confirms path
 * {id} actually matches the document's real owner.
 */
@RestController
@RequestMapping("/users/{id}/verification-documents")
public class VerificationDocumentController {

    private final VerificationDocumentService verificationDocumentService;

    public VerificationDocumentController(VerificationDocumentService verificationDocumentService) {
        this.verificationDocumentService = verificationDocumentService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public VerificationDocumentResponse upload(
            @PathVariable("id") Integer userId,
            @RequestParam String documentType,
            @RequestParam MultipartFile file,
            Authentication authentication
    ) {
        RequestAuthorization.requireSelf(userId, authentication, "You can only upload your own verification documents");
        return verificationDocumentService.upload(userId, documentType, file);
    }

    @GetMapping("/{docId}")
    public ResponseEntity<Resource> download(
            @PathVariable("id") Integer userId,
            @PathVariable Integer docId,
            Authentication authentication
    ) {
        Integer requesterId = RequestAuthorization.principalUserId(authentication);
        boolean privileged = RequestAuthorization.hasElevatedRole(authentication);

        Resource resource = verificationDocumentService.loadForDownload(docId, userId, requesterId, privileged);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
