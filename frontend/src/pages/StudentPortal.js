import React, { useState } from 'react';
import { Container, Row, Col, Card, Tab, Tabs, Badge, Button, ProgressBar, ListGroup, Alert, Form } from 'react-bootstrap';
import {
  FiUser, FiBook, FiCheckCircle, FiAward, FiDollarSign, FiHeart,
  FiMessageSquare, FiHelpCircle, FiCalendar, FiClipboard, FiTrendingUp,
  FiClock, FiDownload, FiUpload, FiSearch, FiFlag
} from 'react-icons/fi';
import '../styles/StudentPortal.css';

// Dashboard Tab
function DashboardTab({ user }) {
  return (
    <div className="tab-content py-4">
      <Row>
        <Col lg={8}>
          <Card className="content-card mb-3">
            <Card.Header className="bg-light">
              <Card.Title className="mb-0">Upcoming Classes</Card.Title>
            </Card.Header>
            <Card.Body>
              <ListGroup variant="flush">
                <ListGroup.Item>
                  <div className="d-flex justify-content-between align-items-center">
                    <div>
                      <h6 className="mb-1">Mathematics</h6>
                      <small className="text-muted">Room 101 • 09:00 AM - 10:00 AM</small>
                    </div>
                    <Badge bg="primary">Today</Badge>
                  </div>
                </ListGroup.Item>
                <ListGroup.Item>
                  <div className="d-flex justify-content-between align-items-center">
                    <div>
                      <h6 className="mb-1">English</h6>
                      <small className="text-muted">Room 105 • 10:30 AM - 11:30 AM</small>
                    </div>
                    <Badge bg="info">Today</Badge>
                  </div>
                </ListGroup.Item>
                <ListGroup.Item>
                  <div className="d-flex justify-content-between align-items-center">
                    <div>
                      <h6 className="mb-1">Physics</h6>
                      <small className="text-muted">Lab 201 • 02:00 PM - 03:30 PM</small>
                    </div>
                    <Badge bg="success">Today</Badge>
                  </div>
                </ListGroup.Item>
              </ListGroup>
            </Card.Body>
          </Card>

          <Card className="content-card">
            <Card.Header className="bg-light">
              <Card.Title className="mb-0">Recent Announcements</Card.Title>
            </Card.Header>
            <Card.Body>
              <ListGroup variant="flush">
                <ListGroup.Item>
                  <small className="text-muted">Nov 16, 2025</small>
                  <h6 className="mb-1 mt-2">School Annual Exam Schedule Released</h6>
                  <p className="mb-0 text-muted">Final exams will begin from December 1st.</p>
                </ListGroup.Item>
                <ListGroup.Item>
                  <small className="text-muted">Nov 14, 2025</small>
                  <h6 className="mb-1 mt-2">Library Book Return Deadline</h6>
                  <p className="mb-0 text-muted">Please return all borrowed books by Nov 20th.</p>
                </ListGroup.Item>
              </ListGroup>
            </Card.Body>
          </Card>
        </Col>

        <Col lg={4}>
          <Card className="content-card mb-3">
            <Card.Header className="bg-light">
              <Card.Title className="mb-0">Academic Progress</Card.Title>
            </Card.Header>
            <Card.Body>
              <div className="mb-3">
                <div className="d-flex justify-content-between mb-1">
                  <small>Mathematics</small>
                  <small className="fw-bold">85%</small>
                </div>
                <ProgressBar now={85} variant="primary" />
              </div>
              <div className="mb-3">
                <div className="d-flex justify-content-between mb-1">
                  <small>English</small>
                  <small className="fw-bold">90%</small>
                </div>
                <ProgressBar now={90} variant="success" />
              </div>
              <div className="mb-3">
                <div className="d-flex justify-content-between mb-1">
                  <small>Science</small>
                  <small className="fw-bold">78%</small>
                </div>
                <ProgressBar now={78} variant="info" />
              </div>
              <div>
                <div className="d-flex justify-content-between mb-1">
                  <small>History</small>
                  <small className="fw-bold">88%</small>
                </div>
                <ProgressBar now={88} variant="warning" />
              </div>
            </Card.Body>
          </Card>

          <Card className="content-card">
            <Card.Header className="bg-light">
              <Card.Title className="mb-0">Quick Links</Card.Title>
            </Card.Header>
            <Card.Body>
              <div className="d-grid gap-2">
                <Button variant="outline-primary" size="sm">
                  <FiDownload className="me-2" /> Download Timetable
                </Button>
                <Button variant="outline-primary" size="sm">
                  <FiMessageSquare className="me-2" /> Contact Teacher
                </Button>
                <Button variant="outline-primary" size="sm">
                  <FiHelpCircle className="me-2" /> Request Support
                </Button>
              </div>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </div>
  );
}

