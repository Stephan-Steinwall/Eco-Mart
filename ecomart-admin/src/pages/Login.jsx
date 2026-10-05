import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';

export default function Login() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleLogin = async (e) => {
        e.preventDefault(); // Prevents the page from refreshing
        setError('');
        setLoading(true);

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
                localStorage.setItem('USER_EMAIL', response.data.email);

                // Teleport to the Dashboard
                navigate('/dashboard');
            } else {
                setError('Access denied — this account is not an admin.');
            }
        } catch (error) {
            console.error(error);
            setError('Invalid credentials or server error.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-bg px-6">
            <div className="w-full max-w-sm">
                <div className="text-center mb-10">
                    <div className="text-4xl mb-3">🌱</div>
                    <h1 className="font-display text-4xl font-bold text-text-primary tracking-tight">Admin console</h1>
                    <p className="text-text-secondary mt-2">Sign in to manage EcoMart</p>
                </div>

                {error && (
                    <div className="bg-error-container text-error text-sm font-semibold rounded-xl px-4 py-3 mb-5 text-center">
                        {error}
                    </div>
                )}

                <form onSubmit={handleLogin} className="flex flex-col gap-4">
                    <input
                        type="email"
                        placeholder="Admin email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        required
                        className="w-full px-4 py-3.5 rounded-xl border border-outline-strong bg-surface text-text-primary placeholder:text-text-tertiary outline-none focus:border-brand-primary focus:ring-2 focus:ring-brand-primary-container transition-all"
                    />
                    <input
                        type="password"
                        placeholder="Password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                        className="w-full px-4 py-3.5 rounded-xl border border-outline-strong bg-surface text-text-primary placeholder:text-text-tertiary outline-none focus:border-brand-primary focus:ring-2 focus:ring-brand-primary-container transition-all"
                    />
                    <button
                        type="submit"
                        disabled={loading}
                        className="mt-2 w-full py-3.5 rounded-xl bg-brand-primary text-white font-bold hover:bg-brand-primary-dark active:scale-[0.99] transition-all disabled:opacity-60"
                    >
                        {loading ? 'Signing in…' : 'Log in'}
                    </button>
                </form>
            </div>
        </div>
    );
}
