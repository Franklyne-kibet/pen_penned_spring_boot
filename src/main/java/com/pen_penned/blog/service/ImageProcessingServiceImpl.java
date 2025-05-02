package com.pen_penned.blog.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;


@Service
@RequiredArgsConstructor
public class ImageProcessingServiceImpl implements ImageProcessingService {

    @Value("${app.image.thumbnail.width:300}")
    private int thumbnailWidth;

    @Override
    public CompletableFuture<ByteArrayInputStream> resizeImage(
            MultipartFile originalImage,
            int targetWidth, int targetHeight) throws IOException {

        return CompletableFuture.supplyAsync(() -> {
            try {

                BufferedImage originalBufferedImage = ImageIO.read(originalImage.getInputStream());

                if (originalBufferedImage == null) {
                    throw new IOException("Failed to read image from uploaded file.");
                }

                // Ensure valid image type
                int imageType = originalBufferedImage.getType();
                if (imageType == 0) {
                    imageType = BufferedImage.TYPE_INT_RGB;
                }

                BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, imageType);

                Graphics2D graphics2D = resizedImage.createGraphics();

                // For better quality rendering
                graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics2D.drawImage(originalBufferedImage, 0, 0, targetWidth, targetHeight, null);
                graphics2D.dispose();

                String formatName = getImageFormat(originalImage.getContentType());
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ImageIO.write(resizedImage, formatName, outputStream);

                return new ByteArrayInputStream(outputStream.toByteArray());

            } catch (IOException e) {
                throw new RuntimeException("Image resizing failed", e);
            }
        });
    }

    @Override
    public CompletableFuture<ByteArrayInputStream> createThumbnail(
            MultipartFile originalImage) throws IOException {
        return createCustomThumbnail(originalImage, thumbnailWidth);
    }

    @Override
    public CompletableFuture<ByteArrayInputStream> createCustomThumbnail(
            MultipartFile originalImage, int width) throws IOException {
        return CompletableFuture.supplyAsync(() -> {
            try {
                BufferedImage originalBufferedImage = ImageIO.read(originalImage.getInputStream());

                if (originalBufferedImage == null) {
                    throw new IOException("Failed to read image from uploaded file.");
                }

                int originalWidth = originalBufferedImage.getWidth();
                int originalHeight = originalBufferedImage.getHeight();
                int thumbnailHeight = (width * originalHeight) / originalWidth;

                // Resize directly here to avoid nested CompletableFuture
                int imageType = originalBufferedImage.getType();
                if (imageType == 0) {
                    imageType = BufferedImage.TYPE_INT_RGB;
                }

                BufferedImage resizedImage = new BufferedImage(width, thumbnailHeight, imageType);
                Graphics2D graphics2D = resizedImage.createGraphics();
                graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics2D.drawImage(originalBufferedImage, 0, 0, width, thumbnailHeight, null);
                graphics2D.dispose();

                String formatName = getImageFormat(originalImage.getContentType());
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ImageIO.write(resizedImage, formatName, outputStream);

                return new ByteArrayInputStream(outputStream.toByteArray());

            } catch (IOException e) {
                throw new RuntimeException("Thumbnail creation failed", e);
            }
        });
    }

    private String getImageFormat(String contentType) {
        return switch (contentType != null ? contentType.toLowerCase() : "") {
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            case "image/bmp" -> "bmp";
            case "image/jpeg", "image/jpg" -> "jpeg";
            default -> throw new IllegalArgumentException("Unsupported image content type: " + contentType);
        };
    }

}
