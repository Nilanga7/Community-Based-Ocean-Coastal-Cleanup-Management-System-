package lk.tideline.cleanup.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lk.tideline.cleanup.model.InfoAttachment;
import lk.tideline.cleanup.model.InfoRequest;
import lk.tideline.cleanup.model.InfoRequestStatus;

import java.time.Instant;
import java.util.List;

public final class InfoRequestDtos {

    private InfoRequestDtos() {
    }

    public record InfoResponseRequest(@NotBlank @Size(max = 2000) String description) {
    }

    public record AttachmentResponse(String url, String name, String contentType, long sizeBytes) {
        public static AttachmentResponse from(InfoAttachment attachment) {
            return new AttachmentResponse(
                    "/api/reports/info-attachments/" + attachment.getStoredName(),
                    attachment.getOriginalName(),
                    attachment.getContentType(),
                    attachment.getSizeBytes());
        }
    }

    public record InfoRequestResponse(
            Long id,
            Long reportId,
            String reportReference,
            UserDtos.UserSummary requestedBy,
            String message,
            InfoRequestStatus status,
            String responseText,
            List<AttachmentResponse> attachments,
            Instant createdAt,
            Instant respondedAt
    ) {
        public static InfoRequestResponse from(InfoRequest request) {
            return new InfoRequestResponse(
                    request.getId(),
                    request.getReport().getId(),
                    request.getReport().getReference(),
                    UserDtos.UserSummary.from(request.getRequestedBy()),
                    request.getMessage(),
                    request.getStatus(),
                    request.getResponseText(),
                    request.getAttachments().stream().map(AttachmentResponse::from).toList(),
                    request.getCreatedAt(),
                    request.getRespondedAt());
        }
    }
}
