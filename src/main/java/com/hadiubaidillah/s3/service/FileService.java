package com.hadiubaidillah.s3.service;

import com.hadiubaidillah.s3.model.File;
import com.hadiubaidillah.s3.repository.FileRepository;
import com.hadiubaidillah.s3.util.AESUtil;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileService {

    @Value("${minio.bucket}")
    private String bucketName;

    private final MinioClient minioClient;

    private final SecretKey secretKey;

    private final FileRepository fileRepository;

    private final Tika tika = new Tika();

    public FileService(
            MinioClient minioClient,
            @Value("${encryption.secret.key}") String base64SecretKey,
            FileRepository fileRepository
    ) {
        this.minioClient = minioClient;
        this.secretKey = AESUtil.decodeKey(base64SecretKey);
        this.fileRepository = fileRepository;
    }



    public List<File> getAllFiles() {
        return fileRepository.findAll();
    }

    @Transactional
    public File uploadFile(
            String code,
            MultipartFile multipartFile,
            Optional<String> id
    ) throws Exception {

        String codeDecrypted = getCodeDecrypted(code);

        // Check if the file is empty
        if (multipartFile.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
        }

        // Validate MIME type
        String contentType = multipartFile.getContentType();
        List<String> acceptedMimeType = getCodeDecryptedMimeType(codeDecrypted, false);
        if(acceptedMimeType == null) {
            System.out.println("Mime-Type not found");
        }
        else if (contentType == null || !acceptedMimeType.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Upload File type are allowed");
        }

        String fileName = multipartFile.getOriginalFilename();

        // Get MIME type
        String mimeType = multipartFile.getContentType();
        if(mimeType == null) {
            mimeType = tika.detect(multipartFile.getInputStream());
        }

        File file = id.map(UUID::fromString)
                .flatMap(fileRepository::findById)
                .orElse(null);

        if(file == null) {
            file = new File(UUID.randomUUID());
            file.setCreatedAt(LocalDateTime.now());
            file.setCreatedBy(null);
        }

        file.setName(fileName);
        file.setMimeType(mimeType);

        if(id.isPresent()) {
            file.setUpdatedAt(LocalDateTime.now());
            file.setUpdatedBy(null);
        }
//        file.setDeletedAt(LocalDateTime.now());
//        file.setDeletedBy(null);

        fileRepository.save(file);

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(getObjectPath(code, file.getId().toString()))
                        .stream(multipartFile.getInputStream(), multipartFile.getSize(), -1)
                        .contentType(multipartFile.getContentType())
                        .build()
        );

        return file;
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
        File file = fileRepository.getReferenceById(UUID.fromString(fileName));
        System.out.println(file.getName());
        file.setDeletedAt(LocalDateTime.now());
        file.setDeletedBy(null);
        fileRepository.save(file);
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(getObjectPath(code, fileName))
                        .build()
        );
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

}