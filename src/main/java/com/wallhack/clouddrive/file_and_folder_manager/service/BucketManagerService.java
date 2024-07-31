package com.wallhack.clouddrive.file_and_folder_manager.service;

import com.wallhack.clouddrive.file_and_folder_manager.exception.BucketDoesNotExistException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.*;

import java.util.concurrent.CompletableFuture;

@Service
@AllArgsConstructor
public class BucketManagerService {
    private S3AsyncClient client;

    public CompletableFuture<Boolean> bucketExists(String bucketName) {
        HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                .bucket(bucketName)
                .build();

        return client.headBucket(headBucketRequest)
                .thenApply(response -> true)
                .exceptionally(ex -> false);
    }

    public CompletableFuture<String> createBucket(String bucketName) {
        return bucketExists(bucketName).thenCompose(exists -> {

            if (!exists) {
                CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                        .bucket(bucketName)
                        .build();

                return client.createBucket(createBucketRequest)
                        .thenApply(response -> bucketName)
                        .exceptionally(throwable -> {
                            throw new BucketDoesNotExistException("Failed to create bucket: " + bucketName);
                        });
            }
            return CompletableFuture.completedFuture(bucketName);
        });
    }

}
