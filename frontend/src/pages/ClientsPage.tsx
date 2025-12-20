import { useState } from 'react';
import { useClients } from '../hooks';
import { ClientRequest } from '../api';

export function ClientsPage() {
  const { clients, loading, error, createClient, deleteClient, refresh } = useClients();
  const [showForm, setShowForm] = useState(false);
  const [formData, setFormData] = useState<ClientRequest>({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    address: '',
    city: '',
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await createClient(formData);
      setFormData({
        firstName: '',
        lastName: '',
        email: '',
        phone: '',
        address: '',
        city: '',
      });
      setShowForm(false);
    } catch (err) {
      alert('Failed to create client');
    }
  };

  const handleDelete = async (id: string) => {
    if (window.confirm('Are you sure you want to delete this client?')) {
      try {
        await deleteClient(id);
      } catch (err) {
        alert('Failed to delete client');
      }
    }
  };

  if (loading) {
    return <div className="loading">Loading clients...</div>;
  }

  if (error) {
    return (
      <div className="error">
        <p>Error: {error}</p>
        <button className="btn btn-primary" onClick={refresh}>
          Retry
        </button>
      </div>
    );
  }

  return (
    <div>
      <div className="page-header">
        <h1>Clients</h1>
        <button className="btn btn-primary" onClick={() => setShowForm(true)}>
          Add Client
        </button>
      </div>

      {showForm && (
        <div className="modal-overlay" onClick={() => setShowForm(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h2>New Client</h2>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>First Name *</label>
                <input
                  type="text"
                  value={formData.firstName}
                  onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label>Last Name *</label>
                <input
                  type="text"
                  value={formData.lastName}
                  onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label>Email *</label>
                <input
                  type="email"
                  value={formData.email}
                  onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label>Phone</label>
                <input
                  type="tel"
                  value={formData.phone || ''}
                  onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                />
              </div>
              <div className="form-group">
                <label>Address</label>
                <input
                  type="text"
                  value={formData.address || ''}
                  onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                />
              </div>
              <div className="form-group">
                <label>City</label>
                <input
                  type="text"
                  value={formData.city || ''}
                  onChange={(e) => setFormData({ ...formData, city: e.target.value })}
                />
              </div>
              <div className="modal-actions">
                <button type="button" className="btn" onClick={() => setShowForm(false)}>
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary">
                  Create
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {clients.length === 0 ? (
        <div className="empty">
          <p>No clients yet. Add your first client!</p>
        </div>
      ) : (
        <div className="grid">
          {clients.map((client) => (
            <div key={client.id} className="card">
              <h3>
                {client.firstName} {client.lastName}
              </h3>
              <p>Email: {client.email}</p>
              {client.phone && <p>Phone: {client.phone}</p>}
              {client.address && (
                <p>
                  Address: {client.address}
                  {client.city && `, ${client.city}`}
                </p>
              )}
              <p className="meta">
                Created: {new Date(client.createdAt).toLocaleDateString()}
              </p>
              <div className="card-actions">
                <button
                  className="btn btn-danger btn-sm"
                  onClick={() => handleDelete(client.id)}
                >
                  Delete
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
