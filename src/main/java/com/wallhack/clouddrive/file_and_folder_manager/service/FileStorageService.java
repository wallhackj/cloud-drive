package com.wallhack.clouddrive.file_and_folder_manager.service;

import com.wallhack.clouddrive.file_and_folder_manager.entity.FileInfo;
import lombok.AllArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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

    public Mono<Boolean> uploadFile(String bucketName, FileInfo file) {
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(file.key())
                .build();

        try {
            return Mono.fromFuture(() -> client.putObject(putRequest, AsyncRequestBody.fromPublisher(toFlux(file.file()))))
                    .then(Mono.just(true))
                    .onErrorResume(e -> {
                        e.printStackTrace();
                        return Mono.just(false);
                    });

        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    public Mono<Boolean> deleteFile(String bucketName, String key) {
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        return Mono.fromFuture(() -> client.deleteObject(deleteRequest))
                .then(Mono.just(true))
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.just(false);
                });
    }

    public Mono<Flux<DataBuffer>> downloadFile(String bucketName, String key) {
        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        return Mono.fromFuture(() -> client.getObject(getRequest, AsyncResponseTransformer.toPublisher()))
                .flatMap(publisher -> {
                    Flux<DataBuffer> dataBufferFlux = Flux.from(publisher)
                            .map(byteBuffer -> new DefaultDataBufferFactory().wrap(byteBuffer));
                    return Mono.just(dataBufferFlux);
                })
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.error(new RuntimeException("Failed to download file", e));
                });
    }



    public Mono<String> renameFile(String bucketName, String key, String newKey) {
        CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                .sourceBucket(bucketName)
                .sourceKey(key)
                .destinationBucket(bucketName)
                .destinationKey(newKey)
                .contentDisposition("attachment; filename=" + newKey)
                .metadataDirective(MetadataDirective.REPLACE)
                .build();

        return Mono.fromFuture(() -> client.copyObject(copyRequest))
                .flatMap(copyResponse -> deleteFile(bucketName, key)
                        .flatMap(deleteResponse -> {
                                        if (deleteResponse) {
                                            return Mono.just(newKey);
                                        } else {
                                            return Mono.error(new RuntimeException("Failed to delete original file"));
                                        }
                        }))
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.error(new RuntimeException("Failed to rename original file", e));
                });
    }


    public Mono<List<FileInfo>> listAllFiles(String bucketName) {
        return Mono.fromFuture(bucketManager.bucketExists(bucketName)
                .thenCompose(bucket -> {
                    if (!bucket){
                        return CompletableFuture.failedFuture(new RuntimeException("Bucket does not exist"));
                    }else {
                        ListObjectsV2Request listV2Request = ListObjectsV2Request.builder()
                                .bucket(bucketName)
                                .build();

                        return client.listObjectsV2(listV2Request)
                                .thenApply(result -> result.contents()
                                        .stream()
                                        .map(s3Object -> new FileInfo(s3Object.key()
                                                , s3Object.lastModified().toString(), null))
                                        .collect(Collectors.toList()));
                    }
                }));
    }
}
