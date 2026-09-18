package de.co2management.backend.service;

import de.co2management.backend.dto.BenutzerRequest;
import de.co2management.backend.dto.BenutzerResponse;
import de.co2management.backend.entity.Benutzer;
import de.co2management.backend.repository.BenutzerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BenutzerService implements UserDetailsService {

    private final BenutzerRepository benutzerRepo;
    private final PasswordEncoder passwordEncoder;
    private final StandortService standortService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Benutzer b = benutzerRepo.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Benutzer nicht gefunden: " + username));
        return User.withUsername(b.getUsername())
            .password(b.getPasswort())
            .roles(b.getRolle().name())
            .build();
    }

    // --- CRUD ---

    public List<BenutzerResponse> findAll() {
        return benutzerRepo.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public Benutzer findById(Long id) {
        return benutzerRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Benutzer nicht gefunden: " + id));
    }

    public BenutzerResponse findResponseById(Long id) {
        return toResponse(findById(id));
    }

    public BenutzerResponse create(BenutzerRequest req) {
        if (req.passwort() == null || req.passwort().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwort ist bei Neuanlage Pflicht");
        }
        Benutzer b = new Benutzer();
        b.setUsername(req.username());
        b.setEmail(req.email());
        b.setPasswort(passwordEncoder.encode(req.passwort()));
        b.setRolle(req.rolle());
        b.setAktiv(req.aktiv() == null || req.aktiv());
        b.setErstelltAm(LocalDateTime.now());
        b.setStandort(standortService.findById(req.standortId()));
        return toResponse(benutzerRepo.save(b));
    }

    public BenutzerResponse update(Long id, BenutzerRequest req) {
        Benutzer b = findById(id);
        b.setUsername(req.username());
        b.setEmail(req.email());
        if (req.passwort() != null && !req.passwort().isBlank()) {
            b.setPasswort(passwordEncoder.encode(req.passwort()));
        }
        b.setRolle(req.rolle());
        b.setStandort(standortService.findById(req.standortId()));
        if (req.aktiv() != null) {
            b.setAktiv(req.aktiv());
        }
        return toResponse(benutzerRepo.save(b));
    }

    public void deleteById(Long id) {
        if (!benutzerRepo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Benutzer nicht gefunden: " + id);
        }
        benutzerRepo.deleteById(id);
    }

    private BenutzerResponse toResponse(Benutzer b) {
        return new BenutzerResponse(
                b.getId(),
                b.getUsername(),
                b.getEmail(),
                b.getRolle(),
                b.getStandort().getId(),
                b.getStandort().getName(),
                b.isAktiv(),
                b.getErstelltAm()
        );
    }
}