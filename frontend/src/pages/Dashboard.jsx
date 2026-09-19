import { useState, useEffect, useContext } from 'react';
import {
    BarChart, Bar, LineChart, Line, XAxis, YAxis, CartesianGrid,
    Tooltip, Legend, ResponsiveContainer
} from 'recharts';
import api from '../api/axiosConfig';
import { AuthContext } from '../store/AuthContext';
import Navigation from '../components/Navigation';

const MONATSNAMEN = ['Jan', 'Feb', 'Mär', 'Apr', 'Mai', 'Jun', 'Jul', 'Aug', 'Sep', 'Okt', 'Nov', 'Dez'];

function formatMonat(monatStr) {
    const [jahr, monat] = monatStr.split('-');
    return `${MONATSNAMEN[parseInt(monat, 10) - 1] ?? monat} ${jahr}`;
}

function Kachel({ titel, wert, einheit = 'kg CO₂' }) {
    return (
        <div style={{
            flex: 1,
            padding: 16,
            border: '1px solid var(--border)',
            borderRadius: 'var(--radius)',
            background: 'var(--bg-elevated)',
            boxShadow: 'var(--shadow-sm)',
            textAlign: 'center'
        }}>
            <div style={{ fontSize: 14, color: 'var(--text)' }}>{titel}</div>
            <div style={{ fontSize: 28, fontWeight: 'bold', color: 'var(--text-h)' }}>
                {typeof wert === 'number' ? wert.toLocaleString('de-DE', { maximumFractionDigits: 1 }) : wert}
            </div>
            <div style={{ fontSize: 12, color: 'var(--text)' }}>{einheit}</div>
        </div>
    );
}

function ChartCard({ titel, children }) {
    return (
        <div style={{
            flex: 1,
            minWidth: 400,
            height: 350,
            padding: 16,
            border: '1px solid var(--border)',
            borderRadius: 'var(--radius)',
            background: 'var(--bg-elevated)',
            boxShadow: 'var(--shadow-sm)'
        }}>
            <h3>{titel}</h3>
            <ResponsiveContainer width="100%" height="85%">
                {children}
            </ResponsiveContainer>
        </div>
    );
}

export default function Dashboard() {
    const { authState } = useContext(AuthContext);
    const rolle = authState?.rolle;

    const [standortDaten, setStandortDaten] = useState([]);
    const [zeitreiheDaten, setZeitreiheDaten] = useState([]);
    const [ladeFehler, setLadeFehler] = useState('');
    const [ladeStatus, setLadeStatus] = useState(true);

    useEffect(() => {
        Promise.all([
            api.get('/emissionen/statistik/standort'),
            api.get('/emissionen/statistik/zeitreihe')
        ])
            .then(([standortRes, zeitreiheRes]) => {
                setStandortDaten(standortRes.data);
                setZeitreiheDaten(zeitreiheRes.data);
            })
            .catch(() => setLadeFehler('Statistikdaten konnten nicht geladen werden.'))
            .finally(() => setLadeStatus(false));
    }, []);

    const gesamtSumme = standortDaten.reduce((summe, s) => summe + s.gesamtCO2, 0);

    const aktuellerMonatKey = new Date().toISOString().slice(0, 7);
    const aktuellerMonatEintrag = zeitreiheDaten.find(z => z.monat === aktuellerMonatKey);
    const aktuellerMonatWert = aktuellerMonatEintrag ? aktuellerMonatEintrag.gesamtCO2 : 0;

    const eigenerStandort = authState?.standortName
        ? standortDaten.find(s => s.standortName === authState.standortName)
        : null;

    // Rollenbasierte Sichtbarkeit der Charts:
    // - BENUTZER: nur eigene Entwicklung über Zeit (Standortvergleich ergibt bei nur einem
    //   erlaubten Standort keinen Sinn, siehe serverseitige Einschränkung in EmissionService)
    // - NACHHALTIGKEITSBEAUFTRAGTER: Fokus auf Vergleich der Standorte (eigene Story)
    // - FUEHRUNGSKRAFT: Fokus auf zeitliche Entwicklung (eigene Story)
    // - ADMINISTRATOR: volle Übersicht (beide Charts), keine eigene Story dazu, aber
    //   sinnvoll aufgrund administrativer Gesamtverantwortung
    const zeigeStandortChart = rolle === 'NACHHALTIGKEITSBEAUFTRAGTER' || rolle === 'ADMINISTRATOR';
    const zeigeZeitreiheChart = rolle === 'FUEHRUNGSKRAFT' || rolle === 'ADMINISTRATOR' || rolle === 'BENUTZER';

    if (ladeStatus) {
        return (
            <div>
                <Navigation />
                <main style={{ padding: 24 }}><p>Lade Dashboard...</p></main>
            </div>
        );
    }

    return (
        <div>
            <Navigation />
            <div className="verwaltung-container">
                <h2>Dashboard</h2>
                {ladeFehler && <p className="fehler-text">{ladeFehler}</p>}

                <div style={{ display: 'flex', gap: 16, marginBottom: 32 }}>
                    {rolle !== 'BENUTZER' && (
                        <Kachel titel="Gesamtemissionen" wert={gesamtSumme} />
                    )}
                    <Kachel titel="Aktueller Monat" wert={aktuellerMonatWert} />
                    <Kachel
                        titel={eigenerStandort ? eigenerStandort.standortName : 'Standorte'}
                        wert={eigenerStandort ? eigenerStandort.gesamtCO2 : standortDaten.length}
                        einheit={eigenerStandort ? 'kg CO₂' : 'Standorte'}
                    />
                </div>

                {!zeigeStandortChart && !zeigeZeitreiheChart && (
                    <p>Für deine Rolle sind aktuell keine grafischen Auswertungen vorgesehen.</p>
                )}

                <div style={{ display: 'flex', gap: 24, flexWrap: 'wrap' }}>
                    {zeigeStandortChart && (
                        <ChartCard titel="Gesamtemissionen pro Standort">
                            <BarChart data={standortDaten}>
                                <CartesianGrid strokeDasharray="3 3" />
                                <XAxis dataKey="standortName" />
                                <YAxis unit=" kg" />
                                <Tooltip formatter={(value) => [`${value} kg`, 'CO₂']} />
                                <Legend />
                                <Bar dataKey="gesamtCO2" name="CO₂ (kg)" fill="#1f9d7c" />
                            </BarChart>
                        </ChartCard>
                    )}

                    {zeigeZeitreiheChart && (
                        <ChartCard titel={rolle === 'BENUTZER' ? 'Meine Emissionsentwicklung' : 'Emissionsentwicklung pro Monat'}>
                            <LineChart data={zeitreiheDaten}>
                                <CartesianGrid strokeDasharray="3 3" />
                                <XAxis dataKey="monat" tickFormatter={formatMonat} />
                                <YAxis unit=" kg" />
                                <Tooltip formatter={(value) => [`${value} kg`, 'CO₂']} labelFormatter={formatMonat} />
                                <Legend />
                                <Line type="monotone" dataKey="gesamtCO2" name="CO₂ (kg)" stroke="#1565c0" />
                            </LineChart>
                        </ChartCard>
                    )}
                </div>
            </div>
        </div>
    );
}