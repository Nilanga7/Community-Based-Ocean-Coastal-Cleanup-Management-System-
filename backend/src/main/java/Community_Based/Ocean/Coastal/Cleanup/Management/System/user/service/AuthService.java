package Community_Based.Ocean.Coastal.Cleanup.Management.System.user.service;

import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.config.JwtService;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.Organization;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.User;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.VolunteerDiver;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.VolunteerNonDiver;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.enums.UserRole;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.enums.UserStatus;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.error.BusinessValidationException;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.error.ConflictException;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.error.ForbiddenOperationException;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.repository.OrganizationRepository;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.repository.UserRepository;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.repository.VolunteerDiverRepository;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.repository.VolunteerNonDiverRepository;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.user.dto.AuthResponse;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.user.dto.LoginRequest;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.user.dto.RegisterRequest;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Public registration/login for the three self-registerable roles (see RegisterRequest's
 * Javadoc) — ADMIN/GOVERNMENT_OFFICER account creation is a separate, admin-only flow.
 */
@Service
public class AuthService {

    // Deliberately identical for "no such email" and "wrong password" — see login() below.
    private static final String INVALID_CREDENTIALS_MESSAGE = "Invalid email or password";
    private static final String DUPLICATE_EMAIL_MESSAGE = "An account with this email already exists.";
    // Matches CONSTRAINT uk_users_email UNIQUE (email) in V1__init_schema.sql — used to confirm a
    // DataIntegrityViolationException on the users insert is actually the email conflict before
    // classifying it as one (see isDuplicateEmailConstraintViolation).
    private static final String EMAIL_UNIQUE_CONSTRAINT_NAME = "uk_users_email";

    private final UserRepository userRepository;
    private final VolunteerNonDiverRepository volunteerNonDiverRepository;
    private final VolunteerDiverRepository volunteerDiverRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            VolunteerNonDiverRepository volunteerNonDiverRepository,
            VolunteerDiverRepository volunteerDiverRepository,
            OrganizationRepository organizationRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.volunteerNonDiverRepository = volunteerNonDiverRepository;
        this.volunteerDiverRepository = volunteerDiverRepository;
        this.organizationRepository = organizationRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (!isSelfRegisterable(request.role())) {
            throw new ForbiddenOperationException("Admin and government officer accounts cannot self-register");
        }

        validateRoleDetails(request.role(), request.roleDetails());

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ConflictException(DUPLICATE_EMAIL_MESSAGE);
        }

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .phone(request.phone())
                .role(request.role())
                .status(UserStatus.ACTIVE)
                .createdDate(LocalDateTime.now())
                .build();

        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            // Backstop for a concurrent registration racing the findByEmail check above. Only
            // reclassify as "duplicate email" if the violated constraint is actually
            // uk_users_email — any other constraint violation here is unexpected (validation
            // above should have already ruled out the known causes) and must not be mislabeled;
            // rethrow it as-is so it surfaces as a genuine 500, not a misleading conflict.
            if (isDuplicateEmailConstraintViolation(e)) {
                throw new ConflictException(DUPLICATE_EMAIL_MESSAGE);
            }
            throw e;
        }

        createRoleSubtype(user, request.roleDetails());

        return issueAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException(INVALID_CREDENTIALS_MESSAGE));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException(INVALID_CREDENTIALS_MESSAGE);
        }

        return issueAuthResponse(user);
    }

    private boolean isSelfRegisterable(UserRole role) {
        return role == UserRole.VOLUNTEER_NON_DIVER
                || role == UserRole.VOLUNTEER_DIVER
                || role == UserRole.ORGANIZATION;
    }

    // organization.organization_name is NOT NULL in V1__init_schema.sql, but that's only
    // conditional on role=ORGANIZATION, so Bean Validation on RegisterRequest itself can't express
    // it — checked explicitly here, before any row is written, rather than letting it surface as a
    // DB constraint violation on the (separate) organization insert after the user row already
    // exists.
    private void validateRoleDetails(UserRole role, RegisterRequest.RoleDetails details) {
        if (role == UserRole.ORGANIZATION) {
            String organizationName = details == null ? null : details.organizationName();
            if (organizationName == null || organizationName.isBlank()) {
                throw new BusinessValidationException("organizationName is required when role is ORGANIZATION");
            }
        }
    }

    private boolean isDuplicateEmailConstraintViolation(DataIntegrityViolationException e) {
        // Walk the whole chain rather than using getMostSpecificCause(): a
        // ConstraintViolationException's own SQLException is often set as its cause too, in which
        // case the "most specific" cause is that raw SQLException, one level past the
        // ConstraintViolationException that actually carries the constraint name.
        for (Throwable cause = e; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException cve) {
                String constraintName = cve.getConstraintName();
                if (constraintName != null
                        && constraintName.toLowerCase(Locale.ROOT).contains(EMAIL_UNIQUE_CONSTRAINT_NAME)) {
                    return true;
                }
            }
        }

        // Fallback for drivers/dialects that don't populate getConstraintName(): MySQL's own
        // duplicate-key message names the constraint too, e.g.
        // "Duplicate entry 'x@example.com' for key 'users.uk_users_email'".
        String message = e.getMostSpecificCause().getMessage();
        return message != null && message.toLowerCase(Locale.ROOT).contains(EMAIL_UNIQUE_CONSTRAINT_NAME);
    }

    private void createRoleSubtype(User user, RegisterRequest.RoleDetails details) {
        switch (user.getRole()) {
            case VOLUNTEER_NON_DIVER -> volunteerNonDiverRepository.save(
                    VolunteerNonDiver.builder()
                            .user(user)
                            .availability(details == null ? null : details.availability())
                            .specialization(details == null ? null : details.specialization())
                            .build()
            );
            case VOLUNTEER_DIVER -> volunteerDiverRepository.save(
                    VolunteerDiver.builder()
                            .user(user)
                            .experience(details == null ? null : details.experience())
                            .certification(details == null ? null : details.certification())
                            .preferredRegion(details == null ? null : details.preferredRegion())
                            .build()
            );
            case ORGANIZATION -> organizationRepository.save(
                    Organization.builder()
                            .user(user)
                            .organizationName(details == null ? null : details.organizationName())
                            .organizationType(details == null ? null : details.organizationType())
                            .businessRegNum(details == null ? null : details.businessRegNum())
                            .build()
            );
            default -> throw new IllegalStateException(
                    "Unreachable: role was already validated as self-registerable"
            );
        }
    }

    private AuthResponse issueAuthResponse(User user) {
        String token = jwtService.generateToken(user.getUserId(), user.getRole());
        return new AuthResponse(token, jwtService.getExpirationMs(), user.getUserId(), user.getRole());
    }
}
