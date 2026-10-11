package lk.tideline.cleanup.controller;

import jakarta.validation.Valid;
import lk.tideline.cleanup.dto.UserDtos.UpdateDiverProfileRequest;
import lk.tideline.cleanup.dto.UserDtos.UpdateProfileRequest;
import lk.tideline.cleanup.dto.UserDtos.PublicProfileResponse;
import lk.tideline.cleanup.dto.UserDtos.UserResponse;
import lk.tideline.cleanup.service.CurrentUserService;
import lk.tideline.cleanup.service.DocumentStorageService;
import lk.tideline.cleanup.service.UserService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final CurrentUserService currentUser;

    private final DocumentStorageService storage;

    public UserController(UserService userService, CurrentUserService currentUser, DocumentStorageService storage) {
        this.userService = userService;
        this.currentUser = currentUser;
        this.storage = storage;
    }

    /** Add or replace your profile picture. */
    @PutMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserResponse updateAvatar(@RequestPart("photo") MultipartFile photo) {
        return userService.updateAvatar(currentUser.require().getId(), photo);
    }

    @DeleteMapping("/me/avatar")
    public UserResponse removeAvatar() {
        return userService.removeAvatar(currentUser.require().getId());
    }

    /** Profile pictures are shown wherever a member's name appears, so they're public. */
    @GetMapping("/avatars/{fileName}")
    public ResponseEntity<Resource> avatar(@PathVariable String fileName) {
        return ResponseEntity.ok()
                .contentType(MediaTypeFactory.getMediaType(fileName).orElse(MediaType.APPLICATION_OCTET_STREAM))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
                .header("X-Content-Type-Options", "nosniff")
                .body(new FileSystemResource(storage.avatarPath(fileName)));
    }

    @GetMapping("/me")
    public UserResponse me() {
        return userService.view(currentUser.require().getId());
    }

    @PutMapping("/me")
    public UserResponse updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(currentUser.require().getId(), request);
    }

    @PutMapping("/me/diver-profile")
    public UserResponse updateDiverProfile(@Valid @RequestBody UpdateDiverProfileRequest request) {
        return userService.updateDiverProfile(currentUser.require().getId(), request);
    }

    /** Another member's profile: no email, phone or location. */
    @GetMapping("/{id}")
    public PublicProfileResponse get(@PathVariable Long id) {
        return userService.publicProfile(id, currentUser.require());
    }
}
