package org.vg.markusbro.core.service.plugins.vitals.report;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.vg.markusbro.core.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.stream.Stream;

public class VitalReportGenerator {

    public static void generateReport(File outputFile, VitalReportParams params) throws IOException, DocumentException {
        final Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(outputFile));

        document.open();

        addTitle(document, params.getReportTitle());
        addMetadata(document, params);
        addRecords(document, params);

        document.close();
    }

    private static void addTitle(Document document, String text) throws DocumentException {
        final Paragraph header = new Paragraph(text, FontFactory.getFont(FontFactory.TIMES_BOLD, 32, BaseColor.BLACK));
        document.add(header);
    }

    private static void addSubtitle(Document document, String text) throws DocumentException {
        final Paragraph metadata = new Paragraph(text, FontFactory.getFont(FontFactory.TIMES, 16, BaseColor.GRAY));
        metadata.setSpacingBefore(10);
        metadata.setSpacingAfter(20);
        document.add(metadata);
    }

    private static String formatDateTime(Date time) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(time);
    }

    private static void addMetadata(Document document, VitalReportParams params) throws DocumentException {
        addSubtitle(document, "Metadata");

        final PdfPTable table = new PdfPTable(2);
        final SimpleDateFormat dateFmt = Utils.getDateFormatter();

        table.addCell("Patient ID");
        table.addCell("@" + params.getUserName() + " (" + params.getUserId() + ")");
        table.addCell("Report generated at");
        table.addCell(formatDateTime(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant())));
        table.addCell("Effective period");
        table.addCell(dateFmt.format(params.getReportPeriodStart()) + " – " + dateFmt.format(params.getReportPeriodEnd()));
        table.addCell("Average value");
        table.addCell(params.getAverageValue());
        table.addCell("Min value");
        table.addCell(params.getMinValue().value() + " (" + formatDateTime(params.getMinValue().time()) + ")");
        table.addCell("Max value");
        table.addCell(params.getMaxValue().value() + " (" + formatDateTime(params.getMaxValue().time()) + ")");

        document.add(table);
    }

    private static void addRecords(Document document, VitalReportParams params) throws DocumentException {
        addSubtitle(document, "Records");
        final PdfPTable table = new PdfPTable(2);

        Stream.of("Time", "Value")
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setPhrase(new Phrase(columnTitle));
                    table.addCell(header);
                });

        params.getData().forEach(e -> {
            table.addCell(formatDateTime(e.time()));

            if (e.value().equals(params.getMinValue().value()) || e.value().equals(params.getMaxValue().value())) {
                table.addCell(new PdfPCell(new Phrase(e.value(), new Font(Font.FontFamily.UNDEFINED, Font.UNDEFINED, Font.BOLD, null))));
            } else {
                table.addCell(e.value());
            }
        });

        document.add(table);
    }
}
