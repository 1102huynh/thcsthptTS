import React, { useState } from 'react';
import { Container, Form, Button, Alert, Card } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import authService from '../services/authService';
import './LoginPage.css';

function LoginPage({ onLogin }) {
  const [username, setUsername] = useState('admin');
  const [password, setPassword] = useState('Test@123');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      console.log('Attempting login with:', username);
      const userData = await authService.login(username, password);
      console.log('Login successful, user data:', userData);

      if (userData && userData.accessToken) {
        onLogin(userData);
        navigate('/');
      } else {
        setError('Login failed: No access token received');
      }
    } catch (err) {
      console.error('Login error:', err);
      const errorMessage = err?.response?.data?.message || err?.message || 'Invalid credentials. Please try again.';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <Container className="d-flex align-items-center justify-content-center min-vh-100">
        <Card className="login-card">
          <Card.Body>
            <div className="text-center mb-4">
              <h1 className="login-title">School Management System</h1>
              <p className="login-subtitle">Sign In to Your Account</p>
            </div>

            {error && <Alert variant="danger">{error}</Alert>}

            <Form onSubmit={handleSubmit}>
              <Form.Group className="mb-3">
                <Form.Label>Username</Form.Label>
                <Form.Control
                  type="text"
                  placeholder="Enter username"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  disabled={loading}
                />
              </Form.Group>

              <Form.Group className="mb-4">
                <Form.Label>Password</Form.Label>
                <Form.Control
                  type="password"
                  placeholder="Enter password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  disabled={loading}
                />
              </Form.Group>

              <Button
                variant="primary"
                type="submit"
                className="w-100"
                disabled={loading}
              >
                {loading ? 'Signing In...' : 'Sign In'}
              </Button>
            </Form>

            <div className="mt-4 pt-4 border-top">
              <p className="text-center text-muted mb-0">
                <small>Test Credentials:</small>
              </p>
              <ul className="small text-muted ps-3">
                <li>admin / Test@123</li>
                <li>principal / Test@123</li>
                <li>teacher1 / Test@123</li>
                <li>student1 / Test@123</li>
              </ul>
            </div>
          </Card.Body>
        </Card>
      </Container>
    </div>
  );
}

export default LoginPage;

