package com.wallhack.clouddrive.file_and_folder_manager.service;

import com.wallhack.clouddrive.file_and_folder_manager.entity.FileInfo;
import lombok.AllArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
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

    public Mono<Boolean> uploadFolder(String bucketName, List<MultipartFile> files) {
        return Mono.fromFuture(() -> bucketManager.createBucket(bucketName))
                .flatMapMany(bucket -> Flux.fromIterable(files)
                        .flatMap(file -> {
                            String key = file.getOriginalFilename();
                            try {
                                InputStream inputStream = file.getInputStream();
                                FileInfo fileInfo = new FileInfo(key, "", inputStream);
                                return fileStorageService.uploadFile(bucketName, fileInfo);
                            } catch (IOException e) {
                                return Mono.error(e);
                            }
                        })
                )
                .then(Mono.just(true)) // Complete the whole process with a successful boolean
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.just(false); // Return false in case of error
                });
    }


    public Mono<byte[]> downloadFolder(String bucketName, String folderName) {
        return listKeysInFolder(bucketName, folderName)
                .flatMapMany(Flux::fromIterable) // Flatten list of keys into Flux
                .flatMap(key -> {
                    // Check if the key represents a folder by checking if it ends with '/'
                    if (key.endsWith("/")) {
                        // If it's a folder, recursively download its contents
                        String subfolderName = key.substring(0, key.length() - 1); // Remove trailing '/'
                        return downloadFolder(bucketName, subfolderName).flux(); // Recursively call downloadFolder
                    } else {
                        // If it's a file, download the file and return its data buffer Flux
                        return  fileStorageService.downloadFile(bucketName, key)
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
                                e.printStackTrace();
                                return Mono.error(e);
                            });
                });
    }

    private Mono<List<String>> listKeysInFolder(String bucketName, String folderName) {
        return Mono.fromFuture(() -> CompletableFuture.supplyAsync(() -> {
            List<String> keys = new ArrayList<>();
            String prefix = folderName.endsWith("/") ? folderName : folderName + "/";

            ListObjectsV2Request listObjects = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(prefix)
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
        }));
    }


    public Mono<Boolean> deleteFolder(String bucketName, String folderName) {
        return listKeysInFolder(bucketName, folderName)
                .flatMapMany(Flux::fromIterable)
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
                    e.printStackTrace();
                    return Mono.just(false);
                });
    }

    public Mono<String> renameFolder(String bucketName, String folderName, String newFolderName) {
        return listKeysInFolder(bucketName, folderName)
                .flatMapMany(Flux::fromIterable)
                .flatMap(key -> {
                    // Construct the new key for each file
                    String newKey = key.replace(folderName, newFolderName);
                    return fileStorageService.renameFile(bucketName, key, newKey);
                })
                .then(Mono.just(newFolderName))
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.just(folderName); // Return original folder name if error occurs
                });
    }

}
