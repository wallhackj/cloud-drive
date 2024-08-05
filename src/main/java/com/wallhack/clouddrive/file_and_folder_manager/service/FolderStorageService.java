package com.wallhack.clouddrive.file_and_folder_manager.service;

import com.wallhack.clouddrive.file_and_folder_manager.entity.FileInfo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@AllArgsConstructor
public class FolderStorageService {
    private final S3AsyncClient client;
    private final BucketManagerService bucketManager;
    private final FileStorageService fileStorageService;

    public CompletableFuture<Boolean> uploadFolder(String bucketName, List<MultipartFile> files) {
        return bucketManager.createBucket(bucketName)
                .thenCompose(bucket -> {
                    List<CompletableFuture<String>> uploadFutures = new ArrayList<>();

                    files.forEach(file -> {
                        String key = file.getOriginalFilename();
                        try (InputStream inputStream = file.getInputStream()) {
                            FileInfo fileInfo = new FileInfo(key, "", inputStream);
                            uploadFutures.add(fileStorageService.uploadFile(bucketName, fileInfo));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });

                    return CompletableFuture.allOf(uploadFutures.toArray(new CompletableFuture[0]))
                            .thenApply(v -> true)
                            .exceptionally(ex -> {
                                ex.printStackTrace();
                                return false;
                            });
                });

    }

    public Mono<byte[]> downloadFolder(String bucketName, String folderName) {
        return Mono.fromCallable(() -> listKeysInFolder(bucketName, folderName))
                .flatMapMany(keys -> Flux.fromIterable(keys)
                        .flatMap(key -> Mono.fromFuture(() -> fileStorageService.downloadFile(bucketName, key))
                                .flatMapMany(flux -> flux) // Flatten Flux<DataBuffer> inside CompletableFuture
                        )
                )
                .collectList()
                .flatMap(dataBuffers -> {
                    // Create filenames list for zipping
                    List<String> filenames = listKeysInFolder(bucketName, folderName);
                    return ZipDataBuffer.toZipFlux(dataBuffers, filenames);
                });
    }

    private List<String> listKeysInFolder(String bucketName, String folderName) {
        List<String> keys = new ArrayList<>();
        String prefix = folderName.endsWith("/") ? folderName : folderName + "/";

        ListObjectsV2Request listObjects = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .delimiter("/")
                .build();

        ListObjectsV2Response listObjectsResponse;
        String continuationToken = null;

        do {
            listObjectsResponse = client.listObjectsV2(listObjects.toBuilder().continuationToken(continuationToken).build()).join();

            for (S3Object s3Object : listObjectsResponse.contents()) {
                keys.add(s3Object.key());
            }

            continuationToken = listObjectsResponse.nextContinuationToken();
        } while (continuationToken != null);

        return keys;
    }


//    public CompletableFuture<Boolean> deleteFolder(String bucketName, String folderName) {}
}
