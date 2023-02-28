package com.kanwise.report_service.service.report.implementation.personal;

import com.kanwise.report_service.configuration.pdf.PdfConfigurationProperties;
import com.kanwise.report_service.service.pdf.implementation.PdfService;
import com.kanwise.report_service.service.report.common.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static com.kanwise.report_service.model.report.ReportType.PERSONAL;
import static java.nio.file.Files.delete;

@Slf4j

@RequiredArgsConstructor
@Service
public class PersonalReportService implements ReportService {

    private final PdfService pdfService;
    private final PdfConfigurationProperties pdfConfigurationProperties;

    @Override
    public MultipartFile generateReport(Map<String, Object> data, String fileName) {
        pdfService.generatePdf(PERSONAL, data, fileName);
        return getReportAsMultipartFile(fileName, pdfConfigurationProperties.getGeneratedDirectory(PERSONAL));
    }

    @Override
    public void removeReport(String fileName) throws IOException {
        Path path = Paths.get(pdfConfigurationProperties.getGeneratedDirectory(PERSONAL) + fileName);
        delete(path);
    }
}
