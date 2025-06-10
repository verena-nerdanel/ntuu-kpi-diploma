package org.vg.markusbro.core.service.plugins.vitals.report;

import com.itextpdf.text.DocumentException;
import org.junit.Ignore;
import org.junit.Test;
import org.vg.markusbro.core.service.plugins.vitals.TemporalVital;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VitalReportGeneratorTest {

    @Test
    @Ignore
    public void shouldGenerateReport() throws DocumentException, IOException {
        // given
        final Path outputFile = Paths.get("temp.pdf");
        final VitalReportParams params = VitalReportParams.builder()
                .userId(1234L)
                .userName("John Smith")
                .reportTitle("Blood glucose")
                .reportPeriodStart(new java.sql.Date(2025 - 1900, 4 + 1, 1))
                .reportPeriodEnd(new java.sql.Date(2025 - 1900, 4 + 1, 30))
                .averageValue("95 mg/dL")
                .minValue(new TemporalVital(new Date(), "36 mg/dL"))
                .maxValue(new TemporalVital(new Date(), "270 mg/dL"))
                .data(Arrays.asList(
                        new TemporalVital(new Date(), "40 mg/dL"),
                        new TemporalVital(new Date(), "270 mg/dL"),
                        new TemporalVital(new Date(), "50 mg/dL"),
                        new TemporalVital(new Date(), "36 mg/dL"),
                        new TemporalVital(new Date(), "60 mg/dL")
                ))
                .build();

        Files.deleteIfExists(outputFile);
        assertFalse(Files.exists(outputFile));

        // when
        VitalReportGenerator.generateReport(outputFile.toFile(), params);

        // then
        assertTrue(Files.exists(outputFile));
    }
}
