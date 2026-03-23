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
        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', backgroundColor: '#f4f7f6', fontFamily: '"Inter", system-ui, -apple-system, sans-serif' }}>
            <div style={{ padding: '48px', backgroundColor: '#ffffff', borderRadius: '24px', boxShadow: '0 10px 40px rgba(0,0,0,0.08)', width: '340px', border: '1px solid #f1f5f9' }}>
                <div style={{ textAlign: 'center', marginBottom: '32px' }}>
                    <div style={{ fontSize: '3rem', marginBottom: '8px' }}>🍃</div>
                    <h2 style={{ color: '#2c3e50', margin: 0, fontSize: '1.8rem', fontWeight: '800', letterSpacing: '-0.5px' }}>EcoMart Admin</h2>
                    <p style={{ color: '#64748b', margin: '8px 0 0 0', fontSize: '0.9rem', fontWeight: '500' }}>Please sign in to continue</p>
                </div>

                {error && <div style={{ backgroundColor: '#fef2f2', color: '#ef4444', padding: '12px', borderRadius: '8px', marginBottom: '20px', textAlign: 'center', fontWeight: '600', fontSize: '0.9rem', border: '1px solid #fee2e2' }}>{error}</div>}

                <form onSubmit={handleLogin} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                    <div>
                        <input
                            type="email"
                            placeholder="Admin Email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            required
                            style={{ width: '100%', boxSizing: 'border-box', padding: '14px 16px', borderRadius: '12px', border: '1px solid #cbd5e1', outline: 'none', fontSize: '1rem', transition: 'all 0.2s', backgroundColor: '#f8fafc', color: '#1a1a1a' }}
                            onFocus={(e) => { e.target.style.borderColor = '#4CAF50'; e.target.style.boxShadow = '0 0 0 3px rgba(76,175,80,0.15)'; e.target.style.backgroundColor = '#ffffff'; }}
                            onBlur={(e) => { e.target.style.borderColor = '#cbd5e1'; e.target.style.boxShadow = 'none'; e.target.style.backgroundColor = '#f8fafc'; }}
                        />
                    </div>
                    <div>
                        <input
                            type="password"
                            placeholder="Password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                            style={{ width: '100%', boxSizing: 'border-box', padding: '14px 16px', borderRadius: '12px', border: '1px solid #cbd5e1', outline: 'none', fontSize: '1rem', transition: 'all 0.2s', backgroundColor: '#f8fafc', color: '#1a1a1a' }}
                            onFocus={(e) => { e.target.style.borderColor = '#4CAF50'; e.target.style.boxShadow = '0 0 0 3px rgba(76,175,80,0.15)'; e.target.style.backgroundColor = '#ffffff'; }}
                            onBlur={(e) => { e.target.style.borderColor = '#cbd5e1'; e.target.style.boxShadow = 'none'; e.target.style.backgroundColor = '#f8fafc'; }}
                        />
                    </div>
                    <button
                        type="submit"
                        style={{ padding: '14px', backgroundColor: '#4CAF50', color: 'white', border: 'none', borderRadius: '12px', cursor: 'pointer', fontWeight: '700', fontSize: '1rem', marginTop: '10px', boxShadow: '0 4px 12px rgba(76,175,80,0.3)', transition: 'all 0.2s' }}
                        onMouseOver={(e) => { e.currentTarget.style.transform = 'translateY(-2px)'; e.currentTarget.style.boxShadow = '0 6px 16px rgba(76,175,80,0.4)'; e.currentTarget.style.backgroundColor = '#43a047'; }}
                        onMouseOut={(e) => { e.currentTarget.style.transform = 'translateY(0)'; e.currentTarget.style.boxShadow = '0 4px 12px rgba(76,175,80,0.3)'; e.currentTarget.style.backgroundColor = '#4CAF50'; }}>
                        Login to Dashboard
                    </button>
                </form>
            </div>
        </div>
    );
}