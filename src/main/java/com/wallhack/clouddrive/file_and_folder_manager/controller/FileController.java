package com.wallhack.clouddrive.file_and_folder_manager.controller;

import com.wallhack.clouddrive.file_and_folder_manager.entity.FileInfo;
import com.wallhack.clouddrive.file_and_folder_manager.service.FileStorageService;
import lombok.AllArgsConstructor;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.DataBufferDecoder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;

@Controller
@AllArgsConstructor
public class FileController {
    private FileStorageService fileService;

    @GetMapping("/file")
    public String showUploadForm() {
        return "file_test";
    }

    //username min 3 ,max 63

    @PostMapping("/uploadFile")
    public Mono<ResponseEntity<String>> handleFileUpload(@RequestParam("username") String username,
                                                         @RequestParam("file") MultipartFile file){
        return Mono.fromFuture(() -> {
                    try {
                        return fileService.uploadFile(username, new FileInfo(file.getOriginalFilename(),
                                        "never", file.getInputStream()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .flatMap(uploadResult -> Mono.just(ResponseEntity.ok("File uploaded successfully")))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("File upload failed: " + e.getMessage())));

    }

    @GetMapping("/downloadFile")
    public Mono<ResponseEntity<Flux<DataBuffer>>> handleFileDownload(@RequestParam("username") String username,
                                                                     @RequestParam("fileName") String fileName) {
        DataBufferDecoder dataBufferDecoder = new DataBufferDecoder();

        return Mono.fromFuture(() -> fileService.downloadFile(username, fileName))
                .flatMapMany(flux -> flux)  // Flatten the CompletableFuture<Flux<DataBuffer>> to Flux<DataBuffer>
                .transform(dataBufferFlux -> dataBufferDecoder.decode(dataBufferFlux, ResolvableType.forClass(DataBuffer.class), null, null))  // Decode the DataBuffer Flux
                .collectList()  // Collect the DataBuffer instances into a List<DataBuffer>
                .map(decodedDataBuffers -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                        .body(Flux.fromIterable(decodedDataBuffers)))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }


    @DeleteMapping("/deleteFile")
    public Mono<ResponseEntity<String>> handleFileDelete(@RequestParam("username") String username,
                                                         @RequestParam("fileName") String fileName) {

        return Mono.fromFuture(() -> fileService.deleteFile(username, fileName))
                .map(deleteResult -> {
                    if (deleteResult) {
                        return ResponseEntity.ok("File deleted successfully");
                    } else return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found");

                })
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.status(
                                HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting file: " + e.getMessage())));
    }

    @PutMapping("/renameFile")
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

    @GetMapping("/listFiles")
    public Mono<ResponseEntity<List<FileInfo>>> handleFileList(@RequestParam("username") String username) {
        return Mono.fromFuture(() -> fileService.listAllFiles(username))
                .map(files -> ResponseEntity.ok().body(files))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

//
//    private @NotNull Mono<byte[]> fluxOfDataBufferToByteArray(Flux<DataBuffer> flux) {
//        return DataBufferUtils.join(flux)
//                .map(dataBuffer -> {
//                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
//                    dataBuffer.read(bytes);
//                    DataBufferUtils.release(dataBuffer);
//                    return bytes;
//                });
//    }


}
