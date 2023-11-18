package com.elleined.marketplaceapi.service.image;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageUploaderTest {

    @Mock
    private ImageUploader imageUploader;

    @Test
    void upload() throws IOException {
        imageUploader.upload(anyString(), any(MultipartFile.class));
    }

//    @Test
//    void uploadIntegrationTest() throws IOException {
//        ImageUploader imageUploader1 = new ImageUploader();
//
//        Path picturePath = Paths.get("C:\\Users\\Acer\\Desktop\\Downloads\\tropa.jpg");
//        MultipartFile multipartFile = new MockMultipartFile(
//                "file",
//                "tropa.jpg",
//                "image/jpeg",
//                Files.readAllBytes(picturePath)
//        );
//        imageUploader1.upload("C:\\Image uploads\\", multipartFile);
//    }
}