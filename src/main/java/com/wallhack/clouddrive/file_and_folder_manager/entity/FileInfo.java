package com.wallhack.clouddrive.file_and_folder_manager.entity;

import org.springframework.web.multipart.MultipartFile;

public record FileInfo(String key, String lastModified, MultipartFile file) {}
