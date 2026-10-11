package lk.tideline.cleanup.service;

import lk.tideline.cleanup.config.TidelineProperties;
import lk.tideline.cleanup.model.AccountDocument;
import lk.tideline.cleanup.model.DocumentKind;
import lk.tideline.cleanup.model.User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Stores uploads on disk: diver certificates (PDF, JPEG, PNG) and report evidence (photos and
 * videos, kept in their own sub-folder because evidence is public). Files are accepted only when
 * their contents really are an allowed type, and are saved under generated names so an upload can
 * never choose its own path.
 */
@Service
public class DocumentStorageService {

    public static final long MAX_FILE_BYTES = 5L * 1024 * 1024;
    public static final int MAX_FILES = 5;
    public static final long MAX_PHOTO_BYTES = 8L * 1024 * 1024;
    public static final long MAX_VIDEO_BYTES = 25L * 1024 * 1024;
    public static final int MAX_EVIDENCE_FILES = 6;
    private static final String EVIDENCE_FOLDER = "evidence";
    private static final String INFO_FOLDER = "info";
    private static final String AVATAR_FOLDER = "avatars";
    public static final long MAX_AVATAR_BYTES = 5L * 1024 * 1024;
    private static final Pattern INFO_NAME = Pattern.compile("[0-9a-f-]{36}\\.(jpg|png|webp)");
    private static final Pattern EVIDENCE_NAME = Pattern.compile("[0-9a-f-]{36}\\.(jpg|png|webp|mp4|mov|webm)");

    private final Path root;

