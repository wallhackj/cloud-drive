package com.wallhack.clouddrive.file_and_folder_manager.service;

import com.wallhack.clouddrive.file_and_folder_manager.entity.FileInfo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SearchService {
    private final BucketManagerService bucketManager;
    private final S3AsyncClient client;

    public Mono<List<FileInfo>> listAllFiles(String bucketName) {
        return Mono.fromFuture(bucketManager.bucketExists(bucketName).thenCompose(bucket -> {
            if (!bucket) {
                return CompletableFuture.failedFuture(new RuntimeException("Bucket does not exist"));
            }

            ListObjectsV2Request listV2Request = ListObjectsV2Request.builder().bucket(bucketName).build();

            return client.listObjectsV2(listV2Request).thenApply(result -> result
                    .contents()
                    .stream()
                    .map(s3Object -> new FileInfo(s3Object.key(), s3Object
                            .lastModified()
                            .toString(), null))
                    .collect(Collectors.toList()));

        }));
    }

    public Mono<FileInfo> searchFile(String bucketName, String fileName) {
        return listAllFiles(bucketName).flatMapMany(Flux::fromIterable).filter(fileInfo -> {
            String key = fileInfo.key().toLowerCase();
            String searchFileName = fileName.toLowerCase();

//      System.out.println(key.substring(fileInfo.key().lastIndexOf("/") + 1));
//      System.out.println(key.substring(fileInfo.key().lastIndexOf("/") + 1, fileInfo.key().lastIndexOf(".")));

            return searchFileName.equals(key.substring(fileInfo.key()
                    .lastIndexOf("/") + 1)) || searchFileName.equals(key.substring(fileInfo.key()
                    .lastIndexOf("/") + 1, fileInfo.key().lastIndexOf(".")));
        }).next();
    }
}
