package com.ecom.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {

    Boolean uploadFileS3(MultipartFile file, Integer bucketType) throws IOException;
}
