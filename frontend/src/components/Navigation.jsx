import { useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';
import { AuthContext } from '../store/AuthContext';

export default function Navigation() {
    const { authState, dispatch } = useContext(AuthContext);
    const navigate = useNavigate();

    const handleLogout = async () => {
        await api.post('/auth/logout');
        dispatch({ type: 'LOGOUT' });
        navigate('/login');
    };

    const istAdmin = authState.rolle === 'ADMINISTRATOR';

    return (
        <nav style={{ display: 'flex', gap: 16, padding: 16, background: '#f0f0f0' }}>
            <span><strong>CO₂-Management</strong></span>
            <button onClick={() => navigate('/dashboard')}>Dashboard</button>
            <button onClick={() => navigate('/emissionen')}>Emissionen</button>
            <button onClick={() => navigate('/berichte')}>Berichte</button>
            {istAdmin && (
                <>
                    <button onClick={() => navigate('/benutzer')}>Benutzerverwaltung</button>
                    <button onClick={() => navigate('/standorte')}>Standortverwaltung</button>
                </>
            )}
            <span style={{ marginLeft: 'auto' }}>
                {authState.username} | <button onClick={handleLogout}>Abmelden</button>
            </span>
        </nav>
    );
}
