package com.kanwise.report_service.service.report.common;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;

public interface ReportService {

    MultipartFile generateReport(Map<String, Object> data, String reportFileName);

    void removeReport(String reportFileName) throws IOException;

    default MultipartFile getReportAsMultipartFile(String fileName, String directory) {
        Path path = Paths.get(directory, fileName);
        String originalFileName = path.getFileName().toString();
        byte[] content;
        try {
            content = Files.readAllBytes(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return new MockMultipartFile(fileName, originalFileName, APPLICATION_PDF_VALUE, content);
    }
}
