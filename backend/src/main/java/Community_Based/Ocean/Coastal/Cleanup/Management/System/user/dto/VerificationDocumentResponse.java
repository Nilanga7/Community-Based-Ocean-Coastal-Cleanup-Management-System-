package Community_Based.Ocean.Coastal.Cleanup.Management.System.user.dto;

import java.time.LocalDateTime;

public record VerificationDocumentResponse(
        Integer documentId,
        String documentType,
        String fileUrl,
        LocalDateTime uploadedDate
) {
}
