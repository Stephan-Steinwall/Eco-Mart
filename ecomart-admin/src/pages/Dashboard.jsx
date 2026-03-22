import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';

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

    const handleLogout = () => {
        localStorage.clear();
        navigate('/');
    };

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
        <div style={{ padding: '30px', fontFamily: 'sans-serif', backgroundColor: '#f9f9f9', minHeight: '100vh' }}>

            {/* Header Section */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '30px', backgroundColor: 'white', padding: '20px', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.05)' }}>
                <h1 style={{ margin: 0, color: '#333' }}>EcoMart Admin 🍃</h1>
                <div>

                    <button onClick={handleLogout} style={{ padding: '8px 16px', backgroundColor: '#dc3545', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
                        Logout
                    </button>
                    <span style={{ marginRight: '20px', fontWeight: 'bold' }}>Logged in as: {adminName}</span>
                    <button onClick={() => navigate('/products')} style={{ padding: '8px 16px', marginRight: '10px', backgroundColor: '#1E90FF', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
                        Manage Products
                    </button>

                    <button onClick={() => navigate('/analytics')} style={{ padding: '8px 16px', marginRight: '10px', backgroundColor: '#9370DB', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
                        View Analytics
                    </button>
                </div>
            </div>

            {error && <p style={{ color: 'red', textAlign: 'center' }}>{error}</p>}

            {/* Orders Table */}
            <div style={{ backgroundColor: 'white', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.05)', overflow: 'hidden' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                    <thead style={{ backgroundColor: '#4CAF50', color: 'white' }}>
                        <tr>
                            <th style={{ padding: '15px' }}>Order ID</th>
                            <th style={{ padding: '15px' }}>Date</th>
                            <th style={{ padding: '15px' }}>Total Amount</th>
                            <th style={{ padding: '15px' }}>GPS Target</th>
                            <th style={{ padding: '15px' }}>Status</th>
                            <th style={{ padding: '15px' }}>Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        {orders.map((order) => (
                            <tr key={order.id} style={{ borderBottom: '1px solid #eee' }}>
                                <td style={{ padding: '15px', fontWeight: 'bold' }}>#{order.id}</td>
                                <td style={{ padding: '15px' }}>{new Date(order.orderDate).toLocaleDateString()}</td>
                                <td style={{ padding: '15px' }}>Rs. {order.totalAmount.toFixed(2)}</td>
                                <td style={{ padding: '15px', fontSize: '12px', color: '#666' }}>
                                    {order.latitude ? `${order.latitude.toFixed(4)}, ${order.longitude.toFixed(4)}` : 'N/A'}
                                </td>
                                <td style={{ padding: '15px' }}>
                                    <span style={{
                                        backgroundColor: getStatusColor(order.status),
                                        color: 'white',
                                        padding: '5px 10px',
                                        borderRadius: '20px',
                                        fontSize: '12px',
                                        fontWeight: 'bold'
                                    }}>
                                        {order.status}
                                    </span>
                                </td>
                                <td style={{ padding: '15px' }}>
                                    <select
                                        value={order.status}
                                        onChange={(e) => handleStatusChange(order.id, e.target.value)}
                                        style={{ padding: '5px', borderRadius: '4px', cursor: 'pointer' }}
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
                                <td colSpan="6" style={{ padding: '20px', textAlign: 'center', color: '#888' }}>
                                    No orders found in the database.
                                </td>
                            </tr>
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    );
}