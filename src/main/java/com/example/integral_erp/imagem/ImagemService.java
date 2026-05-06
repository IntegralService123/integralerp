package com.example.integral_erp.imagem;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImagemService {

    @Value("${spring.supabase.supabase-url}")
    private String supabaseUrl;

    @Value("${spring.supabase.anon-key}")
    private String supabaseKey;

    @Value("${spring.supabase.bucket}")
    private String bucketName;

    public String upload(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename().replace(" ", "_");

        String actionUrl = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + fileName;

        String publicUrl = supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + fileName;

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + supabaseKey);
        headers.set("apikey", supabaseKey);
        headers.setContentType(MediaType.valueOf(file.getContentType()));

        HttpEntity<byte[]> entity = new HttpEntity<>(file.getBytes(), headers);

        // Faz o upload via PUT para o Supabase
        restTemplate.postForEntity(actionUrl, entity, String.class);

        // Retorna a URL pública direta
        return publicUrl;
    }
}
