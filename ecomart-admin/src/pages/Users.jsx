import { useState, useEffect } from 'react';
import api from '../api/axiosConfig';
import AdminNavbar from '../components/AdminNavbar';

export default function Users() {
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

    return (
        <div className="min-h-screen bg-bg p-8 lg:p-10">
            <AdminNavbar title="Users" icon="👥" />

            <div className="bg-surface border border-outline rounded-2xl overflow-hidden">
                <div className="overflow-x-auto">
                    <table className="w-full text-left border-collapse">
                        <thead className="bg-surface-alt border-b border-outline">
                            <tr>
                                <th className="px-6 py-4 text-text-secondary font-bold text-xs tracking-wide">ID</th>
                                <th className="px-6 py-4 text-text-secondary font-bold text-xs tracking-wide">Name</th>
                                <th className="px-6 py-4 text-text-secondary font-bold text-xs tracking-wide">Email</th>
                                <th className="px-6 py-4 text-text-secondary font-bold text-xs tracking-wide">Role</th>
                                <th className="px-6 py-4 text-text-secondary font-bold text-xs tracking-wide text-right">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {loading && (
                                <tr><td colSpan={5} className="px-6 py-16 text-center text-text-tertiary">Loading users…</td></tr>
                            )}
                            {!loading && users.map((user) => (
                                <tr key={user.id} className="border-b border-outline last:border-0 hover:bg-surface-alt/60 transition-colors">
                                    <td className="px-6 py-4 font-bold text-text-primary">{user.id}</td>
                                    <td className="px-6 py-4 font-bold text-text-primary">{user.firstName} {user.lastName}</td>
                                    <td className="px-6 py-4 text-text-secondary">{user.email}</td>
                                    <td className="px-6 py-4">
                                        <select
                                            value={user.role}
                                            onChange={(e) => handleRoleChange(user.id, e.target.value)}
                                            className={`px-3.5 py-2 rounded-full text-xs font-extrabold tracking-wide cursor-pointer outline-none border ${
                                                user.role === 'ROLE_ADMIN'
                                                    ? 'bg-warning-container text-warning border-warning/30'
                                                    : 'bg-brand-primary-container text-brand-primary-dark border-brand-primary/20'
                                            }`}
                                        >
                                            <option value="ROLE_USER">Customer</option>
                                            <option value="ROLE_ADMIN">Admin</option>
                                        </select>
                                    </td>
                                    <td className="px-6 py-4 text-right">
                                        <button onClick={() => handleDelete(user.id)} className="px-4 py-2 rounded-lg text-xs font-bold text-error bg-error-container border border-error/20 hover:brightness-95 transition-all">
                                            Delete
                                        </button>
                                    </td>
                                </tr>
                            ))}
                            {!loading && users.length === 0 && (
                                <tr>
                                    <td colSpan={5} className="px-6 py-20 text-center">
                                        <div className="text-5xl mb-4">👥</div>
                                        <div className="text-lg font-bold text-text-secondary">No users found</div>
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
}
