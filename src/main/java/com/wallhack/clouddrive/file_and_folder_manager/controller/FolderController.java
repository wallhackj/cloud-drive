package com.wallhack.clouddrive.file_and_folder_manager.controller;

import com.wallhack.clouddrive.file_and_folder_manager.service.FolderStorageService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Controller
@AllArgsConstructor
public class FolderController {
    private final FolderStorageService folderStorageService;

    @GetMapping("/folderPage")
    public String folderPage() {
        return "mainPage";
    }

    @PostMapping("/uploadDirectory")
    public Mono<ResponseEntity<Boolean>> handlerUploadFolder(@RequestParam("username") String username,
                                                             @RequestParam("directory") List<MultipartFile> files) {
        return folderStorageService.uploadFolder(username, files)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Failed to upload folder: {}", files, e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }


    @GetMapping("/downloadFolder")
    public Mono<ResponseEntity<byte[]>> handleFolderDownload(@RequestParam("username") String username,
                                                             @RequestParam("folderName") String folderName) {
        return folderStorageService.downloadFolder(username, folderName)
                .map(zipBytes -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + folderName + ".zip\"")
                        .body(zipBytes))
                .onErrorResume(e -> {
                    log.error("Failed to download folder: {}", folderName, e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    @DeleteMapping("/deleteFolder")
    public Mono<ResponseEntity<Boolean>> handlerFolderDeleting(@RequestParam("username") String username,
                                                               @RequestParam("folderName") String folderName) {
        return folderStorageService.deleteFolder(username, folderName)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Failed to delete folder: {}", folderName, e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    @PutMapping("/renameFolder")
    public Mono<ResponseEntity<String>> handlerFolderRename(@RequestParam("username") String username,
                                                            @RequestParam("folderName") String folderName,
                                                            @RequestParam("newFolderName") String newFolderName) {
        return folderStorageService.renameFolder(username, folderName, newFolderName)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Failed to rename or move folder: {}", folderName, e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }
}
