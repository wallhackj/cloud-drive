package com.wallhack.clouddrive.file_and_folder_manager.entity;

import java.io.InputStream;

public record FileInfo(String key, String lastModified, InputStream file) {}
