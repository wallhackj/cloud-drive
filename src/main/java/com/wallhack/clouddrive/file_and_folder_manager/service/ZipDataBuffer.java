package com.wallhack.clouddrive.file_and_folder_manager.service;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
                        ByteBuffer byteBuffer = dataBuffer.asByteBuffer();
                        byte[] bytes = new byte[byteBuffer.remaining()];
                        byteBuffer.get(bytes);
                        zipOutputStream.write(bytes);
                        zipOutputStream.closeEntry();
                    }
                }
                zipOutputStream.finish();
                return byteArrayOutputStream.toByteArray();
            } catch (Exception e) {
                throw new RuntimeException("Failed to zip files", e);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
