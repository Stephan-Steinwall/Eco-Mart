import axios from 'axios';

// Create a custom instance of Axios
const api = axios.create({
    baseURL: 'http://localhost:8080/api/',
});

// Add an interceptor to inject the token into requests
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('JWT_TOKEN');
    // Login/register must never carry a stored token: an old expired one would otherwise be sent along
    // and could stop the admin from signing in again.
    const isAuthRequest = (config.url || '').replace(/^\//, '').startsWith('auth/');
    if (token && !isAuthRequest) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
}, (error) => {
    return Promise.reject(error);
});

export default api;