package de.co2management.backend.service;

import de.co2management.backend.dto.BerichtRequest;
import de.co2management.backend.entity.Bericht;
import de.co2management.backend.entity.Benutzer;
import de.co2management.backend.enums.RolleEnum;
import de.co2management.backend.repository.BenutzerRepository;
import de.co2management.backend.repository.BerichtRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BerichtService {

    private final BerichtRepository berichtRepo;
    private final EmissionService emissionService;
    private final StandortService standortService;
    private final BenutzerRepository benutzerRepo;

    public Bericht createBericht(BerichtRequest req, String username) {
        Benutzer ersteller = benutzerRepo.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Benutzer nicht gefunden"));

        Bericht b = new Bericht();
        b.setZeitraumVon(req.zeitraumVon());
        b.setZeitraumBis(req.zeitraumBis());
        b.setGesamtCo2Kg(emissionService.calcGesamtemission(req.standortId()));
        b.setErstelltAm(LocalDate.now());
        b.setStandort(standortService.findById(req.standortId()));
        b.setErstelltVon(ersteller);
        return berichtRepo.save(b);
    }

    public Bericht getBericht(Long id, Benutzer aktuellerBenutzer) {
        Bericht bericht = berichtRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Bericht nicht gefunden: " + id));
        pruefeZugriff(bericht, aktuellerBenutzer);
        return bericht;
    }

    // Platzhalter – PDF-Generierung kann mit iText oder JasperReports ergänzt werden
    public byte[] generatePdf(Long berichtId, Benutzer aktuellerBenutzer) {
        // getBericht prüft bereits den Zugriff und wirft ggf. 403
        getBericht(berichtId, aktuellerBenutzer);
        return new byte[0];
    }

    public List<Bericht> findAll(Benutzer aktuellerBenutzer) {
        if (aktuellerBenutzer.getRolle() == RolleEnum.BENUTZER) {
            return berichtRepo.findByStandortId(aktuellerBenutzer.getStandort().getId());
        }
        return berichtRepo.findAll();
    }

    private void pruefeZugriff(Bericht bericht, Benutzer aktuellerBenutzer) {
        if (aktuellerBenutzer.getRolle() != RolleEnum.BENUTZER) {
            return;
        }
        Long eigenerStandortId = aktuellerBenutzer.getStandort() != null
            ? aktuellerBenutzer.getStandort().getId()
            : null;
        Long berichtStandortId = bericht.getStandort() != null
            ? bericht.getStandort().getId()
            : null;

        if (eigenerStandortId == null || !eigenerStandortId.equals(berichtStandortId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Kein Zugriff auf diesen Bericht");
        }
    }
}