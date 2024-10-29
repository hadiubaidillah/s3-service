package com.hadiubaidillah.s3.service;

import com.hadiubaidillah.s3.util.AESUtil;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@Service
public class MinioService {

    @Value("${minio.bucket}")
    private String bucketName;

    private final MinioClient minioClient;

    private final SecretKey secretKey;

    public MinioService(MinioClient minioClient, @Value("${encryption.secret.key}") String base64SecretKey) {
        this.minioClient = minioClient;
        this.secretKey = AESUtil.decodeKey(base64SecretKey);
    }

    private String getCodeDecrypted(String code) {
        try {
            return AESUtil.decrypt(code, secretKey);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Encryption/Decryption process failed when tying to get folder.", e);
        }
    }

    private List<String> getCodeDecryptedMimeType(String code, boolean isEncrypted) {
            try {
                String codeDecrypted = isEncrypted ? getCodeDecrypted(code) : code;
                return Arrays.asList(codeDecrypted.split("\\|")[1].split(","));
            } catch (ArrayIndexOutOfBoundsException e) {
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
    }

    private String getFolderPath(String code, boolean isEncrypted) {
        try {
            String codeDecrypted = isEncrypted ? getCodeDecrypted(code) : code;
            System.out.println("Decrypted: " + codeDecrypted);
            String folder = codeDecrypted.split("\\|")[0];
            return "uploads/" + folder + "/";
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Get folder path failed.", e);
        }
    }

    private String getObjectPath(String code, String fileName) throws Exception {
        String filePath = getFolderPath(code, true) + fileName;
        System.out.println("filePath = " + filePath);
        return filePath;
    }

    public String uploadFile(String code, MultipartFile file) throws Exception {

        String codeDecrypted = getCodeDecrypted(code);

        // Check if the file is empty
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
        }

        // Validate MIME type
        String contentType = file.getContentType();
        List<String> mimeType = getCodeDecryptedMimeType(codeDecrypted, false);
        if(mimeType == null) {
            System.out.println("Mime type not found");
        }
        else if (contentType == null || !mimeType.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image files are allowed");
        }

        String fileName = file.getOriginalFilename();

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(getObjectPath(code, fileName))
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );
        return fileName;
    }

    public InputStream downloadFile(String code, String fileName) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(getObjectPath(code, fileName))
                        .build()
        );
    }

    public void deleteFile(String code, String fileName) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(getObjectPath(code, fileName))
                        .build()
        );
    }

}