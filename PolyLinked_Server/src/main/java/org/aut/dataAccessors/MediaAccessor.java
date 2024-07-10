package org.aut.dataAccessors;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

public class MediaAccessor {
    public static final String VIDEO_EXTENSIONS = "mp4, m4v, flv";
    public static final String AUDIO_EXTENSIONS = "mp3, aac, wav, aiff, m4a";
    public static final String IMAGE_EXTENSIONS = "jpg, jpeg, png, gif, bmp";

    private MediaAccessor() {
    }

    static void createDirectories() throws IOException {
        for (MediaPath path : MediaPath.values())
            if (!Files.isDirectory(path.value)) Files.createDirectories(path.value);
    }

    public static File getMedia(String fileId, MediaPath mediaPath) {
        try (Stream<Path> paths = Files.list(mediaPath.value)) {
            for (Path path : paths.toList()) {
                if (path.toString().contains(fileId)) return path.toFile();
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    public enum MediaPath {
        PROFILES(Path.of("./src/main/resources/profiles")),
        BACKGROUNDS(Path.of("./src/main/resources/backgrounds")),
        POSTS(Path.of("./src/main/resources/posts")),
        MESSAGES(Path.of("./src/main/resources/messages")),
        COMMENTS(Path.of("./src/main/resources/comments"));

        private final Path value;

        MediaPath(Path value) {
            this.value = value;
        }

        public Path value() {
            return value;
        }
    }
}
