import React, { useState, useEffect } from 'react';
import { Container, Table, Button, Alert } from 'react-bootstrap';
import { FiEdit, FiTrash2, FiPlus } from 'react-icons/fi';
import { studentService } from '../services/dataService';

function StudentManagement() {
  const [students, setStudents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchStudents();
  }, []);

  const fetchStudents = async () => {
    try {
      setLoading(true);
      const response = await studentService.getAll();
      setStudents(response.data || []);
    } catch (err) {
      setError('Failed to load students');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container className="py-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h1>Student Management</h1>
        <Button variant="primary"><FiPlus /> Add Student</Button>
      </div>

      {error && <Alert variant="danger">{error}</Alert>}

      {loading ? (
        <div className="text-center"><div className="spinner-border"></div></div>
      ) : (
        <div className="table-responsive">
          <Table striped bordered hover>
            <thead>
              <tr>
                <th>Roll Number</th>
                <th>Name</th>
                <th>Class</th>
                <th>Section</th>
                <th>Email</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {students.length > 0 ? (
                students.map((student) => (
                  <tr key={student.id}>
                    <td>{student.rollNumber}</td>
                    <td>{student.user?.firstName} {student.user?.lastName}</td>
                    <td>{student.className}</td>
                    <td>{student.section}</td>
                    <td>{student.user?.email}</td>
                    <td><span className="badge bg-success">{student.status}</span></td>
                    <td>
                      <Button variant="sm" className="me-2"><FiEdit /> Edit</Button>
                      <Button variant="sm" variant="danger"><FiTrash2 /> Delete</Button>
                    </td>
                  </tr>
                ))
              ) : (
                <tr><td colSpan="7" className="text-center py-3">No students found</td></tr>
              )}
            </tbody>
          </Table>
        </div>
      )}
    </Container>
  );
}

export default StudentManagement;

