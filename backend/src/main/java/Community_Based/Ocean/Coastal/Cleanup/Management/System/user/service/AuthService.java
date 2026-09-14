package Community_Based.Ocean.Coastal.Cleanup.Management.System.user.service;

import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.config.JwtService;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.Admin;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.GovernmentOfficer;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.Organization;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.User;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.VolunteerDiver;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.VolunteerNonDiver;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.enums.UserRole;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.enums.UserStatus;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.error.BusinessValidationException;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.error.ConflictException;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.error.ForbiddenOperationException;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.repository.AdminRepository;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.repository.GovernmentOfficerRepository;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.repository.OrganizationRepository;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.repository.UserRepository;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.repository.VolunteerDiverRepository;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.repository.VolunteerNonDiverRepository;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.user.dto.AdminCreatedUserRequest;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.user.dto.AuthResponse;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.user.dto.LoginRequest;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.user.dto.RegisterRequest;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.user.dto.UserProfileResponse;
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
    private final AdminRepository adminRepository;
    private final GovernmentOfficerRepository governmentOfficerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            VolunteerNonDiverRepository volunteerNonDiverRepository,
            VolunteerDiverRepository volunteerDiverRepository,
            OrganizationRepository organizationRepository,
            AdminRepository adminRepository,
            GovernmentOfficerRepository governmentOfficerRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.volunteerNonDiverRepository = volunteerNonDiverRepository;
        this.volunteerDiverRepository = volunteerDiverRepository;
        this.organizationRepository = organizationRepository;
        this.adminRepository = adminRepository;
        this.governmentOfficerRepository = governmentOfficerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (!isSelfRegisterable(request.role())) {
            throw new ForbiddenOperationException("Admin and government officer accounts cannot self-register");
        }

        validateRoleDetails(request.role(), request.roleDetails());

        User user = createUserRow(
                request.firstName(), request.lastName(), request.email(),
                request.password(), request.phone(), request.role()
        );

        createRoleSubtype(user, request.roleDetails());

        return issueAuthResponse(user);
    }

    /**
     * Admin-only provisioning of an ADMIN/GOVERNMENT_OFFICER account (POST /admin/users,
     * @PreAuthorize("hasRole('ADMIN')") on the controller). Unlike register(), this does not
     * auto-log-in the new account — it's provisioning someone else's account, not the caller's
     * own session — so it returns a UserProfileResponse, not an AuthResponse/token.
     */
    @Transactional
    public UserProfileResponse createAdminOrGovernmentOfficerAccount(AdminCreatedUserRequest request) {
        if (!isAdminCreatable(request.role())) {
            throw new ForbiddenOperationException(
                    "This endpoint only creates ADMIN or GOVERNMENT_OFFICER accounts"
            );
        }

        User user = createUserRow(
                request.firstName(), request.lastName(), request.email(),
                request.password(), request.phone(), request.role()
        );

        createAdminOrGovernmentOfficerSubtype(user, request.roleDetails());

        return toProfileResponse(user);
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

    private boolean isAdminCreatable(UserRole role) {
        return role == UserRole.ADMIN || role == UserRole.GOVERNMENT_OFFICER;
    }

    // Shared by register() and createAdminOrGovernmentOfficerAccount() — the only difference
    // between the two flows is which roles are allowed and which subtype row gets created
    // afterward; the User row itself (hash password, reject duplicate email cleanly) is identical.
    private User createUserRow(
            String firstName, String lastName, String email, String rawPassword, String phone, UserRole role
    ) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ConflictException(DUPLICATE_EMAIL_MESSAGE);
        }

        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .phone(phone)
                .role(role)
                .status(UserStatus.ACTIVE)
                .createdDate(LocalDateTime.now())
                .build();

        try {
            return userRepository.save(user);
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

    private void createAdminOrGovernmentOfficerSubtype(User user, AdminCreatedUserRequest.RoleDetails details) {
        switch (user.getRole()) {
            case ADMIN -> adminRepository.save(Admin.builder().user(user).build());
            case GOVERNMENT_OFFICER -> governmentOfficerRepository.save(
                    GovernmentOfficer.builder()
                            .user(user)
                            .department(details == null ? null : details.department())
                            .designation(details == null ? null : details.designation())
                            .build()
            );
            default -> throw new IllegalStateException(
                    "Unreachable: role was already validated as admin-creatable"
            );
        }
    }

    private AuthResponse issueAuthResponse(User user) {
        String token = jwtService.generateToken(user.getUserId(), user.getRole());
        return new AuthResponse(token, jwtService.getExpirationMs(), user.getUserId(), user.getRole());
    }

    private UserProfileResponse toProfileResponse(User user) {
        return new UserProfileResponse(
                user.getUserId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.getStatus(),
                user.getDateOfBirth(),
                user.getAddressLine(),
                user.getLatitude(),
                user.getLongitude(),
                user.getCreatedDate()
        );
    }
}
