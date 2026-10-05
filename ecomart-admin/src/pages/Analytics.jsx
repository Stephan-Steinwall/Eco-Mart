import { useState, useEffect } from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import api from '../api/axiosConfig';
import AdminNavbar from '../components/AdminNavbar';

export default function Analytics() {
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
            revenue += order.totalAmount;
            const dateStr = new Date(order.orderDate).toLocaleDateString();
            if (!salesByDate[dateStr]) {
                salesByDate[dateStr] = 0;
            }
            salesByDate[dateStr] += order.totalAmount;
        });

        setTotalRevenue(revenue);

        const formattedChartData = Object.keys(salesByDate).map(date => ({
            date: date,
            sales: salesByDate[date]
        }));

        setChartData(formattedChartData);
    };

    return (
        <div className="min-h-screen bg-bg p-8 lg:p-10">
            <AdminNavbar title="Analytics" icon="📊" />

            <div className="flex flex-col sm:flex-row gap-6 mb-8">
                <div className="flex-1 bg-surface border border-outline rounded-2xl p-7 relative overflow-hidden">
                    <div className="absolute left-0 top-0 bottom-0 w-1.5 bg-brand-primary" />
                    <h3 className="text-text-secondary font-bold text-sm uppercase tracking-wide">Total revenue</h3>
                    <p className="font-display text-4xl font-bold text-text-primary mt-3">
                        {loading ? '…' : `Rs. ${totalRevenue.toFixed(2)}`}
                    </p>
                </div>
                <div className="flex-1 bg-surface border border-outline rounded-2xl p-7 relative overflow-hidden">
                    <div className="absolute left-0 top-0 bottom-0 w-1.5 bg-info" />
                    <h3 className="text-text-secondary font-bold text-sm uppercase tracking-wide">Total orders</h3>
                    <p className="font-display text-4xl font-bold text-text-primary mt-3">
                        {loading ? '…' : orders.length}
                    </p>
                </div>
            </div>

            <div className="bg-surface border border-outline rounded-2xl p-8">
                <h2 className="font-display text-xl font-bold text-text-primary mb-6">Revenue over time</h2>

                <div className="w-full h-[380px]">
                    {!loading && chartData.length === 0 ? (
                        <div className="h-full flex flex-col items-center justify-center text-text-tertiary">
                            <div className="text-4xl mb-3">📈</div>
                            <p className="font-semibold">No sales yet — the chart fills in once orders come through.</p>
                        </div>
                    ) : (
                        <ResponsiveContainer>
                            <LineChart data={chartData} margin={{ top: 5, right: 30, left: 10, bottom: 5 }}>
                                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E6E1D6" />
                                <XAxis dataKey="date" axisLine={false} tickLine={false} tick={{ fill: '#716C62', fontSize: 12, fontWeight: 500 }} dy={10} />
                                <YAxis axisLine={false} tickLine={false} tick={{ fill: '#716C62', fontSize: 12, fontWeight: 500 }} dx={-10} />
                                <Tooltip formatter={(value) => `Rs. ${value.toFixed(2)}`} contentStyle={{ borderRadius: '12px', border: '1px solid #E6E1D6', boxShadow: '0 8px 25px rgba(0,0,0,0.08)', fontWeight: 600 }} itemStyle={{ color: '#1F6D4C' }} />
                                <Legend wrapperStyle={{ paddingTop: '20px', fontWeight: 600, color: '#716C62' }} />
                                <Line type="monotone" dataKey="sales" name="Daily sales" stroke="#1F6D4C" strokeWidth={3} dot={{ r: 4, strokeWidth: 2, fill: '#fff', stroke: '#1F6D4C' }} activeDot={{ r: 7, strokeWidth: 0, fill: '#1F6D4C' }} />
                            </LineChart>
                        </ResponsiveContainer>
                    )}
                </div>
            </div>
        </div>
    );
}
