import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';
import AdminNavbar from '../components/AdminNavbar';

export default function Users() {
    const navigate = useNavigate();
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchUsers();
    }, []);

    const fetchUsers = async () => {
        try {
            const response = await api.get('/admin/users');
            setUsers(response.data);
            setLoading(false);
        } catch (err) {
            console.error("Failed to fetch users", err);
            setLoading(false);
        }
    };

    const handleRoleChange = async (userId, newRole) => {
        // Safety check: Don't let the admin accidentally demote themselves!
        const currentAdminEmail = localStorage.getItem('USER_EMAIL');
        const targetUser = users.find(u => u.id === userId);

        if (targetUser && targetUser.email === currentAdminEmail && newRole !== 'ROLE_ADMIN') {
            alert("You cannot demote your own admin account!");
            return;
        }

        try {
            await api.put(`/admin/users/${userId}/role`, { role: newRole });
            // Update UI locally
            setUsers(users.map(user =>
                user.id === userId ? { ...user, role: newRole } : user
            ));
        } catch (err) {
            alert("Error updating user role.");
        }
    };

    const handleDelete = async (userId) => {
        if (window.confirm("WARNING: Are you sure you want to delete this user? This cannot be undone.")) {
            try {
                await api.delete(`/admin/users/${userId}`);
                setUsers(users.filter(user => user.id !== userId));
            } catch (err) {
                alert("Error deleting user. They might be tied to existing orders.");
            }
        }
    };

    if (loading) return <h2 style={{ textAlign: 'center', marginTop: '50px' }}>Loading Users...</h2>;

    return (
        <div style={{ padding: '40px', fontFamily: '"Inter", system-ui, -apple-system, sans-serif', backgroundColor: '#f4f7f6', minHeight: '100vh', color: '#1a1a1a' }}>

            <AdminNavbar title="User Management" icon="👥" />

            {/* Users Table */}
            <div style={{ backgroundColor: '#ffffff', borderRadius: '16px', overflow: 'hidden', boxShadow: '0 4px 20px rgba(0,0,0,0.04)', border: '1px solid #f1f5f9' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                    <thead style={{ backgroundColor: '#f8fafc', borderBottom: '2px solid #e2e8f0' }}>
                        <tr>
                            <th style={{ padding: '20px 24px', color: '#64748b', fontWeight: '700', textTransform: 'uppercase', fontSize: '0.75rem', letterSpacing: '0.05em' }}>ID</th>
                            <th style={{ padding: '20px 24px', color: '#64748b', fontWeight: '700', textTransform: 'uppercase', fontSize: '0.75rem', letterSpacing: '0.05em' }}>Name</th>
                            <th style={{ padding: '20px 24px', color: '#64748b', fontWeight: '700', textTransform: 'uppercase', fontSize: '0.75rem', letterSpacing: '0.05em' }}>Email</th>
                            <th style={{ padding: '20px 24px', color: '#64748b', fontWeight: '700', textTransform: 'uppercase', fontSize: '0.75rem', letterSpacing: '0.05em' }}>Role</th>
                            <th style={{ padding: '20px 24px', color: '#64748b', fontWeight: '700', textTransform: 'uppercase', fontSize: '0.75rem', letterSpacing: '0.05em', textAlign: 'right' }}>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {users.map((user, index) => (
                            <tr key={user.id} style={{ borderBottom: '1px solid #f1f5f9', transition: 'background-color 0.2s', backgroundColor: index % 2 === 0 ? '#ffffff' : '#fafafa' }} onMouseOver={(e) => e.currentTarget.style.backgroundColor = '#f1f5f9'} onMouseOut={(e) => e.currentTarget.style.backgroundColor = index % 2 === 0 ? '#ffffff' : '#fafafa'}>
                                <td style={{ padding: '20px 24px', fontWeight: '700', color: '#0f172a', fontSize: '0.95rem' }}>{user.id}</td>
                                <td style={{ padding: '20px 24px', fontWeight: '700', color: '#334155', fontSize: '1rem' }}>{user.firstName} {user.lastName}</td>
                                <td style={{ padding: '20px 24px', color: '#475569', fontSize: '0.95rem' }}>{user.email}</td>
                                <td style={{ padding: '20px 24px' }}>
                                    <select
                                        value={user.role}
                                        onChange={(e) => handleRoleChange(user.id, e.target.value)}
                                        style={{
                                            padding: '8px 14px',
                                            borderRadius: '24px',
                                            cursor: 'pointer',
                                            backgroundColor: user.role === 'ROLE_ADMIN' ? '#FFF3E0' : '#E8F5E9',
                                            fontWeight: '800',
                                            fontSize: '0.75rem',
                                            letterSpacing: '0.5px',
                                            textTransform: 'uppercase',
                                            color: user.role === 'ROLE_ADMIN' ? '#FF9800' : '#4CAF50',
                                            border: user.role === 'ROLE_ADMIN' ? '1px solid #FFE0B2' : '1px solid #C8E6C9',
                                            outline: 'none',
                                            appearance: 'auto',
                                            textAlign: 'center'
                                        }}
                                    >
                                        <option value="ROLE_USER">Customer</option>
                                        <option value="ROLE_ADMIN">Admin</option>
                                    </select>
                                </td>
                                <td style={{ padding: '20px 24px', textAlign: 'right' }}>
                                    <button onClick={() => handleDelete(user.id)} style={{ padding: '8px 16px', backgroundColor: '#FEE2E2', color: '#DC2626', border: '1px solid #FECACA', borderRadius: '8px', cursor: 'pointer', fontWeight: '600', fontSize: '0.85rem', transition: 'all 0.2s' }} onMouseOver={(e) => { e.currentTarget.style.backgroundColor = '#FECACA'; e.currentTarget.style.transform = 'translateY(-1px)'; }} onMouseOut={(e) => { e.currentTarget.style.backgroundColor = '#FEE2E2'; e.currentTarget.style.transform = 'translateY(0)'; }}>
                                        Delete
                                    </button>
                                </td>
                            </tr>
                        ))}
                        {users.length === 0 && (
                            <tr>
                                <td colSpan="5" style={{ padding: '60px', textAlign: 'center', color: '#94a3b8' }}>
                                    <div style={{ marginBottom: '16px', fontSize: '3rem' }}>👥</div>
                                    <div style={{ fontSize: '1.2rem', fontWeight: '600', color: '#64748b' }}>No users found</div>
                                </td>
                            </tr>
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    );
}