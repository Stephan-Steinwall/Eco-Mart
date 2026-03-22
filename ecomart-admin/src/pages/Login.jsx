import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';

export default function Login() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const handleLogin = async (e) => {
        e.preventDefault(); // Prevents the page from refreshing
        setError('');

        try {
            const response = await api.post('/auth/login', {
                email: email,
                password: password
            });

            // Check if the user is actually an Admin!
            if (response.data.role === 'ROLE_ADMIN') {
                // Save the data
                localStorage.setItem('JWT_TOKEN', response.data.token);
                localStorage.setItem('USER_NAME', response.data.firstName);

                // Teleport to the Dashboard
                navigate('/dashboard');
            } else {
                setError('Access Denied: You are not an Admin.');
            }
        } catch (error) {
            console.error(error);
            setError('Invalid credentials or server error.');
        }
    };

    return (
        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', backgroundColor: '#f4f4f4' }}>
            <div style={{ padding: '40px', backgroundColor: 'white', borderRadius: '8px', boxShadow: '0 4px 6px rgba(0,0,0,0.1)', width: '300px' }}>
                <h2 style={{ color: '#4CAF50', textAlign: 'center', marginBottom: '20px' }}>EcoMart Admin</h2>

                {error && <p style={{ color: 'red', fontSize: '14px', textAlign: 'center' }}>{error}</p>}

                <form onSubmit={handleLogin} style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                    <input
                        type="email"
                        placeholder="Admin Email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        required
                        style={{ padding: '10px', borderRadius: '4px', border: '1px solid #ccc' }}
                    />
                    <input
                        type="password"
                        placeholder="Password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                        style={{ padding: '10px', borderRadius: '4px', border: '1px solid #ccc' }}
                    />
                    <button
                        type="submit"
                        style={{ padding: '10px', backgroundColor: '#4CAF50', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}>
                        Login to Dashboard
                    </button>
                </form>
            </div>
        </div>
    );
}