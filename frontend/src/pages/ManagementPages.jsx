import React from 'react';
import { Container, Row, Col, Card } from 'react-bootstrap';

const ManagementPage = ({ title, description }) => (
  <Container className="py-4">
    <h1 className="mb-4">{title}</h1>
    <Card>
      <Card.Body className="text-center py-5">
        <h4 className="text-muted">{description}</h4>
        <p className="text-muted">Component coming soon...</p>
      </Card.Body>
    </Card>
  </Container>
);

export const GradeManagement = () => <ManagementPage title="Grade Management" description="Manage student grades" />;
export const FeeManagement = () => <ManagementPage title="Fee Management" description="Manage student fees and payments" />;

export default ManagementPage;

