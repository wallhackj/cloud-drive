package com.wallhack.clouddrive.file_and_folder_manager.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
public class ZipDataBuffer {

    public static Mono<byte[]> toZipFlux(List<DataBuffer> dataBuffers, List<String> filenames) {
        return Mono.fromCallable(() -> {
            try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                 ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {

                Iterator<String> filenameIterator = filenames.iterator();

                for (DataBuffer dataBuffer : dataBuffers) {
                    if (filenameIterator.hasNext()) {
                        String filename = filenameIterator.next();
                        ZipEntry zipEntry = new ZipEntry(filename);
                        zipOutputStream.putNextEntry(zipEntry);
                        int byteBuffer = dataBuffer.readableByteCount();
                        byte[] bytes = new byte[byteBuffer];
                        dataBuffer.read(bytes);
                        zipOutputStream.write(bytes);
                        zipOutputStream.closeEntry();

                        DataBufferUtils.release(dataBuffer);
                    }
                }
                zipOutputStream.finish();

                return byteArrayOutputStream.toByteArray();
            } catch (Exception e) {
                log.error("Failed to zip data buffer:", e);
                throw new RuntimeException("Failed to zip files", e);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
