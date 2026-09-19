package de.co2management.backend.service;

import de.co2management.backend.dto.StandortStatistikResponse;
import de.co2management.backend.dto.ZeitreihenPunkt;
import de.co2management.backend.entity.Benutzer;
import de.co2management.backend.entity.EmissionEintrag;
import de.co2management.backend.enums.RolleEnum;
import de.co2management.backend.repository.EmissionEintragRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmissionService {

    private final EmissionEintragRepository emissionRepo;

    public List<EmissionEintrag> findByStandortId(Long standortId) {
        return emissionRepo.findByStandortId(standortId);
    }

    public List<EmissionEintrag> filterByZeitraum(LocalDate von, LocalDate bis) {
        return emissionRepo.findByDatumBetween(von, bis);
    }

    public Double calcGesamtemission(Long standortId) {
        return emissionRepo.findByStandortId(standortId)
            .stream()
            .mapToDouble(e -> e.getWertCo2Kg() != null ? e.getWertCo2Kg() : 0.0)
            .sum();
    }

    public EmissionEintrag findById(Long id) {
        return emissionRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Eintrag nicht gefunden: " + id));
    }

    public EmissionEintrag save(EmissionEintrag entry) {
        return emissionRepo.save(entry);
    }

    public void deleteById(Long id) {
        emissionRepo.deleteById(id);
    }

    public List<EmissionEintrag> findGefiltert(Long standortId, LocalDate von, LocalDate bis, Benutzer aktuellerBenutzer) {
        Long effektiverStandortId = ermittleEffektivenStandort(standortId, aktuellerBenutzer);

        if (effektiverStandortId != null && von != null && bis != null) {
            return emissionRepo.findByStandortIdAndDatumBetween(effektiverStandortId, von, bis);
        } else if (effektiverStandortId != null) {
            return findByStandortId(effektiverStandortId);
        } else if (von != null && bis != null) {
            return filterByZeitraum(von, bis);
        } else {
            return emissionRepo.findAll();
        }
    }

    public List<StandortStatistikResponse> statistikProStandort(Benutzer aktuellerBenutzer) {
        Long effektiverStandortId = ermittleEffektivenStandort(null, aktuellerBenutzer);

        List<EmissionEintrag> eintraege = effektiverStandortId != null
            ? findByStandortId(effektiverStandortId)
            : emissionRepo.findAll();

        Map<String, Double> summeProStandort = eintraege.stream()
            .collect(Collectors.groupingBy(
                e -> e.getStandort().getName(),
                Collectors.summingDouble(e -> e.getWertCo2Kg() != null ? e.getWertCo2Kg() : 0.0)
            ));

        return summeProStandort.entrySet().stream()
            .map(e -> new StandortStatistikResponse(e.getKey(), e.getValue()))
            .toList();
    }

    public List<ZeitreihenPunkt> zeitreihe(Long standortId, LocalDate von, LocalDate bis, Benutzer aktuellerBenutzer) {
        Long effektiverStandortId = ermittleEffektivenStandort(standortId, aktuellerBenutzer);

        LocalDate effektivBis = bis != null ? bis : LocalDate.now();
        LocalDate effektivVon = von != null ? von : effektivBis.minusMonths(11).withDayOfMonth(1);

        List<EmissionEintrag> eintraege = effektiverStandortId != null
            ? emissionRepo.findByStandortIdAndDatumBetween(effektiverStandortId, effektivVon, effektivBis)
            : filterByZeitraum(effektivVon, effektivBis);

        Map<String, Double> summeProMonat = eintraege.stream()
            .collect(Collectors.groupingBy(
                e -> e.getDatum().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                TreeMap::new,
                Collectors.summingDouble(e -> e.getWertCo2Kg() != null ? e.getWertCo2Kg() : 0.0)
            ));

        return summeProMonat.entrySet().stream()
            .map(e -> new ZeitreihenPunkt(e.getKey(), e.getValue()))
            .toList();
    }

    private Long ermittleEffektivenStandort(Long angefragterStandortId, Benutzer aktuellerBenutzer) {
        if (aktuellerBenutzer.getRolle() == RolleEnum.BENUTZER) {
            return aktuellerBenutzer.getStandort().getId();
        }
        return angefragterStandortId;
    }
}