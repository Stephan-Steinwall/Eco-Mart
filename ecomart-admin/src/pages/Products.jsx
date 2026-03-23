import { useState, useEffect } from 'react';
import api from '../api/axiosConfig';
import AdminNavbar from '../components/AdminNavbar';

export default function Products() {
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);

    // Form State
    const [editingId, setEditingId] = useState(null);
    const [formData, setFormData] = useState({ name: '', description: '', price: '', imageUrl: '' });

    async function fetchProducts() {
        try {
            const response = await api.get('/admin/products');
            setProducts(response.data);
            setLoading(false);
        } catch (err) {
            console.error("Failed to fetch products", err);
            setLoading(false);
        }
    }

    useEffect(() => {
        fetchProducts();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const handleInputChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            if (editingId) {
                // Update existing
                await api.put(`/admin/products/${editingId}`, formData);
            } else {
                // Create new
                await api.post('/admin/products', formData);
            }
            // Reset form and refresh list
            setFormData({ name: '', description: '', price: '', imageUrl: '' });
            setEditingId(null);
            fetchProducts();
        } catch (error) {
            console.error("Failed to save product:", error);
            alert("Error saving product!");
        }
    };

    const handleEdit = (product) => {
        setEditingId(product.id);
        setFormData({ name: product.name, description: product.description, price: product.price, imageUrl: product.imageUrl });
    };

    const handleDelete = async (id) => {
        if (window.confirm("Are you sure you want to delete this product?")) {
            try {
                await api.delete(`/admin/products/${id}`);
                fetchProducts();
            } catch (error) {
                console.error("Failed to delete product:", error);
                alert("Error deleting product.");
            }
        }
    };

    if (loading) return <h2>Loading Products...</h2>;

    return (
        <div style={{ padding: '40px', fontFamily: '"Inter", system-ui, -apple-system, sans-serif', backgroundColor: '#f4f7f6', minHeight: '100vh', color: '#1a1a1a' }}>
            <AdminNavbar title="Product Management" icon="📦" />

            {/* The Add/Edit Form */}
            <div style={{ backgroundColor: '#ffffff', padding: '30px', borderRadius: '16px', marginBottom: '40px', boxShadow: '0 4px 20px rgba(0,0,0,0.03)', border: '1px solid #f1f5f9' }}>
                <h3 style={{ marginTop: 0, marginBottom: '20px', color: '#475569', fontSize: '1.2rem', fontWeight: '700' }}>{editingId ? '✏️ Edit Product' : '➕ Add New Product'}</h3>
                <form onSubmit={handleSubmit} style={{ display: 'flex', gap: '16px', flexWrap: 'wrap', alignItems: 'center' }}>
                    <input type="text" name="name" placeholder="Product Name" value={formData.name} onChange={handleInputChange} required style={{ padding: '12px 16px', flex: '1', minWidth: '200px', borderRadius: '10px', border: '1px solid #cbd5e1', outline: 'none', fontSize: '1rem', transition: 'box-shadow 0.2s, border-color 0.2s', backgroundColor: '#f8fafc' }} onFocus={(e) => { e.target.style.borderColor = '#1E90FF'; e.target.style.boxShadow = '0 0 0 3px rgba(30,144,255,0.15)'; }} onBlur={(e) => { e.target.style.borderColor = '#cbd5e1'; e.target.style.boxShadow = 'none'; }} />
                    <input type="text" name="description" placeholder="Description" value={formData.description} onChange={handleInputChange} required style={{ padding: '12px 16px', flex: '2', minWidth: '300px', borderRadius: '10px', border: '1px solid #cbd5e1', outline: 'none', fontSize: '1rem', transition: 'box-shadow 0.2s, border-color 0.2s', backgroundColor: '#f8fafc' }} onFocus={(e) => { e.target.style.borderColor = '#1E90FF'; e.target.style.boxShadow = '0 0 0 3px rgba(30,144,255,0.15)'; }} onBlur={(e) => { e.target.style.borderColor = '#cbd5e1'; e.target.style.boxShadow = 'none'; }} />
                    <input type="number" name="price" placeholder="Price (Rs)" value={formData.price} onChange={handleInputChange} required style={{ padding: '12px 16px', width: '120px', borderRadius: '10px', border: '1px solid #cbd5e1', outline: 'none', fontSize: '1rem', transition: 'box-shadow 0.2s, border-color 0.2s', backgroundColor: '#f8fafc' }} onFocus={(e) => { e.target.style.borderColor = '#1E90FF'; e.target.style.boxShadow = '0 0 0 3px rgba(30,144,255,0.15)'; }} onBlur={(e) => { e.target.style.borderColor = '#cbd5e1'; e.target.style.boxShadow = 'none'; }} />
                    <input type="text" name="imageUrl" placeholder="Image URL" value={formData.imageUrl} onChange={handleInputChange} style={{ padding: '12px 16px', flex: '1', minWidth: '200px', borderRadius: '10px', border: '1px solid #cbd5e1', outline: 'none', fontSize: '1rem', transition: 'box-shadow 0.2s, border-color 0.2s', backgroundColor: '#f8fafc' }} onFocus={(e) => { e.target.style.borderColor = '#1E90FF'; e.target.style.boxShadow = '0 0 0 3px rgba(30,144,255,0.15)'; }} onBlur={(e) => { e.target.style.borderColor = '#cbd5e1'; e.target.style.boxShadow = 'none'; }} />
                    
                    <button type="submit" style={{ padding: '12px 24px', backgroundColor: editingId ? '#1E90FF' : '#4CAF50', color: 'white', border: 'none', borderRadius: '10px', cursor: 'pointer', fontWeight: '700', fontSize: '1rem', transition: 'all 0.2s', boxShadow: editingId ? '0 4px 12px rgba(30,144,255,0.3)' : '0 4px 12px rgba(76,175,80,0.3)' }} onMouseOver={(e) => { e.currentTarget.style.transform = 'translateY(-2px)'; e.currentTarget.style.boxShadow = editingId ? '0 6px 16px rgba(30,144,255,0.4)' : '0 6px 16px rgba(76,175,80,0.4)'; }} onMouseOut={(e) => { e.currentTarget.style.transform = 'translateY(0)'; e.currentTarget.style.boxShadow = editingId ? '0 4px 12px rgba(30,144,255,0.3)' : '0 4px 12px rgba(76,175,80,0.3)'; }}>
                        {editingId ? 'Update Product' : 'Add Product'}
                    </button>
                    {editingId && (
                        <button type="button" onClick={() => { setEditingId(null); setFormData({ name: '', description: '', price: '', imageUrl: '' }); }} style={{ padding: '12px 24px', backgroundColor: '#ffffff', color: '#64748b', border: '1px solid #cbd5e1', borderRadius: '10px', cursor: 'pointer', fontWeight: '600', fontSize: '1rem', transition: 'all 0.2s' }} onMouseOver={(e) => { e.currentTarget.style.backgroundColor = '#f1f5f9'; }} onMouseOut={(e) => { e.currentTarget.style.backgroundColor = '#ffffff'; }}>
                            Cancel
                        </button>
                    )}
                </form>
            </div>

            {/* The Products Table */}
            <div style={{ backgroundColor: '#ffffff', borderRadius: '16px', overflow: 'hidden', boxShadow: '0 4px 20px rgba(0,0,0,0.04)', border: '1px solid #f1f5f9' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                    <thead style={{ backgroundColor: '#f8fafc', borderBottom: '2px solid #e2e8f0' }}>
                        <tr>
                            <th style={{ padding: '20px 24px', color: '#64748b', fontWeight: '700', textTransform: 'uppercase', fontSize: '0.75rem', letterSpacing: '0.05em' }}>ID</th>
                            <th style={{ padding: '20px 24px', color: '#64748b', fontWeight: '700', textTransform: 'uppercase', fontSize: '0.75rem', letterSpacing: '0.05em' }}>Name</th>
                            <th style={{ padding: '20px 24px', color: '#64748b', fontWeight: '700', textTransform: 'uppercase', fontSize: '0.75rem', letterSpacing: '0.05em' }}>Price</th>
                            <th style={{ padding: '20px 24px', color: '#64748b', fontWeight: '700', textTransform: 'uppercase', fontSize: '0.75rem', letterSpacing: '0.05em', textAlign: 'right' }}>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {products.map((product, index) => (
                            <tr key={product.id} style={{ borderBottom: '1px solid #f1f5f9', transition: 'background-color 0.2s', backgroundColor: index % 2 === 0 ? '#ffffff' : '#fafafa' }} onMouseOver={(e) => e.currentTarget.style.backgroundColor = '#f1f5f9'} onMouseOut={(e) => e.currentTarget.style.backgroundColor = index % 2 === 0 ? '#ffffff' : '#fafafa'}>
                                <td style={{ padding: '20px 24px', fontWeight: '700', color: '#0f172a', fontSize: '0.95rem' }}>{product.id}</td>
                                <td style={{ padding: '20px 24px', fontWeight: '600', color: '#334155', fontSize: '1rem' }}>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                                        {product.imageUrl && (
                                            <div style={{ width: '40px', height: '40px', borderRadius: '8px', overflow: 'hidden', border: '1px solid #e2e8f0', flexShrink: 0 }}>
                                                <img src={product.imageUrl} alt={product.name} style={{ width: '100%', height: '100%', objectFit: 'cover' }} onError={(e) => { e.target.style.display = 'none'; }} />
                                            </div>
                                        )}
                                        <span>{product.name}</span>
                                    </div>
                                </td>
                                <td style={{ padding: '20px 24px', fontWeight: '700', color: '#0f172a', fontSize: '0.95rem' }}>Rs. {product.price}</td>
                                <td style={{ padding: '20px 24px', display: 'flex', gap: '12px', justifyContent: 'flex-end' }}>
                                    <button onClick={() => handleEdit(product)} style={{ padding: '8px 16px', backgroundColor: '#FFF3E0', color: '#FF9800', border: '1px solid #FFE0B2', borderRadius: '8px', cursor: 'pointer', fontWeight: '600', fontSize: '0.85rem', transition: 'all 0.2s' }} onMouseOver={(e) => { e.currentTarget.style.backgroundColor = '#FFE0B2'; e.currentTarget.style.transform = 'translateY(-1px)'; }} onMouseOut={(e) => { e.currentTarget.style.backgroundColor = '#FFF3E0'; e.currentTarget.style.transform = 'translateY(0)'; }}>Edit</button>
                                    <button onClick={() => handleDelete(product.id)} style={{ padding: '8px 16px', backgroundColor: '#FEE2E2', color: '#DC2626', border: '1px solid #FECACA', borderRadius: '8px', cursor: 'pointer', fontWeight: '600', fontSize: '0.85rem', transition: 'all 0.2s' }} onMouseOver={(e) => { e.currentTarget.style.backgroundColor = '#FECACA'; e.currentTarget.style.transform = 'translateY(-1px)'; }} onMouseOut={(e) => { e.currentTarget.style.backgroundColor = '#FEE2E2'; e.currentTarget.style.transform = 'translateY(0)'; }}>Delete</button>
                                </td>
                            </tr>
                        ))}
                        {products.length === 0 && (
                            <tr>
                                <td colSpan="4" style={{ padding: '60px', textAlign: 'center', color: '#94a3b8' }}>
                                    <div style={{ marginBottom: '16px', fontSize: '3rem' }}>🛍️</div>
                                    <div style={{ fontSize: '1.2rem', fontWeight: '600', color: '#64748b' }}>No products yet</div>
                                    <div style={{ fontSize: '0.9rem', marginTop: '8px' }}>Add some products to get started!</div>
                                </td>
                            </tr>
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    );
}