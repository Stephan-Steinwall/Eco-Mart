import { useState, useEffect } from 'react';
import api from '../api/axiosConfig';

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
        <div style={{ padding: '30px', fontFamily: 'sans-serif', backgroundColor: '#f9f9f9', minHeight: '100vh' }}>
            <h1 style={{ color: '#333' }}>📦 Product Management</h1>

            {/* The Add/Edit Form */}
            <div style={{ backgroundColor: 'white', padding: '20px', borderRadius: '8px', marginBottom: '30px', boxShadow: '0 2px 4px rgba(0,0,0,0.05)' }}>
                <h3>{editingId ? 'Edit Product' : 'Add New Product'}</h3>
                <form onSubmit={handleSubmit} style={{ display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
                    <input type="text" name="name" placeholder="Product Name" value={formData.name} onChange={handleInputChange} required style={{ padding: '8px', flex: '1' }} />
                    <input type="text" name="description" placeholder="Description" value={formData.description} onChange={handleInputChange} required style={{ padding: '8px', flex: '2' }} />
                    <input type="number" name="price" placeholder="Price (Rs)" value={formData.price} onChange={handleInputChange} required style={{ padding: '8px', width: '100px' }} />
                    <input type="text" name="imageUrl" placeholder="Image URL" value={formData.imageUrl} onChange={handleInputChange} style={{ padding: '8px', flex: '1' }} />
                    <button type="submit" style={{ padding: '8px 16px', backgroundColor: editingId ? '#1E90FF' : '#4CAF50', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
                        {editingId ? 'Update' : 'Add'}
                    </button>
                    {editingId && (
                        <button type="button" onClick={() => { setEditingId(null); setFormData({ name: '', description: '', price: '', imageUrl: '' }); }} style={{ padding: '8px 16px', backgroundColor: '#ccc', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
                            Cancel
                        </button>
                    )}
                </form>
            </div>

            {/* The Products Table */}
            <div style={{ backgroundColor: 'white', borderRadius: '8px', overflow: 'hidden', boxShadow: '0 2px 4px rgba(0,0,0,0.05)' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                    <thead style={{ backgroundColor: '#333', color: 'white' }}>
                        <tr>
                            <th style={{ padding: '15px' }}>ID</th>
                            <th style={{ padding: '15px' }}>Name</th>
                            <th style={{ padding: '15px' }}>Price</th>
                            <th style={{ padding: '15px' }}>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {products.map((product) => (
                            <tr key={product.id} style={{ borderBottom: '1px solid #eee' }}>
                                <td style={{ padding: '15px' }}>{product.id}</td>
                                <td style={{ padding: '15px', fontWeight: 'bold' }}>{product.name}</td>
                                <td style={{ padding: '15px' }}>Rs. {product.price}</td>
                                <td style={{ padding: '15px', display: 'flex', gap: '10px' }}>
                                    <button onClick={() => handleEdit(product)} style={{ padding: '5px 10px', backgroundColor: '#FFA500', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>Edit</button>
                                    <button onClick={() => handleDelete(product.id)} style={{ padding: '5px 10px', backgroundColor: '#dc3545', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>Delete</button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}