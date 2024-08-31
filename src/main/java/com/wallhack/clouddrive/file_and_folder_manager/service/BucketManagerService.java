package com.wallhack.clouddrive.file_and_folder_manager.service;

import com.wallhack.clouddrive.file_and_folder_manager.rest.BucketDoesNotExistException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.*;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@AllArgsConstructor
public class BucketManagerService {
    private S3AsyncClient client;

    /**
     * Checks if a bucket exists.
     *
     * @param bucketName the name of the bucket to check.
     * @return a CompletableFuture that completes with true if the bucket exists, false otherwise.
     */
    public CompletableFuture<Boolean> bucketExists(String bucketName) {
        HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                .bucket(bucketName)
                .build();

        return client.headBucket(headBucketRequest)
                .thenApply(response -> true)
                .exceptionally(ex -> {
                    log.warn("Failed to check if bucket {} exists", bucketName, ex);
                    return false;
                });
    }


    /**
     * Creates a bucket if it does not already exist.
     *
     * @param bucketName the name of the bucket to create.
     * @return a CompletableFuture that completes with the bucket name if the bucket was created or already exists.
     * @throws BucketDoesNotExistException if the bucket creation fails.
     */
    public CompletableFuture<String> createBucket(String bucketName) {
        return bucketExists(bucketName).thenCompose(exists -> {
            if (!exists) {
                CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                        .bucket(bucketName)
                        .build();

                return client.createBucket(createBucketRequest)
                        .thenApply(response -> bucketName)
                        .exceptionally(throwable -> {
                            log.warn("Failed to create bucket: {}", bucketName);
                            throw new BucketDoesNotExistException("Failed to create bucket: " + bucketName);
                        });
            }

            return CompletableFuture.completedFuture(bucketName);
        });
    }

}