// Profile Tab
function ProfileTab({ user }) {
  return (
    <div className="tab-content py-4">
      <Row>
        <Col lg={8}>
          <Card className="content-card">
            <Card.Header className="bg-light">
              <Card.Title className="mb-0">Personal Information</Card.Title>
            </Card.Header>
            <Card.Body>
              <Row>
                <Col md={6} className="mb-3">
                  <label className="text-muted small">First Name</label>
                  <p className="fw-bold">{user?.firstName || 'Student'}</p>
                </Col>
                <Col md={6} className="mb-3">
                  <label className="text-muted small">Last Name</label>
                  <p className="fw-bold">{user?.lastName || 'Name'}</p>
                </Col>
              </Row>
              <Row>
                <Col md={6} className="mb-3">
                  <label className="text-muted small">Student ID</label>
                  <p className="fw-bold">STU001</p>
                </Col>
                <Col md={6} className="mb-3">
                  <label className="text-muted small">Email</label>
                  <p className="fw-bold">{user?.email || 'student@school.com'}</p>
                </Col>
              </Row>
              <Row>
                <Col md={6} className="mb-3">
                  <label className="text-muted small">Class</label>
                  <p className="fw-bold">10A</p>
                </Col>
                <Col md={6} className="mb-3">
                  <label className="text-muted small">Roll Number</label>
                  <p className="fw-bold">025</p>
                </Col>
              </Row>
              <Row>
                <Col md={6} className="mb-3">
                  <label className="text-muted small">Date of Birth</label>
                  <p className="fw-bold">January 15, 2009</p>
                </Col>
                <Col md={6} className="mb-3">
                  <label className="text-muted small">Gender</label>
                  <p className="fw-bold">Male</p>
                </Col>
              </Row>
            </Card.Body>
          </Card>
        </Col>

        <Col lg={4}>
          <Card className="content-card mb-3">
            <Card.Header className="bg-light">
              <Card.Title className="mb-0">Contact Information</Card.Title>
            </Card.Header>
            <Card.Body>
              <div className="mb-2">
                <small className="text-muted">Phone</small>
                <p className="fw-bold">+84 900 123 456</p>
              </div>
              <div className="mb-2">
                <small className="text-muted">Address</small>
                <p className="fw-bold">123 Main Street, City</p>
              </div>
            </Card.Body>
          </Card>

          <Card className="content-card">
            <Card.Header className="bg-light">
              <Card.Title className="mb-0">Parent Information</Card.Title>
            </Card.Header>
            <Card.Body>
              <div className="mb-2">
                <small className="text-muted">Father's Name</small>
                <p className="fw-bold">Mr. Parent Name</p>
              </div>
              <div>
                <small className="text-muted">Father's Phone</small>
                <p className="fw-bold">+84 900 000 001</p>
              </div>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </div>
  );
}

// Timetable Tab
function TimetableTab() {
  const timetable = {
    'Monday': [
      { time: '09:00-10:00', subject: 'Mathematics', room: '101' },
      { time: '10:30-11:30', subject: 'English', room: '105' },
      { time: '12:00-01:00', subject: 'Lunch Break', room: '-' },
      { time: '02:00-03:30', subject: 'Physics', room: '201' },
    ],
    'Tuesday': [
      { time: '09:00-10:00', subject: 'Chemistry', room: '202' },
      { time: '10:30-11:30', subject: 'Biology', room: '203' },
      { time: '12:00-01:00', subject: 'Lunch Break', room: '-' },
      { time: '02:00-03:30', subject: 'History', room: '104' },
    ],
  };

  return (
    <div className="tab-content py-4">
      {Object.keys(timetable).map((day) => (
        <Card key={day} className="content-card mb-3">
          <Card.Header className="bg-light">
            <Card.Title className="mb-0">{day}</Card.Title>
          </Card.Header>
          <Card.Body className="p-0">
            <ListGroup variant="flush">
              {timetable[day].map((item, idx) => (
                <ListGroup.Item key={idx}>
                  <Row className="align-items-center">
                    <Col md={3}>
                      <small className="text-muted">{item.time}</small>
                    </Col>
                    <Col md={6}>
                      <h6 className="mb-0">{item.subject}</h6>
                    </Col>
                    <Col md={3} className="text-end">
                      <Badge bg="light" text="dark">Room {item.room}</Badge>
                    </Col>
                  </Row>
                </ListGroup.Item>
              ))}
            </ListGroup>
          </Card.Body>
        </Card>
      ))}
    </div>
  );
}

