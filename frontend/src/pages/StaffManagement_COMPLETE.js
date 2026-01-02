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
    const [formData, setFormData] = useState({
        firstName: '',
        lastName: '',
        email: '',
        position: '',
        department: '',
        phone: ''
    });

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
        if (window.confirm('Are you sure you want to delete this staff member?')) {
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
        setFormData({
            firstName: '',
            lastName: '',
            email: '',
            position: '',
            department: '',
            phone: ''
        });
        setShowModal(true);
    };

    const handleEdit = (member) => {
        setSelectedStaff(member);
        setFormData({
            firstName: member.user?.firstName || '',
            lastName: member.user?.lastName || '',
            email: member.user?.email || '',
            position: member.position || '',
            department: member.department || '',
            phone: member.user?.phone || ''
        });
        setShowModal(true);
    };

    const handleSave = async () => {
        try {
            // Validate required fields
            if (!formData.firstName || !formData.lastName || !formData.email || !formData.position) {
                setError('Please fill in all required fields');
                return;
            }

            if (selectedStaff) {
                // Update existing staff
                await staffService.update(selectedStaff.id, formData);
            } else {
                // Create new staff
                await staffService.create(formData);
            }
            setShowModal(false);
            setError('');
            fetchStaff(); // Refresh list
        } catch (err) {
            setError('Failed to save staff member: ' + (err.message || 'Unknown error'));
        }
    };

    return (
        <Container className="management-container py-4">
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h1>Staff Management</h1>
                <Button variant="primary" onClick={handleAddNew}>
                    <FiPlus /> Add Staff
                </Button>
            </div>

            {error && <Alert variant="danger" onClose={() => setError('')} dismissible>{error}</Alert>}

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
                                            <Button size="sm" className="me-2" onClick={() => handleEdit(member)}>
                                                <FiEdit /> Edit
                                            </Button>
                                            <Button size="sm" variant="danger" onClick={() => handleDelete(member.id)}>
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

            <Modal show={showModal} onHide={() => setShowModal(false)} size="lg">
                <Modal.Header closeButton>
                    <Modal.Title>{selectedStaff ? 'Edit Staff Member' : 'Add New Staff Member'}</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <Form>
                        <div className="row">
                            <div className="col-md-6">
                                <Form.Group className="mb-3">
                                    <Form.Label>First Name <span className="text-danger">*</span></Form.Label>
                                    <Form.Control
                                        type="text"
                                        placeholder="Enter first name"
                                        value={formData.firstName}
                                        onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                                        required
                                    />
                                </Form.Group>
                            </div>
                            <div className="col-md-6">
                                <Form.Group className="mb-3">
                                    <Form.Label>Last Name <span className="text-danger">*</span></Form.Label>
                                    <Form.Control
                                        type="text"
                                        placeholder="Enter last name"
                                        value={formData.lastName}
                                        onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                                        required
                                    />
                                </Form.Group>
                            </div>
                        </div>

                        <Form.Group className="mb-3">
                            <Form.Label>Email <span className="text-danger">*</span></Form.Label>
                            <Form.Control
                                type="email"
                                placeholder="Enter email"
                                value={formData.email}
                                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                                required
                            />
                        </Form.Group>

                        <Form.Group className="mb-3">
                            <Form.Label>Phone</Form.Label>
                            <Form.Control
                                type="text"
                                placeholder="Enter phone number"
                                value={formData.phone}
                                onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                            />
                        </Form.Group>

                        <div className="row">
                            <div className="col-md-6">
                                <Form.Group className="mb-3">
                                    <Form.Label>Position <span className="text-danger">*</span></Form.Label>
                                    <Form.Control
                                        as="select"
                                        value={formData.position}
                                        onChange={(e) => setFormData({ ...formData, position: e.target.value })}
                                        required
                                    >
                                        <option value="">Select Position</option>
                                        <option value="PRINCIPAL">Principal</option>
                                        <option value="TEACHER">Teacher</option>
                                        <option value="LIBRARIAN">Librarian</option>
                                        <option value="ACCOUNTANT">Accountant</option>
                                        <option value="ADMIN_STAFF">Admin Staff</option>
                                    </Form.Control>
                                </Form.Group>
                            </div>
                            <div className="col-md-6">
                                <Form.Group className="mb-3">
                                    <Form.Label>Department</Form.Label>
                                    <Form.Control
                                        type="text"
                                        placeholder="Enter department"
                                        value={formData.department}
                                        onChange={(e) => setFormData({ ...formData, department: e.target.value })}
                                    />
                                </Form.Group>
                            </div>
                        </div>
                    </Form>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={() => setShowModal(false)}>
                        Cancel
                    </Button>
                    <Button variant="primary" onClick={handleSave}>
                        <FiPlus className="me-1" />
                        {selectedStaff ? 'Update' : 'Save'}
                    </Button>
                </Modal.Footer>
            </Modal>
        </Container>
    );
}

export default StaffManagement;
