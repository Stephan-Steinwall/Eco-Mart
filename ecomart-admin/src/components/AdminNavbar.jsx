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
                className={`px-4 py-2 rounded-xl text-sm transition-colors ${
                    isActive
                        ? 'bg-brand-primary-container text-brand-primary-dark font-bold'
                        : 'text-text-secondary font-semibold hover:bg-surface-alt hover:text-text-primary'
                }`}
            >
                {label}
            </button>
        );
    };

    return (
        <div className="flex items-center justify-between bg-surface border border-outline rounded-2xl px-6 py-4 mb-8">
            <div className="flex items-center gap-3">
                <span className="text-3xl leading-none">{icon}</span>
                <h1 className="font-display text-2xl font-bold text-text-primary tracking-tight">{title}</h1>
            </div>

            <div className="flex items-center gap-1.5">
                <NavButton path="/dashboard" label="Orders" />
                <NavButton path="/products" label="Products" />
                <NavButton path="/users" label="Users" />
                <NavButton path="/analytics" label="Analytics" />

                <div className="w-px h-6 bg-outline mx-3" />

                <div className="flex items-center gap-4">
                    <span className="text-sm font-semibold text-text-secondary">
                        Hi, <span className="text-text-primary">{adminName}</span>
                    </span>
                    <button
                        onClick={handleLogout}
                        className="px-4 py-2 rounded-lg text-sm font-semibold text-error border border-error-container bg-surface hover:bg-error-container transition-colors"
                    >
                        Logout
                    </button>
                </div>
            </div>
        </div>
    );
}
