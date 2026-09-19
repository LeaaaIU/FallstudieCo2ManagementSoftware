import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';
import Navigation from '../components/Navigation';

const KATEGORIEN = ['GESCHAEFTSREISE', 'STROMVERBRAUCH', 'FUHRPARK', 'SONSTIGES'];

export default function EmissionFormular() {
    const navigate = useNavigate();
    const [form, setForm] = useState({
        kategorie: 'STROMVERBRAUCH',
        wertCo2Kg: '',
        datum: '',
        beschreibung: '',
        standortId: 1, // dynamisch nach Login setzen
    });
    const [fehler, setFehler] = useState('');

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await api.post('/emissionen', {
                ...form,
                wertCo2Kg: parseFloat(form.wertCo2Kg),
            });
            navigate('/emissionen');
        } catch (err) {
            setFehler('Speichern fehlgeschlagen. Eingaben prüfen.');
        }
    };

    return (
        <div>
            <Navigation />
            <div className="verwaltung-container">
                <form
                    onSubmit={handleSubmit}
                    className="verwaltung-formular"
                    style={{ margin: '24px auto' }}
                >
                    <h2>Neuer Emissionseintrag</h2>
                    {fehler && <p className="fehler-text">{fehler}</p>}

                    <label>
                        Kategorie
                        <select value={form.kategorie} onChange={e => setForm({ ...form, kategorie: e.target.value })}>
                            {KATEGORIEN.map(k => <option key={k} value={k}>{k}</option>)}
                        </select>
                    </label>

                    <label>
                        CO₂ in kg
                        <input
                            type="number"
                            value={form.wertCo2Kg}
                            onChange={e => setForm({ ...form, wertCo2Kg: e.target.value })}
                        />
                    </label>

                    <label>
                        Datum
                        <input
                            type="date"
                            value={form.datum}
                            onChange={e => setForm({ ...form, datum: e.target.value })}
                        />
                    </label>

                    <label>
                        Beschreibung
                        <input
                            value={form.beschreibung}
                            onChange={e => setForm({ ...form, beschreibung: e.target.value })}
                        />
                    </label>

                    <div className="formular-aktionen">
                        <button type="submit">Speichern</button>
                        <button type="button" onClick={() => navigate('/emissionen')}>Abbrechen</button>
                    </div>
                </form>
            </div>
        </div>
    );
}