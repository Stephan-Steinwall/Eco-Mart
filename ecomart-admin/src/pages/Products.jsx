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
                await api.put(`/admin/products/${editingId}`, formData);
            } else {
                await api.post('/admin/products', formData);
            }
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

    const inputClass = "px-4 py-3 rounded-xl border border-outline-strong bg-bg text-text-primary placeholder:text-text-tertiary outline-none focus:border-brand-primary focus:ring-2 focus:ring-brand-primary-container transition-all";

    return (
        <div className="min-h-screen bg-bg p-8 lg:p-10">
            <AdminNavbar title="Products" icon="🥬" />

            <div className="bg-surface border border-outline rounded-2xl p-7 mb-8">
                <h3 className="font-display text-lg font-bold text-text-primary mb-5">
                    {editingId ? 'Edit product' : 'Add a new product'}
                </h3>
                <form onSubmit={handleSubmit} className="flex flex-wrap gap-4 items-center">
                    <input type="text" name="name" placeholder="Product name" value={formData.name} onChange={handleInputChange} required className={`${inputClass} flex-1 min-w-[200px]`} />
                    <input type="text" name="description" placeholder="Description" value={formData.description} onChange={handleInputChange} required className={`${inputClass} flex-[2] min-w-[280px]`} />
                    <input type="number" name="price" placeholder="Price (Rs)" value={formData.price} onChange={handleInputChange} required className={`${inputClass} w-32`} />
                    <input type="text" name="imageUrl" placeholder="Image URL" value={formData.imageUrl} onChange={handleInputChange} className={`${inputClass} flex-1 min-w-[200px]`} />

                    <button type="submit" className={`px-6 py-3 rounded-xl font-bold text-white transition-all active:scale-[0.98] ${editingId ? 'bg-info hover:brightness-95' : 'bg-brand-primary hover:bg-brand-primary-dark'}`}>
                        {editingId ? 'Update product' : 'Add product'}
                    </button>
                    {editingId && (
                        <button type="button" onClick={() => { setEditingId(null); setFormData({ name: '', description: '', price: '', imageUrl: '' }); }} className="px-6 py-3 rounded-xl font-semibold text-text-secondary border border-outline-strong bg-surface hover:bg-surface-alt transition-colors">
                            Cancel
                        </button>
                    )}
                </form>
            </div>

            <div className="bg-surface border border-outline rounded-2xl overflow-hidden">
                <div className="overflow-x-auto">
                    <table className="w-full text-left border-collapse">
                        <thead className="bg-surface-alt border-b border-outline">
                            <tr>
                                <th className="px-6 py-4 text-text-secondary font-bold text-xs tracking-wide">ID</th>
                                <th className="px-6 py-4 text-text-secondary font-bold text-xs tracking-wide">Name</th>
                                <th className="px-6 py-4 text-text-secondary font-bold text-xs tracking-wide">Price</th>
                                <th className="px-6 py-4 text-text-secondary font-bold text-xs tracking-wide text-right">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {loading && (
                                <tr><td colSpan={4} className="px-6 py-16 text-center text-text-tertiary">Loading products…</td></tr>
                            )}
                            {!loading && products.map((product) => (
                                <tr key={product.id} className="border-b border-outline last:border-0 hover:bg-surface-alt/60 transition-colors">
                                    <td className="px-6 py-4 font-bold text-text-primary">{product.id}</td>
                                    <td className="px-6 py-4">
                                        <div className="flex items-center gap-3">
                                            {product.imageUrl && (
                                                <img
                                                    src={product.imageUrl}
                                                    alt={product.name}
                                                    className="w-10 h-10 rounded-lg object-cover border border-outline shrink-0"
                                                    onError={(e) => { e.target.style.display = 'none'; }}
                                                />
                                            )}
                                            <span className="font-semibold text-text-primary">{product.name}</span>
                                        </div>
                                    </td>
                                    <td className="px-6 py-4 font-bold text-text-primary">Rs. {product.price}</td>
                                    <td className="px-6 py-4">
                                        <div className="flex gap-2.5 justify-end">
                                            <button onClick={() => handleEdit(product)} className="px-4 py-2 rounded-lg text-xs font-bold text-warning bg-warning-container border border-warning/30 hover:brightness-95 transition-all">Edit</button>
                                            <button onClick={() => handleDelete(product.id)} className="px-4 py-2 rounded-lg text-xs font-bold text-error bg-error-container border border-error/20 hover:brightness-95 transition-all">Delete</button>
                                        </div>
                                    </td>
                                </tr>
                            ))}
                            {!loading && products.length === 0 && (
                                <tr>
                                    <td colSpan={4} className="px-6 py-20 text-center">
                                        <div className="text-5xl mb-4">🛍️</div>
                                        <div className="text-lg font-bold text-text-secondary">No products yet</div>
                                        <div className="text-sm text-text-tertiary mt-1">Add some products to get started!</div>
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
