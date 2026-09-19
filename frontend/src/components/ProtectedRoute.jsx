import { useContext } from 'react';
import { Navigate } from 'react-router-dom';
import { AuthContext } from '../store/AuthContext';

function ProtectedRoute({ rolle, children }) {
    const { authState } = useContext(AuthContext);

    if (authState.loading) {
        return <div>Lade...</div>;
    }
    if (!authState.isAuthenticated) {
        return <Navigate to="/login" replace />;
    }
    if (rolle && !rolle.includes(authState.rolle)) {
        return <Navigate to="/emissionen" replace />;
    }
    return children;
}

export default ProtectedRoute;
