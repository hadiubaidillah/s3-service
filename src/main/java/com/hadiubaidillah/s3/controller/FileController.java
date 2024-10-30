package com.hadiubaidillah.s3.controller;

import com.hadiubaidillah.s3.model.File;
import com.hadiubaidillah.s3.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Optional;

@RestController
@RequestMapping("/files")
@Tag(name = "File API", description = "API for file upload and management")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @Operation(
            summary = "Upload a file",
            description = "Uploads a file and returns the file metadata"
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(
            @RequestParam("code") String code,
            @RequestParam("file") MultipartFile multipartFile,
            @RequestParam("id") Optional<String> id
    ) {
        try {
            File file = fileService.uploadFile(code, multipartFile, id);
            return ResponseEntity.ok(file);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading file: " + e.getMessage());
        }
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<byte[]> downloadFile(
            @RequestParam("code") String code,
            @PathVariable String fileName

    ) {
        try {
            InputStream fileStream = fileService.downloadFile(code, fileName);
            byte[] fileContent = fileStream.readAllBytes();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData(fileName, fileName);
            return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{fileName}")
    public ResponseEntity<String> deleteFile(
            @RequestParam("code") String code,
            @PathVariable String fileName) {
        try {
            fileService.deleteFile(code, fileName);
            return new ResponseEntity<>("File deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error deleting file: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

