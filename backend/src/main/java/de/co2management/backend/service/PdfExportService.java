package de.co2management.backend.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import de.co2management.backend.entity.EmissionEintrag;
import de.co2management.backend.entity.Standort;
import de.co2management.backend.repository.EmissionEintragRepository;
import de.co2management.backend.repository.StandortRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PdfExportService {

    private final EmissionEintragRepository emissionEintragRepository;
    private final StandortRepository standortRepository;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public byte[] exportEmissionsPdf(Long standortId, LocalDate von, LocalDate bis) {

        List<EmissionEintrag> eintraege;
        String standortName;

        if (standortId != null) {
            Standort standort = standortRepository.findById(standortId)
                .orElseThrow(() -> new IllegalArgumentException("Standort mit ID " + standortId + " nicht gefunden"));
            standortName = standort.getName();
            eintraege = emissionEintragRepository.findByStandortIdAndDatumBetween(standortId, von, bis);
        } else {
            standortName = "Alle Standorte";
            eintraege = emissionEintragRepository.findByDatumBetween(von, bis);
        }

        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font sumFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

            Paragraph title = new Paragraph("CO2-Emissionsbericht", titleFont);
            title.setSpacingAfter(8);
            document.add(title);

            Paragraph meta = new Paragraph();
            meta.setFont(metaFont);
            meta.add("Standort: " + standortName + "\n");
            meta.add("Zeitraum: " + von.format(DATE_FORMAT) + " \u2013 " + bis.format(DATE_FORMAT) + "\n");
            meta.add("Erstellt am: " + LocalDate.now().format(DATE_FORMAT));
            meta.setSpacingAfter(16);
            document.add(meta);

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2f, 3f, 2f, 5f});
            table.setHeaderRows(1);

            addHeaderCell(table, "Datum", headerFont);
            addHeaderCell(table, "Kategorie", headerFont);
            addHeaderCell(table, "CO2 (kg)", headerFont);
            addHeaderCell(table, "Beschreibung", headerFont);

            double summe = 0.0;

            for (EmissionEintrag e : eintraege) {
                table.addCell(new PdfPCell(new Paragraph(e.getDatum().format(DATE_FORMAT), cellFont)));
                table.addCell(new PdfPCell(new Paragraph(e.getKategorie().name(), cellFont)));

                double wert = e.getWertCo2Kg() != null ? e.getWertCo2Kg() : 0.0;
                summe += wert;

                PdfPCell wertCell = new PdfPCell(new Paragraph(String.format(Locale.GERMANY, "%.2f", wert), cellFont));
                wertCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(wertCell);

                table.addCell(new PdfPCell(new Paragraph(
                    e.getBeschreibung() != null ? e.getBeschreibung() : "", cellFont)));
            }

            if (eintraege.isEmpty()) {
                PdfPCell emptyCell = new PdfPCell(new Paragraph("Keine Emissionseinträge im gewählten Zeitraum", cellFont));
                emptyCell.setColspan(4);
                emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(emptyCell);
            }

            PdfPCell sumLabelCell = new PdfPCell(new Paragraph("Summe", sumFont));
            sumLabelCell.setColspan(2);
            sumLabelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(sumLabelCell);

            PdfPCell sumValueCell = new PdfPCell(new Paragraph(String.format(Locale.GERMANY, "%.2f", summe), sumFont));
            sumValueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(sumValueCell);

            table.addCell(new PdfPCell(new Paragraph("")));

            document.add(table);
            document.close();

        } catch (DocumentException ex) {
            throw new RuntimeException("Fehler beim Erstellen des PDF-Berichts", ex);
        }

        return out.toByteArray();
    }

    private void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(new Color(230, 230, 230));
        table.addCell(cell);
    }
}