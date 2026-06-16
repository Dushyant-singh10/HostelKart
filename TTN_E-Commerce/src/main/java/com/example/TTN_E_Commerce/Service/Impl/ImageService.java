package com.example.TTN_E_Commerce.Service.Impl;

import com.example.TTN_E_Commerce.Exception.CustomBadRequestException;
import com.example.TTN_E_Commerce.Exception.NotFoundException;
import com.example.TTN_E_Commerce.Repository.CustomerRepository;
import com.example.TTN_E_Commerce.Repository.SellerRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class ImageService {

    @Autowired private CustomerRepository customerRepository;
    @Autowired private SellerRepository sellerRepository;

    private UUID resolveUser(String email, String role) {
        if (role.equals("ROLE_CUSTOMER")) {
            return customerRepository.findByUserEmail(email)
                    .orElseThrow(() -> new NotFoundException("Customer not found"))
                    .getUser().getId();
        } else if (role.equals("ROLE_SELLER")) {
            return sellerRepository.findByUserEmail(email)
                    .orElseThrow(() -> new NotFoundException("Seller not found"))
                    .getUser().getId();
        } else {
            throw new CustomBadRequestException("Invalid role");
        }
    }

    @Transactional
    public ResponseEntity<?> uploadProfileImage(MultipartFile image) throws IOException {

        String contentType = image.getContentType();
        if (contentType == null ||
                !(contentType.equals("image/jpeg") ||
                        contentType.equals("image/png")  ||
                        contentType.equals("image/bmp"))) {
            throw new CustomBadRequestException("Invalid image format. Allowed: jpg, png, bmp");
        }

        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String role  = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().iterator().next().getAuthority();

        UUID id = resolveUser(email, role);

        File directory = new File("uploads/users");
        if (!directory.exists()) directory.mkdirs();

        String originalFileName = image.getOriginalFilename();
        if (originalFileName == null || !originalFileName.contains(".")) {
            throw new CustomBadRequestException("Invalid file name");
        }
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String fileName  = id + extension;
        Path path        = Paths.get("uploads/users", fileName);

        File folder = new File("uploads/users");
        File[] oldFiles = folder.listFiles((dir, name) -> name.startsWith(id.toString()));
        if (oldFiles != null) {
            for (File old : oldFiles) old.delete();
        }

        Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

        return ResponseEntity.ok("Image uploaded successfully");
    }

    public ResponseEntity<?> getImage() throws IOException {

        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String role  = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().iterator().next().getAuthority();

        UUID id = resolveUser(email, role);

        File folder = new File("uploads/users");
        File[] files = folder.listFiles((dir, name) -> name.startsWith(id.toString()));

        if (files == null || files.length == 0) {
            throw new NotFoundException("Profile image not found");
        }

        Path path = files[0].toPath();
        org.springframework.core.io.Resource resource = new UrlResource(path.toUri());

        // Extension se MediaType detect karo
        String filename  = files[0].getName();
        MediaType mediaType = filename.endsWith(".png") ? MediaType.IMAGE_PNG
                : filename.endsWith(".bmp")             ? MediaType.valueOf("image/bmp")
                : MediaType.IMAGE_JPEG;

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(resource);
    }
}