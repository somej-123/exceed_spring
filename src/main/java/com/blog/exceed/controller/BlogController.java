package com.blog.exceed.controller;

import com.blog.exceed.dao.BlogAttachmentDao;
import com.blog.exceed.dao.BlogCategoryDao;
import com.blog.exceed.service.BlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@RequestMapping("/api/blog")
public class BlogController {

    @Value("${blog.image.upload-dir}")
    private String uploadDir;

    @Value("${backend.url:}")
    private String backendUrl;

    @Autowired
    private BlogService blogService;

    @PostMapping("/upload-image")
    @Transactional
    public ResponseEntity<?> uploadImage(@RequestParam("image") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "파일이 비어 있습니다."));
        }
        // 50MB 제한 체크
        long maxSize = 50L * 1024 * 1024; // 50MB
        if (file.getSize() > maxSize) {
            return ResponseEntity.badRequest().body(Map.of("error", "파일 크기는 50MB를 초과할 수 없습니다."));
        }
        try {
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String ext = originalFilename.contains(".") ? originalFilename.substring(originalFilename.lastIndexOf('.')) : "";
            String newFilename = UUID.randomUUID() + ext;
            Path imagePath = Paths.get(uploadDir);
            if (!Files.exists(imagePath)) {
                Files.createDirectories(imagePath);
            }
            Path targetPath = imagePath.resolve(newFilename);
            file.transferTo(targetPath.toFile());
            String imageUrl = "/images/" + newFilename;
            String fullImageUrl = backendUrl != null && !backendUrl.isBlank() ? backendUrl + imageUrl : imageUrl;

            // DB 저장
            BlogAttachmentDao attachment = new BlogAttachmentDao();
            attachment.setBlogPostId(null); // 글 작성 전에는 null
            attachment.setFileUrl(imageUrl);
            attachment.setFileType(file.getContentType());
            attachment.setFileSize(file.getSize());
            // uploadedAt은 DB now()로 처리
            blogService.saveAttachment(attachment);

            Map<String, Object> result = new HashMap<>();
            result.put("success", 1);
            result.put("file", Map.of(
                "url", fullImageUrl
            ));
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "파일 업로드 실패: " + e.getMessage()));
        }
    }

    //카테고리 목록 조회 (페이징/검색 지원)
    @GetMapping("/categories")
    @Transactional
    public ResponseEntity<?> getCategoriesPaged(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "search", required = false) String search
    ) {
        // 서비스에서 페이징/검색 처리하도록 위임
        Map<String, Object> result = blogService.getCategoriesPaged(page, size, search);
        return ResponseEntity.ok(result);
    }

    // 카테고리 신규 등록
    @PostMapping("/categories")
    @Transactional
    public ResponseEntity<?> EditCategories(@RequestBody BlogCategoryDao entity) {
        try {
            blogService.saveCategory(entity);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "카테고리 저장 실패: " + e.getMessage()));
        }
        
        return ResponseEntity.ok(entity);
    }
    

    // @GetMapping("/images/{filename}")
    // public ResponseEntity<?> getImage(@PathVariable String filename) {
    //     try {
    //         Path imagePath = Paths.get(uploadDir, filename);
    //         if (Files.exists(imagePath)) {
    //             return ResponseEntity.ok(Files.readAllBytes(imagePath));
    //         } else {
    //             return ResponseEntity.notFound().build();
    //         }
    //     } catch (IOException e) {
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "이미지 로드 실패: " + e.getMessage()));
    //     }
    // }

    

    // @GetMapping("/{postId}/attachments")
    // public ResponseEntity<?> getAttachments(@PathVariable("postId") Long postId) {
    //     return ResponseEntity.ok(blogService.getAttachmentsByPostId(postId));
    // }
}
