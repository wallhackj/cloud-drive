package com.wallhack.clouddrive.file_and_folder_manager.controller;

import com.wallhack.clouddrive.file_and_folder_manager.service.FolderStorageService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.List;

@Controller
@AllArgsConstructor
public class FolderController {
    private final FolderStorageService folderStorageService;

    @GetMapping("/folderPage")
    public String folderPage() {
        return "mainPage";
    }

    @PostMapping("/uploadDirectory")
    public Mono<ResponseEntity<Boolean>> handlerUploadFolder(@RequestParam("username") String bucketName,
                                                            @RequestParam("directory") List<MultipartFile> files) {
        return folderStorageService.uploadFolder(bucketName, files)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }


    @GetMapping("/downloadFolder")
    public Mono<ResponseEntity<byte[]>> handleFolderDownload(@RequestParam("username") String bucketName,
                                                             @RequestParam("folderName") String folderName) {
        return folderStorageService.downloadFolder(bucketName, folderName)
                .map(zipBytes -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + folderName + ".zip\"")
                        .body(zipBytes))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

    @DeleteMapping("/deleteFolder")
    public Mono<ResponseEntity<Boolean>> handlerFolderDeleting(@RequestParam("username") String bucketName,
                                                               @RequestParam("folderName") String folderName) {
        return folderStorageService.deleteFolder(bucketName, folderName)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

    @PutMapping("/renameFolder")
    public Mono<ResponseEntity<String>> handlerFolderRename(@RequestParam("username") String bucketName,
                                                            @RequestParam("folderName") String folderName,
                                                            @RequestParam("newFolderName") String newFolderName) {
        return folderStorageService.renameFolder(bucketName, folderName, newFolderName)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

}
