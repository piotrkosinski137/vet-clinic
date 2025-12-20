import { useState } from 'react';
import { usePatients } from '../hooks';
import { PatientRequest, Species } from '../api';

const SPECIES_OPTIONS: Species[] = ['DOG', 'CAT', 'BIRD', 'RABBIT', 'HAMSTER', 'FISH', 'REPTILE', 'OTHER'];

export function PatientsPage() {
  const { patients, loading, error, createPatient, deletePatient, refresh } = usePatients();
  const [showForm, setShowForm] = useState(false);
  const [formData, setFormData] = useState<PatientRequest>({
    name: '',
    species: 'DOG',
    breed: '',
    notes: '',
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await createPatient(formData);
      setFormData({ name: '', species: 'DOG', breed: '', notes: '' });
      setShowForm(false);
    } catch (err) {
      alert('Failed to create patient');
    }
  };

  const handleDelete = async (id: string) => {
    if (window.confirm('Are you sure you want to delete this patient?')) {
      try {
        await deletePatient(id);
      } catch (err) {
        alert('Failed to delete patient');
      }
    }
  };

  if (loading) {
    return <div className="loading">Loading patients...</div>;
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
        <h1>Patients</h1>
        <button className="btn btn-primary" onClick={() => setShowForm(true)}>
          Add Patient
        </button>
      </div>

      {showForm && (
        <div className="modal-overlay" onClick={() => setShowForm(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h2>New Patient</h2>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Name *</label>
                <input
                  type="text"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label>Species *</label>
                <select
                  value={formData.species}
                  onChange={(e) => setFormData({ ...formData, species: e.target.value as Species })}
                >
                  {SPECIES_OPTIONS.map((s) => (
                    <option key={s} value={s}>
                      {s}
                    </option>
                  ))}
                </select>
              </div>
              <div className="form-group">
                <label>Breed</label>
                <input
                  type="text"
                  value={formData.breed || ''}
                  onChange={(e) => setFormData({ ...formData, breed: e.target.value })}
                />
              </div>
              <div className="form-group">
                <label>Notes</label>
                <textarea
                  value={formData.notes || ''}
                  onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
                  rows={3}
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

      {patients.length === 0 ? (
        <div className="empty">
          <p>No patients yet. Add your first patient!</p>
        </div>
      ) : (
        <div className="grid">
          {patients.map((patient) => (
            <div key={patient.id} className="card">
              <h3>{patient.name}</h3>
              <span className="species-badge">{patient.species}</span>
              {patient.breed && <p>Breed: {patient.breed}</p>}
              {patient.weight && <p>Weight: {patient.weight} kg</p>}
              {patient.notes && <p>Notes: {patient.notes}</p>}
              <p className="meta">
                Created: {new Date(patient.createdAt).toLocaleDateString()}
              </p>
              <div className="card-actions">
                <button
                  className="btn btn-danger btn-sm"
                  onClick={() => handleDelete(patient.id)}
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
