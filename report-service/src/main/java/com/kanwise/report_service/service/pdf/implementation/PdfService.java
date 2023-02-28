package com.kanwise.report_service.service.pdf.implementation;

import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.kanwise.report_service.configuration.pdf.PdfConfigurationProperties;
import com.kanwise.report_service.model.report.ReportType;
import com.kanwise.report_service.service.pdf.common.IPdfService;
import com.kanwise.report_service.service.template.common.IHtmlTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class PdfService implements IPdfService {

    private final PdfConfigurationProperties pdfConfigurationProperties;
    private final IHtmlTemplateService htmlTemplateService;

    @Override
    public void generatePdf(ReportType reportType, Map<String, Object> data, String fileName) {
        try (PdfDocument pdfDocument = new PdfDocument(
                new PdfWriter(
                        new File(pdfConfigurationProperties.paths().get(reportType) + fileName)
                ))) {
            String html = htmlTemplateService.generateHtml(data, reportType);
            pdfDocument.setDefaultPageSize(PageSize.A4.rotate());
            HtmlConverter.convertToPdf(html, pdfDocument.getWriter());
        } catch (Exception exception) {
            log.error("Error while generating pdf", exception);
        }
    }
}
