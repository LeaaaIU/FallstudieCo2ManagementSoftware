package de.co2management.backend.service;

import de.co2management.backend.dto.StandortRequest;
import de.co2management.backend.dto.StandortResponse;
import de.co2management.backend.entity.Standort;
import de.co2management.backend.repository.StandortRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StandortService {

    private final StandortRepository standortRepository;

    // Internes Lookup (z. B. von BenutzerService für die FK-Auflösung) – liefert Entity, kein DTO
    public Standort findById(Long id) {
        return standortRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Standort nicht gefunden: " + id));
    }

    public List<StandortResponse> findAll() {
        return standortRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public StandortResponse findResponseById(Long id) {
        return toResponse(findById(id));
    }

    public StandortResponse create(StandortRequest req) {
        Standort s = new Standort();
        s.setName(req.name());
        s.setAdresse(req.adresse());
        s.setErstelltAm(LocalDateTime.now());
        return toResponse(standortRepository.save(s));
    }

    public StandortResponse update(Long id, StandortRequest req) {
        Standort s = findById(id);
        s.setName(req.name());
        s.setAdresse(req.adresse());
        return toResponse(standortRepository.save(s));
    }

    public void deleteById(Long id) {
        if (!standortRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Standort nicht gefunden: " + id);
        }
        standortRepository.deleteById(id);
    }

    private StandortResponse toResponse(Standort s) {
        return new StandortResponse(s.getId(), s.getName(), s.getAdresse(), s.getErstelltAm());
    }
}