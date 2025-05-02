package com.pen_penned.blog.util;

import com.pen_penned.blog.exception.ImageProcessingException;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ImageValidator {

    private static final List<String> SUPPORTED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp");

    public static void validateImage(MultipartFile file) throws IOException {
        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !SUPPORTED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw ImageProcessingException.invalidFormat();
        }

        // Try to read the image to verify it's valid
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw ImageProcessingException.invalidFormat();
            }
        } catch (IOException e) {
            throw ImageProcessingException.invalidFormat();
        }
    }
}
