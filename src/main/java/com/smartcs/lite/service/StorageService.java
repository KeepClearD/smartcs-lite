package com.smartcs.lite.service;

import com.smartcs.lite.config.StorageConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;
import java.net.URI;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {

    private final StorageConfig config;
    private S3Client s3Client;

    @PostConstruct
    public void init() {
        s3Client = S3Client.builder()
                .endpointOverride(URI.create(config.getEndpoint()))
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(config.getAccessKey(), config.getSecretKey())))
                .forcePathStyle(true)
                .build();

        // 确保 Bucket 存在
        try {
            s3Client.createBucket(CreateBucketRequest.builder()
                    .bucket(config.getBucket()).build());
            log.info("Storage bucket '{}' ready", config.getBucket());
        } catch (BucketAlreadyExistsException | BucketAlreadyOwnedByYouException e) {
            log.info("Bucket '{}' already exists", config.getBucket());
        }
    }

    /**
     * 上传文件
     */
    public String upload(String key, InputStream inputStream, String contentType, long size) {
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(config.getBucket())
                        .key(key)
                        .contentType(contentType)
                        .build(),
                RequestBody.fromInputStream(inputStream, size));
        return key;
    }

    /**
     * 下载文件
     */
    public InputStream download(String key) {
        return s3Client.getObject(
                GetObjectRequest.builder()
                        .bucket(config.getBucket())
                        .key(key)
                        .build());
    }

    /**
     * 生成存储 Key
     */
    public String generateKey(Long tenantId, String category, String filename) {
        return tenantId + "/" + category + "/" + System.currentTimeMillis() + "_" + filename;
    }
}
