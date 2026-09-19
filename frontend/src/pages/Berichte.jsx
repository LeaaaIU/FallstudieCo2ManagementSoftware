import { useState, useEffect } from 'react';
import { useAuth } from '../store/AuthContext';
import api from '../api/axiosConfig';
import Navigation from '../components/Navigation';

function Berichte() {
  const { user } = useAuth();
  const darfAlleStandorteWaehlen =
    user?.rolle === 'ADMINISTRATOR' ||
    user?.rolle === 'NACHHALTIGKEITSBEAUFTRAGTER' ||
    user?.rolle === 'FUEHRUNGSKRAFT';

  const [standorte, setStandorte] = useState([]);
  const [ausgewaehlterStandort, setAusgewaehlterStandort] = useState('');
  const [von, setVon] = useState('');
  const [bis, setBis] = useState('');
  const [ladeStandorte, setLadeStandorte] = useState(false);
  const [exportLaeuft, setExportLaeuft] = useState(false);
  const [fehler, setFehler] = useState('');

  useEffect(() => {
    if (darfAlleStandorteWaehlen) {
      setLadeStandorte(true);
      api
        .get('/standorte')
        .then((response) => setStandorte(response.data))
        .catch(() => setFehler('Standorte konnten nicht geladen werden.'))
        .finally(() => setLadeStandorte(false));
    } else if (user?.standortId) {
      setAusgewaehlterStandort(String(user.standortId));
    }
  }, [darfAlleStandorteWaehlen, user?.id, user?.standortId]);

  const handleExport = async () => {
    setFehler('');
    setExportLaeuft(true);
    try {
      const params = {};
      if (ausgewaehlterStandort) params.standortId = ausgewaehlterStandort;
      if (von) params.von = von;
      if (bis) params.bis = bis;

      const response = await api.get('/berichte/export', {
        params,
        responseType: 'blob',
      });

      const blob = new Blob([response.data], { type: 'application/pdf' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute(
        'download',
        `Emissionsbericht_${new Date().toISOString().slice(0, 10)}.pdf`
      );
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      setFehler('PDF-Export fehlgeschlagen. Bitte Auswahl prüfen.');
    } finally {
      setExportLaeuft(false);
    }
  };

  return (
    <>
      <Navigation />
      <div className="berichte-container">
        <h2>Berichte</h2>

        {fehler && <p className="fehler-text">{fehler}</p>}

        <div className="filter-zeile">
          <label htmlFor="standort-auswahl">Standort</label>
          {darfAlleStandorteWaehlen ? (
            <select
              id="standort-auswahl"
              value={ausgewaehlterStandort}
              onChange={(e) => setAusgewaehlterStandort(e.target.value)}
              disabled={ladeStandorte}
            >
              <option value="">Alle Standorte</option>
              {standorte.map((standort) => (
                <option key={standort.id} value={standort.id}>
                  {standort.name}
                </option>
              ))}
            </select>
          ) : (
            <input
              id="standort-auswahl"
              type="text"
              value={user?.standortName || ''}
              disabled
            />
          )}
        </div>

        <div className="filter-zeile">
          <label htmlFor="von-datum">Von</label>
          <input
            id="von-datum"
            type="date"
            value={von}
            onChange={(e) => setVon(e.target.value)}
          />
        </div>

        <div className="filter-zeile">
          <label htmlFor="bis-datum">Bis</label>
          <input
            id="bis-datum"
            type="date"
            value={bis}
            onChange={(e) => setBis(e.target.value)}
          />
        </div>

        <button onClick={handleExport} disabled={exportLaeuft}>
          {exportLaeuft ? 'Export läuft …' : 'Als PDF herunterladen'}
        </button>
      </div>
    </>
  );
}

export default Berichte;