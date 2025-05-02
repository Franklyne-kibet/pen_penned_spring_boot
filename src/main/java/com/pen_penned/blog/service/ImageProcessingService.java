package com.pen_penned.blog.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public interface ImageProcessingService {

    /**
     * Resizes an image to the target dimensions
     *
     * @param originalImage The original image file
     * @param targetWidth   Target width in pixels
     * @param targetHeight  Target height in pixels
     * @return Input stream of the resized image
     * @throws IOException If image processing fails
     */
    CompletableFuture<ByteArrayInputStream> resizeImage(
            MultipartFile originalImage,
            int targetWidth, int targetHeight) throws IOException;

    /**
     * Creates a thumbnail version of an image
     *
     * @param originalImage The original image file
     * @return Input stream of the thumbnail image
     * @throws IOException If image processing fails
     */
    CompletableFuture<ByteArrayInputStream> createThumbnail(
            MultipartFile originalImage) throws IOException;

    /**
     * Creates a thumbnail with custom dimensions
     *
     * @param originalImage The original image file
     * @param width         Target width for the thumbnail
     * @return Input stream of the thumbnail image
     * @throws IOException If image processing fails
     */
    CompletableFuture<ByteArrayInputStream> createCustomThumbnail(
            MultipartFile originalImage,
            int width) throws IOException;

}
