import { useState, useEffect } from 'react';
import api from '../api/axiosConfig';
import AdminNavbar from '../components/AdminNavbar';

const STATUS_STYLES = {
    PENDING: 'bg-status-pending/15 text-status-pending',
    PREPARING: 'bg-status-preparing/15 text-status-preparing',
    ON_THE_WAY: 'bg-status-on-the-way/15 text-status-on-the-way',
    DELIVERED: 'bg-status-delivered/15 text-status-delivered',
    CANCELLED: 'bg-status-cancelled/15 text-status-cancelled',
};

export default function Dashboard() {
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

    return (
        <div className="min-h-screen bg-bg p-8 lg:p-10">
            <AdminNavbar title="Orders" icon="📦" />

            {error && (
                <div className="bg-error-container text-error font-semibold rounded-xl px-5 py-4 mb-6 text-center">
                    {error}
                </div>
            )}

            <div className="bg-surface border border-outline rounded-2xl overflow-hidden">
                <div className="overflow-x-auto">
                    <table className="w-full text-left border-collapse">
                        <thead className="bg-surface-alt border-b border-outline">
                            <tr>
                                {['Order', 'Date', 'Total', 'Location', 'Status', 'Update'].map((h) => (
                                    <th key={h} className="px-6 py-4 text-text-secondary font-bold text-xs tracking-wide">{h}</th>
                                ))}
                            </tr>
                        </thead>
                        <tbody>
                            {loading && (
                                <tr><td colSpan={6} className="px-6 py-16 text-center text-text-tertiary">Loading orders…</td></tr>
                            )}
                            {!loading && orders.map((order) => (
                                <tr key={order.id} className="border-b border-outline last:border-0 hover:bg-surface-alt/60 transition-colors">
                                    <td className="px-6 py-5 font-bold text-text-primary">#{order.id}</td>
                                    <td className="px-6 py-5 text-text-secondary font-medium">
                                        {new Date(order.orderDate).toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' })}
                                    </td>
                                    <td className="px-6 py-5 font-bold text-text-primary">Rs. {order.totalAmount.toFixed(2)}</td>
                                    <td className="px-6 py-5 text-xs text-text-secondary font-mono">
                                        {order.latitude ? `${order.latitude.toFixed(4)}, ${order.longitude.toFixed(4)}` : 'No data'}
                                    </td>
                                    <td className="px-6 py-5">
                                        <span className={`inline-block px-3 py-1.5 rounded-full text-xs font-extrabold tracking-wide ${STATUS_STYLES[order.status] || 'bg-surface-alt text-text-secondary'}`}>
                                            {order.status.replace('_', ' ')}
                                        </span>
                                    </td>
                                    <td className="px-6 py-5">
                                        <select
                                            value={order.status}
                                            onChange={(e) => handleStatusChange(order.id, e.target.value)}
                                            className="px-3 py-2 rounded-lg border border-outline-strong bg-surface text-text-primary font-semibold text-sm cursor-pointer outline-none focus:border-brand-primary focus:ring-2 focus:ring-brand-primary-container"
                                        >
                                            <option value="PENDING">Pending</option>
                                            <option value="PREPARING">Preparing</option>
                                            <option value="ON_THE_WAY">On the way</option>
                                            <option value="DELIVERED">Delivered</option>
                                            <option value="CANCELLED">Cancelled</option>
                                        </select>
                                    </td>
                                </tr>
                            ))}
                            {!loading && orders.length === 0 && (
                                <tr>
                                    <td colSpan={6} className="px-6 py-20 text-center">
                                        <div className="text-5xl mb-4">📭</div>
                                        <div className="text-lg font-bold text-text-secondary">No orders yet</div>
                                        <div className="text-sm text-text-tertiary mt-1">Waiting for the first customer!</div>
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
