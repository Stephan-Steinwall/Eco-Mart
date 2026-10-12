import { useState, useEffect, useCallback } from 'react';
import api from '../api/axiosConfig';
import AdminNavbar from '../components/AdminNavbar';

const DURATIONS = [
    { label: '30 minutes', minutes: 30 },
    { label: '1 hour', minutes: 60 },
    { label: '3 hours', minutes: 180 },
    { label: '6 hours', minutes: 360 },
    { label: '12 hours', minutes: 720 },
    { label: '24 hours', minutes: 1440 },
    { label: '3 days', minutes: 4320 },
    { label: '7 days', minutes: 10080 },
];

const EMPTY_FORM = { title: '', message: '', discountPercent: 20, durationMinutes: 180 };

const RESULT_STYLES = {
    success: 'bg-brand-primary-container text-brand-primary-dark',
    warning: 'bg-warning-container text-text-primary',
    error: 'bg-error-container text-error',
};

function formatRemaining(ms) {
    const totalMinutes = Math.max(Math.floor(ms / 60000), 0);
    const days = Math.floor(totalMinutes / 1440);
    const hours = Math.floor((totalMinutes % 1440) / 60);
    const minutes = totalMinutes % 60;
    if (days > 0) return `${days}d ${hours}h left`;
    if (hours > 0) return `${hours}h ${minutes}m left`;
    return `${Math.max(minutes, 1)}m left`;
}

