import { useReducer, useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import api from './api/axiosConfig';
import { authReducer, initialAuthState } from './store/authReducer';
import { AuthContext } from './store/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Emissionen from './pages/Emissionen';
import EmissionFormular from './pages/EmissionFormular';
import Benutzerverwaltung from './pages/Benutzerverwaltung';
import Standortverwaltung from './pages/Standortverwaltung';
import Berichte from './pages/Berichte';

function App() {
    const [authState, dispatch] = useReducer(authReducer, initialAuthState);

    useEffect(() => {
        dispatch({ type: 'INIT_START' });
        api.get('/auth/me')
            .then(response => {
                dispatch({ type: 'INIT_USER', payload: response.data });
            })
            .catch(() => {
                dispatch({ type: 'INIT_FAILED' });
            });
    }, []);

    return (
        <AuthContext.Provider value={{ authState, dispatch }}>
            <BrowserRouter>
                <Routes>
                    <Route path="/login" element={<Login />} />

                    <Route
                        path="/dashboard"
                        element={
                            <ProtectedRoute>
                                <Dashboard />
                            </ProtectedRoute>
                        }
                    />

                    <Route
                        path="/emissionen"
                        element={
                            <ProtectedRoute>
                                <Emissionen />
                            </ProtectedRoute>
                        }
                    />

                    <Route
                        path="/emissionen/neu"
                        element={
                            <ProtectedRoute>
                                <EmissionFormular />
                            </ProtectedRoute>
                        }
                    />

                    <Route
                        path="/benutzer"
                        element={
                            <ProtectedRoute rolle={['ADMINISTRATOR']}>
                                <Benutzerverwaltung />
                            </ProtectedRoute>
                        }
                    />

                    <Route
                        path="/standorte"
                        element={
                            <ProtectedRoute rolle={['ADMINISTRATOR']}>
                                <Standortverwaltung />
                            </ProtectedRoute>
                        }
                    />

                    <Route
                        path="/berichte"
                        element={
                            <ProtectedRoute>
                                <Berichte />
                            </ProtectedRoute>
                        }
                    />

                    <Route path="/" element={<Navigate to="/dashboard" replace />} />
                </Routes>
            </BrowserRouter>
        </AuthContext.Provider>
    );
}

export default App;