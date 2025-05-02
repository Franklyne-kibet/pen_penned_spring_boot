package com.pen_penned.blog.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public interface S3Service {

    /**
     * Uploads a file to S3 bucket
     *
     * @param directory Directory path within the bucket
     * @param file The file to upload
     * @return The S3 object key
     * @throws IOException If file reading fails
     */
    String uploadFile(String directory, MultipartFile file) throws IOException;

    /**
     * Uploads an input stream to S3 bucket
     *
     * @param directory Directory path within the bucket
     * @param filename The name to give the file
     * @param contentType The content type of the file
     * @param inputStream The input stream to upload
     * @param contentLength The length of the content
     * @return The S3 object key
     */
    String uploadStream(String directory, String filename, String contentType,
                        InputStream inputStream, long contentLength);

    /**
     * Gets a URL for the file in S3
     *
     * @param fileKey The S3 object key
     * @return The public URL of the file
     */
    String getFileUrl(String fileKey);

    /**
     * Deletes a file from S3
     *
     * @param fileKey The S3 object key to delete
     */
    void deleteFile(String fileKey);

    /**
     * Generate a presigned URL for temporary access
     *
     * @param fileKey The S3 object key
     * @param expirationMinutes Minutes before the URL expires
     * @return A presigned URL for the object
     */
    String generatePresignedUrl(String fileKey, long expirationMinutes);
}
