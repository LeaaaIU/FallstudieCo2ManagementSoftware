import { useState, useEffect } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../store/AuthContext';
import api from '../api/axiosConfig';
import Navigation from '../components/Navigation';

const ROLLEN = ['ADMINISTRATOR', 'NACHHALTIGKEITSBEAUFTRAGTER', 'FUEHRUNGSKRAFT', 'BENUTZER'];

const LEER_FORMULAR = {
  username: '',
  email: '',
  passwort: '',
  rolle: 'BENUTZER',
  standortId: '',
  aktiv: true,
};

function Benutzerverwaltung() {
  const { user } = useAuth();

  const [benutzerListe, setBenutzerListe] = useState([]);
  const [standortListe, setStandortListe] = useState([]);
  const [formular, setFormular] = useState(LEER_FORMULAR);
  const [formularSichtbar, setFormularSichtbar] = useState(false);
  const [bearbeiteId, setBearbeiteId] = useState(null);
  const [ladeFehler, setLadeFehler] = useState('');

  useEffect(() => {
    if (user?.rolle === 'ADMINISTRATOR') {
      ladeBenutzer();
      ladeStandorte();
    }
  }, [user]);

  const ladeBenutzer = async () => {
    try {
      const antwort = await api.get('/benutzer');
      setBenutzerListe(antwort.data);
    } catch (fehler) {
      setLadeFehler('Benutzer konnten nicht geladen werden.');
      console.error(fehler);
    }
  };

  const ladeStandorte = async () => {
    try {
      const antwort = await api.get('/standorte');
      setStandortListe(antwort.data);
    } catch (fehler) {
      console.error(fehler);
    }
  };

  const formularOeffnenNeu = () => {
    setFormular(LEER_FORMULAR);
    setBearbeiteId(null);
    setFormularSichtbar(true);
  };

  const formularOeffnenBearbeiten = (benutzer) => {
    setFormular({
      username: benutzer.username,
      email: benutzer.email,
      passwort: '', // Passwort wird nur bei Eingabe geändert
      rolle: benutzer.rolle,
      standortId: benutzer.standortId ?? '',
      aktiv: benutzer.aktiv,
    });
    setBearbeiteId(benutzer.id);
    setFormularSichtbar(true);
  };

  const formularSchliessen = () => {
    setFormularSichtbar(false);
    setFormular(LEER_FORMULAR);
    setBearbeiteId(null);
  };

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormular((prev) => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const payload = {
      username: formular.username,
      email: formular.email,
      rolle: formular.rolle,
      standortId: formular.standortId || null,
      aktiv: formular.aktiv,
    };
    // Passwort nur mitsenden, wenn ein neues eingegeben wurde
    if (formular.passwort) {
      payload.passwort = formular.passwort;
    }

    try {
      if (bearbeiteId) {
        await api.put(`/benutzer/${bearbeiteId}`, payload);
      } else {
        await api.post('/benutzer', payload);
      }
      formularSchliessen();
      ladeBenutzer();
    } catch (fehler) {
      alert('Speichern fehlgeschlagen. Bitte Eingaben prüfen.');
      console.error(fehler);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Diesen Benutzer wirklich löschen?')) return;
    try {
      await api.delete(`/benutzer/${id}`);
      ladeBenutzer();
    } catch (fehler) {
      alert('Löschen fehlgeschlagen.');
      console.error(fehler);
    }
  };

  if (!user) {
    return null; // AuthContext lädt noch
  }

  if (user.rolle !== 'ADMINISTRATOR') {
    return <Navigate to="/dashboard" replace />;
  }

  return (
    <>
      <Navigation />
      <div className="verwaltung-container">
        <h1>Benutzerverwaltung</h1>

      {ladeFehler && <p className="fehler-text">{ladeFehler}</p>}

      <button onClick={formularOeffnenNeu}>Neu anlegen</button>

      {formularSichtbar && (
        <form onSubmit={handleSubmit} className="verwaltung-formular">
          <h2>{bearbeiteId ? 'Benutzer bearbeiten' : 'Neuen Benutzer anlegen'}</h2>

          <label>
            Benutzername
            <input
              type="text"
              name="username"
              value={formular.username}
              onChange={handleChange}
              required
            />
          </label>

          <label>
            E-Mail
            <input
              type="email"
              name="email"
              value={formular.email}
              onChange={handleChange}
              required
            />
          </label>

          <label>
            Passwort {bearbeiteId && '(leer lassen, um es nicht zu ändern)'}
            <input
              type="password"
              name="passwort"
              value={formular.passwort}
              onChange={handleChange}
              required={!bearbeiteId}
            />
          </label>

          <label>
            Rolle
            <select name="rolle" value={formular.rolle} onChange={handleChange}>
              {ROLLEN.map((r) => (
                <option key={r} value={r}>{r}</option>
              ))}
            </select>
          </label>

          <label>
            Standort
            <select name="standortId" value={formular.standortId} onChange={handleChange} required>
              <option value="">-- bitte wählen --</option>
              {standortListe.map((s) => (
                <option key={s.id} value={s.id}>{s.name}</option>
              ))}
            </select>
          </label>

          <label className="checkbox-label">
            <input
              type="checkbox"
              name="aktiv"
              checked={formular.aktiv}
              onChange={handleChange}
            />
            Aktiv
          </label>

          <div className="formular-aktionen">
            <button type="submit">Speichern</button>
            <button type="button" onClick={formularSchliessen}>Abbrechen</button>
          </div>
        </form>
      )}

      <table className="verwaltung-tabelle">
        <thead>
          <tr>
            <th>ID</th>
            <th>Benutzername</th>
            <th>E-Mail</th>
            <th>Rolle</th>
            <th>Standort</th>
            <th>Aktiv</th>
            <th>Aktionen</th>
          </tr>
        </thead>
        <tbody>
          {benutzerListe.map((b) => (
            <tr key={b.id}>
              <td>{b.id}</td>
              <td>{b.username}</td>
              <td>{b.email}</td>
              <td>{b.rolle}</td>
              <td>{b.standortName ?? '-'}</td>
              <td>{b.aktiv ? 'Ja' : 'Nein'}</td>
              <td>
                <button onClick={() => formularOeffnenBearbeiten(b)}>Bearbeiten</button>
                <button onClick={() => handleDelete(b.id)}>Löschen</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
    </>
  );
}

export default Benutzerverwaltung;