// Attendance Tab
function AttendanceTab() {
  return (
    <div className="tab-content py-4">
      <Row>
        <Col lg={8}>
          <Card className="content-card">
            <Card.Header className="bg-light">
              <Card.Title className="mb-0">Monthly Attendance</Card.Title>
            </Card.Header>
            <Card.Body>
              <div className="attendance-grid">
                {[...Array(30)].map((_, i) => (
                  <div key={i} className={`attendance-day ${i % 3 === 0 ? 'absent' : 'present'}`}>
                    <span>{i + 1}</span>
                  </div>
                ))}
              </div>
            </Card.Body>
          </Card>
        </Col>
        <Col lg={4}>
          <Card className="content-card mb-3">
            <Card.Header className="bg-light">
              <Card.Title className="mb-0">Attendance Summary</Card.Title>
            </Card.Header>
            <Card.Body>
              <div className="mb-3">
                <div className="d-flex justify-content-between mb-2">
                  <span>Total Days</span>
                  <strong>30</strong>
                </div>
                <div className="d-flex justify-content-between mb-2">
                  <span>Present</span>
                  <strong className="text-success">28</strong>
                </div>
                <div className="d-flex justify-content-between mb-2">
                  <span>Absent</span>
                  <strong className="text-danger">2</strong>
                </div>
              </div>
              <ProgressBar now={92} label="92%" />
            </Card.Body>
          </Card>

          <Card className="content-card">
            <Card.Header className="bg-light">
              <Card.Title className="mb-0">Legend</Card.Title>
            </Card.Header>
            <Card.Body>
              <div className="mb-2">
                <div className="d-flex align-items-center">
                  <div className="attendance-day present" style={{ marginRight: '10px' }}></div>
                  <small>Present</small>
                </div>
              </div>
              <div>
                <div className="d-flex align-items-center">
                  <div className="attendance-day absent" style={{ marginRight: '10px' }}></div>
                  <small>Absent</small>
                </div>
              </div>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </div>
  );
}

