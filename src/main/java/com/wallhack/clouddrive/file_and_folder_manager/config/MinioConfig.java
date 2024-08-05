package com.wallhack.clouddrive.file_and_folder_manager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.internal.crt.S3CrtAsyncClient;


import java.net.URI;

@Configuration
public class MinioConfig {

    @Value("${minio.access.url}")
    private String minioUrl;

    @Value("${minio.access.name}")
    private String accessKey;

    @Value("${minio.access.secret}")
    private String accessSecret;

    @Bean
    public S3AsyncClient generateMinioClient(){
        StaticCredentialsProvider provider = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, accessSecret));
        return S3CrtAsyncClient.builder()
                .forcePathStyle(true)
                .endpointOverride(URI.create(minioUrl))
                .region(Region.EU_CENTRAL_1)
                .credentialsProvider(provider)
                .build();
    }

}
