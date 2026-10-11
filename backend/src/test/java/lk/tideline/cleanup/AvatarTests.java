package lk.tideline.cleanup;

import lk.tideline.cleanup.dto.UserDtos.UserResponse;
import lk.tideline.cleanup.model.Role;
import lk.tideline.cleanup.model.User;
import lk.tideline.cleanup.repository.UserRepository;
import lk.tideline.cleanup.service.DocumentStorageService;
import lk.tideline.cleanup.service.NotFoundException;
import lk.tideline.cleanup.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Adding, replacing and removing a profile picture. */
@SpringBootTest
@TestPropertySource(properties = {"tideline.seed-demo-data=false", "tideline.uploads.directory=target/test-uploads"})
class AvatarTests {

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};
    private static final byte[] JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0};

    @Autowired
    private UserService userService;

    @Autowired
    private DocumentStorageService storage;

    @Autowired
    private UserRepository users;

    @Test
    void aMemberCanAddReplaceAndRemoveTheirProfilePicture() {
        User member = user();

        UserResponse added = userService.updateAvatar(member.getId(), photo("me.png", PNG));
        assertThat(added.avatarUrl()).startsWith("/api/users/avatars/").endsWith(".png");
        Path first = storage.avatarPath(stored(added));

        UserResponse replaced = userService.updateAvatar(member.getId(), photo("new.jpg", JPG));
        assertThat(replaced.avatarUrl()).endsWith(".jpg");
        assertThat(Files.exists(first)).as("the old picture is deleted").isFalse();
        assertThat(userService.publicProfile(member.getId(), member).avatarUrl()).isEqualTo(replaced.avatarUrl());

        String current = stored(replaced);
        UserResponse removed = userService.removeAvatar(member.getId());
        assertThat(removed.avatarUrl()).isNull();
        assertThatThrownBy(() -> storage.avatarPath(current)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void onlyImagesCanBeAProfilePicture() {
        User member = user();
        assertThatThrownBy(() -> userService.updateAvatar(member.getId(),
                photo("me.png", "<svg onload=alert(1)>".getBytes(StandardCharsets.UTF_8))))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(users.findById(member.getId()).orElseThrow().getAvatarStoredName()).isNull();
    }

    private static String stored(UserResponse response) {
        return response.avatarUrl().substring("/api/users/avatars/".length());
    }

    private static MockMultipartFile photo(String name, byte[] bytes) {
        return new MockMultipartFile("photo", name, "image/png", bytes);
    }

    private User user() {
        User user = new User();
        user.setFullName("Avatar tester");
        user.setEmail(UUID.randomUUID() + "@test.lk");
        user.setPasswordHash("x");
        user.setRole(Role.CITIZEN);
        return users.save(user);
    }
}
