package de.co2management.backend;

import de.co2management.backend.dto.BenutzerRequest;
import de.co2management.backend.dto.BenutzerResponse;
import de.co2management.backend.dto.BerichtRequest;
import de.co2management.backend.dto.StandortRequest;
import de.co2management.backend.dto.StandortResponse;
import de.co2management.backend.dto.StandortStatistikResponse;
import de.co2management.backend.dto.ZeitreihenPunkt;
import de.co2management.backend.entity.Benutzer;
import de.co2management.backend.entity.Bericht;
import de.co2management.backend.entity.EmissionEintrag;
import de.co2management.backend.entity.Standort;
import de.co2management.backend.enums.KategorieEnum;
import de.co2management.backend.enums.RolleEnum;
import de.co2management.backend.repository.BenutzerRepository;
import de.co2management.backend.repository.BerichtRepository;
import de.co2management.backend.repository.EmissionEintragRepository;
import de.co2management.backend.repository.StandortRepository;
import de.co2management.backend.service.BenutzerService;
import de.co2management.backend.service.BerichtService;
import de.co2management.backend.service.EmissionService;
import de.co2management.backend.service.PdfExportService;
import de.co2management.backend.service.StandortService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-Tests der Geschaeftslogikschicht des CO2-Management-Systems.
 *
 * Getestet werden die Service-Klassen isoliert von Datenbank und Webschicht.
 * Die Repositories werden als Mock-Objekte bereitgestellt, sodass jeder Test
 * ausschliesslich das Verhalten der jeweiligen Service-Methode prueft.
 *
 * Ausfuehrung: mvnw test   bzw.   mvnw -Dtest=CO2ManagementUnitTests test
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CO2ManagementUnitTests {

    @Mock
    private EmissionEintragRepository emissionRepo;
    @Mock
    private StandortRepository standortRepo;
    @Mock
    private BenutzerRepository benutzerRepo;
    @Mock
    private BerichtRepository berichtRepo;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private StandortService standortServiceMock;
    @Mock
    private EmissionService emissionServiceMock;

    private EmissionService emissionService;
    private StandortService standortService;
    private BenutzerService benutzerService;
    private BerichtService berichtService;
    private PdfExportService pdfExportService;

    private Standort standortA;
    private Standort standortB;

    @BeforeEach
    void setUp() {
        emissionService = new EmissionService(emissionRepo);
        standortService = new StandortService(standortRepo);
        benutzerService = new BenutzerService(benutzerRepo, passwordEncoder, standortServiceMock);
        berichtService = new BerichtService(berichtRepo, emissionServiceMock, standortServiceMock, benutzerRepo);
        pdfExportService = new PdfExportService(emissionRepo, standortRepo);

        standortA = standort(1L, "Standort A", "Musterstrasse 1, 80331 Muenchen");
        standortB = standort(2L, "Standort B", "Industrieweg 5, 70173 Stuttgart");
    }

    // ------------------------------------------------------------------
    // Testdaten-Hilfsmethoden
    // ------------------------------------------------------------------

    private Standort standort(Long id, String name, String adresse) {
        Standort s = new Standort();
        s.setId(id);
        s.setName(name);
        s.setAdresse(adresse);
        s.setErstelltAm(LocalDateTime.now());
        return s;
    }

    private Benutzer benutzer(Long id, String username, RolleEnum rolle, Standort standort) {
        Benutzer b = new Benutzer();
        b.setId(id);
        b.setUsername(username);
        b.setEmail(username + "@co2.de");
        b.setPasswort("$2a$10$verschluesselt");
        b.setRolle(rolle);
        b.setAktiv(true);
        b.setErstelltAm(LocalDateTime.now());
        b.setStandort(standort);
        return b;
    }

    private EmissionEintrag eintrag(Long id, KategorieEnum kategorie, Double wert,
                                    LocalDate datum, Standort standort) {
        EmissionEintrag e = new EmissionEintrag();
        e.setId(id);
        e.setKategorie(kategorie);
        e.setWertCo2Kg(wert);
        e.setDatum(datum);
        e.setBeschreibung("Testeintrag " + id);
        e.setErfasstAm(LocalDateTime.now());
        e.setStandort(standort);
        return e;
    }

    // ==================================================================
    // 1. EmissionService
    // ==================================================================

    @Test
    @DisplayName("T01 - calcGesamtemission summiert alle CO2-Werte eines Standorts")
    void calcGesamtemissionSummiertAlleWerte() {
        when(emissionRepo.findByStandortId(1L)).thenReturn(List.of(
                eintrag(1L, KategorieEnum.STROMVERBRAUCH, 120.5, LocalDate.of(2026, 1, 15), standortA),
                eintrag(2L, KategorieEnum.FUHRPARK, 79.5, LocalDate.of(2026, 2, 10), standortA)));

        Double summe = emissionService.calcGesamtemission(1L);

        assertEquals(200.0, summe, 0.0001);
    }

    @Test
    @DisplayName("T02 - calcGesamtemission behandelt fehlende Werte als 0 kg")
    void calcGesamtemissionIgnoriertNullWerte() {
        when(emissionRepo.findByStandortId(1L)).thenReturn(List.of(
                eintrag(1L, KategorieEnum.STROMVERBRAUCH, 100.0, LocalDate.of(2026, 1, 15), standortA),
                eintrag(2L, KategorieEnum.SONSTIGES, null, LocalDate.of(2026, 1, 20), standortA)));

        assertEquals(100.0, emissionService.calcGesamtemission(1L), 0.0001);
    }

    @Test
    @DisplayName("T03 - calcGesamtemission liefert 0 kg, wenn keine Eintraege vorliegen")
    void calcGesamtemissionOhneEintraege() {
        when(emissionRepo.findByStandortId(99L)).thenReturn(List.of());

        assertEquals(0.0, emissionService.calcGesamtemission(99L), 0.0001);
    }

    @Test
    @DisplayName("T04 - findGefiltert erzwingt fuer die Rolle BENUTZER den eigenen Standort")
    void findGefiltertErzwingtEigenenStandortFuerBenutzer() {
        Benutzer standardbenutzer = benutzer(10L, "benutzer", RolleEnum.BENUTZER, standortA);
        when(emissionRepo.findByStandortId(1L)).thenReturn(List.of(
                eintrag(1L, KategorieEnum.FUHRPARK, 50.0, LocalDate.of(2026, 3, 1), standortA)));

        // Der Benutzer fragt bewusst den fremden Standort B (ID 2) an
        List<EmissionEintrag> ergebnis = emissionService.findGefiltert(2L, null, null, standardbenutzer);

        assertEquals(1, ergebnis.size());
        assertEquals("Standort A", ergebnis.get(0).getStandort().getName());
        verify(emissionRepo).findByStandortId(1L);
        verify(emissionRepo, never()).findByStandortId(2L);
    }

    @Test
    @DisplayName("T05 - findGefiltert kombiniert Standort und Zeitraum")
    void findGefiltertMitStandortUndZeitraum() {
        Benutzer admin = benutzer(1L, "admin", RolleEnum.ADMINISTRATOR, standortA);
        LocalDate von = LocalDate.of(2026, 1, 1);
        LocalDate bis = LocalDate.of(2026, 3, 31);
        when(emissionRepo.findByStandortIdAndDatumBetween(2L, von, bis)).thenReturn(List.of(
                eintrag(5L, KategorieEnum.GESCHAEFTSREISE, 42.0, LocalDate.of(2026, 2, 2), standortB)));

        List<EmissionEintrag> ergebnis = emissionService.findGefiltert(2L, von, bis, admin);

        assertEquals(1, ergebnis.size());
        verify(emissionRepo).findByStandortIdAndDatumBetween(2L, von, bis);
    }

    @Test
    @DisplayName("T06 - findGefiltert filtert ohne Standortangabe nur nach Zeitraum")
    void findGefiltertNurZeitraum() {
        Benutzer nachhaltigkeit = benutzer(2L, "nachhaltigkeit", RolleEnum.NACHHALTIGKEITSBEAUFTRAGTER, standortA);
        LocalDate von = LocalDate.of(2026, 1, 1);
        LocalDate bis = LocalDate.of(2026, 12, 31);
        when(emissionRepo.findByDatumBetween(von, bis)).thenReturn(List.of(
                eintrag(6L, KategorieEnum.STROMVERBRAUCH, 10.0, LocalDate.of(2026, 5, 5), standortA),
                eintrag(7L, KategorieEnum.STROMVERBRAUCH, 20.0, LocalDate.of(2026, 6, 5), standortB)));

        List<EmissionEintrag> ergebnis = emissionService.findGefiltert(null, von, bis, nachhaltigkeit);

        assertEquals(2, ergebnis.size());
        verify(emissionRepo).findByDatumBetween(von, bis);
    }

    @Test
    @DisplayName("T07 - findGefiltert liefert ohne Filter alle Eintraege")
    void findGefiltertOhneFilter() {
        Benutzer admin = benutzer(1L, "admin", RolleEnum.ADMINISTRATOR, standortA);
        when(emissionRepo.findAll()).thenReturn(List.of(
                eintrag(8L, KategorieEnum.SONSTIGES, 5.0, LocalDate.of(2026, 4, 4), standortA)));

        assertEquals(1, emissionService.findGefiltert(null, null, null, admin).size());
        verify(emissionRepo).findAll();
    }

    @Test
    @DisplayName("T08 - findById wirft eine Ausnahme, wenn der Eintrag nicht existiert")
    void findByIdWirftAusnahmeBeiUnbekannterId() {
        when(emissionRepo.findById(404L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> emissionService.findById(404L));
        assertTrue(ex.getMessage().contains("404"));
    }

    @Test
    @DisplayName("T09 - statistikProStandort summiert die Emissionen je Standort")
    void statistikProStandortGruppiertKorrekt() {
        Benutzer admin = benutzer(1L, "admin", RolleEnum.ADMINISTRATOR, standortA);
        when(emissionRepo.findAll()).thenReturn(List.of(
                eintrag(1L, KategorieEnum.STROMVERBRAUCH, 100.0, LocalDate.of(2026, 1, 1), standortA),
                eintrag(2L, KategorieEnum.FUHRPARK, 50.0, LocalDate.of(2026, 1, 2), standortA),
                eintrag(3L, KategorieEnum.GESCHAEFTSREISE, 25.0, LocalDate.of(2026, 1, 3), standortB)));

        List<StandortStatistikResponse> statistik = emissionService.statistikProStandort(admin);

        assertEquals(2, statistik.size());
        Double summeA = statistik.stream()
                .filter(s -> s.standortName().equals("Standort A"))
                .findFirst().orElseThrow().gesamtCO2();
        Double summeB = statistik.stream()
                .filter(s -> s.standortName().equals("Standort B"))
                .findFirst().orElseThrow().gesamtCO2();
        assertEquals(150.0, summeA, 0.0001);
        assertEquals(25.0, summeB, 0.0001);
    }

    @Test
    @DisplayName("T10 - statistikProStandort zeigt der Rolle BENUTZER nur den eigenen Standort")
    void statistikProStandortIstFuerBenutzerEingeschraenkt() {
        Benutzer standardbenutzer = benutzer(10L, "benutzer", RolleEnum.BENUTZER, standortB);
        when(emissionRepo.findByStandortId(2L)).thenReturn(List.of(
                eintrag(3L, KategorieEnum.GESCHAEFTSREISE, 25.0, LocalDate.of(2026, 1, 3), standortB)));

        List<StandortStatistikResponse> statistik = emissionService.statistikProStandort(standardbenutzer);

        assertEquals(1, statistik.size());
        assertEquals("Standort B", statistik.get(0).standortName());
        verify(emissionRepo, never()).findAll();
    }

    @Test
    @DisplayName("T11 - zeitreihe gruppiert die Emissionen monatsweise und aufsteigend")
    void zeitreiheGruppiertNachMonat() {
        Benutzer fuehrungskraft = benutzer(3L, "fuehrungskraft", RolleEnum.FUEHRUNGSKRAFT, standortA);
        LocalDate von = LocalDate.of(2026, 1, 1);
        LocalDate bis = LocalDate.of(2026, 3, 31);
        when(emissionRepo.findByDatumBetween(von, bis)).thenReturn(List.of(
                eintrag(1L, KategorieEnum.STROMVERBRAUCH, 30.0, LocalDate.of(2026, 3, 15), standortA),
                eintrag(2L, KategorieEnum.STROMVERBRAUCH, 10.0, LocalDate.of(2026, 1, 10), standortA),
                eintrag(3L, KategorieEnum.FUHRPARK, 5.0, LocalDate.of(2026, 1, 20), standortA)));

        List<ZeitreihenPunkt> reihe = emissionService.zeitreihe(null, von, bis, fuehrungskraft);

        assertEquals(2, reihe.size());
        assertEquals("2026-01", reihe.get(0).monat());
        assertEquals(15.0, reihe.get(0).gesamtCO2(), 0.0001);
        assertEquals("2026-03", reihe.get(1).monat());
        assertEquals(30.0, reihe.get(1).gesamtCO2(), 0.0001);
    }

    // ==================================================================
    // 2. StandortService
    // ==================================================================

    @Test
    @DisplayName("T12 - create legt einen Standort an und setzt den Erstellungszeitpunkt")
    void createStandortSetztErstelltAm() {
        when(standortRepo.save(any(Standort.class))).thenAnswer(inv -> inv.getArgument(0));

        StandortResponse response = standortService.create(new StandortRequest("Standort C", "Hafenweg 3, 20457 Hamburg"));

        assertEquals("Standort C", response.name());
        assertEquals("Hafenweg 3, 20457 Hamburg", response.adresse());
        assertNotNull(response.erstelltAm());
    }

    @Test
    @DisplayName("T13 - findById wirft 404, wenn der Standort nicht existiert")
    void findStandortByIdWirft404() {
        when(standortRepo.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> standortService.findById(99L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    @DisplayName("T14 - update aendert Name und Adresse eines bestehenden Standorts")
    void updateStandortAendertFelder() {
        when(standortRepo.findById(1L)).thenReturn(Optional.of(standortA));
        when(standortRepo.save(any(Standort.class))).thenAnswer(inv -> inv.getArgument(0));

        StandortResponse response = standortService.update(1L, new StandortRequest("Werk Sued", "Neue Strasse 9"));

        assertEquals("Werk Sued", response.name());
        assertEquals("Neue Strasse 9", response.adresse());
    }

    @Test
    @DisplayName("T15 - deleteById wirft 404 statt kommentarlos nichts zu tun")
    void deleteStandortWirft404() {
        when(standortRepo.existsById(99L)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> standortService.deleteById(99L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(standortRepo, never()).deleteById(99L);
    }

    // ==================================================================
    // 3. BenutzerService
    // ==================================================================

    @Test
    @DisplayName("T16 - Neuanlage speichert das Passwort ausschliesslich als Hash")
    void createBenutzerVerschluesseltPasswort() {
        when(standortServiceMock.findById(1L)).thenReturn(standortA);
        when(passwordEncoder.encode("test123")).thenReturn("$2a$10$gehasht");
        when(benutzerRepo.save(any(Benutzer.class))).thenAnswer(inv -> inv.getArgument(0));

        BenutzerRequest req = new BenutzerRequest("neu", "neu@co2.de", "test123", RolleEnum.BENUTZER, 1L, true);
        BenutzerResponse response = benutzerService.create(req);

        verify(passwordEncoder).encode("test123");
        assertEquals("neu", response.username());
        assertEquals("Standort A", response.standortName());
        assertTrue(response.aktiv());
    }

    @Test
    @DisplayName("T17 - Neuanlage ohne Passwort wird mit 400 abgelehnt")
    void createBenutzerOhnePasswortWirdAbgelehnt() {
        BenutzerRequest req = new BenutzerRequest("neu", "neu@co2.de", "  ", RolleEnum.BENUTZER, 1L, true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> benutzerService.create(req));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(benutzerRepo, never()).save(any(Benutzer.class));
    }

    @Test
    @DisplayName("T18 - Bearbeiten ohne Passworteingabe laesst das bestehende Passwort unveraendert")
    void updateBenutzerBehaeltPasswortBeiLeeremFeld() {
        Benutzer bestehend = benutzer(10L, "benutzer", RolleEnum.BENUTZER, standortA);
        String altesPasswort = bestehend.getPasswort();
        when(benutzerRepo.findById(10L)).thenReturn(Optional.of(bestehend));
        when(standortServiceMock.findById(1L)).thenReturn(standortA);
        when(benutzerRepo.save(any(Benutzer.class))).thenAnswer(inv -> inv.getArgument(0));

        BenutzerRequest req = new BenutzerRequest("benutzer", "neu@co2.de", null, RolleEnum.FUEHRUNGSKRAFT, 1L, true);
        BenutzerResponse response = benutzerService.update(10L, req);

        assertEquals(altesPasswort, bestehend.getPasswort());
        assertEquals(RolleEnum.FUEHRUNGSKRAFT, response.rolle());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("T19 - loadUserByUsername liefert die Rolle als Spring-Security-Autoritaet")
    void loadUserByUsernameLiefertRolle() {
        when(benutzerRepo.findByUsername("admin"))
                .thenReturn(Optional.of(benutzer(1L, "admin", RolleEnum.ADMINISTRATOR, standortA)));

        UserDetails details = benutzerService.loadUserByUsername("admin");

        assertEquals("admin", details.getUsername());
        assertTrue(details.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRATOR")));
    }

    @Test
    @DisplayName("T20 - loadUserByUsername wirft eine Ausnahme bei unbekanntem Benutzernamen")
    void loadUserByUsernameWirftAusnahme() {
        when(benutzerRepo.findByUsername("unbekannt")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> benutzerService.loadUserByUsername("unbekannt"));
    }

    // ==================================================================
    // 4. BerichtService
    // ==================================================================

    @Test
    @DisplayName("T21 - createBericht uebernimmt die berechnete Gesamtemission des Standorts")
    void createBerichtUebernimmtGesamtemission() {
        when(benutzerRepo.findByUsername("nachhaltigkeit"))
                .thenReturn(Optional.of(benutzer(2L, "nachhaltigkeit", RolleEnum.NACHHALTIGKEITSBEAUFTRAGTER, standortA)));
        when(emissionServiceMock.calcGesamtemission(1L)).thenReturn(275.5);
        when(standortServiceMock.findById(1L)).thenReturn(standortA);
        when(berichtRepo.save(any(Bericht.class))).thenAnswer(inv -> inv.getArgument(0));

        BerichtRequest req = new BerichtRequest(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), 1L);
        Bericht bericht = berichtService.createBericht(req, "nachhaltigkeit");

        assertEquals(275.5, bericht.getGesamtCo2Kg(), 0.0001);
        assertEquals("Standort A", bericht.getStandort().getName());
        assertEquals("nachhaltigkeit", bericht.getErstelltVon().getUsername());
    }

    @Test
    @DisplayName("T22 - Ein Benutzer darf keinen Bericht eines fremden Standorts oeffnen")
    void getBerichtVerweigertFremdenStandort() {
        Bericht fremderBericht = new Bericht();
        fremderBericht.setId(7L);
        fremderBericht.setStandort(standortB);
        when(berichtRepo.findById(7L)).thenReturn(Optional.of(fremderBericht));

        Benutzer standardbenutzer = benutzer(10L, "benutzer", RolleEnum.BENUTZER, standortA);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> berichtService.getBericht(7L, standardbenutzer));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    @DisplayName("T23 - Ein Benutzer darf den Bericht des eigenen Standorts oeffnen")
    void getBerichtErlaubtEigenenStandort() {
        Bericht eigenerBericht = new Bericht();
        eigenerBericht.setId(8L);
        eigenerBericht.setStandort(standortA);
        when(berichtRepo.findById(8L)).thenReturn(Optional.of(eigenerBericht));

        Benutzer standardbenutzer = benutzer(10L, "benutzer", RolleEnum.BENUTZER, standortA);

        assertEquals(8L, berichtService.getBericht(8L, standardbenutzer).getId());
    }

    @Test
    @DisplayName("T24 - findAll liefert der Rolle BENUTZER nur Berichte des eigenen Standorts")
    void findAllBerichteIstFuerBenutzerEingeschraenkt() {
        Benutzer standardbenutzer = benutzer(10L, "benutzer", RolleEnum.BENUTZER, standortA);
        when(berichtRepo.findByStandortId(1L)).thenReturn(List.of(new Bericht()));

        assertEquals(1, berichtService.findAll(standardbenutzer).size());
        verify(berichtRepo).findByStandortId(1L);
        verify(berichtRepo, never()).findAll();
    }

    // ==================================================================
    // 5. PdfExportService
    // ==================================================================

    @Test
    @DisplayName("T25 - Der PDF-Export erzeugt ein gueltiges PDF-Dokument")
    void pdfExportErzeugtGueltigesPdf() {
        LocalDate von = LocalDate.of(2026, 1, 1);
        LocalDate bis = LocalDate.of(2026, 12, 31);
        when(standortRepo.findById(1L)).thenReturn(Optional.of(standortA));
        when(emissionRepo.findByStandortIdAndDatumBetween(1L, von, bis)).thenReturn(List.of(
                eintrag(1L, KategorieEnum.STROMVERBRAUCH, 120.5, LocalDate.of(2026, 2, 1), standortA)));

        byte[] pdf = pdfExportService.exportEmissionsPdf(1L, von, bis);

        assertNotNull(pdf);
        assertFalse(pdf.length == 0);
        assertTrue(new String(pdf, 0, 4, StandardCharsets.ISO_8859_1).equals("%PDF"));
    }

    @Test
    @DisplayName("T26 - Der PDF-Export funktioniert auch ohne Eintraege im Zeitraum")
    void pdfExportOhneEintraege() {
        LocalDate von = LocalDate.of(2026, 1, 1);
        LocalDate bis = LocalDate.of(2026, 1, 31);
        when(emissionRepo.findByDatumBetween(von, bis)).thenReturn(List.of());

        byte[] pdf = pdfExportService.exportEmissionsPdf(null, von, bis);

        assertTrue(pdf.length > 0);
    }

    @Test
    @DisplayName("T27 - Der PDF-Export bricht bei unbekanntem Standort kontrolliert ab")
    void pdfExportWirftBeiUnbekanntemStandort() {
        when(standortRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> pdfExportService.exportEmissionsPdf(99L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31)));
    }

    // ==================================================================
    // 6. Domaenenmodell
    // ==================================================================

    @Test
    @DisplayName("T28 - Das Rollenmodell umfasst genau die vier fachlich vorgesehenen Rollen")
    void rollenModellIstVollstaendig() {
        assertEquals(4, RolleEnum.values().length);
        assertEquals(RolleEnum.ADMINISTRATOR, RolleEnum.valueOf("ADMINISTRATOR"));
        assertEquals(RolleEnum.NACHHALTIGKEITSBEAUFTRAGTER, RolleEnum.valueOf("NACHHALTIGKEITSBEAUFTRAGTER"));
        assertEquals(RolleEnum.FUEHRUNGSKRAFT, RolleEnum.valueOf("FUEHRUNGSKRAFT"));
        assertEquals(RolleEnum.BENUTZER, RolleEnum.valueOf("BENUTZER"));
    }

    @Test
    @DisplayName("T29 - Die Emissionskategorien bilden die Quellen des Product Backlogs ab")
    void kategorienSindVollstaendig() {
        assertEquals(4, KategorieEnum.values().length);
        assertEquals(KategorieEnum.GESCHAEFTSREISE, KategorieEnum.valueOf("GESCHAEFTSREISE"));
        assertEquals(KategorieEnum.STROMVERBRAUCH, KategorieEnum.valueOf("STROMVERBRAUCH"));
        assertEquals(KategorieEnum.FUHRPARK, KategorieEnum.valueOf("FUHRPARK"));
        assertEquals(KategorieEnum.SONSTIGES, KategorieEnum.valueOf("SONSTIGES"));
    }

    @Test
    @DisplayName("T30 - save delegiert den Emissionseintrag unveraendert an das Repository")
    void saveDelegiertAnRepository() {
        EmissionEintrag neu = eintrag(null, KategorieEnum.GESCHAEFTSREISE, 33.3, LocalDate.of(2026, 7, 1), standortA);
        when(emissionRepo.save(neu)).thenReturn(neu);

        EmissionEintrag gespeichert = emissionService.save(neu);

        assertEquals(33.3, gespeichert.getWertCo2Kg(), 0.0001);
        verify(emissionRepo).save(eq(neu));
    }
}
