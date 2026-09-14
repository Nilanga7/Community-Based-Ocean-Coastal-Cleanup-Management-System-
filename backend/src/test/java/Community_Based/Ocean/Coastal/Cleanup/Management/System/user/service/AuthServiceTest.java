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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.SQLException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private VolunteerNonDiverRepository volunteerNonDiverRepository;
    @Mock
    private VolunteerDiverRepository volunteerDiverRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    // Concrete class with a 2-arg constructor — mock() bypasses the constructor via Mockito's
    // inline mock maker, so this doesn't need @Mock's usual interface/no-arg assumptions.
    private final JwtService jwtService = mock(JwtService.class);

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository, volunteerNonDiverRepository, volunteerDiverRepository,
                organizationRepository, passwordEncoder, jwtService
        );
    }

    private RegisterRequest registerRequest(UserRole role, RegisterRequest.RoleDetails details) {
        return new RegisterRequest(
                "Ada", "Lovelace", "ada@example.com", "supersecret", "0771234567", role, details
        );
    }

    private void stubSuccessfulSave() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setUserId(1);
            return user;
        });
        when(jwtService.generateToken(eq(1), any(UserRole.class))).thenReturn("fake-jwt");
        when(jwtService.getExpirationMs()).thenReturn(86_400_000L);
    }

    @Test
    void register_volunteerNonDiver_createsUserAndSubtypeRow() {
        stubSuccessfulSave();
        RegisterRequest.RoleDetails details =
                new RegisterRequest.RoleDetails("weekends", "beach cleanup", null, null, null, null, null, null);

        AuthResponse response = authService.register(registerRequest(UserRole.VOLUNTEER_NON_DIVER, details));

        assertThat(response.token()).isEqualTo("fake-jwt");
        assertThat(response.userId()).isEqualTo(1);
        assertThat(response.role()).isEqualTo(UserRole.VOLUNTEER_NON_DIVER);

        ArgumentCaptor<VolunteerNonDiver> captor = ArgumentCaptor.forClass(VolunteerNonDiver.class);
        verify(volunteerNonDiverRepository).save(captor.capture());
        assertThat(captor.getValue().getAvailability()).isEqualTo("weekends");
        assertThat(captor.getValue().getSpecialization()).isEqualTo("beach cleanup");
        assertThat(captor.getValue().getUser().getUserId()).isEqualTo(1);

        verify(volunteerDiverRepository, never()).save(any());
        verify(organizationRepository, never()).save(any());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("hashed-password");
        assertThat(userCaptor.getValue().getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void register_volunteerDiver_createsUserAndSubtypeRow() {
        stubSuccessfulSave();
        RegisterRequest.RoleDetails details =
                new RegisterRequest.RoleDetails(null, null, "5 years", "PADI", "Southern Province", null, null, null);

        authService.register(registerRequest(UserRole.VOLUNTEER_DIVER, details));

        ArgumentCaptor<VolunteerDiver> captor = ArgumentCaptor.forClass(VolunteerDiver.class);
        verify(volunteerDiverRepository).save(captor.capture());
        assertThat(captor.getValue().getExperience()).isEqualTo("5 years");
        assertThat(captor.getValue().getCertification()).isEqualTo("PADI");
        assertThat(captor.getValue().getPreferredRegion()).isEqualTo("Southern Province");
        assertThat(captor.getValue().getUser().getUserId()).isEqualTo(1);

        verify(volunteerNonDiverRepository, never()).save(any());
        verify(organizationRepository, never()).save(any());
    }

    @Test
    void register_organization_createsUserAndSubtypeRow() {
        stubSuccessfulSave();
        RegisterRequest.RoleDetails details =
                new RegisterRequest.RoleDetails(null, null, null, null, null, "Ocean Cleanup Org", "NGO", "REG-123");

        authService.register(registerRequest(UserRole.ORGANIZATION, details));

        ArgumentCaptor<Organization> captor = ArgumentCaptor.forClass(Organization.class);
        verify(organizationRepository).save(captor.capture());
        assertThat(captor.getValue().getOrganizationName()).isEqualTo("Ocean Cleanup Org");
        assertThat(captor.getValue().getOrganizationType()).isEqualTo("NGO");
        assertThat(captor.getValue().getBusinessRegNum()).isEqualTo("REG-123");
        assertThat(captor.getValue().getUser().getUserId()).isEqualTo(1);

        verify(volunteerNonDiverRepository, never()).save(any());
        verify(volunteerDiverRepository, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class, names = {"ADMIN", "GOVERNMENT_OFFICER"})
    void register_adminOrGovernmentOfficerRole_isRejected(UserRole role) {
        RegisterRequest request = registerRequest(role, null);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("Admin and government officer accounts cannot self-register");

        verify(userRepository, never()).save(any());
        verify(volunteerNonDiverRepository, never()).save(any());
        verify(volunteerDiverRepository, never()).save(any());
        verify(organizationRepository, never()).save(any());
    }

    @Test
    void register_duplicateEmail_isRejectedCleanly() {
        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(User.builder().userId(99).email("ada@example.com").build()));

        RegisterRequest request = registerRequest(UserRole.VOLUNTEER_NON_DIVER, null);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("An account with this email already exists.");

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_organizationWithoutOrganizationName_isRejectedAsValidationErrorBeforeAnySave() {
        RegisterRequest.RoleDetails detailsMissingName =
                new RegisterRequest.RoleDetails(null, null, null, null, null, null, "NGO", "REG-123");

        RegisterRequest request = registerRequest(UserRole.ORGANIZATION, detailsMissingName);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("organizationName is required when role is ORGANIZATION");

        // Must fail before touching the database at all — not after the user row is already in.
        verify(userRepository, never()).save(any());
        verify(organizationRepository, never()).save(any());
    }

    @Test
    void register_organizationWithNullRoleDetails_isRejectedAsValidationError() {
        RegisterRequest request = registerRequest(UserRole.ORGANIZATION, null);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessValidationException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_uniqueEmailConstraintViolationOnSave_isClassifiedAsDuplicateEmail() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-password");
        ConstraintViolationException emailConstraint = new ConstraintViolationException(
                "could not execute statement", new SQLException("Duplicate entry"), "uk_users_email"
        );
        when(userRepository.save(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("insert failed", emailConstraint));

        RegisterRequest request = registerRequest(UserRole.VOLUNTEER_NON_DIVER, null);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("An account with this email already exists.");
    }

    @Test
    void register_unrelatedConstraintViolationOnSave_isNotMisclassifiedAsDuplicateEmail() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-password");
        ConstraintViolationException unrelatedConstraint = new ConstraintViolationException(
                "could not execute statement", new SQLException("Column 'first_name' cannot be null"),
                "some_other_constraint"
        );
        DataIntegrityViolationException original =
                new DataIntegrityViolationException("insert failed", unrelatedConstraint);
        when(userRepository.save(any(User.class))).thenThrow(original);

        RegisterRequest request = registerRequest(UserRole.VOLUNTEER_NON_DIVER, null);

        // Must propagate as the original exception, not be reinterpreted as a duplicate-email
        // conflict just because it happened to fail on the same save() call.
        assertThatThrownBy(() -> authService.register(request))
                .isNotInstanceOf(ConflictException.class)
                .isSameAs(original);
    }

    @Test
    void login_correctCredentials_returnsValidToken() {
        User user = User.builder()
                .userId(1)
                .email("ada@example.com")
                .password("hashed-password")
                .role(UserRole.VOLUNTEER_NON_DIVER)
                .status(UserStatus.ACTIVE)
                .build();
        when(userRepository.findByEmail("ada@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("supersecret", "hashed-password")).thenReturn(true);
        when(jwtService.generateToken(1, UserRole.VOLUNTEER_NON_DIVER)).thenReturn("fake-jwt");
        when(jwtService.getExpirationMs()).thenReturn(86_400_000L);

        AuthResponse response = authService.login(new LoginRequest("ada@example.com", "supersecret"));

        assertThat(response.token()).isEqualTo("fake-jwt");
        assertThat(response.userId()).isEqualTo(1);
        assertThat(response.role()).isEqualTo(UserRole.VOLUNTEER_NON_DIVER);
    }

    @Test
    void login_wrongPassword_andLogin_nonExistentEmail_failIdentically() {
        User user = User.builder()
                .userId(1)
                .email("ada@example.com")
                .password("hashed-password")
                .role(UserRole.VOLUNTEER_NON_DIVER)
                .build();
        when(userRepository.findByEmail("ada@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        Throwable wrongPasswordFailure = catchThrowable(() ->
                authService.login(new LoginRequest("ada@example.com", "wrong-password")));
        Throwable noSuchEmailFailure = catchThrowable(() ->
                authService.login(new LoginRequest("nobody@example.com", "irrelevant")));

        assertThat(wrongPasswordFailure).isInstanceOf(BadCredentialsException.class);
        assertThat(noSuchEmailFailure).isInstanceOf(BadCredentialsException.class);
        assertThat(wrongPasswordFailure.getMessage()).isEqualTo(noSuchEmailFailure.getMessage());
    }
}
