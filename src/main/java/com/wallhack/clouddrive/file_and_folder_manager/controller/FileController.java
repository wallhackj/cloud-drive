package com.wallhack.clouddrive.file_and_folder_manager.controller;

import com.wallhack.clouddrive.file_and_folder_manager.entity.FileInfo;
import com.wallhack.clouddrive.file_and_folder_manager.service.FileStorageService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Controller
@AllArgsConstructor
public class FileController {
    private FileStorageService fileService;

    @PostMapping("/uploadFile")
    public Mono<ResponseEntity<String>> handleFileUpload(@RequestParam("username") String username,
                                                         @RequestParam("file") MultipartFile file) {
        return Mono.fromCallable(() -> new FileInfo(file.getOriginalFilename(), "never", file.getInputStream()))
                .flatMap(fileInfo -> fileService.uploadFile(username, fileInfo)
                        .thenReturn(ResponseEntity.ok("File uploaded successfully")))
                .onErrorResume(e -> {
                    log.error("File upload failed :", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }


    @GetMapping("/downloadFile")
    public Mono<ResponseEntity<byte[]>> handleFileDownload(@RequestParam("username") String username,
                                                           @RequestParam("fileName") String fileName) {
        return fileService.downloadFile(username, fileName)
                .flatMap(this::fluxOfDataBufferToByteArray)
                .map(bytes -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                        .body(bytes)) // Set the response body as the byte array because it's not encrypted :(
                .onErrorResume(e -> {
                    log.error("Failed to download file: {}", fileName, e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    @DeleteMapping("/deleteFile")
    public Mono<ResponseEntity<String>> handleFileDelete(@RequestParam("username") String username,
                                                         @RequestParam("fileName") String fileName) {
        return fileService.deleteFile(username, fileName)
                .map(deleteResult -> {
                    if (deleteResult) {
                        return ResponseEntity.ok("File deleted successfully");
                    } else return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("File not found");
                }).onErrorResume(e -> {
                    log.error("Failed to delete file: {}", fileName, e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .build());
                });
    }

    @PutMapping("/renameFile")
    public Mono<ResponseEntity<String>> handleFileRename(@RequestParam("username") String username,
                                                         @RequestParam("fileName") String fileName,
                                                         @RequestParam("newFileName") String newFileName) {
        return fileService.renameOrMoveFile(username, fileName, newFileName)
                .map(renameResult -> {
                    if (renameResult.equals(newFileName)) {
                        return ResponseEntity.ok("File renamed successfully");
                    } else return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("File not found");
                }).onErrorResume(e -> {
                    log.error("Failed to rename file: {}", fileName, e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    private Mono<byte[]> fluxOfDataBufferToByteArray(Flux<DataBuffer> flux) {
        return DataBufferUtils.join(flux).map(dataBuffer -> {
            byte[] bytes = new byte[dataBuffer.readableByteCount()];
            dataBuffer.read(bytes);
            DataBufferUtils.release(dataBuffer);
            return bytes;
        });
    }
}
