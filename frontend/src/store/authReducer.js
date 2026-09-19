export const initialAuthState = {
  isAuthenticated: false,
  username: null,
  rolle: null,
  standortId: null,
  standortName: null,
  loading: true,
  error: null,
};

export function authReducer(state, action) {
  switch (action.type) {
    case 'INIT_START':
      return { ...state, loading: true, error: null };

    case 'INIT_USER':
      return {
        ...state,
        isAuthenticated: true,
        username: action.payload.username,
        rolle: action.payload.rolle,
        standortId: action.payload.standortId,
        standortName: action.payload.standortName,
        loading: false,
        error: null,
      };

    case 'INIT_FAILED':
      return { ...initialAuthState, loading: false };

    case 'LOGIN_SUCCESS':
      return {
        ...state,
        isAuthenticated: true,
        username: action.payload.username,
        rolle: action.payload.rolle,
        standortId: action.payload.standortId,
        standortName: action.payload.standortName,
        loading: false,
        error: null,
      };

    case 'LOGIN_ERROR':
      return { ...state, isAuthenticated: false, loading: false, error: action.payload };

    case 'LOGOUT':
      return { ...initialAuthState, loading: false };

    default:
      return state;
  }
}