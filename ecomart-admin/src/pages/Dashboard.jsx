import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';
import AdminNavbar from '../components/AdminNavbar';

export default function Dashboard() {
    const navigate = useNavigate();
    const adminName = localStorage.getItem('USER_NAME');

    // State variables to hold our data
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    async function fetchOrders() {
        try {
            const response = await api.get('/admin/orders');
            setOrders(response.data);
            setLoading(false);
        } catch (error) {
            console.error(error);
            setError('Failed to fetch orders. Make sure Spring Boot is running!');
            setLoading(false);
        }
    }

    // Fetch orders as soon as the component loads
    useEffect(() => {
        fetchOrders();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);



    // The function that updates the status in the database
    const handleStatusChange = async (orderId, newStatus) => {
        try {
            await api.put(`/admin/orders/${orderId}/status`, { status: newStatus });

            // Update the UI locally so we don't have to refresh the whole page
            setOrders(orders.map(order =>
                order.id === orderId ? { ...order, status: newStatus } : order
            ));
        } catch (err) {
            alert('Error updating status: ' + err.message);
        }
    };

    // A helper to make the status badges look colorful
    const getStatusColor = (status) => {
        switch (status) {
            case 'PENDING': return '#FFA500'; // Orange
            case 'PREPARING': return '#1E90FF'; // Blue
            case 'ON_THE_WAY': return '#9370DB'; // Purple
            case 'DELIVERED': return '#32CD32'; // Green
            case 'CANCELLED': return '#FF0000'; // Red
            default: return '#808080';
        }
    };

    if (loading) return <h2 style={{ textAlign: 'center', marginTop: '50px' }}>Loading Orders...</h2>;

    return (
        <div style={{ padding: '40px', fontFamily: '"Inter", system-ui, -apple-system, sans-serif', backgroundColor: '#f4f7f6', minHeight: '100vh', color: '#1a1a1a' }}>

            <AdminNavbar title="EcoMart Admin" icon="🍃" />

            {error && <div style={{ backgroundColor: '#fef2f2', color: '#ef4444', padding: '16px', borderRadius: '12px', marginBottom: '24px', textAlign: 'center', fontWeight: '600', border: '1px solid #fee2e2' }}>{error}</div>}

            {/* Orders Table */}
            <div style={{ backgroundColor: '#ffffff', borderRadius: '16px', boxShadow: '0 4px 20px rgba(0,0,0,0.04)', overflow: 'hidden', border: '1px solid #f1f5f9' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                    <thead style={{ backgroundColor: '#f8fafc', borderBottom: '2px solid #e2e8f0' }}>
                        <tr>
                            <th style={{ padding: '20px 24px', color: '#64748b', fontWeight: '700', textTransform: 'uppercase', fontSize: '0.75rem', letterSpacing: '0.05em' }}>Order ID</th>
                            <th style={{ padding: '20px 24px', color: '#64748b', fontWeight: '700', textTransform: 'uppercase', fontSize: '0.75rem', letterSpacing: '0.05em' }}>Date</th>
                            <th style={{ padding: '20px 24px', color: '#64748b', fontWeight: '700', textTransform: 'uppercase', fontSize: '0.75rem', letterSpacing: '0.05em' }}>Total Amount</th>
                            <th style={{ padding: '20px 24px', color: '#64748b', fontWeight: '700', textTransform: 'uppercase', fontSize: '0.75rem', letterSpacing: '0.05em' }}>Location (Lat, Lng)</th>
                            <th style={{ padding: '20px 24px', color: '#64748b', fontWeight: '700', textTransform: 'uppercase', fontSize: '0.75rem', letterSpacing: '0.05em' }}>Status</th>
                            <th style={{ padding: '20px 24px', color: '#64748b', fontWeight: '700', textTransform: 'uppercase', fontSize: '0.75rem', letterSpacing: '0.05em' }}>Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        {orders.map((order, index) => (
                            <tr key={order.id} style={{ borderBottom: '1px solid #f1f5f9', transition: 'background-color 0.2s', backgroundColor: index % 2 === 0 ? '#ffffff' : '#fafafa' }} onMouseOver={(e) => e.currentTarget.style.backgroundColor = '#f1f5f9'} onMouseOut={(e) => e.currentTarget.style.backgroundColor = index % 2 === 0 ? '#ffffff' : '#fafafa'}>
                                <td style={{ padding: '20px 24px', fontWeight: '700', color: '#0f172a', fontSize: '0.95rem' }}>#{order.id}</td>
                                <td style={{ padding: '20px 24px', color: '#475569', fontSize: '0.95rem', fontWeight: '500' }}>{new Date(order.orderDate).toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' })}</td>
                                <td style={{ padding: '20px 24px', fontWeight: '700', color: '#0f172a', fontSize: '0.95rem' }}>Rs. {order.totalAmount.toFixed(2)}</td>
                                <td style={{ padding: '20px 24px', fontSize: '0.85rem', color: '#64748b', fontFamily: 'monospace', fontWeight: '500' }}>
                                    {order.latitude ? `${order.latitude.toFixed(4)}, ${order.longitude.toFixed(4)}` : 'No Data'}
                                </td>
                                <td style={{ padding: '20px 24px' }}>
                                    <span style={{
                                        backgroundColor: getStatusColor(order.status) + '1A', // 10% opacity roughly
                                        color: getStatusColor(order.status),
                                        padding: '6px 14px',
                                        borderRadius: '24px',
                                        fontSize: '0.75rem',
                                        fontWeight: '800',
                                        display: 'inline-block',
                                        textTransform: 'uppercase',
                                        letterSpacing: '0.5px',
                                        border: `1px solid ${getStatusColor(order.status)}40`
                                    }}>
                                        {order.status.replace('_', ' ')}
                                    </span>
                                </td>
                                <td style={{ padding: '20px 24px' }}>
                                    <select
                                        value={order.status}
                                        onChange={(e) => handleStatusChange(order.id, e.target.value)}
                                        style={{ padding: '10px 14px', borderRadius: '8px', cursor: 'pointer', border: '1px solid #cbd5e1', backgroundColor: '#ffffff', color: '#334155', fontWeight: '600', fontSize: '0.85rem', outline: 'none', transition: 'all 0.2s', boxShadow: '0 1px 3px rgba(0,0,0,0.05)' }}
                                        onFocus={(e) => { e.target.style.borderColor = '#4CAF50'; e.target.style.boxShadow = '0 0 0 3px rgba(76,175,80,0.15)'; }}
                                        onBlur={(e) => { e.target.style.borderColor = '#cbd5e1'; e.target.style.boxShadow = '0 1px 3px rgba(0,0,0,0.05)'; }}
                                    >
                                        <option value="PENDING">Pending</option>
                                        <option value="PREPARING">Preparing</option>
                                        <option value="ON_THE_WAY">On The Way</option>
                                        <option value="DELIVERED">Delivered</option>
                                        <option value="CANCELLED">Cancelled</option>
                                    </select>
                                </td>
                            </tr>
                        ))}
                        {orders.length === 0 && (
                            <tr>
                                <td colSpan="6" style={{ padding: '60px', textAlign: 'center', color: '#94a3b8' }}>
                                    <div style={{ marginBottom: '16px', fontSize: '3rem' }}>📭</div>
                                    <div style={{ fontSize: '1.2rem', fontWeight: '600', color: '#64748b' }}>No orders found</div>
                                    <div style={{ fontSize: '0.9rem', marginTop: '8px' }}>Waiting for the first customer!</div>
                                </td>
                            </tr>
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    );
}