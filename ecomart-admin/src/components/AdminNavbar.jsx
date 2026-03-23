import { useNavigate, useLocation } from 'react-router-dom';

export default function AdminNavbar({ title, icon }) {
    const navigate = useNavigate();
    const location = useLocation();
    const adminName = localStorage.getItem('USER_NAME') || 'Admin';

    const handleLogout = () => {
        if (window.confirm("Are you sure you want to logout?")) {
            localStorage.clear();
            navigate('/');
        }
    };

    const NavButton = ({ path, label }) => {
        const isActive = location.pathname === path;
        return (
            <button 
                onClick={() => navigate(path)} 
                style={{ 
                    padding: '10px 18px', 
                    backgroundColor: isActive ? '#f1f5f9' : 'transparent', 
                    color: isActive ? '#0f172a' : '#64748b', 
                    border: 'none', 
                    borderRadius: '10px', 
                    cursor: 'pointer', 
                    fontWeight: isActive ? '700' : '600', 
                    fontSize: '14px', 
                    transition: 'all 0.2s ease',
                    boxShadow: isActive ? 'inset 0 2px 4px rgba(0,0,0,0.02)' : 'none'
                }} 
                onMouseOver={(e) => { if (!isActive) { e.currentTarget.style.backgroundColor = '#f8fafc'; e.currentTarget.style.color = '#334155'; } }} 
                onMouseOut={(e) => { if (!isActive) { e.currentTarget.style.backgroundColor = 'transparent'; e.currentTarget.style.color = '#64748b'; } }}
            >
                {label}
            </button>
        );
    };

    return (
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '40px', backgroundColor: '#ffffff', padding: '20px 32px', borderRadius: '16px', boxShadow: '0 4px 20px rgba(0,0,0,0.03)' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                <span style={{ fontSize: '2rem' }}>{icon}</span>
                <h1 style={{ margin: 0, fontSize: '1.6rem', fontWeight: '800', color: '#2c3e50', letterSpacing: '-0.5px' }}>{title}</h1>
            </div>
            
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <NavButton path="/dashboard" label="Orders" />
                <NavButton path="/products" label="Products" />
                <NavButton path="/users" label="👥 Users" />
                <NavButton path="/analytics" label="📊 Analytics" />

                <div style={{ width: '1px', height: '24px', backgroundColor: '#e2e8f0', margin: '0 12px' }}></div>

                <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                    <span style={{ fontWeight: '600', color: '#64748b', fontSize: '14px' }}>Hi, <span style={{ color: '#0f172a' }}>{adminName}</span></span>
                    <button 
                        onClick={handleLogout} 
                        style={{ padding: '8px 16px', backgroundColor: '#fff', color: '#ef4444', border: '1px solid #fee2e2', borderRadius: '8px', cursor: 'pointer', fontWeight: '600', fontSize: '13px', transition: 'all 0.2s ease' }} 
                        onMouseOver={(e) => { e.currentTarget.style.backgroundColor = '#fef2f2'; }} 
                        onMouseOut={(e) => { e.currentTarget.style.backgroundColor = '#fff'; }}
                    >
                        Logout
                    </button>
                </div>
            </div>
        </div>
    );
}
