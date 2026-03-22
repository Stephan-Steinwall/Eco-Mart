import axios from 'axios';

// Create a custom instance of Axios
const api = axios.create({
    baseURL: 'http://localhost:8080/api/',
});

// Add an interceptor to inject the token into requests
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('JWT_TOKEN');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
}, (error) => {
    return Promise.reject(error);
});

export default api;