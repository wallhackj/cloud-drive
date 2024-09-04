package com.wallhack.clouddrive.file_and_folder_manager.service;

import com.wallhack.clouddrive.file_and_folder_manager.entity.FileInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@AllArgsConstructor
public class FileStorageService {
    private final BucketManagerService bucketManager;
    private final S3AsyncClient client;

    private static Flux<ByteBuffer> toFlux(InputStream inputStream) {
        return DataBufferUtils.readByteChannel(() -> Channels.newChannel(inputStream),
                        DefaultDataBufferFactory.sharedInstance, 4096)
                .flatMapSequential(dataBuffer -> Flux.fromIterable(dataBuffer::readableByteBuffers));
    }

    public Mono<Boolean> uploadFile(String bucketName, FileInfo file) {
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(file.key())
                .build();

        try {
            return Mono.fromFuture(() -> bucketManager.createBucket(bucketName)
                            .thenCompose(v -> client.putObject(putRequest,
                                    AsyncRequestBody.fromPublisher(toFlux(file.file())))))
                    .then(Mono.just(true)).onErrorResume(e -> {
                        log.error("Failed to upload file", e);
                        return Mono.just(false);
                    });

        } catch (Exception e) {
            log.error("Error in bucketservice", e);
            return Mono.error(e);
        }
    }

    public Mono<Boolean> deleteFile(String bucketName, String key) {
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        return Mono.fromFuture(() -> client.deleteObject(deleteRequest))
                .thenReturn(true).onErrorResume(e -> {
                    log.error("Failed to delete file", e);
                    return Mono.just(false);
                });
    }

    public Mono<Flux<DataBuffer>> downloadFile(String bucketName, String key) {
        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        return Mono.fromFuture(() -> client.getObject(getRequest, AsyncResponseTransformer.toPublisher())).flatMap(publisher -> {
            Flux<DataBuffer> dataBufferFlux = Flux.from(publisher)
                    .map(byteBuffer -> new DefaultDataBufferFactory().wrap(byteBuffer));
            return Mono.just(dataBufferFlux);
        }).onErrorResume(e -> {
            log.error("Failed to download file", e);
            return Mono.error(new RuntimeException("Failed to download file", e));
        });
    }

    public Mono<String> renameOrMoveFile(String bucketName, String key, String newKey) {
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
                        .flatMap(deleteResponse -> deleteResponse ?
                                Mono.just(newKey) : Mono.error(new RuntimeException("Failed to delete original file"))))
                .onErrorResume(e -> {
                    log.error("Failed to rename file", e);
                    return Mono.error(new RuntimeException("Failed to rename original file", e));
                });
    }
}
