import { useState, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';
import { AuthContext } from '../store/AuthContext';

export default function Login() {
    const { dispatch } = useContext(AuthContext);
    const navigate = useNavigate();
    const [form, setForm] = useState({ username: '', passwort: '' });
    const [fehler, setFehler] = useState('');

    const handleLogin = async () => {
        try {
            await api.post('/auth/login', form);
            const meRes = await api.get('/auth/me');
            dispatch({ type: 'LOGIN_SUCCESS', payload: meRes.data });
            navigate('/dashboard');
        } catch (e) {
            setFehler('Login fehlgeschlagen. Zugangsdaten prüfen.');
        }
    };

    return (
        <div style={{ maxWidth: 700, margin: '70px auto', display: 'flex', flexDirection: 'column', gap: 12 }}>
            <h1>CO2-Management Tool</h1>
            {fehler && <p style={{ color: 'red' }}>{fehler}</p>}
            <input
                placeholder="Benutzername"
                value={form.username}
                onChange={e => setForm({ ...form, username: e.target.value })}
            />
            <input
                type="password"
                placeholder="Passwort"
                value={form.passwort}
                onChange={e => setForm({ ...form, passwort: e.target.value })}
            />
            <button onClick={handleLogin}>Anmelden</button>
        </div>
    );
}