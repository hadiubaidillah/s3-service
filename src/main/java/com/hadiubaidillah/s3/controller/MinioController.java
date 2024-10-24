package com.hadiubaidillah.s3.controller;

import com.hadiubaidillah.s3.service.MinioService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@RestController
@RequestMapping("/files")
public class MinioController {

    private final MinioService minioService;

    public MinioController(MinioService minioService) {
        this.minioService = minioService;
    }

    @PostMapping
    public ResponseEntity<String> uploadFile(
            @RequestParam("code") String code,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            String fileName = minioService.uploadFile(code, file);
            return new ResponseEntity<>(fileName, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error uploading file: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<byte[]> downloadFile(
            @RequestParam("code") String code,
            @PathVariable String fileName
    ) {
        try {
            InputStream fileStream = minioService.downloadFile(code, fileName);
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
            minioService.deleteFile(code, fileName);
            return new ResponseEntity<>("File deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error deleting file: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

