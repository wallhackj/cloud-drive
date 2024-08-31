package com.wallhack.clouddrive.file_and_folder_manager.controller;

import com.wallhack.clouddrive.file_and_folder_manager.entity.FileInfo;
import com.wallhack.clouddrive.file_and_folder_manager.service.SearchService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/listFiles")
    public Mono<ResponseEntity<List<FileInfo>>> handleFileList(@RequestParam("username") String username) {
        return searchService.listAllFiles(username)
                .map(files -> ResponseEntity.ok().body(files))
                .onErrorResume(e -> {
                    log.error("Failed to list files: {}", username, e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    @GetMapping("/searchFile")
    public Mono<ResponseEntity<FileInfo>> handleSearch(@RequestParam("username") String username,
                                                       @RequestParam("searchedFile") String searchedFile) {
        return searchService.searchFile(username, searchedFile)
                .map(file -> ResponseEntity.ok().body(file))
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build()))
                .onErrorResume(e -> {
                    log.error("Failed to find file/folder: {}", searchedFile, e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }
}

