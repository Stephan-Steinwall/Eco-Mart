import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, BarChart, Bar } from 'recharts';
import api from '../api/axiosConfig';

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
        <div style={{ padding: '30px', fontFamily: 'sans-serif', backgroundColor: '#f9f9f9', minHeight: '100vh' }}>

            {/* Header */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '30px', backgroundColor: 'white', padding: '20px', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.05)' }}>
                <h1 style={{ margin: 0, color: '#333' }}>📊 Sales Analytics</h1>
                <div>
                    <button onClick={() => navigate('/dashboard')} style={{ padding: '8px 16px', marginRight: '10px', backgroundColor: '#4CAF50', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
                        Back to Orders
                    </button>
                    <button onClick={() => navigate('/products')} style={{ padding: '8px 16px', backgroundColor: '#1E90FF', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
                        Manage Products
                    </button>
                </div>
            </div>

            {/* Top KPI Cards */}
            <div style={{ display: 'flex', gap: '20px', marginBottom: '30px' }}>
                <div style={{ flex: 1, backgroundColor: 'white', padding: '20px', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.05)', borderLeft: '5px solid #4CAF50' }}>
                    <h3 style={{ margin: 0, color: '#666' }}>Total Revenue</h3>
                    <h1 style={{ margin: '10px 0 0 0', color: '#333' }}>Rs. {totalRevenue.toFixed(2)}</h1>
                </div>
                <div style={{ flex: 1, backgroundColor: 'white', padding: '20px', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.05)', borderLeft: '5px solid #1E90FF' }}>
                    <h3 style={{ margin: 0, color: '#666' }}>Total Orders</h3>
                    <h1 style={{ margin: '10px 0 0 0', color: '#333' }}>{orders.length}</h1>
                </div>
            </div>

            {/* The Recharts Graph */}
            <div style={{ backgroundColor: 'white', padding: '30px', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.05)' }}>
                <h2 style={{ marginTop: 0, color: '#333', marginBottom: '20px' }}>Revenue Over Time</h2>

                <div style={{ width: '100%', height: 400 }}>
                    <ResponsiveContainer>
                        <LineChart data={chartData} margin={{ top: 5, right: 30, left: 20, bottom: 5 }}>
                            <CartesianGrid strokeDasharray="3 3" />
                            <XAxis dataKey="date" />
                            <YAxis />
                            <Tooltip formatter={(value) => `Rs. ${value.toFixed(2)}`} />
                            <Legend />
                            <Line type="monotone" dataKey="sales" name="Daily Sales" stroke="#4CAF50" strokeWidth={3} activeDot={{ r: 8 }} />
                        </LineChart>
                    </ResponsiveContainer>
                </div>
            </div>

        </div>
    );
}