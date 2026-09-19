# CO₂-Management-System

Webbasierte Anwendung zur Erfassung, Auswertung und Visualisierung von CO₂-Emissionsdaten
eines Automobilzulieferers mit mehreren Standorten.

Fallstudie im Kurs **DLBITOWAWBI01 – Programmierung von Web-Anwendungen –
webbasierte betriebliche Informationssysteme**, IU Internationale Hochschule.

---

## Funktionsumfang

| Funktion | Beschreibung |
|---|---|
| Anmeldung | Sitzungsbasierte Authentifizierung über Spring Security, Passwörter BCrypt-gehasht |
| Standortverwaltung | Standorte anlegen, bearbeiten, löschen (nur Administration) |
| Benutzerverwaltung | Benutzerkonten inkl. Rolle und Standortzuordnung (nur Administration) |
| Emissionserfassung | Erfassung je Kategorie (Geschäftsreise, Stromverbrauch, Fuhrpark, Sonstiges) |
| Zeitraumfilter | Filterung der Emissionseinträge nach Von-/Bis-Datum |
| Dashboard | Kennzahlkacheln, Balkendiagramm je Standort, Zeitreihe je Monat |
| PDF-Export | Emissionsbericht als PDF mit Kopfdaten, Tabelle und Summenzeile |
| Standorttrennung | Die Rolle `BENUTZER` erhält serverseitig ausschließlich Daten des eigenen Standorts |

## Rollen

`ADMINISTRATOR` · `NACHHALTIGKEITSBEAUFTRAGTER` · `FUEHRUNGSKRAFT` · `BENUTZER`

## Technologiestack

**Backend:** Java 21, Spring Boot, Spring MVC, Spring Security, Spring Data JPA / Hibernate,
H2 (dateibasiert), Jakarta Validation, Lombok, OpenPDF
**Frontend:** React, Vite, React Router, Axios, Recharts, `useReducer` + Context für den Auth-Zustand

## Projektstruktur

```
backend/                       Spring-Boot-Backend
  pom.xml
  src/main/java/de/co2management/backend/
    config/       SecurityConfig, AppConfig, DataInitializer
    controller/   REST-Endpunkte + GlobalExceptionHandler
    service/      Geschäftslogik
    repository/   Spring-Data-JPA-Repositories
    entity/       JPA-Entitäten
    enums/        RolleEnum, KategorieEnum
    dto/          Request-/Response-Records
  src/test/java/de/co2management/backend/
    CO2ManagementUnitTests.java   30 Unit-Tests der Geschäftslogik

frontend/                      React-Anwendung
  package.json
  src/pages/        Login, Dashboard, Emissionen, EmissionFormular,
                    Berichte, Benutzerverwaltung, Standortverwaltung
  src/components/   Navigation, ProtectedRoute
  src/store/        AuthContext, authReducer
  src/api/          axiosConfig

Dokumente/                     Entwurfs- und Begleitmaterial
  UML Diagramme/               Klassen-, ER-, Schichten- und Komponentendiagramm
                               (jeweils als .drawio und .png)
  co2_navigationsdiagramm_v2.drawio
  Emissionsbericht_2026-07-19.pdf   Beispielausgabe des PDF-Exports
```

## Lokale Ausführung

**Voraussetzungen:** JDK 21, Node.js 20 oder neuer

### Backend starten

```bash
cd backend
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```

Das Backend läuft auf `http://localhost:8080`. Beim ersten Start legt der
`DataInitializer` zwei Standorte, vier Benutzerkonten und Beispiel-Emissionsdaten an.

### Frontend starten

```bash
cd frontend
npm install
npm run dev
```

Das Frontend läuft auf `http://localhost:5173` und ist in der CORS-Konfiguration
des Backends freigegeben.

## Demo-Zugänge

| Benutzername | Passwort | Rolle | Standort |
|---|---|---|---|
| `admin` | `test123` | ADMINISTRATOR | Standort A |
| `nachhaltigkeit` | `test123` | NACHHALTIGKEITSBEAUFTRAGTER | Standort A |
| `fuehrungskraft` | `test123` | FUEHRUNGSKRAFT | Standort B |
| `benutzer` | `test123` | BENUTZER | Standort A |

> Die Zugangsdaten sind reine Demodaten für den lokalen Entwicklungsbetrieb.

## Tests ausführen

```bash
cd backend
./mvnw test                   # Windows: mvnw.cmd test
```

Die Klasse `CO2ManagementUnitTests` enthält 30 Unit-Tests für `EmissionService`,
`StandortService`, `BenutzerService`, `BerichtService` und `PdfExportService`.
Die Repositories werden dabei durch Mockito-Mocks ersetzt, sodass keine Datenbank
benötigt wird.

## REST-Schnittstelle (Auszug)

| Methode | Pfad | Beschreibung |
|---|---|---|
| POST | `/api/auth/login` | Anmeldung |
| GET | `/api/auth/me` | Angemeldeter Benutzer inkl. Rolle und Standort |
| POST | `/api/auth/logout` | Abmeldung |
| GET | `/api/emissionen` | Emissionseinträge, optional gefiltert (`standortId`, `von`, `bis`) |
| POST | `/api/emissionen` | Emissionseintrag anlegen |
| GET | `/api/emissionen/statistik/standort` | Gesamtemissionen je Standort |
| GET | `/api/emissionen/statistik/zeitreihe` | Monatliche Emissionsentwicklung |
| GET | `/api/berichte/export` | PDF-Export für Standort und Zeitraum |
| GET/POST/PUT/DELETE | `/api/standorte` | Standortverwaltung |
| GET/POST/PUT/DELETE | `/api/benutzer` | Benutzerverwaltung (nur ADMINISTRATOR) |

## Hinweise zum Entwicklungsstand

Die Anwendung ist als Prüfungsleistung für den lokalen Entwicklungsbetrieb ausgelegt.
Für einen produktiven Einsatz wären insbesondere HTTPS, ein aktivierter CSRF-Schutz
und ein serverbasiertes Datenbanksystem anstelle der dateibasierten H2-Datenbank
erforderlich.
