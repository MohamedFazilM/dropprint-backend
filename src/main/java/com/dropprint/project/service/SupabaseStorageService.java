package com.dropprint.project.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class SupabaseStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-key}")
    private String serviceKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String uploadFile(MultipartFile file, String bucket) throws IOException {
        String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();
        String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + fileName;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + serviceKey);
        headers.set("Content-Type", file.getContentType());

        HttpEntity<byte[]> requestEntity = new HttpEntity<>(file.getBytes(), headers);
        restTemplate.exchange(uploadUrl, HttpMethod.POST, requestEntity, String.class);

        return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + fileName;
    }
}