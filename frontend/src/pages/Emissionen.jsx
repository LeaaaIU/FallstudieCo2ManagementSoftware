import { useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';
import { AuthContext } from '../store/AuthContext';
import Navigation from '../components/Navigation';

export default function Emissionen() {
    const { authState } = useContext(AuthContext);
    const [eintraege, setEintraege] = useState([]);
    const [ladeFehler, setLadeFehler] = useState('');
    const [von, setVon] = useState('');
    const [bis, setBis] = useState('');
    const navigate = useNavigate();

    const ladeEintraege = (zusatzParams = {}) => {
        api.get('/emissionen', { params: { standortId: 1, ...zusatzParams } }) // standortId dynamisch nach Login setzen
            .then(res => setEintraege(res.data))
            .catch(() => setLadeFehler('Emissionen konnten nicht geladen werden.'));
    };

    useEffect(() => {
        ladeEintraege();
    }, []);

    const handleFiltern = () => {
        const params = {};
        if (von) params.von = von;
        if (bis) params.bis = bis;
        ladeEintraege(params);
    };

    const handleZuruecksetzen = () => {
        setVon('');
        setBis('');
        ladeEintraege();
    };

    return (
        <div>
            <Navigation />
            <main style={{ padding: 24 }}>
                <h2>Emissionseinträge</h2>
                <button onClick={() => navigate('/emissionen/neu')}>+ Neuer Eintrag</button>

                <div style={{ display: 'flex', gap: 12, alignItems: 'flex-end', margin: '16px 0' }}>
                    <label>
                        Von:<br />
                        <input type="date" value={von} onChange={e => setVon(e.target.value)} />
                    </label>
                    <label>
                        Bis:<br />
                        <input type="date" value={bis} onChange={e => setBis(e.target.value)} />
                    </label>
                    <button onClick={handleFiltern}>Filtern</button>
                    <button onClick={handleZuruecksetzen}>Zurücksetzen</button>
                </div>

                {ladeFehler && <p style={{ color: 'red' }}>{ladeFehler}</p>}
                <table style={{ width: '100%', marginTop: 16, borderCollapse: 'collapse' }}>
                    <thead>
                        <tr>
                            <th>Kategorie</th>
                            <th>CO2 (kg)</th>
                            <th>Datum</th>
                            <th>Beschreibung</th>
                        </tr>
                    </thead>
                    <tbody>
                        {eintraege.map(e => (
                            <tr key={e.id}>
                                <td>{e.kategorie}</td>
                                <td>{e.wertCo2Kg}</td>
                                <td>{e.datum}</td>
                                <td>{e.beschreibung}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </main>
        </div>
    );
}