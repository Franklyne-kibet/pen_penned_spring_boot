package com.pen_penned.blog.util;

import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

public class MultipartFileUtil {

    public static MultipartFile createMultipartFileFromInputStream(
            byte[] data, String originalFilename, String contentType
    ) {
        return new MultipartFile() {

            @Override
            @NonNull
            public String getName() {
                return "thumbnail";
            }

            @Override
            public String getOriginalFilename() {
                return "thumb_" + originalFilename;
            }

            @Override
            public String getContentType() {
                return contentType;
            }

            @Override
            public boolean isEmpty() {
                return data.length == 0;
            }

            @Override
            public long getSize() {
                return data.length;
            }

            @Override
            @NonNull
            public byte[] getBytes() {
                return data;
            }

            @Override
            @NonNull
            public InputStream getInputStream() {
                return new ByteArrayInputStream(data);
            }

            @Override
            public void transferTo(@NonNull File dest) throws IOException {
                try (FileOutputStream out = new FileOutputStream(dest)) {
                    out.write(data);
                }
            }
        };
    }
}
