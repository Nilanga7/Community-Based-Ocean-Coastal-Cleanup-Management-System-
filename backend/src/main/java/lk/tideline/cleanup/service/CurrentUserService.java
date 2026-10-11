package lk.tideline.cleanup.service;

import lk.tideline.cleanup.model.User;
import lk.tideline.cleanup.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User require() {
        return find().orElseThrow(() -> new IllegalStateException("You need to sign in to do that."));
    }

    public Optional<User> find() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        return userRepository.findByEmailIgnoreCase(authentication.getName());
    }
}