    public DocumentStorageService(TidelineProperties properties) {
        this.root = Paths.get(properties.getUploads().getDirectory()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root.resolve(EVIDENCE_FOLDER));
            Files.createDirectories(root.resolve(INFO_FOLDER));
            Files.createDirectories(root.resolve(AVATAR_FOLDER));
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot create the upload directory " + root, e);
        }
    }

    /** An upload that passed validation, held in memory until its account is saved. */
    public record CheckedFile(String originalName, String contentType, String extension, byte[] bytes) {
    }

    private record FileType(String contentType, String extension) {
    }

    /** Certificate files for a diver's registration. */
    public List<CheckedFile> check(List<MultipartFile> files) {
        List<CheckedFile> checked = new ArrayList<>();
        for (MultipartFile file : present(files, MAX_FILES)) {
            String name = displayName(file.getOriginalFilename());
            if (file.getSize() > MAX_FILE_BYTES) {
                throw new IllegalArgumentException(name + " is larger than 5 MB.");
            }
            byte[] bytes = bytes(file, name);
            FileType type = detect(bytes);
            if (type == null) {
                throw new IllegalArgumentException(name + " is not a PDF, JPG or PNG file.");
            }
            checked.add(new CheckedFile(name, type.contentType(), type.extension(), bytes));
        }
        return checked;
    }

    /** Photo and video evidence for a pollution report. */
    public List<CheckedFile> checkEvidence(List<MultipartFile> files) {
        List<CheckedFile> checked = new ArrayList<>();
        for (MultipartFile file : present(files, MAX_EVIDENCE_FILES)) {
            String name = displayName(file.getOriginalFilename());
            byte[] bytes = bytes(file, name);
            FileType type = detectMedia(bytes);
            if (type == null) {
                throw new IllegalArgumentException(name + " is not a supported photo or video. Use JPG, PNG, WebP, MP4, MOV or WebM.");
            }
            boolean video = type.contentType().startsWith("video/");
            if (bytes.length > (video ? MAX_VIDEO_BYTES : MAX_PHOTO_BYTES)) {
                throw new IllegalArgumentException(name + " is larger than " + (video ? "25" : "8") + " MB.");
            }
            checked.add(new CheckedFile(name, type.contentType(), type.extension(), bytes));
        }
        return checked;
    }

    /** Writes evidence and returns the generated name it is stored under. */
    public String saveEvidence(CheckedFile file) {
        String storedName = UUID.randomUUID() + file.extension();
        write(EVIDENCE_FOLDER + "/" + storedName, file.bytes());
        return storedName;
    }

    /** The file behind an evidence URL. Only generated evidence names resolve, never certificates. */
    public Path evidencePath(String storedName) {
        if (storedName == null || !EVIDENCE_NAME.matcher(storedName).matches()) {
            throw new NotFoundException("That file was not found.");
        }
        Path path = resolve(EVIDENCE_FOLDER + "/" + storedName);
        if (!Files.isRegularFile(path)) {
            throw new NotFoundException("That file was not found.");
        }
        return path;
    }

    /** Photos a reporter attaches when answering an information request. Not publicly served. */
    public List<CheckedFile> checkInfoPhotos(List<MultipartFile> files) {
        List<CheckedFile> checked = checkEvidence(files);
        for (CheckedFile file : checked) {
            if (!file.contentType().startsWith("image/")) {
                throw new IllegalArgumentException(file.originalName() + " is not a photo. Attach JPG, PNG or WebP images.");
            }
        }
        return checked;
    }

    /** A profile picture: one JPG, PNG or WebP image up to 5 MB. */
    public CheckedFile checkAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Choose a photo to upload.");
        }
        CheckedFile checked = checkInfoPhotos(List.of(file)).get(0);
        if (checked.bytes().length > MAX_AVATAR_BYTES) {
            throw new IllegalArgumentException(checked.originalName() + " is larger than 5 MB.");
        }
        return checked;
    }

    public String saveAvatar(CheckedFile file) {
        String storedName = UUID.randomUUID() + file.extension();
        write(AVATAR_FOLDER + "/" + storedName, file.bytes());
        return storedName;
    }

    public Path avatarPath(String storedName) {
        if (storedName == null || !INFO_NAME.matcher(storedName).matches()) {
            throw new NotFoundException("That file was not found.");
        }
        Path path = resolve(AVATAR_FOLDER + "/" + storedName);
        if (!Files.isRegularFile(path)) {
            throw new NotFoundException("That file was not found.");
        }
        return path;
    }

    /** Best-effort: a missing file is fine, the account no longer points at it. */
    public void deleteAvatar(String storedName) {
        if (storedName == null || !INFO_NAME.matcher(storedName).matches()) {
            return;
        }
        try {
            Files.deleteIfExists(resolve(AVATAR_FOLDER + "/" + storedName));
        } catch (IOException ignored) {
            // Leaving an orphaned file behind is harmless.
        }
    }

    public String saveInfoPhoto(CheckedFile file) {
        String storedName = UUID.randomUUID() + file.extension();
        write(INFO_FOLDER + "/" + storedName, file.bytes());
        return storedName;
    }

    public Path infoPhotoPath(String storedName) {
        if (storedName == null || !INFO_NAME.matcher(storedName).matches()) {
            throw new NotFoundException("That file was not found.");
        }
        Path path = resolve(INFO_FOLDER + "/" + storedName);
        if (!Files.isRegularFile(path)) {
            throw new NotFoundException("That file was not found.");
        }
        return path;
    }

    private static List<MultipartFile> present(List<MultipartFile> files, int max) {
        List<MultipartFile> present = files == null
                ? List.of()
                : files.stream().filter(file -> file != null && !file.isEmpty()).toList();
        if (present.size() > max) {
            throw new IllegalArgumentException("Attach up to " + max + " files.");
        }
        return present;
    }

    private static byte[] bytes(MultipartFile file, String name) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read " + name + ". Try attaching it again.");
        }
    }

    public AccountDocument save(User owner, CheckedFile file, DocumentKind kind) {
        String storedName = UUID.randomUUID() + file.extension();
        write(storedName, file.bytes());

        AccountDocument document = new AccountDocument();
        document.setUser(owner);
        document.setKind(kind);
        document.setOriginalName(file.originalName());
        document.setStoredName(storedName);
        document.setContentType(file.contentType());
        document.setSizeBytes(file.bytes().length);
        return document;
    }

    public void write(String storedName, byte[] bytes) {
        try {
            Files.write(resolve(storedName), bytes);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save an uploaded file.", e);
        }
    }

    public byte[] read(AccountDocument document) {
        try {
            return Files.readAllBytes(resolve(document.getStoredName()));
        } catch (NoSuchFileException e) {
            throw new NotFoundException("That file is no longer on the server.");
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read that file.", e);
        }
    }

    private Path resolve(String storedName) {
        Path path = root.resolve(storedName).normalize();
        if (!path.startsWith(root) || path.equals(root)) {
            throw new IllegalArgumentException("Invalid file reference.");
        }
        return path;
    }

    private static FileType detect(byte[] bytes) {
        if (startsWith(bytes, 0x25, 0x50, 0x44, 0x46)) {
            return new FileType("application/pdf", ".pdf");
        }
        if (startsWith(bytes, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)) {
            return new FileType("image/png", ".png");
        }
        if (startsWith(bytes, 0xFF, 0xD8, 0xFF)) {
            return new FileType("image/jpeg", ".jpg");
        }
        return null;
    }

    private static FileType detectMedia(byte[] bytes) {
        if (startsWith(bytes, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)) {
            return new FileType("image/png", ".png");
        }
        if (startsWith(bytes, 0xFF, 0xD8, 0xFF)) {
            return new FileType("image/jpeg", ".jpg");
        }
        if (ascii(bytes, 0, "RIFF") && ascii(bytes, 8, "WEBP")) {
            return new FileType("image/webp", ".webp");
        }
        if (startsWith(bytes, 0x1A, 0x45, 0xDF, 0xA3)) {
            return new FileType("video/webm", ".webm");
        }
        if (ascii(bytes, 4, "ftyp") && bytes.length >= 12) {
            String brand = new String(bytes, 8, 4, StandardCharsets.US_ASCII);
            if (brand.equals("qt  ")) {
                return new FileType("video/quicktime", ".mov");
            }
            // HEIC and AVIF photos use the same container, but most browsers can't display them.
            if (List.of("heic", "heix", "hevc", "mif1", "msf1", "avif").contains(brand)) {
                return null;
            }
            return new FileType("video/mp4", ".mp4");
        }
        return null;
    }

    private static boolean ascii(byte[] data, int offset, String text) {
        if (data.length < offset + text.length()) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            if (data[offset + i] != text.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    private static boolean startsWith(byte[] data, int... signature) {
        if (data.length < signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if ((data[i] & 0xFF) != signature[i]) {
                return false;
            }
        }
        return true;
    }

    private static String displayName(String original) {
        String name = StringUtils.getFilename(StringUtils.cleanPath(original == null ? "" : original));
        name = name == null ? "" : name.replaceAll("[\\p{Cntrl}\"\\\\]", "").trim();
        if (name.isEmpty()) {
            return "file";
        }
        return name.length() > 150 ? name.substring(name.length() - 150) : name;
    }
}
