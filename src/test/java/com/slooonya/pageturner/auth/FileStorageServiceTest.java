package com.slooonya.pageturner.auth;

import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceTest {

    private Path tempDirectory;
    private FileStorageService fileStorageService;


    @BeforeEach
    void setUp() throws Exception {
        tempDirectory = Files.createTempDirectory("uploads");

        fileStorageService = new FileStorageService(tempDirectory.toString());

        fileStorageService.init();
    }


    @AfterEach
    void tearDown() throws Exception {
        Files.walk(tempDirectory)
            .sorted((a, b) -> b.compareTo(a))
            .forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (Exception ignored) {}
            });
    }

    @Test
    void storeAvatar_shouldStoreValidImage() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "avatar",
                "profile.png",
                "image/png",
                "image-data".getBytes()
            );


        String filename = fileStorageService.storeAvatar(file, "john");

        assertNotNull(filename);
        assertTrue(filename.startsWith("john-avatar-"));
        assertTrue(filename.endsWith(".png"));

        assertTrue(Files.exists(tempDirectory.resolve(filename)));
    }

    @Test
    void storeAvatar_shouldAcceptJpeg() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "avatar",
                "profile.jpg",
                "image/jpeg",
                "image-data".getBytes()
            );

        String filename = fileStorageService.storeAvatar(file, "john");

        assertTrue(filename.endsWith(".jpeg") || filename.endsWith(".jpg"));
    }

    @Test
    void storeAvatar_shouldReturnNullForEmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "avatar",
                "",
                "image/png",
                new byte[0]
            );


        String result = fileStorageService.storeAvatar(file, "john");

        assertNull(result);
    }

    @Test
    void validateFile_shouldRejectInvalidType() {
        MockMultipartFile file = new MockMultipartFile(
                "avatar",
                "test.txt",
                "text/plain",
                "hello".getBytes()
            );


        IOException exception =
            assertThrows(
                IOException.class,
                () -> fileStorageService.validateFile(file)
            );


        assertEquals(
            "Invalid file type. Only JPEG/PNG images are allowed",
            exception.getMessage()
        );
    }

    @Test
    void validateFile_shouldRejectLargeFile() {
        byte[] data = new byte[5 * 1024 * 1024 + 1];

        MockMultipartFile file =
            new MockMultipartFile(
                "avatar",
                "large.png",
                "image/png",
                data
            );


        IOException exception =
            assertThrows(
                IOException.class,
                () -> fileStorageService.validateFile(file)
            );


        assertEquals("File size exceeds 5 MB", exception.getMessage());
    }

    @Test
    void loadAvatar_shouldRejectPathTraversal() {
        assertThrows(
            IOException.class,
            () -> fileStorageService.loadAvatar("../secret.txt")
        );
    }

    @Test
    void loadAvatar_shouldReturnResourceWhenFileExists() throws Exception {
        Path file = tempDirectory.resolve("avatar.png");

        Files.write(file, "image".getBytes());

        UrlResource resource = fileStorageService.loadAvatar("avatar.png");

        assertNotNull(resource);
        assertTrue(resource.exists());
    }

    @Test
    void deleteAvatar_shouldDeleteExistingFile() throws Exception {
        Path file = tempDirectory.resolve("avatar.png");

        Files.write(file, "image".getBytes());

        fileStorageService.deleteAvatar("avatar.png");

        assertFalse(Files.exists(file));
    }
}