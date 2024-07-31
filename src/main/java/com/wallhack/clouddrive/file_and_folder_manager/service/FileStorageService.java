package com.wallhack.clouddrive.file_and_folder_manager.service;

import lombok.AllArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.util.concurrent.CompletableFuture;

@Service
@AllArgsConstructor
public class FileStorageService {
    private final BucketManagerService bucketManager;
    private final S3AsyncClient client;

    private static Flux<ByteBuffer> toFlux(InputStream inputStream) {
        return DataBufferUtils.readByteChannel(() -> Channels.newChannel(inputStream),
                        DefaultDataBufferFactory.sharedInstance,
                        4096)
                .flatMapSequential(dataBuffer -> Flux.fromIterable(dataBuffer::readableByteBuffers));
    }

    public CompletableFuture<String> uploadFile(String bucketName, MultipartFile file) {
       return bucketManager.createBucket(bucketName)
               .thenCompose(bucket -> {
                   var fileName = file.getOriginalFilename();

                   PutObjectRequest putRequest = PutObjectRequest.builder()
                           .bucket(bucket)
                           .key(fileName)
                           .build();

                   try (InputStream inputStream = file.getInputStream()){

                       return client.putObject(putRequest,AsyncRequestBody.fromPublisher(toFlux(inputStream)))
                               .thenApply(resp -> fileName);
                   }catch (Exception e){
                       return CompletableFuture.failedFuture(e);
                   }
               });
    }

    public CompletableFuture<Flux<DataBuffer>> downloadFile(String bucketName, String key) {
        return bucketManager.bucketExists(bucketName)
                .thenCompose(bucket -> {
                    if (!bucket){
                        return CompletableFuture.failedFuture(new RuntimeException("Bucket does not exist"));
                    }
                    GetObjectRequest getRequest = GetObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .build();

                    return client.getObject(getRequest, AsyncResponseTransformer.toPublisher())
                            .thenApply(publisher -> Flux.from(publisher)
                                    .map(byteBuffer -> new DefaultDataBufferFactory().wrap(byteBuffer))
                            );
                });
    }

    public CompletableFuture<Boolean> deleteFile(String bucketName, String key) {
        return bucketManager.bucketExists(bucketName)
                .thenCompose(bucket -> {
                    if (!bucket){
                        return CompletableFuture.failedFuture(new RuntimeException("Bucket does not exist"));
                    }
                    DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .build();

                    return client.deleteObject(deleteRequest)
                            .thenApply(resp -> true);
                });
    }

    public CompletableFuture<String> renameFile(String bucketName, String key, String newKey) {
        return bucketManager.bucketExists(bucketName)
                .thenCompose(bucket -> {
                    if (!bucket){
                        return CompletableFuture.failedFuture(new RuntimeException("Bucket does not exist"));
                    }
                    CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                            .sourceBucket(bucketName)
                            .sourceKey(key)
                            .destinationBucket(bucketName)
                            .destinationKey(newKey)
                            .contentDisposition("attachment; filename=" + newKey)
                            .metadataDirective(MetadataDirective.REPLACE)
                            .build();
                    return client.copyObject(copyRequest)
                            .thenCompose(copyResponse -> deleteFile(bucketName, key)
                                    .thenApply(deleteResponse -> {
                                        if (deleteResponse) {
                                            return newKey;
                                        } else {
                                            throw new RuntimeException("Failed to delete original file");
                                        }
                                    }));
                });
    }



}
