import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, BarChart, Bar } from 'recharts';
import api from '../api/axiosConfig';
import AdminNavbar from '../components/AdminNavbar';

export default function Analytics() {
    const navigate = useNavigate();
    const [orders, setOrders] = useState([]);
    const [chartData, setChartData] = useState([]);
    const [totalRevenue, setTotalRevenue] = useState(0);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchData();
    }, []);

    const fetchData = async () => {
        try {
            const response = await api.get('/admin/orders');
            const fetchedOrders = response.data;
            setOrders(fetchedOrders);
            processAnalytics(fetchedOrders);
            setLoading(false);
        } catch (err) {
            console.error("Failed to fetch analytics data", err);
            setLoading(false);
        }
    };

    // This function crunches the numbers for our charts!
    const processAnalytics = (data) => {
        let revenue = 0;
        const salesByDate = {};

        data.forEach(order => {
            // Add to total revenue
            revenue += order.totalAmount;

            // Group sales by date
            const dateStr = new Date(order.orderDate).toLocaleDateString();
            if (!salesByDate[dateStr]) {
                salesByDate[dateStr] = 0;
            }
            salesByDate[dateStr] += order.totalAmount;
        });

        setTotalRevenue(revenue);

        // Convert our grouped object into an array for Recharts
        const formattedChartData = Object.keys(salesByDate).map(date => ({
            date: date,
            sales: salesByDate[date]
        }));

        setChartData(formattedChartData);
    };

    if (loading) return <h2 style={{ textAlign: 'center', marginTop: '50px' }}>Loading Analytics...</h2>;

    return (
        <div style={{ padding: '40px', fontFamily: '"Inter", system-ui, -apple-system, sans-serif', backgroundColor: '#f4f7f6', minHeight: '100vh', color: '#1a1a1a' }}>

            <AdminNavbar title="Sales Analytics" icon="📊" />

            {/* Top KPI Cards */}
            <div style={{ display: 'flex', gap: '24px', marginBottom: '40px' }}>
                <div style={{ flex: 1, backgroundColor: '#ffffff', padding: '30px', borderRadius: '16px', boxShadow: '0 4px 20px rgba(0,0,0,0.04)', border: '1px solid #f1f5f9', position: 'relative', overflow: 'hidden' }}>
                    <div style={{ width: '6px', height: '100%', backgroundColor: '#4CAF50', position: 'absolute', left: 0, top: 0 }}></div>
                    <h3 style={{ margin: 0, color: '#64748b', fontSize: '1rem', fontWeight: '600', textTransform: 'uppercase', letterSpacing: '0.5px' }}>Total Revenue</h3>
                    <h1 style={{ margin: '12px 0 0 0', color: '#0f172a', fontSize: '2.5rem', fontWeight: '800', letterSpacing: '-1px' }}>Rs. {totalRevenue.toFixed(2)}</h1>
                </div>
                <div style={{ flex: 1, backgroundColor: '#ffffff', padding: '30px', borderRadius: '16px', boxShadow: '0 4px 20px rgba(0,0,0,0.04)', border: '1px solid #f1f5f9', position: 'relative', overflow: 'hidden' }}>
                    <div style={{ width: '6px', height: '100%', backgroundColor: '#1E90FF', position: 'absolute', left: 0, top: 0 }}></div>
                    <h3 style={{ margin: 0, color: '#64748b', fontSize: '1rem', fontWeight: '600', textTransform: 'uppercase', letterSpacing: '0.5px' }}>Total Orders</h3>
                    <h1 style={{ margin: '12px 0 0 0', color: '#0f172a', fontSize: '2.5rem', fontWeight: '800', letterSpacing: '-1px' }}>{orders.length}</h1>
                </div>
            </div>

            {/* The Recharts Graph */}
            <div style={{ backgroundColor: '#ffffff', padding: '32px', borderRadius: '16px', boxShadow: '0 4px 20px rgba(0,0,0,0.04)', border: '1px solid #f1f5f9' }}>
                <h2 style={{ marginTop: 0, color: '#2c3e50', marginBottom: '24px', fontSize: '1.4rem', fontWeight: '700' }}>Revenue Over Time</h2>

                <div style={{ width: '100%', height: 400 }}>
                    <ResponsiveContainer>
                        <LineChart data={chartData} margin={{ top: 5, right: 30, left: 20, bottom: 5 }}>
                            <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                            <XAxis dataKey="date" axisLine={false} tickLine={false} tick={{ fill: '#64748b', fontSize: 12, fontWeight: 500 }} dy={10} />
                            <YAxis axisLine={false} tickLine={false} tick={{ fill: '#64748b', fontSize: 12, fontWeight: 500 }} dx={-10} />
                            <Tooltip formatter={(value) => `Rs. ${value.toFixed(2)}`} contentStyle={{ borderRadius: '12px', border: 'none', boxShadow: '0 8px 25px rgba(0,0,0,0.1)', fontWeight: '600' }} itemStyle={{ color: '#4CAF50' }} />
                            <Legend wrapperStyle={{ paddingTop: '20px', fontWeight: '600', color: '#475569' }} />
                            <Line type="monotone" dataKey="sales" name="Daily Sales" stroke="#4CAF50" strokeWidth={4} dot={{ r: 4, strokeWidth: 2, fill: '#ffffff', stroke: '#4CAF50' }} activeDot={{ r: 8, strokeWidth: 0, fill: '#4CAF50', filter: 'drop-shadow(0px 0px 5px rgba(76,175,80,0.5))' }} />
                        </LineChart>
                    </ResponsiveContainer>
                </div>
            </div>

        </div>
    );
}