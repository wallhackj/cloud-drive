package com.wallhack.clouddrive.file_and_folder_manager.service;

import com.wallhack.clouddrive.file_and_folder_manager.entity.FileInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class FolderStorageService {
    private final S3AsyncClient client;
    private final BucketManagerService bucketManager;
    private final FileStorageService fileStorageService;

    public Mono<Boolean> uploadFolder(String bucketName, List<MultipartFile> files) {
        return Mono.fromFuture(() -> bucketManager.createBucket(bucketName))
                .flatMapMany(bucket -> Flux.fromIterable(files)
                        .flatMap(file -> uploadResult(bucketName, file))
                )
                .all(uploadFolder -> uploadFolder)
                .onErrorResume(e -> {
                    log.error("Failed to upload a folder{}", e.getMessage());
                    return Mono.just(false); // Return false in case of error
                });
    }

    private Mono<Boolean> uploadResult(String bucketName, MultipartFile file) {
        String key = file.getOriginalFilename();
        if (isValidFilename(key)) {
            return Mono.fromCallable(file::getInputStream)
                    .flatMap(inputStream -> {
                        FileInfo fileInfo = new FileInfo(key, "", inputStream);
                        return fileStorageService.uploadFile(bucketName, fileInfo);
                    })
                    .onErrorResume(IOException.class, e -> {
                        log.error("Failed to get input stream for file '{}': {}", key, e.getMessage());
                        return Mono.just(false);
                    });
        }
        log.warn("Skipping file with invalid name '{}'", key);
        return Mono.just(false);
    }

    private boolean isValidFilename(String filename) {
        // Filename should not contain "." or "\\" or any other invalid characters
        String regex = "^[^\\\\.!?]+$"; // Regex to disallow '.' and '\\'
        return filename != null && filename.matches(regex);
    }

    public Mono<byte[]> downloadFolder(String bucketName, String folderName) {
        return listKeysInFolder(bucketName, folderName).flatMapMany(Flux::fromIterable) // Flatten list of keys into Flux
                .flatMap(key -> {
                    // Check if the key represents a folder by checking if it ends with '/'
                    if (key.endsWith("/")) {
                        // If it's a folder, recursively download its contents
                        String subfolderName = key.substring(0, key.length() - 1); // Remove trailing '/'
                        return downloadFolder(bucketName, subfolderName).flux(); // Recursively call downloadFolder
                    } else {
                        // If it's a file, download the file and return its data buffer Flux
                        return fileStorageService.downloadFile(bucketName, key)
                                .flatMapMany(Flux::from); // Flatten CompletableFuture<Flux<DataBuffer>> to Flux<DataBuffer>
                    }
                })
                .cast(DataBuffer.class)
                .collectList()
                .flatMap(dataBuffers -> {
                    // List all keys again to get filenames for zipping
                    return listKeysInFolder(bucketName, folderName)
                            .flatMap(filenames -> ZipDataBuffer.toZipFlux(dataBuffers, filenames))// Zip data buffers with filenames
                            .onErrorResume(e -> {
                                log.error("Failed to download a folder{}", e.getMessage());
                                return Mono.error(e);
                            });
                });
    }

    private Mono<List<String>> listKeysInFolder(String bucketName, String folderName) {
        return Mono.defer(() -> {
            List<String> keys = new ArrayList<>();
            String prefix = folderName.endsWith("/") ? folderName : folderName + "/";

            ListObjectsV2Request listObjects = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(prefix)
                    .build();

            return Flux.defer(() -> listKeysInFolderRecursive(listObjects, keys))
                    .then(Mono.just(keys));
        });
    }

    private Mono<Void> listKeysInFolderRecursive(ListObjectsV2Request listObjects, List<String> keys) {
        return fetchListObjects(listObjects).flatMapMany(response -> {
                    response.contents().forEach(s3Object -> keys.add(s3Object.key()));
                    String continuationToken = response.nextContinuationToken();
                    if (continuationToken != null) {
                        ListObjectsV2Request nextRequest = listObjects.toBuilder()
                                .continuationToken(continuationToken)
                                .build();
                        return listKeysInFolderRecursive(nextRequest, keys);
                    } else {
                        return Mono.empty();
                    }
                })
                .then();
    }

    private Mono<ListObjectsV2Response> fetchListObjects(ListObjectsV2Request request) {
        return Mono.fromFuture(() -> client.listObjectsV2(request));
    }

    public Mono<Boolean> deleteFolder(String bucketName, String folderName) {
        return listKeysInFolder(bucketName, folderName).flatMapMany(Flux::fromIterable)
                .flatMap(key -> {
                    if (key.endsWith("/")) {
                        // If it's a folder, recursively delete its contents
                        String subfolderName = key.substring(0, key.length() - 1); // Remove trailing '/'
                        return deleteFolder(bucketName, subfolderName).flux(); // Recursively call deleteFolder
                    } else {
                        // If it's a file, delete the file
                        return fileStorageService.deleteFile(bucketName, key);
                    }
                })
                .then(Mono.just(true))
                .onErrorResume(e -> {
                    log.error("Failed to delete a folder{}", folderName);
                    return Mono.just(false);
                });
    }

    public Mono<String> renameFolder(String bucketName, String folderName, String newFolderName) {
        return listKeysInFolder(bucketName, folderName).flatMapMany(Flux::fromIterable)
                .flatMap(key -> {
                    // Construct the new key for each file
                    String newKey = key.replace(folderName, newFolderName);
                    return fileStorageService.renameOrMoveFile(bucketName, key, newKey);
                })
                .then(Mono.just(newFolderName))
                .onErrorResume(e -> {
                    log.error("Failed to rename a folder{}", folderName);
                    return Mono.just(folderName); // Return original folder name if error occurs
                });
    }

}
