import { useState, useEffect } from 'react';
import { Navigate } from 'react-router-dom';
import axios from 'axios';
import { useAuth } from '../store/AuthContext';
import Navigation from '../components/Navigation';

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  withCredentials: true,
});

const LEER_FORMULAR = {
  name: '',
  adresse: '',
};

function Standortverwaltung() {
  const { user } = useAuth();

  const [standortListe, setStandortListe] = useState([]);
  const [formular, setFormular] = useState(LEER_FORMULAR);
  const [formularSichtbar, setFormularSichtbar] = useState(false);
  const [bearbeiteId, setBearbeiteId] = useState(null);
  const [ladeFehler, setLadeFehler] = useState('');

  useEffect(() => {
    if (user?.rolle === 'ADMINISTRATOR') {
      ladeStandorte();
    }
  }, [user]);

  const ladeStandorte = async () => {
    try {
      const antwort = await api.get('/standorte');
      setStandortListe(antwort.data);
    } catch (fehler) {
      setLadeFehler('Standorte konnten nicht geladen werden.');
      console.error(fehler);
    }
  };

  const formularOeffnenNeu = () => {
    setFormular(LEER_FORMULAR);
    setBearbeiteId(null);
    setFormularSichtbar(true);
  };

  const formularOeffnenBearbeiten = (standort) => {
    setFormular({
      name: standort.name,
      adresse: standort.adresse,
    });
    setBearbeiteId(standort.id);
    setFormularSichtbar(true);
  };

  const formularSchliessen = () => {
    setFormularSichtbar(false);
    setFormular(LEER_FORMULAR);
    setBearbeiteId(null);
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormular((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (bearbeiteId) {
        await api.put(`/standorte/${bearbeiteId}`, formular);
      } else {
        await api.post('/standorte', formular);
      }
      formularSchliessen();
      ladeStandorte();
    } catch (fehler) {
      alert('Speichern fehlgeschlagen. Bitte Eingaben prüfen.');
      console.error(fehler);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Diesen Standort wirklich löschen?')) return;
    try {
      await api.delete(`/standorte/${id}`);
      ladeStandorte();
    } catch (fehler) {
      alert('Löschen fehlgeschlagen. Möglicherweise sind noch Benutzer oder Emissionsdaten zugeordnet.');
      console.error(fehler);
    }
  };

  if (!user) {
    return null;
  }

  if (user.rolle !== 'ADMINISTRATOR') {
    return <Navigate to="/" replace />;
  }

  return (
    <>
      <Navigation />
    <div className="verwaltung-container">
      <h1>Standortverwaltung</h1>

      {ladeFehler && <p className="fehler-text">{ladeFehler}</p>}

      <button onClick={formularOeffnenNeu}>Neu anlegen</button>

      {formularSichtbar && (
        <form onSubmit={handleSubmit} className="verwaltung-formular">
          <h2>{bearbeiteId ? 'Standort bearbeiten' : 'Neuen Standort anlegen'}</h2>

          <label>
            Name
            <input
              type="text"
              name="name"
              value={formular.name}
              onChange={handleChange}
              required
            />
          </label>

          <label>
            Adresse
            <input
              type="text"
              name="adresse"
              value={formular.adresse}
              onChange={handleChange}
              required
            />
          </label>

          <div className="formular-aktionen">
            <button type="submit">Speichern</button>
            <button type="button" onClick={formularSchliessen}>Abbrechen</button>
          </div>
        </form>
      )}

      <table className="verwaltung-tabelle standort-tabelle">
        <thead>
          <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Adresse</th>
            <th>Aktionen</th>
          </tr>
        </thead>
        <tbody>
          {standortListe.map((s) => (
            <tr key={s.id}>
              <td>{s.id}</td>
              <td>{s.name}</td>
              <td>{s.adresse}</td>
              <td>
                <button onClick={() => formularOeffnenBearbeiten(s)}>Bearbeiten</button>
                <button onClick={() => handleDelete(s.id)}>Löschen</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
    </>
  );
}

export default Standortverwaltung;