import { createContext, useContext, useMemo } from 'react';

export const AuthContext = createContext();

export function useAuth() {
  const { authState, dispatch } = useContext(AuthContext);

  const user = useMemo(() => {
    if (!authState || authState.loading || !authState.isAuthenticated) {
      return null;
    }
    return {
      username: authState.username,
      rolle: authState.rolle,
      standortId: authState.standortId,
      standortName: authState.standortName,
    };
  }, [authState]);

  return { user, authState, dispatch };
}