// Grades Tab
function GradesTab() {
  const subjects = [
    { name: 'Mathematics', midterm: 85, final: 88, overall: 86.5 },
    { name: 'English', midterm: 90, final: 92, overall: 91 },
    { name: 'Physics', midterm: 78, final: 82, overall: 80 },
    { name: 'Chemistry', midterm: 88, final: 85, overall: 86.5 },
    { name: 'Biology', midterm: 92, final: 89, overall: 90.5 },
    { name: 'History', midterm: 87, final: 90, overall: 88.5 },
  ];

  return (
    <div className="tab-content py-4">
      <Card className="content-card">
        <Card.Header className="bg-light">
          <Card.Title className="mb-0">Current Grades</Card.Title>
        </Card.Header>
        <Card.Body className="p-0">
          <div className="table-responsive">
            <table className="table mb-0">
              <thead className="table-light">
                <tr>
                  <th>Subject</th>
                  <th className="text-center">Midterm</th>
                  <th className="text-center">Final</th>
                  <th className="text-center">Overall</th>
                </tr>
              </thead>
              <tbody>
                {subjects.map((subject, idx) => (
                  <tr key={idx}>
                    <td>{subject.name}</td>
                    <td className="text-center">{subject.midterm}</td>
                    <td className="text-center">{subject.final}</td>
                    <td className="text-center">
                      <strong>{subject.overall}</strong>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Card.Body>
      </Card>
    </div>
  );
}

// Assignments Tab
function AssignmentsTab() {
  const assignments = [
    {
      id: 1,
      subject: 'Mathematics',
      title: 'Chapter 5 Exercises',
      dueDate: '2025-11-20',
      status: 'pending',
      description: 'Complete all exercises from page 45-50'
    },
    {
      id: 2,
      subject: 'English',
      title: 'Essay Writing',
      dueDate: '2025-11-18',
      status: 'submitted',
      description: 'Write an essay on "Technology in Modern Life"'
    },
    {
      id: 3,
      subject: 'Physics',
      title: 'Lab Report',
      dueDate: '2025-11-25',
      status: 'pending',
      description: 'Complete the lab experiment and submit report'
    },
  ];

  return (
    <div className="tab-content py-4">
      {assignments.map((assignment) => (
        <Card key={assignment.id} className="content-card mb-3">
          <Card.Body>
            <Row>
              <Col md={8}>
                <h5 className="mb-1">{assignment.title}</h5>
                <p className="text-muted mb-2">{assignment.subject}</p>
                <p className="mb-2">{assignment.description}</p>
                <small className="text-muted">Due: {assignment.dueDate}</small>
              </Col>
              <Col md={4} className="text-end">
                <Badge bg={assignment.status === 'submitted' ? 'success' : 'warning'} className="mb-2">
                  {assignment.status === 'submitted' ? 'Submitted' : 'Pending'}
                </Badge>
                {assignment.status === 'pending' && (
                  <div>
                    <Button variant="primary" size="sm" className="me-2 mt-2">
                      <FiUpload className="me-1" /> Submit
                    </Button>
                  </div>
                )}
              </Col>
            </Row>
          </Card.Body>
        </Card>
      ))}
    </div>
  );
}

// Exams Tab
function ExamsTab() {
  const exams = [
    {
      id: 1,
      title: 'Midterm Mathematics',
      date: '2025-12-01',
      time: '09:00 AM - 11:00 AM',
      room: '101',
      status: 'scheduled'
    },
    {
      id: 2,
      title: 'Midterm English',
      date: '2025-12-02',
      time: '02:00 PM - 04:00 PM',
      room: '105',
      status: 'scheduled'
    },
    {
      id: 3,
      title: 'Physics Quiz',
      date: '2025-11-20',
      time: '10:30 AM - 11:00 AM',
      room: '201',
      status: 'upcoming'
    },
  ];

  return (
    <div className="tab-content py-4">
      {exams.map((exam) => (
        <Card key={exam.id} className="content-card mb-3">
          <Card.Body>
            <Row className="align-items-center">
              <Col md={8}>
                <h5 className="mb-1">{exam.title}</h5>
                <div className="exam-details">
                  <small className="me-3">
                    <FiCalendar className="me-1" />
                    {exam.date}
                  </small>
                  <small className="me-3">
                    <FiClock className="me-1" />
                    {exam.time}
                  </small>
                  <small>
                    <span className="me-1">Room:</span>
                    {exam.room}
                  </small>
                </div>
              </Col>
              <Col md={4} className="text-end">
                <Badge bg="primary">{exam.status === 'upcoming' ? 'Upcoming' : 'Scheduled'}</Badge>
              </Col>
            </Row>
          </Card.Body>
        </Card>
      ))}
    </div>
  );
}

// Library Tab
function LibraryTab() {
  const borrowedBooks = [
    { id: 1, title: 'Advanced Mathematics', author: 'Dr. Smith', dueDate: '2025-11-25', status: 'active' },
    { id: 2, title: 'English Literature', author: 'Prof. Johnson', dueDate: '2025-11-30', status: 'active' },
  ];

  return (
    <div className="tab-content py-4">
      <Row>
        <Col lg={8}>
          <Card className="content-card">
            <Card.Header className="bg-light">
              <Card.Title className="mb-0">Borrowed Books</Card.Title>
            </Card.Header>
            <Card.Body>
              <ListGroup variant="flush">
                {borrowedBooks.map((book) => (
                  <ListGroup.Item key={book.id}>
                    <Row className="align-items-center">
                      <Col md={7}>
                        <h6 className="mb-1">{book.title}</h6>
                        <small className="text-muted">{book.author}</small>
                      </Col>
                      <Col md={3}>
                        <small className="text-muted">Due: {book.dueDate}</small>
                      </Col>
                      <Col md={2} className="text-end">
                        <Button variant="outline-danger" size="sm">Return</Button>
                      </Col>
                    </Row>
                  </ListGroup.Item>
                ))}
              </ListGroup>
            </Card.Body>
          </Card>
        </Col>
        <Col lg={4}>
          <Card className="content-card">
            <Card.Header className="bg-light">
              <Card.Title className="mb-0">Library Info</Card.Title>
            </Card.Header>
            <Card.Body>
              <div className="mb-3">
                <small className="text-muted">Books Borrowed</small>
                <h4>2 / 5</h4>
              </div>
              <Button variant="primary" className="w-100 mb-2">
                <FiSearch className="me-2" /> Search Books
              </Button>
              <Button variant="outline-primary" className="w-100">
                <FiDownload className="me-2" /> View Catalog
              </Button>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </div>
  );
}

// Activities Tab
function ActivitiesTab() {
  const activities = [
    { id: 1, name: 'Debate Club', category: 'Club', status: 'joined', date: '2025-11-18' },
    { id: 2, name: 'Science Fair', category: 'Event', status: 'registered', date: '2025-11-25' },
    { id: 3, name: 'Sports Day', category: 'Event', status: 'participated', date: '2025-11-16' },
    { id: 4, name: 'Art Club', category: 'Club', status: 'not-joined', date: '2025-12-01' },
  ];

  return (
    <div className="tab-content py-4">
      <Row className="mb-3">
        <Col>
          <div className="activity-filters">
            <Button variant="light" className="me-2">All</Button>
            <Button variant="light" className="me-2">Clubs</Button>
            <Button variant="light">Events</Button>
          </div>
        </Col>
      </Row>

      {activities.map((activity) => (
        <Card key={activity.id} className="content-card mb-3">
          <Card.Body>
            <Row className="align-items-center">
              <Col md={8}>
                <h5 className="mb-1">{activity.name}</h5>
                <Badge bg="secondary" className="me-2">{activity.category}</Badge>
                <small className="text-muted">Next: {activity.date}</small>
              </Col>
              <Col md={4} className="text-end">
                {activity.status === 'joined' && (
                  <Badge bg="success">Joined</Badge>
                )}
                {activity.status === 'registered' && (
                  <Badge bg="info">Registered</Badge>
                )}
                {activity.status === 'not-joined' && (
                  <Button variant="primary" size="sm">Join</Button>
                )}
              </Col>
            </Row>
          </Card.Body>
        </Card>
      ))}
    </div>
  );
}

// Fees Tab
function FeesTab() {
  return (
    <div className="tab-content py-4">
      <Card className="content-card mb-3">
        <Card.Header className="bg-light">
          <Card.Title className="mb-0">Fee Information</Card.Title>
        </Card.Header>
        <Card.Body>
          <Row className="mb-3">
            <Col md={6}>
              <label className="text-muted small">Total Fee</label>
              <h4 className="text-danger">₹50,000</h4>
            </Col>
            <Col md={6}>
              <label className="text-muted small">Paid Amount</label>
              <h4 className="text-success">₹35,000</h4>
            </Col>
          </Row>
          <div className="mb-3">
            <div className="d-flex justify-content-between mb-1">
              <span>Payment Status</span>
              <strong>70%</strong>
            </div>
            <ProgressBar now={70} variant="warning" />
          </div>
          <Button variant="primary" className="w-100">Pay Remaining Fee</Button>
        </Card.Body>
      </Card>

      <Card className="content-card">
        <Card.Header className="bg-light">
          <Card.Title className="mb-0">Payment History</Card.Title>
        </Card.Header>
        <Card.Body className="p-0">
          <ListGroup variant="flush">
            <ListGroup.Item>
              <Row className="align-items-center">
                <Col md={6}>
                  <small className="text-muted">Nov 01, 2025</small>
                  <p className="mb-0 fw-bold">First Installment</p>
                </Col>
                <Col md={6} className="text-end">
                  <strong className="text-success">₹20,000</strong>
                </Col>
              </Row>
            </ListGroup.Item>
            <ListGroup.Item>
              <Row className="align-items-center">
                <Col md={6}>
                  <small className="text-muted">Nov 15, 2025</small>
                  <p className="mb-0 fw-bold">Second Installment</p>
                </Col>
                <Col md={6} className="text-end">
                  <strong className="text-success">₹15,000</strong>
                </Col>
              </Row>
            </ListGroup.Item>
          </ListGroup>
        </Card.Body>
      </Card>
    </div>
  );
}

// Records Tab
function RecordsTab() {
  const records = [
    { date: '2025-11-10', type: 'Reward', description: 'Excellent performance in Science', category: 'Academic' },
    { date: '2025-11-01', type: 'Discipline', description: 'Late submission of assignment', category: 'Academic' },
    { date: '2025-10-25', type: 'Reward', description: 'First prize in debate competition', category: 'Co-curricular' },
  ];

  return (
    <div className="tab-content py-4">
      <Card className="content-card">
        <Card.Header className="bg-light">
          <Card.Title className="mb-0">Behavior & Conduct Records</Card.Title>
        </Card.Header>
        <Card.Body>
          <ListGroup variant="flush">
            {records.map((record, idx) => (
              <ListGroup.Item key={idx}>
                <Row>
                  <Col md={2}>
                    <Badge bg={record.type === 'Reward' ? 'success' : 'warning'}>
                      {record.type}
                    </Badge>
                  </Col>
                  <Col md={6}>
                    <h6 className="mb-1">{record.description}</h6>
                    <small className="text-muted">{record.category}</small>
                  </Col>
                  <Col md={4} className="text-end">
                    <small className="text-muted">{record.date}</small>
                  </Col>
                </Row>
              </ListGroup.Item>
            ))}
          </ListGroup>
        </Card.Body>
      </Card>
    </div>
  );
}

// Communication Tab
function CommunicationTab() {
  const messages = [
    { id: 1, from: 'Mrs. Sarah (Mathematics)', subject: 'Regarding your midterm exam', date: '2025-11-15', read: false },
    { id: 2, from: 'Mr. John (Principal)', subject: 'Important announcement', date: '2025-11-14', read: true },
    { id: 3, from: 'Ms. Emily (English)', subject: 'Essay feedback', date: '2025-11-13', read: true },
  ];

  return (
    <div className="tab-content py-4">
      <Row>
        <Col lg={8}>
          <Card className="content-card">
            <Card.Header className="bg-light d-flex justify-content-between align-items-center">
              <Card.Title className="mb-0">Messages</Card.Title>
              <Button variant="primary" size="sm">New Message</Button>
            </Card.Header>
            <Card.Body className="p-0">
              <ListGroup variant="flush">
                {messages.map((msg) => (
                  <ListGroup.Item key={msg.id} className={msg.read ? '' : 'bg-light'}>
                    <Row className="align-items-center">
                      <Col>
                        <h6 className="mb-1">{msg.from}</h6>
                        <p className="mb-0 text-muted">{msg.subject}</p>
                      </Col>
                      <Col md={3} className="text-end">
                        <small className="text-muted">{msg.date}</small>
                        <br />
                        {!msg.read && <Badge bg="primary">Unread</Badge>}
                      </Col>
                    </Row>
                  </ListGroup.Item>
                ))}
              </ListGroup>
            </Card.Body>
          </Card>
        </Col>

        <Col lg={4}>
          <Card className="content-card">
            <Card.Header className="bg-light">
              <Card.Title className="mb-0">Contact Teachers</Card.Title>
            </Card.Header>
            <Card.Body>
              <ListGroup variant="flush">
                <ListGroup.Item>
                  <h6 className="mb-1">Mrs. Sarah (Mathematics)</h6>
                  <small className="text-muted">sarah@school.com</small>
                </ListGroup.Item>
                <ListGroup.Item>
                  <h6 className="mb-1">Mr. John (Physics)</h6>
                  <small className="text-muted">john@school.com</small>
                </ListGroup.Item>
                <ListGroup.Item>
                  <h6 className="mb-1">Ms. Emily (English)</h6>
                  <small className="text-muted">emily@school.com</small>
                </ListGroup.Item>
              </ListGroup>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </div>
  );
}

// Support Tab
function SupportTab() {
  const [supportType, setSupportType] = useState('');

  return (
    <div className="tab-content py-4">
      <Row>
        <Col lg={8}>
          <Card className="content-card">
            <Card.Header className="bg-light">
              <Card.Title className="mb-0">Submit Support Request</Card.Title>
            </Card.Header>
            <Card.Body>
              <div className="mb-3">
                <label className="form-label">Support Type</label>
                <select className="form-select" value={supportType} onChange={(e) => setSupportType(e.target.value)}>
                  <option value="">Select a category</option>
                  <option value="academic">Academic Issue</option>
                  <option value="technical">Technical Problem</option>
                  <option value="counseling">Counseling</option>
                  <option value="other">Other</option>
                </select>
              </div>

              <div className="mb-3">
                <label className="form-label">Subject</label>
                <input type="text" className="form-control" placeholder="Enter subject" />
              </div>

              <div className="mb-3">
                <label className="form-label">Description</label>
                <textarea className="form-control" rows="4" placeholder="Describe your issue"></textarea>
              </div>

              <Button variant="primary" className="w-100">Submit Request</Button>
            </Card.Body>
          </Card>
        </Col>

        <Col lg={4}>
          <Card className="content-card mb-3">
            <Card.Header className="bg-light">
              <Card.Title className="mb-0">Support Tickets</Card.Title>
            </Card.Header>
            <Card.Body>
              <ListGroup variant="flush">
                <ListGroup.Item>
                  <div className="d-flex justify-content-between align-items-center">
                    <div>
                      <h6 className="mb-1">Ticket #001</h6>
                      <small className="text-muted">Academic Query</small>
                    </div>
                    <Badge bg="success">Resolved</Badge>
                  </div>
                </ListGroup.Item>
                <ListGroup.Item>
                  <div className="d-flex justify-content-between align-items-center">
                    <div>
                      <h6 className="mb-1">Ticket #002</h6>
                      <small className="text-muted">Technical Issue</small>
                    </div>
                    <Badge bg="warning">Pending</Badge>
                  </div>
                </ListGroup.Item>
              </ListGroup>
            </Card.Body>
          </Card>

          <Card className="content-card">
            <Card.Header className="bg-light">
              <Card.Title className="mb-0">Help Center</Card.Title>
            </Card.Header>
            <Card.Body>
              <div className="d-grid gap-2">
                <Button variant="outline-primary" size="sm">FAQ</Button>
                <Button variant="outline-primary" size="sm">User Guide</Button>
                <Button variant="outline-primary" size="sm">Contact Admin</Button>
              </div>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </div>
  );
}

// Conduct Evaluation Tab (Vietnam - Hạnh Kiểm)
function ConductEvaluationTab() {
  return (
    <div className="tab-content py-4">
      <Row>
        <Col lg={8}>
          <Card className="content-card mb-3">
            <Card.Header className="bg-light">
              <Card.Title className="mb-0">Conduct Evaluation</Card.Title>
            </Card.Header>
            <Card.Body>
              <Alert variant="success">
                <strong>Semester I - 2024: Good</strong>
              </Alert>
              <ListGroup variant="flush">
                <ListGroup.Item>
                  <div className="d-flex justify-content-between">
                    <span>Learning Attitude</span>
                    <Badge bg="success">Good</Badge>
                  </div>
                </ListGroup.Item>
                <ListGroup.Item>
                  <div className="d-flex justify-content-between">
                    <span>Discipline</span>
                    <Badge bg="success">Good</Badge>
                  </div>
                </ListGroup.Item>
                <ListGroup.Item>
                  <div className="d-flex justify-content-between">
                    <span>Responsibility</span>
                    <Badge bg="success">Good</Badge>
                  </div>
                </ListGroup.Item>
                <ListGroup.Item>
                  <div className="d-flex justify-content-between">
                    <span>Team Cooperation</span>
                    <Badge bg="success">Good</Badge>
                  </div>
                </ListGroup.Item>
              </ListGroup>
            </Card.Body>
          </Card>

          <Card className="content-card">
            <Card.Header className="bg-light">
              <Card.Title className="mb-0">Teacher's Comment</Card.Title>
            </Card.Header>
            <Card.Body>
              <p>Student demonstrates good learning attitude, maintains discipline, and actively participates in class and school activities. Continue to maintain these achievements.</p>
            </Card.Body>
          </Card>
        </Col>

        <Col lg={4}>
          <Card className="content-card">
            <Card.Header className="bg-light">
              <Card.Title className="mb-0">Evaluation Criteria</Card.Title>
            </Card.Header>
            <Card.Body>
              <ListGroup variant="flush">
                <ListGroup.Item className="px-0">
                  <Badge bg="success" className="me-2">GOOD</Badge>
                  <small>Meets all requirements well</small>
                </ListGroup.Item>
                <ListGroup.Item className="px-0">
                  <Badge bg="info" className="me-2">FAIR</Badge>
                  <small>Needs improvement in some areas</small>
                </ListGroup.Item>
                <ListGroup.Item className="px-0">
                  <Badge bg="warning" className="me-2">POOR</Badge>
                  <small>Needs significant improvement</small>
                </ListGroup.Item>
              </ListGroup>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </div>
  );
}

// Clubs Tab (Vietnam - CLB)
function ClubsTab() {
  const clubs = [
    { id: 1, name: 'Art Club', members: 45, leader: 'Ms. Tran', status: 'joined' },
    { id: 2, name: 'Robotics Club', members: 38, leader: 'Mr. Minh', status: 'joined' },
    { id: 3, name: 'English Club', members: 52, leader: 'Mr. Johnson', status: 'available' },
    { id: 4, name: 'Music Club', members: 30, leader: 'Ms. Linh', status: 'available' },
  ];

  return (
    <div className="tab-content py-4">
      {clubs.map((club) => (
        <Card key={club.id} className="content-card mb-3">
          <Card.Body>
            <Row className="align-items-center">
              <Col md={7}>
                <h5 className="mb-1">{club.name}</h5>
                <small className="text-muted">Club Leader: {club.leader}</small><br/>
                <small className="text-muted">Members: {club.members}</small>
              </Col>
              <Col md={5} className="text-end">
                {club.status === 'joined' && (
                  <Badge bg="success" className="me-2">Member</Badge>
                )}
                {club.status === 'available' && (
                  <Button variant="primary" size="sm">Join Club</Button>
                )}
              </Col>
            </Row>
          </Card.Body>
        </Card>
      ))}
    </div>
  );
}

// Permission Requests Tab (Vietnam - Xin Phép Nghỉ Học)
function PermissionRequestsTab() {
  const [showForm, setShowForm] = useState(false);
  const [requests] = useState([
    { id: 1, reason: 'Sick Leave', startDate: '2025-11-15', endDate: '2025-11-16', status: 'approved', approvedBy: 'Ms. Tran' },
    { id: 2, reason: 'Family Matter', startDate: '2025-11-10', endDate: '2025-11-10', status: 'approved', approvedBy: 'Mr. Johnson' },
    { id: 3, reason: 'Medical Appointment', startDate: '2025-11-20', endDate: '2025-11-20', status: 'pending', approvedBy: '-' },
  ]);

  return (
    <div className="tab-content py-4">
      <Row>
        <Col lg={8}>
          <Card className="content-card">
            <Card.Header className="bg-light">
              <Card.Title className="mb-0">Permission Request History</Card.Title>
            </Card.Header>
            <Card.Body>
              <ListGroup variant="flush">
                {requests.map((req) => (
                  <ListGroup.Item key={req.id}>
                    <Row>
                      <Col md={6}>
                        <h6 className="mb-1">{req.reason}</h6>
                        <small className="text-muted">{req.startDate} - {req.endDate}</small>
                      </Col>
                      <Col md={3}>
                        <Badge bg={req.status === 'approved' ? 'success' : 'warning'}>
                          {req.status === 'approved' ? 'Approved' : 'Pending'}
                        </Badge>
                      </Col>
                      <Col md={3} className="text-end">
                        <small className="text-muted">By: {req.approvedBy}</small>
                      </Col>
                    </Row>
                  </ListGroup.Item>
                ))}
              </ListGroup>
            </Card.Body>
          </Card>
        </Col>

        <Col lg={4}>
          <Card className="content-card">
            <Card.Header className="bg-light">
              <Card.Title className="mb-0">New Request</Card.Title>
            </Card.Header>
            <Card.Body>
              <Button variant="primary" className="w-100" onClick={() => setShowForm(!showForm)}>
                <FiFlag className="me-2" /> Request Permission
              </Button>
            </Card.Body>
          </Card>

          {showForm && (
            <Card className="content-card mt-3">
              <Card.Body>
                <Form>
                  <Form.Group className="mb-3">
                    <Form.Label>Reason</Form.Label>
                    <Form.Select>
                      <option>Select reason</option>
                      <option>Sick Leave</option>
                      <option>Family Matter</option>
                      <option>Medical Appointment</option>
                      <option>Other</option>
                    </Form.Select>
                  </Form.Group>
                  <Form.Group className="mb-3">
                    <Form.Label>Start Date</Form.Label>
                    <Form.Control type="date" />
                  </Form.Group>
                  <Form.Group className="mb-3">
                    <Form.Label>End Date</Form.Label>
                    <Form.Control type="date" />
                  </Form.Group>
                  <Form.Group className="mb-3">
                    <Form.Label>Notes</Form.Label>
                    <Form.Control as="textarea" rows={2} />
                  </Form.Group>
                  <Button variant="primary" className="w-100">Submit Request</Button>
                </Form>
              </Card.Body>
            </Card>
          )}
        </Col>
      </Row>
    </div>
  );
}

// Main StudentPortal Component
function StudentPortal({ user }) {
  const [activeTab, setActiveTab] = useState('dashboard');

  return (
    <Container fluid className="student-portal py-4">
      {/* Header */}
      <div className="student-header mb-4">
        <Row className="align-items-center">
          <Col md={8}>
            <h1 className="mb-0">Welcome, {user?.firstName} {user?.lastName}!</h1>
            <p className="text-muted mb-0">Student ID: {user?.id || 'STU001'}</p>
          </Col>
          <Col md={4} className="text-end">
            <div className="student-status">
              <Badge bg="success" className="me-2">Active</Badge>
              <small className="text-muted">Class 10A</small>
            </div>
          </Col>
        </Row>
      </div>

      {/* Quick Stats */}
      <Row className="mb-4">
        <Col lg={3} md={6} className="mb-3">
          <Card className="stat-card">
            <Card.Body className="text-center">
              <FiBook size={32} className="text-primary mb-2" />
              <h6 className="text-muted mb-1">GPA</h6>
              <h2 className="mb-0">3.75</h2>
            </Card.Body>
          </Card>
        </Col>
        <Col lg={3} md={6} className="mb-3">
          <Card className="stat-card">
            <Card.Body className="text-center">
              <FiCheckCircle size={32} className="text-success mb-2" />
              <h6 className="text-muted mb-1">Attendance</h6>
              <h2 className="mb-0">92%</h2>
            </Card.Body>
          </Card>
        </Col>
        <Col lg={3} md={6} className="mb-3">
          <Card className="stat-card">
            <Card.Body className="text-center">
              <FiAward size={32} className="text-warning mb-2" />
              <h6 className="text-muted mb-1">Assignments</h6>
              <h2 className="mb-0">8/10</h2>
            </Card.Body>
          </Card>
        </Col>
        <Col lg={3} md={6} className="mb-3">
          <Card className="stat-card">
            <Card.Body className="text-center">
              <FiMessageSquare size={32} className="text-info mb-2" />
              <h6 className="text-muted mb-1">Messages</h6>
              <h2 className="mb-0">3</h2>
            </Card.Body>
          </Card>
        </Col>
      </Row>

      {/* Main Tabs */}
      <Tabs
        activeKey={activeTab}
        onSelect={(k) => setActiveTab(k)}
        className="student-tabs mb-4"
      >
        <Tab eventKey="dashboard" title={<><FiUser className="me-2" /> Dashboard </>}>
          <DashboardTab user={user} />
        </Tab>

        <Tab eventKey="profile" title={<><FiUser className="me-2" /> My Profile </>}>
          <ProfileTab user={user} />
        </Tab>

        <Tab eventKey="timetable" title={<><FiCalendar className="me-2" /> Timetable </>}>
          <TimetableTab />
        </Tab>

        <Tab eventKey="attendance" title={<><FiCheckCircle className="me-2" /> Attendance </>}>
          <AttendanceTab />
        </Tab>

        <Tab eventKey="grades" title={<><FiTrendingUp className="me-2" /> Grades </>}>
          <GradesTab />
        </Tab>

        <Tab eventKey="conduct" title={<><FiAward className="me-2" /> Conduct </>}>
          <ConductEvaluationTab />
        </Tab>

        <Tab eventKey="assignments" title={<><FiClipboard className="me-2" /> Assignments </>}>
          <AssignmentsTab />
        </Tab>

        <Tab eventKey="exams" title={<><FiAward className="me-2" /> Exams </>}>
          <ExamsTab />
        </Tab>

        <Tab eventKey="library" title={<><FiBook className="me-2" /> Library </>}>
          <LibraryTab />
        </Tab>

        <Tab eventKey="activities" title={<><FiHeart className="me-2" /> Activities </>}>
          <ActivitiesTab />
        </Tab>

        <Tab eventKey="fees" title={<><FiDollarSign className="me-2" /> Fees </>}>
          <FeesTab />
        </Tab>

        <Tab eventKey="records" title={<><FiAward className="me-2" /> Records </>}>
          <RecordsTab />
        </Tab>

        <Tab eventKey="communication" title={<><FiMessageSquare className="me-2" /> Messages </>}>
          <CommunicationTab />
        </Tab>

        <Tab eventKey="support" title={<><FiHelpCircle className="me-2" /> Support </>}>
          <SupportTab />
        </Tab>

        <Tab eventKey="clubs" title={<><FiHeart className="me-2" /> Clubs (CLB) </>}>
          <ClubsTab />
        </Tab>

        <Tab eventKey="permissions" title={<><FiFlag className="me-2" /> Permission Requests </>}>
          <PermissionRequestsTab />
        </Tab>
      </Tabs>
    </Container>
  );
}

export default StudentPortal;

