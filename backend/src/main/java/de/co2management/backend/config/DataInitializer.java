package de.co2management.backend.config;

import de.co2management.backend.entity.Benutzer;
import de.co2management.backend.entity.EmissionEintrag;
import de.co2management.backend.entity.Standort;
import de.co2management.backend.enums.KategorieEnum;
import de.co2management.backend.enums.RolleEnum;
import de.co2management.backend.repository.BenutzerRepository;
import de.co2management.backend.repository.EmissionEintragRepository;
import de.co2management.backend.repository.StandortRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final BenutzerRepository benutzerRepository;
    private final StandortRepository standortRepository;
    private final EmissionEintragRepository emissionEintragRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (standortRepository.count() == 0) {
            Standort standortA = new Standort();
            standortA.setName("Standort A");
            standortA.setAdresse("Musterstraße 1, 80331 München");
            standortA.setErstelltAm(LocalDateTime.now());
            standortRepository.save(standortA);

            Standort standortB = new Standort();
            standortB.setName("Standort B");
            standortB.setAdresse("Industrieweg 5, 70173 Stuttgart");
            standortB.setErstelltAm(LocalDateTime.now());
            standortRepository.save(standortB);
        }

        if (benutzerRepository.count() == 0) {
            Standort standortA = standortRepository.findAll().get(0);
            Standort standortB = standortRepository.findAll().get(1);

            Benutzer admin = new Benutzer();
            admin.setUsername("admin");
            admin.setEmail("admin@co2.de");
            admin.setPasswort(passwordEncoder.encode("test123"));
            admin.setRolle(RolleEnum.ADMINISTRATOR);
            admin.setAktiv(true);
            admin.setErstelltAm(LocalDateTime.now());
            admin.setStandort(standortA);
            benutzerRepository.save(admin);

            Benutzer nachhaltigkeit = new Benutzer();
            nachhaltigkeit.setUsername("nachhaltigkeit");
            nachhaltigkeit.setEmail("nachhaltigkeit@co2.de");
            nachhaltigkeit.setPasswort(passwordEncoder.encode("test123"));
            nachhaltigkeit.setRolle(RolleEnum.NACHHALTIGKEITSBEAUFTRAGTER);
            nachhaltigkeit.setAktiv(true);
            nachhaltigkeit.setErstelltAm(LocalDateTime.now());
            nachhaltigkeit.setStandort(standortA);
            benutzerRepository.save(nachhaltigkeit);

            Benutzer fuehrungskraft = new Benutzer();
            fuehrungskraft.setUsername("fuehrungskraft");
            fuehrungskraft.setEmail("fuehrungskraft@co2.de");
            fuehrungskraft.setPasswort(passwordEncoder.encode("test123"));
            fuehrungskraft.setRolle(RolleEnum.FUEHRUNGSKRAFT);
            fuehrungskraft.setAktiv(true);
            fuehrungskraft.setErstelltAm(LocalDateTime.now());
            fuehrungskraft.setStandort(standortB);
            benutzerRepository.save(fuehrungskraft);

            Benutzer benutzer = new Benutzer();
            benutzer.setUsername("benutzer");
            benutzer.setEmail("benutzer@co2.de");
            benutzer.setPasswort(passwordEncoder.encode("test123"));
            benutzer.setRolle(RolleEnum.BENUTZER);
            benutzer.setAktiv(true);
            benutzer.setErstelltAm(LocalDateTime.now());
            benutzer.setStandort(standortB);
            benutzerRepository.save(benutzer);
        }

        if (emissionEintragRepository.count() == 0) {
            Standort standortA = standortRepository.findAll().get(0);
            Standort standortB = standortRepository.findAll().get(1);

            Benutzer admin = benutzerRepository.findByUsername("admin").orElseThrow();
            Benutzer nachhaltigkeit = benutzerRepository.findByUsername("nachhaltigkeit").orElseThrow();
            Benutzer fuehrungskraft = benutzerRepository.findByUsername("fuehrungskraft").orElseThrow();
            Benutzer benutzer = benutzerRepository.findByUsername("benutzer").orElseThrow();

            emissionEintragRepository.save(erstelleEintrag(
                    KategorieEnum.GESCHAEFTSREISE, 245.5, "Dienstreise München-Berlin",
                    LocalDate.of(2026, 5, 5), standortA, nachhaltigkeit));

            emissionEintragRepository.save(erstelleEintrag(
                    KategorieEnum.STROMVERBRAUCH, 1200.0, "Stromverbrauch Produktionshalle April",
                    LocalDate.of(2026, 5, 12), standortB, benutzer));

            emissionEintragRepository.save(erstelleEintrag(
                    KategorieEnum.FUHRPARK, 380.75, "Fuhrpark Auslieferungsfahrten",
                    LocalDate.of(2026, 5, 20), standortA, nachhaltigkeit));

            emissionEintragRepository.save(erstelleEintrag(
                    KategorieEnum.SONSTIGES, 95.0, "Verpackungsmaterial Entsorgung",
                    LocalDate.of(2026, 6, 2), standortB, fuehrungskraft));

            emissionEintragRepository.save(erstelleEintrag(
                    KategorieEnum.STROMVERBRAUCH, 1100.25, "Stromverbrauch Bürogebäude Mai",
                    LocalDate.of(2026, 6, 8), standortA, nachhaltigkeit));

            emissionEintragRepository.save(erstelleEintrag(
                    KategorieEnum.GESCHAEFTSREISE, 512.0, "Kundentermin Hamburg",
                    LocalDate.of(2026, 6, 15), standortB, benutzer));

            emissionEintragRepository.save(erstelleEintrag(
                    KategorieEnum.FUHRPARK, 420.3, "Fuhrpark Werksverkehr",
                    LocalDate.of(2026, 6, 25), standortA, admin));

            emissionEintragRepository.save(erstelleEintrag(
                    KategorieEnum.STROMVERBRAUCH, 1340.6, "Stromverbrauch Produktionshalle Juni",
                    LocalDate.of(2026, 7, 3), standortB, benutzer));

            emissionEintragRepository.save(erstelleEintrag(
                    KategorieEnum.GESCHAEFTSREISE, 298.4, "Messebesuch Frankfurt",
                    LocalDate.of(2026, 7, 10), standortA, nachhaltigkeit));

            emissionEintragRepository.save(erstelleEintrag(
                    KategorieEnum.FUHRPARK, 367.9, "Fuhrpark Auslieferungsfahrten",
                    LocalDate.of(2026, 7, 16), standortB, fuehrungskraft));
        }
    }

    private EmissionEintrag erstelleEintrag(KategorieEnum kategorie, double wertCo2Kg, String beschreibung,
                                             LocalDate datum, Standort standort, Benutzer erfasstVon) {
        EmissionEintrag eintrag = new EmissionEintrag();
        eintrag.setKategorie(kategorie);
        eintrag.setWertCo2Kg(wertCo2Kg);
        eintrag.setBeschreibung(beschreibung);
        eintrag.setDatum(datum);
        eintrag.setErfasstAm(LocalDateTime.now());
        eintrag.setStandort(standort);
        eintrag.setErfasstVon(erfasstVon);
        return eintrag;
    }
}