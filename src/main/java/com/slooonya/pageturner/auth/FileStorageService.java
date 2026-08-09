package com.slooonya.pageturner.auth;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;


@Service
public class FileStorageService {
    private final Path storageBasePath;
    
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/jpeg", "image/png");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);
    
    public FileStorageService(@Value("${app.uploads.dir}") String uploadDirectory){
        this.storageBasePath = Paths.get(uploadDirectory).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init(){
        try{
            Files.createDirectories(storageBasePath);
            logger.info("Created upload directory at: {}", storageBasePath);
        } catch (IOException e){
            logger.error("Could not create upload directory", e);
            throw new RuntimeException("Failed to create upload directory", e);
        }
    }
    
    public String storeAvatar(MultipartFile file, String username) throws IOException{
        if (file == null || file.isEmpty()){
            return null;
        }

        validateFile(file);

        String filename = generateUniqueFilename(file, username);
        Path destinationPath = storageBasePath.resolve(filename);

        try {
            file.transferTo(destinationPath);
            logger.info("Stored avatar for {} at: {}", username, destinationPath);
            return filename;
        } catch (IOException e) {
            logger.error("Failed to store avatar for {}", username, e);
            throw new IOException("Failed to store avatar file", e);
        }
    }

    public void validateFile(MultipartFile file) throws IOException{
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();

        if (!ALLOWED_IMAGE_TYPES.contains(contentType))
            throw new IOException("Invalid file type. Only JPEG/PNG images are allowed");
        
        if (file.getSize() > MAX_FILE_SIZE)
            throw new IOException("File size exceeds 5 MB");
    }

    private String generateUniqueFilename(MultipartFile file, String username){
        String extension = file.getContentType().split("/")[1];
        return String.format("%s-avatar-%s.%s", formatFilename(username), UUID.randomUUID(), extension);
    }

    private String formatFilename(String filename){
        return filename.replaceAll("[^a-zA-Z0-9.-]", "_");
    }

    public UrlResource loadAvatar(String filename) throws IOException{
        if (filename == null || filename.isBlank()){
            logger.debug("Requested empty filename for avatar");
            return null;
        }

        Path filePath = storageBasePath.resolve(filename).normalize();

        if (!filePath.startsWith(storageBasePath))
            throw new IOException("Attempted path traversal attack");
        
        UrlResource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()){
            logger.warn("Avatar file not found or not readable: {}", filename);
            return null;
        }

        return resource;
    }

    public void deleteAvatar(String filename) throws IOException{
        if (filename == null || filename.isBlank())
            return;
        
        Path filePath = storageBasePath.resolve(filename).normalize();

        if (!filePath.startsWith(storageBasePath))
            throw new IOException("Attempted path traversal attack");

        try {
            Files.deleteIfExists(filePath);
            logger.info("Deleted avatar file: {}", filename);
        } catch (IOException e) {
            logger.error("Failed to delete avatar file: {}", filename, e);
            throw new IOException("Failed to delete avatar file", e);
        }
    }
}
