package Community_Based.Ocean.Coastal.Cleanup.Management.System.user.service;

import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.User;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.repository.UserRepository;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.user.dto.UpdateProfileRequest;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.user.dto.UserProfileResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileService {

    private final UserRepository userRepository;

    public UserProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserProfileResponse getProfile(Integer userId) {
        User user = findUser(userId);
        return toResponse(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(Integer userId, UpdateProfileRequest request) {
        User user = findUser(userId);

        if (request.phone() != null) {
            user.setPhone(request.phone());
        }
        if (request.dateOfBirth() != null) {
            user.setDateOfBirth(request.dateOfBirth());
        }
        if (request.addressLine() != null) {
            user.setAddressLine(request.addressLine());
        }
        if (request.latitude() != null) {
            user.setLatitude(request.latitude());
        }
        if (request.longitude() != null) {
            user.setLongitude(request.longitude());
        }

        user = userRepository.save(user);
        return toResponse(user);
    }

    private User findUser(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("No such user: " + userId));
    }

    private UserProfileResponse toResponse(User user) {
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
