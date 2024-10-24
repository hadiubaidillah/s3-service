package com.hadiubaidillah.s3.service;

import com.hadiubaidillah.s3.util.AESUtil;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.InputStream;

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

    private String getFolderPath(String code) {
        try {
            String appCodeDecrypted = AESUtil.decrypt(code, secretKey);
            System.out.println("Decrypted: " + appCodeDecrypted);
            return "uploads/" + appCodeDecrypted + "/";
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Encryption/Decryption process failed.", e);
        }
    }

    private String getObjectPath(String code, String fileName) throws Exception {
        String filePath = getFolderPath(code) + fileName;
        System.out.println("filePath = " + filePath);
        return filePath;
    }

    public String uploadFile(String code, MultipartFile file) throws Exception {
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