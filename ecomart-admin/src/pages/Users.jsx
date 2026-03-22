import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';

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
        <div style={{ padding: '30px', fontFamily: 'sans-serif', backgroundColor: '#f9f9f9', minHeight: '100vh' }}>

            {/* Header */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '30px', backgroundColor: 'white', padding: '20px', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.05)' }}>
                <h1 style={{ margin: 0, color: '#333' }}>👥 User Management</h1>
                <div>
                    <button onClick={() => navigate('/dashboard')} style={{ padding: '8px 16px', marginRight: '10px', backgroundColor: '#4CAF50', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
                        Back to Orders
                    </button>
                    <button onClick={() => navigate('/analytics')} style={{ padding: '8px 16px', backgroundColor: '#9370DB', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
                        Analytics
                    </button>
                </div>
            </div>

            {/* Users Table */}
            <div style={{ backgroundColor: 'white', borderRadius: '8px', overflow: 'hidden', boxShadow: '0 2px 4px rgba(0,0,0,0.05)' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                    <thead style={{ backgroundColor: '#333', color: 'white' }}>
                        <tr>
                            <th style={{ padding: '15px' }}>ID</th>
                            <th style={{ padding: '15px' }}>Name</th>
                            <th style={{ padding: '15px' }}>Email</th>
                            <th style={{ padding: '15px' }}>Role</th>
                            <th style={{ padding: '15px' }}>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {users.map((user) => (
                            <tr key={user.id} style={{ borderBottom: '1px solid #eee' }}>
                                <td style={{ padding: '15px' }}>{user.id}</td>
                                <td style={{ padding: '15px', fontWeight: 'bold' }}>{user.firstName} {user.lastName}</td>
                                <td style={{ padding: '15px', color: '#666' }}>{user.email}</td>
                                <td style={{ padding: '15px' }}>
                                    <select
                                        value={user.role}
                                        onChange={(e) => handleRoleChange(user.id, e.target.value)}
                                        style={{
                                            padding: '5px',
                                            borderRadius: '4px',
                                            cursor: 'pointer',
                                            backgroundColor: user.role === 'ROLE_ADMIN' ? '#FFF3E0' : '#E8F5E9',
                                            fontWeight: 'bold',
                                            color: user.role === 'ROLE_ADMIN' ? '#FF9800' : '#4CAF50',
                                            border: '1px solid #ccc'
                                        }}
                                    >
                                        <option value="ROLE_USER">Customer</option>
                                        <option value="ROLE_ADMIN">Admin</option>
                                    </select>
                                </td>
                                <td style={{ padding: '15px' }}>
                                    <button onClick={() => handleDelete(user.id)} style={{ padding: '5px 10px', backgroundColor: '#dc3545', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
                                        Delete
                                    </button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}