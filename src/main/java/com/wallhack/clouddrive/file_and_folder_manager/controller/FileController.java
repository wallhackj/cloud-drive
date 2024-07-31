package com.wallhack.clouddrive.file_and_folder_manager.controller;

import com.wallhack.clouddrive.file_and_folder_manager.service.FileStorageService;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Controller()
@AllArgsConstructor
public class FileController {
    private FileStorageService fileService;

    @GetMapping("/upload")
    public String showUploadForm() {
        return "upload";
    }

    //username min 3 ,max 63

    @PostMapping("/upload")
    public Mono<ResponseEntity<String>> handleFileUpload(@RequestParam("username") String username,
                                                         @RequestParam("file") MultipartFile file){
        return Mono.fromFuture(() -> fileService.uploadFile(username, file))
                .flatMap(uploadResult -> Mono.just(ResponseEntity.ok("File uploaded successfully")))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("File upload failed: " + e.getMessage())));

    }

    @GetMapping("/download")
    public Mono<ResponseEntity<byte[]>> handleFileDownload(@RequestParam("username") String username,
                                                           @RequestParam("fileName") String fileName) {
        return Mono.fromFuture(() -> fileService.downloadFile(username, fileName))
                .flatMap(this::fluxOfDataBufferToByteArray)
                .map(bytes -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                        .body(bytes))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

    @DeleteMapping("/delete")
    public Mono<ResponseEntity<String>> handleFileDelete(@RequestParam("username") String username,
                                                         @RequestParam("fileName") String fileName) {
        return Mono.fromFuture(() -> fileService.deleteFile(username, fileName))
                .map(deleteResult -> {
                    if (deleteResult) {
                        return ResponseEntity.ok("File deleted successfully");
                    } else return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found");

                })
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting file: " + e.getMessage())));
    }

    @PutMapping("/rename")
    public Mono<ResponseEntity<String>> handleFileRename(@RequestParam("username") String username,
                                                         @RequestParam("fileName") String fileName,
                                                         @RequestParam("newFileName") String newFileName) {
        return Mono.fromFuture(() -> fileService.renameFile(username, fileName, newFileName))
                .map(renameResult -> {
                    if (renameResult.equals(newFileName)){
                        return ResponseEntity.ok("File renamed successfully");
                    }else return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found");
                })
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }


    private @NotNull Mono<byte[]> fluxOfDataBufferToByteArray(Flux<DataBuffer> flux) {
        return DataBufferUtils.join(flux)
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return bytes;
                });
    }


}
