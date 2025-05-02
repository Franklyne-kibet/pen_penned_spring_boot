package com.pen_penned.blog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Override
    public String uploadFile(String directory, MultipartFile file) throws IOException {
        String fileKey = generateFileKey(directory, file.getOriginalFilename());

        Map<String, String> metadata = new HashMap<>();
        metadata.put("Content-Type", file.getContentType());
        metadata.put("Content-Length", String.valueOf(file.getSize()));

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .contentType(file.getContentType())
                .metadata(metadata)
                // .acl(ObjectCannedACL.PUBLIC_READ)
                .build();

        s3Client.putObject(putObjectRequest,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        log.info("File uploaded successfully to s3: {}", fileKey);
        return fileKey;
    }

    @Override
    public String uploadStream(String directory, String filename,
                               String contentType, InputStream inputStream,
                               long contentLength) {
        String fileKey = generateFileKey(directory, filename);

        Map<String, String> metadata = new HashMap<>();
        metadata.put("Content-Type", contentType);
        metadata.put("Content-Length", String.valueOf(contentLength));

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .contentType(contentType)
                .metadata(metadata)
                // .acl(ObjectCannedACL.PUBLIC_READ)
                .build();

        s3Client.putObject(putObjectRequest,
                RequestBody.fromInputStream(inputStream, contentLength));

        log.info("Stream uploaded successfully to S3: {}", fileKey);
        return fileKey;
    }

    @Override
    public String getFileUrl(String fileKey) {
        return String.format("https://%s.s3.amazonaws.com/%s", bucketName, fileKey);
    }

    @Override
    public void deleteFile(String fileKey) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
        log.info("File deleted from S3: {}", fileKey);
    }

    @Override
    public String generatePresignedUrl(String fileKey, long expirationMinutes) {
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expirationMinutes))
                .getObjectRequest(GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(fileKey)
                        .build())
                .build();
        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    private String generateFileKey(String directory, String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        return directory + "/" + UUID.randomUUID() + extension;
    }
}
