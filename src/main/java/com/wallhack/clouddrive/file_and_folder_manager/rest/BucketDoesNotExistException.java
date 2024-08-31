package com.wallhack.clouddrive.file_and_folder_manager.rest;

import software.amazon.awssdk.services.s3.model.S3Exception;

public class BucketDoesNotExistException extends S3Exception {
    public BucketDoesNotExistException(String message) {
        super(S3Exception.builder()
                .statusCode(404)
                .message(message));
    }
}
