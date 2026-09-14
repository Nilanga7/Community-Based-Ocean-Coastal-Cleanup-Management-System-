package Community_Based.Ocean.Coastal.Cleanup.Management.System.common.config;

import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.error.ErrorCode;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.error.ErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

/**
 * Stateless JWT security config (see CLAUDE.md "Open technical decisions" / Auth mechanism).
 * Only /auth/register and /auth/login are public for this pass; everything else requires a
 * valid Bearer token. Per-endpoint role restrictions (hasRole(UserRole...name()) or
 * @PreAuthorize, now that method security is enabled) get added as each protected endpoint lands
 * in later steps/modules — JwtAuthenticationFilter already grants a ROLE_<UserRole> authority so
 * those checks can be added without touching this class's shape.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/register", "/auth/login").permitAll()
                        // Boot's default error handling internally forwards a 404/500 to /error,
                        // and that forwarded request re-enters this same filter chain. Without
                        // this, an unauthenticated request to any unmapped path gets its real
                        // 404 masked by a 401 from the /error dispatch instead.
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(unauthorizedEntryPoint())
                        .accessDeniedHandler(forbiddenHandler())
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (request, response, authException) -> writeJsonError(
                response, 401, ErrorResponse.of(ErrorCode.UNAUTHORIZED)
        );
    }

    private AccessDeniedHandler forbiddenHandler() {
        return (request, response, accessDeniedException) -> writeJsonError(
                response, 403, ErrorResponse.of(ErrorCode.FORBIDDEN)
        );
    }

    // Built by hand rather than via a JSON library: this class runs before the DispatcherServlet,
    // so GlobalExceptionHandler's @ExceptionHandler machinery can't cover it, and jackson-databind
    // isn't on the compile classpath — jjwt's dependency on it is runtime-scoped only (see
    // backend/pom.xml). The code/message values themselves still come from the same
    // ErrorResponse/ErrorCode types GlobalExceptionHandler uses, not a second literal copy.
    private void writeJsonError(HttpServletResponse response, int status, ErrorResponse errorResponse)
            throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorResponse.ErrorDetail detail = errorResponse.error();
        response.getWriter().write(
                "{\"error\":{\"code\":\"" + detail.code() + "\",\"message\":\"" + detail.message() + "\"}}"
        );
    }
}
