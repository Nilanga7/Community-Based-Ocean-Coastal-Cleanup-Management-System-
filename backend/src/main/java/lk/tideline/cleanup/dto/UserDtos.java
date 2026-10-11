package lk.tideline.cleanup.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lk.tideline.cleanup.model.AccountDocument;
import lk.tideline.cleanup.model.AccountStatus;
import lk.tideline.cleanup.model.CertificationLevel;
import lk.tideline.cleanup.model.DocumentKind;
import lk.tideline.cleanup.model.DiverProfile;
import lk.tideline.cleanup.model.OrganizationType;
import lk.tideline.cleanup.model.Role;
import lk.tideline.cleanup.model.User;

import java.time.Instant;
import java.util.List;

public final class UserDtos {

    private UserDtos() {
    }

    /** Where a member's profile picture is served, or null when they haven't added one. */
    public static String avatarUrlOf(User user) {
        return user == null || user.getAvatarStoredName() == null ? null : "/api/users/avatars/" + user.getAvatarStoredName();
    }

    /** Admin view of an account (module 4). */
    public record AdminUserResponse(
            Long id,
            String fullName,
            String avatarUrl,
            String email,
            Role role,
            AccountStatus accountStatus,
            String province,
            String city,
            boolean suspended,
            String suspensionReason,
            Instant createdAt
    ) {
        public static AdminUserResponse from(User user) {
            return new AdminUserResponse(
                    user.getId(),
                    user.getFullName(),
                    avatarUrlOf(user),
                    user.getEmail(),
                    user.getRole(),
                    user.getAccountStatus(),
                    user.getProvince(),
                    user.getCity(),
                    user.isSuspended(),
                    user.getSuspensionReason(),
                    user.getCreatedAt());
        }
    }

    public record SuspensionRequest(
            @NotNull Boolean suspended,
            @Size(max = 500) String reason
    ) {
    }

    /** What an administrator checks before verifying a diver or organisation. */
    public record AccountReviewResponse(
            Long id,
            String fullName,
            String email,
            String phone,
            Role role,
            AccountStatus accountStatus,
            String reviewNote,
            String province,
            String city,
            CertificationLevel certificationLevel,
            Integer experienceYears,
            String organizationName,
            OrganizationType organizationType,
            String websiteUrl,
            List<DocumentResponse> documents,
            Instant createdAt
    ) {
        public static AccountReviewResponse from(User user) {
            DiverProfile profile = user.getDiverProfile();
            return new AccountReviewResponse(
                    user.getId(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getRole(),
                    user.getAccountStatus(),
                    user.getAccountReviewNote(),
                    user.getProvince(),
                    user.getCity(),
                    profile == null ? null : profile.getCertificationLevel(),
                    profile == null ? null : profile.getExperienceYears(),
                    user.getOrganizationName(),
                    user.getOrganizationType(),
                    user.getWebsiteUrl(),
                    user.getDocuments().stream().map(DocumentResponse::from).toList(),
                    user.getCreatedAt());
        }
    }

    public record DocumentResponse(Long id, String name, DocumentKind kind, String contentType, long sizeBytes, Instant uploadedAt) {
        public static DocumentResponse from(AccountDocument document) {
            return new DocumentResponse(
                    document.getId(),
                    document.getOriginalName(),
                    document.getKind() == null ? DocumentKind.CERTIFICATE : document.getKind(),
                    document.getContentType(),
                    document.getSizeBytes(),
                    document.getUploadedAt());
        }
    }

    public record AccountReviewRequest(
            @NotNull Boolean approved,
            @Size(max = 500) String reason
    ) {
    }

    public record DocumentDownload(String name, String contentType, byte[] bytes) {
    }
}
