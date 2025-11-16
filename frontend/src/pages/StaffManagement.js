import React, { useState, useEffect } from 'react';
import { Container, Table, Button, Modal, Form, Alert } from 'react-bootstrap';
import { FiEdit, FiTrash2, FiPlus } from 'react-icons/fi';
import { staffService } from '../services/dataService';
import './Management.css';

function StaffManagement() {
  const [staff, setStaff] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [selectedStaff, setSelectedStaff] = useState(null);

  useEffect(() => {
    fetchStaff();
  }, []);

  const fetchStaff = async () => {
    try {
      setLoading(true);
      const response = await staffService.getAll();
      setStaff(response.data || []);
    } catch (err) {
      setError('Failed to load staff members');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure?')) {
      try {
        await staffService.delete(id);
        setStaff(staff.filter(s => s.id !== id));
      } catch (err) {
        setError('Failed to delete staff member');
      }
    }
  };

  const handleAddNew = () => {
    setSelectedStaff(null);
    setShowModal(true);
  };

  return (
    <Container className="management-container py-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h1>Staff Management</h1>
        <Button variant="primary" onClick={handleAddNew}>
          <FiPlus /> Add Staff
        </Button>
      </div>

      {error && <Alert variant="danger">{error}</Alert>}

      {loading ? (
        <div className="text-center py-5">
          <div className="spinner-border" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
        </div>
      ) : (
        <div className="table-responsive">
          <Table striped bordered hover>
            <thead>
              <tr>
                <th>Employee ID</th>
                <th>Name</th>
                <th>Position</th>
                <th>Department</th>
                <th>Email</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {staff.length > 0 ? (
                staff.map((member) => (
                  <tr key={member.id}>
                    <td>{member.employeeId}</td>
                    <td>{member.user?.firstName} {member.user?.lastName}</td>
                    <td>{member.position}</td>
                    <td>{member.department}</td>
                    <td>{member.user?.email}</td>
                    <td>
                      <span className={`badge bg-${member.status === 'ACTIVE' ? 'success' : 'danger'}`}>
                        {member.status}
                      </span>
                    </td>
                    <td>
                      <Button variant="sm" className="me-2" onClick={() => { setSelectedStaff(member); setShowModal(true); }}>
                        <FiEdit /> Edit
                      </Button>
                      <Button variant="sm" variant="danger" onClick={() => handleDelete(member.id)}>
                        <FiTrash2 /> Delete
                      </Button>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="7" className="text-center py-3">No staff members found</td>
                </tr>
              )}
            </tbody>
          </Table>
        </div>
      )}

      <Modal show={showModal} onHide={() => setShowModal(false)}>
        <Modal.Header closeButton>
          <Modal.Title>{selectedStaff ? 'Edit Staff' : 'Add New Staff'}</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Form>
            <Form.Group className="mb-3">
              <Form.Label>Name</Form.Label>
              <Form.Control type="text" placeholder="Enter name" />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Position</Form.Label>
              <Form.Control as="select" defaultValue="">
                <option value="">Select Position</option>
                <option value="PRINCIPAL">Principal</option>
                <option value="TEACHER">Teacher</option>
                <option value="LIBRARIAN">Librarian</option>
                <option value="ACCOUNTANT">Accountant</option>
              </Form.Control>
            </Form.Group>
          </Form>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowModal(false)}>
            Cancel
          </Button>
          <Button variant="primary">
            Save
          </Button>
        </Modal.Footer>
      </Modal>
    </Container>
  );
}

export default StaffManagement;