export default function Offers() {
    const [offers, setOffers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [form, setForm] = useState(EMPTY_FORM);
    const [confirmOpen, setConfirmOpen] = useState(false);
    const [sending, setSending] = useState(false);
    const [result, setResult] = useState(null);
    const [now, setNow] = useState(() => Date.now());

    const fetchOffers = useCallback(async () => {
        try {
            const response = await api.get('/admin/offers');
            setOffers(response.data);
        } catch (err) {
            console.error('Failed to fetch offers', err);
            setResult({ kind: 'error', text: 'Failed to load offers. Make sure Spring Boot is running!' });
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchOffers();
        // Re-render every 30s so the "time left" labels stay current.
        const tick = setInterval(() => setNow(Date.now()), 30000);
        return () => clearInterval(tick);
    }, [fetchOffers]);

    // Close the confirm dialog with Escape.
    useEffect(() => {
        if (!confirmOpen) return undefined;
        const onKey = (e) => { if (e.key === 'Escape' && !sending) setConfirmOpen(false); };
        window.addEventListener('keydown', onKey);
        return () => window.removeEventListener('keydown', onKey);
    }, [confirmOpen, sending]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setForm({ ...form, [name]: name === 'discountPercent' || name === 'durationMinutes' ? Number(value) : value });
    };

    const handleReview = (e) => {
        e.preventDefault();
        setResult(null);
        setConfirmOpen(true);
    };

    const handleConfirmSend = async () => {
        setSending(true);
        try {
            const { data } = await api.post('/admin/offers', form);
            if (data.pushStatus === 'SENT') {
                setResult({ kind: 'success', text: 'Offer is live and the push notification was sent to all customers.' });
            } else if (data.pushStatus === 'DISABLED') {
                setResult({
                    kind: 'warning',
                    text: 'Offer is live in the app, but no push notification was sent: the server has no Firebase key configured yet (ecomart.fcm.credentials-path).',
                });
            } else {
                setResult({ kind: 'error', text: 'Offer saved, but Firebase failed to deliver the push. Check the backend log.' });
            }
            setForm(EMPTY_FORM);
            fetchOffers();
        } catch (err) {
            console.error('Failed to create offer', err);
            const detail = typeof err.response?.data === 'string' ? err.response.data : err.message;
            setResult({ kind: 'error', text: `Could not create the offer: ${detail}` });
        } finally {
            setSending(false);
            setConfirmOpen(false);
        }
    };

    const handleEnd = async (offer) => {
        if (!window.confirm(`End "${offer.title}" now? It will disappear from the app.`)) return;
        try {
            await api.post(`/admin/offers/${offer.id}/end`);
            fetchOffers();
        } catch (err) {
            console.error('Failed to end offer', err);
            setResult({ kind: 'error', text: 'Failed to end the offer.' });
        }
    };

    const handleDelete = async (offer) => {
        if (!window.confirm(`Delete "${offer.title}" permanently?`)) return;
        try {
            await api.delete(`/admin/offers/${offer.id}`);
            fetchOffers();
        } catch (err) {
            console.error('Failed to delete offer', err);
            setResult({ kind: 'error', text: 'Failed to delete the offer.' });
        }
    };

    const inputClass = 'w-full px-4 py-3 rounded-xl border border-outline-strong bg-bg text-text-primary placeholder:text-text-tertiary outline-none focus:border-brand-primary focus:ring-2 focus:ring-brand-primary-container transition-all';
    const labelClass = 'block text-xs font-bold text-text-secondary tracking-wide mb-1.5';

    const previewTitle = `${form.discountPercent || 0}% OFF — ${form.title.trim() || 'Your offer title'}`;
    const previewBody = form.message.trim() || 'Your offer message appears here.';
    const durationLabel = DURATIONS.find((d) => d.minutes === form.durationMinutes)?.label;

    return (
        <div className="min-h-screen bg-bg p-8 lg:p-10">
            <AdminNavbar title="Offers" icon="🏷️" />

            {result && (
                <div role="status" className={`${RESULT_STYLES[result.kind]} font-semibold rounded-xl px-5 py-4 mb-6 flex items-start justify-between gap-4`}>
                    <span>{result.text}</span>
                    <button onClick={() => setResult(null)} aria-label="Dismiss message" className="text-lg leading-none opacity-60 hover:opacity-100">×</button>
                </div>
            )}

            <div className="grid grid-cols-1 lg:grid-cols-5 gap-6 mb-8">
                <form onSubmit={handleReview} className="lg:col-span-3 bg-surface border border-outline rounded-2xl p-7">
                    <h3 className="font-display text-lg font-bold text-text-primary mb-5">Create a timed offer</h3>

                    <div className="mb-4">
                        <label htmlFor="title" className={labelClass}>Title</label>
                        <input id="title" type="text" name="title" maxLength={80} required placeholder="e.g. Weekend Fresh Produce Sale" value={form.title} onChange={handleChange} className={inputClass} />
                    </div>

                    <div className="mb-4">
                        <label htmlFor="message" className={labelClass}>Message</label>
                        <textarea id="message" name="message" rows={3} maxLength={240} required placeholder="What should customers know?" value={form.message} onChange={handleChange} className={`${inputClass} resize-none`} />
                        <div className="text-xs text-text-tertiary mt-1 text-right">{form.message.length}/240</div>
                    </div>

                    <div className="grid grid-cols-2 gap-4 mb-6">
                        <div>
                            <label htmlFor="discountPercent" className={labelClass}>Discount (%)</label>
                            <input id="discountPercent" type="number" name="discountPercent" min={1} max={90} required value={form.discountPercent} onChange={handleChange} className={inputClass} />
                        </div>
                        <div>
                            <label htmlFor="durationMinutes" className={labelClass}>Runs for</label>
                            <select id="durationMinutes" name="durationMinutes" value={form.durationMinutes} onChange={handleChange} className={`${inputClass} cursor-pointer`}>
                                {DURATIONS.map((d) => <option key={d.minutes} value={d.minutes}>{d.label}</option>)}
                            </select>
                        </div>
                    </div>

                    <button type="submit" className="px-6 py-3 rounded-xl font-bold text-white bg-brand-primary hover:bg-brand-primary-dark transition-all active:scale-[0.98]">
                        Review &amp; send
                    </button>
                </form>

                <div className="lg:col-span-2 bg-surface border border-outline rounded-2xl p-7">
                    <h3 className="font-display text-lg font-bold text-text-primary mb-1">Notification preview</h3>
                    <p className="text-sm text-text-tertiary mb-5">This is what customers will see on their phone.</p>
                    <div className="bg-surface-alt border border-outline rounded-2xl p-4 flex gap-3">
                        <div className="w-9 h-9 rounded-full bg-brand-primary text-white flex items-center justify-center text-lg shrink-0" aria-hidden="true">🌱</div>
                        <div className="min-w-0">
                            <div className="text-xs text-text-tertiary font-semibold mb-0.5">EcoMart · now</div>
                            <div className="font-bold text-text-primary text-sm break-words">{previewTitle}</div>
                            <div className="text-sm text-text-secondary mt-0.5 break-words">{previewBody}</div>
                        </div>
                    </div>
                </div>
            </div>

            <div className="bg-surface border border-outline rounded-2xl overflow-hidden">
                <div className="overflow-x-auto">
                    <table className="w-full text-left border-collapse">
                        <thead className="bg-surface-alt border-b border-outline">
                            <tr>
                                {['Offer', 'Discount', 'Status', 'Created', 'Actions'].map((h, i) => (
                                    <th key={h} className={`px-6 py-4 text-text-secondary font-bold text-xs tracking-wide ${i === 4 ? 'text-right' : ''}`}>{h}</th>
                                ))}
                            </tr>
                        </thead>
                        <tbody>
                            {loading && (
                                <tr><td colSpan={5} className="px-6 py-16 text-center text-text-tertiary">Loading offers…</td></tr>
                            )}
                            {!loading && offers.map((offer) => {
                                const remaining = new Date(offer.expiresAt).getTime() - now;
                                const isActive = remaining > 0;
                                return (
                                    <tr key={offer.id} className="border-b border-outline last:border-0 hover:bg-surface-alt/60 transition-colors">
                                        <td className="px-6 py-4 max-w-md">
                                            <div className="font-semibold text-text-primary">{offer.title}</div>
                                            <div className="text-sm text-text-secondary truncate">{offer.message}</div>
                                        </td>
                                        <td className="px-6 py-4 font-bold text-text-primary">{offer.discountPercent}%</td>
                                        <td className="px-6 py-4">
                                            <span className={`inline-block px-3 py-1.5 rounded-full text-xs font-extrabold tracking-wide ${isActive ? 'bg-status-delivered/15 text-status-delivered' : 'bg-surface-alt text-text-secondary'}`}>
                                                {isActive ? `Active · ${formatRemaining(remaining)}` : 'Ended'}
                                            </span>
                                        </td>
                                        <td className="px-6 py-4 text-text-secondary text-sm font-medium">
                                            {new Date(offer.createdAt).toLocaleString(undefined, { month: 'short', day: 'numeric', hour: 'numeric', minute: '2-digit' })}
                                        </td>
                                        <td className="px-6 py-4">
                                            <div className="flex gap-2.5 justify-end">
                                                {isActive && (
                                                    <button onClick={() => handleEnd(offer)} className="px-4 py-2 rounded-lg text-xs font-bold text-warning bg-warning-container border border-warning/30 hover:brightness-95 transition-all">End now</button>
                                                )}
                                                <button onClick={() => handleDelete(offer)} className="px-4 py-2 rounded-lg text-xs font-bold text-error bg-error-container border border-error/20 hover:brightness-95 transition-all">Delete</button>
                                            </div>
                                        </td>
                                    </tr>
                                );
                            })}
                            {!loading && offers.length === 0 && (
                                <tr>
                                    <td colSpan={5} className="px-6 py-20 text-center">
                                        <div className="text-5xl mb-4">🏷️</div>
                                        <div className="text-lg font-bold text-text-secondary">No offers yet</div>
                                        <div className="text-sm text-text-tertiary mt-1">Create your first timed offer above.</div>
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>

            {confirmOpen && (
                <div className="fixed inset-0 z-50 bg-black/40 flex items-center justify-center p-4" onClick={() => !sending && setConfirmOpen(false)}>
                    <div role="dialog" aria-modal="true" aria-labelledby="confirm-title" className="bg-surface rounded-2xl p-7 w-full max-w-md shadow-xl" onClick={(e) => e.stopPropagation()}>
                        <h3 id="confirm-title" className="font-display text-xl font-bold text-text-primary mb-2">Send this offer to all customers?</h3>
                        <p className="text-sm text-text-secondary mb-4">
                            The offer goes live immediately for <strong>{durationLabel}</strong> and every customer's phone gets this notification. A sent notification can't be recalled.
                        </p>
                        <div className="bg-surface-alt border border-outline rounded-xl p-4 mb-6">
                            <div className="font-bold text-text-primary text-sm break-words">{previewTitle}</div>
                            <div className="text-sm text-text-secondary mt-0.5 break-words">{previewBody}</div>
                        </div>
                        <div className="flex gap-3 justify-end">
                            <button autoFocus onClick={() => setConfirmOpen(false)} disabled={sending} className="px-5 py-2.5 rounded-xl font-semibold text-text-secondary border border-outline-strong bg-surface hover:bg-surface-alt transition-colors disabled:opacity-50">
                                Cancel
                            </button>
                            <button onClick={handleConfirmSend} disabled={sending} className="px-5 py-2.5 rounded-xl font-bold text-white bg-brand-primary hover:bg-brand-primary-dark transition-all disabled:opacity-60">
                                {sending ? 'Sending…' : 'Confirm & send'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
