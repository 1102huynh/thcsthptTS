import React, { useState } from 'react';
import { Container, Row, Col, Card, Tab, Tabs, Badge, Button, ProgressBar, ListGroup, Alert, Form } from 'react-bootstrap';
import {
  FiUser, FiBook, FiCheckCircle, FiAward, FiDollarSign, FiHeart,
  FiMessageSquare, FiHelpCircle, FiCalendar, FiClipboard, FiTrendingUp,
  FiClock, FiDownload, FiUpload, FiSearch, FiFlag, FiInfo, FiAlertTriangle
} from 'react-icons/fi';
import { studentService, timetableService } from '../services/dataService';
import { authService } from '../services/authService';
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
  const [isEditing, setIsEditing] = useState(false);
  const [studentId, setStudentId] = useState(null);
  const [profileData, setProfileData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    address: '',
    dateOfBirth: '',
    gender: '',
    fatherName: '',
    fatherPhone: '',
    motherName: '',
    motherPhone: ''
  });
  const [showSuccessAlert, setShowSuccessAlert] = useState(false);
  const [showErrorAlert, setShowErrorAlert] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  // Load student data on component mount
  React.useEffect(() => {
    const loadStudentData = async () => {
      try {
        if (user?.userId) {
          const response = await studentService.getByUserId(user.userId);
          const student = response.data;
          setStudentId(student.id);

          // Map student data to profile data - NO HARDCODED DEFAULTS
          setProfileData({
            firstName: student.user?.firstName || '',
            lastName: student.user?.lastName || '',
            email: student.user?.email || '',
            phone: student.user?.phoneNumber || '',
            address: student.address || '',
            dateOfBirth: student.dateOfBirth || '',
            gender: student.gender || '',
            fatherName: student.fatherName || '',
            fatherPhone: student.fatherPhone || '',
            motherName: student.motherName || '',
            motherPhone: student.motherPhone || ''
          });
        }
      } catch (error) {
        console.error('Error loading student data:', error);
        setErrorMessage('Failed to load student profile');
        setShowErrorAlert(true);
      }
    };

    loadStudentData();
  }, [user]);

  const handleInputChange = (field, value) => {
    setProfileData(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const handleSave = async () => {
    try {
      if (!studentId) {
        setErrorMessage('Student ID not found');
        setShowErrorAlert(true);
        return;
      }

      // Prepare the data to send to backend
      const updateData = {
        firstName: profileData.firstName,
        lastName: profileData.lastName,
        dateOfBirth: profileData.dateOfBirth,
        gender: profileData.gender,
        address: profileData.address,
        fatherName: profileData.fatherName,
        fatherPhone: profileData.fatherPhone,
        motherName: profileData.motherName,
        motherPhone: profileData.motherPhone,
        user: {
          firstName: profileData.firstName,
          lastName: profileData.lastName
        }
      };

      console.log("=== FRONTEND SAVE DEBUG ===");
      console.log("Student ID:", studentId);
      console.log("Data being sent to backend:");
      console.log(updateData);
      console.log("Calling API: PUT /api/v1/students/" + studentId);

      const response = await studentService.update(studentId, updateData);

      console.log("API response received:");
      console.log(response.data);

      // Reload the profile data from server to ensure sync
      const reloadResponse = await studentService.getByUserId(user.userId);
      const student = reloadResponse.data;

      console.log("Reloaded student data from server:");
      console.log(student);

      // Update the form with fresh data from server - NO HARDCODED DEFAULTS
      setProfileData({
        firstName: student.user?.firstName || '',
        lastName: student.user?.lastName || '',
        email: student.user?.email || '',
        phone: student.user?.phoneNumber || '',
        address: student.address || '',
        dateOfBirth: student.dateOfBirth || '',
        gender: student.gender || '',
        fatherName: student.fatherName || '',
        fatherPhone: student.fatherPhone || '',
        motherName: student.motherName || '',
        motherPhone: student.motherPhone || ''
      });

      console.log("=== END SAVE DEBUG ===\n");

      setIsEditing(false);
      setShowSuccessAlert(true);
      setTimeout(() => setShowSuccessAlert(false), 3000);
    } catch (error) {
      console.error('Error saving profile:', error);
      setErrorMessage(error.response?.data?.message || 'Failed to save profile. Please try again.');
      setShowErrorAlert(true);
      setTimeout(() => setShowErrorAlert(false), 3000);
    }
  };

  const handleCancel = () => {
    // Reset form data - you may want to reload from server
    setIsEditing(false);
  };

  return (
    <div className="tab-content py-4">
      {showSuccessAlert && (
        <Alert variant="success" className="mb-3">
          <FiCheckCircle className="me-2" />
          Profile updated successfully!
        </Alert>
      )}
      {showErrorAlert && (
        <Alert variant="danger" className="mb-3" onClose={() => setShowErrorAlert(false)} dismissible>
          <strong>Error:</strong> {errorMessage}
        </Alert>
      )}

      <Row>
        <Col lg={8}>
          <Card className="content-card">
            <Card.Header className="bg-light d-flex justify-content-between align-items-center">
              <Card.Title className="mb-0">Personal Information</Card.Title>
              <div>
                {isEditing ? (
                  <>
                    <Button variant="success" size="sm" className="me-2" onClick={handleSave}>
                      <FiCheckCircle className="me-1" /> Save
                    </Button>
                    <Button variant="secondary" size="sm" onClick={handleCancel}>
                      Cancel
                    </Button>
                  </>
                ) : (
                  <Button variant="primary" size="sm" onClick={() => setIsEditing(true)}>
                    <FiUser className="me-1" /> Edit Profile
                  </Button>
                )}
              </div>
            </Card.Header>
            <Card.Body>
              <Row>
                <Col md={6} className="mb-3">
                  <label className="text-secondary small fw-semibold">First Name</label>
                  {isEditing ? (
                    <Form.Control
                      type="text"
                      value={profileData.firstName}
                      onChange={(e) => handleInputChange('firstName', e.target.value)}
                    />
                  ) : (
                    <p className="fw-bold text-dark">{profileData.firstName}</p>
                  )}
                </Col>
                <Col md={6} className="mb-3">
                  <label className="text-secondary small fw-semibold">Last Name</label>
                  {isEditing ? (
                    <Form.Control
                      type="text"
                      value={profileData.lastName}
                      onChange={(e) => handleInputChange('lastName', e.target.value)}
                    />
                  ) : (
                    <p className="fw-bold text-dark">{profileData.lastName}</p>
                  )}
                </Col>
              </Row>
              <Row>
                <Col md={6} className="mb-3">
                  <label className="text-secondary small fw-semibold">Student ID</label>
                  <p className="fw-bold text-dark">STU001 <small className="text-muted">(Cannot be changed)</small></p>
                </Col>
                <Col md={6} className="mb-3">
                  <label className="text-secondary small fw-semibold">Email</label>
                  {isEditing ? (
                    <Form.Control
                      type="email"
                      value={profileData.email}
                      onChange={(e) => handleInputChange('email', e.target.value)}
                    />
                  ) : (
                    <p className="fw-bold text-dark">{profileData.email}</p>
                  )}
                </Col>
              </Row>
              <Row>
                <Col md={6} className="mb-3">
                  <label className="text-secondary small fw-semibold">Class</label>
                  <p className="fw-bold text-dark">10A <small className="text-muted">(Cannot be changed)</small></p>
                </Col>
                <Col md={6} className="mb-3">
                  <label className="text-secondary small fw-semibold">Roll Number</label>
                  <p className="fw-bold text-dark">025 <small className="text-muted">(Cannot be changed)</small></p>
                </Col>
              </Row>
              <Row>
                <Col md={6} className="mb-3">
                  <label className="text-secondary small fw-semibold">Date of Birth</label>
                  {isEditing ? (
                    <Form.Control
                      type="date"
                      value={profileData.dateOfBirth}
                      onChange={(e) => handleInputChange('dateOfBirth', e.target.value)}
                    />
                  ) : (
                    <p className="fw-bold text-dark">{new Date(profileData.dateOfBirth).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' })}</p>
                  )}
                </Col>
                <Col md={6} className="mb-3">
                  <label className="text-secondary small fw-semibold">Gender</label>
                  {isEditing ? (
                    <Form.Select
                      value={profileData.gender}
                      onChange={(e) => handleInputChange('gender', e.target.value)}
                    >
                      <option value="Male">Male</option>
                      <option value="Female">Female</option>
                      <option value="Other">Other</option>
                    </Form.Select>
                  ) : (
                    <p className="fw-bold text-dark">{profileData.gender}</p>
                  )}
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
              <div className="mb-3">
                <small className="text-secondary fw-semibold">Phone</small>
                {isEditing ? (
                  <Form.Control
                    type="tel"
                    value={profileData.phone}
                    onChange={(e) => handleInputChange('phone', e.target.value)}
                    placeholder="+84 900 123 456"
                  />
                ) : (
                  <p className="fw-bold text-dark">{profileData.phone}</p>
                )}
              </div>
              <div className="mb-2">
                <small className="text-secondary fw-semibold">Address</small>
                {isEditing ? (
                  <Form.Control
                    as="textarea"
                    rows={2}
                    value={profileData.address}
                    onChange={(e) => handleInputChange('address', e.target.value)}
                  />
                ) : (
                  <p className="fw-bold text-dark">{profileData.address}</p>
                )}
              </div>
            </Card.Body>
          </Card>

          <Card className="content-card">
            <Card.Header className="bg-light">
              <Card.Title className="mb-0">Parent Information</Card.Title>
            </Card.Header>
            <Card.Body>
              <div className="mb-3">
                <small className="text-secondary fw-semibold">Father's Name</small>
                {isEditing ? (
                  <Form.Control
                    type="text"
                    value={profileData.fatherName}
                    onChange={(e) => handleInputChange('fatherName', e.target.value)}
                  />
                ) : (
                  <p className="fw-bold text-dark">{profileData.fatherName}</p>
                )}
              </div>
              <div className="mb-3">
                <small className="text-secondary fw-semibold">Father's Phone</small>
                {isEditing ? (
                  <Form.Control
                    type="tel"
                    value={profileData.fatherPhone}
                    onChange={(e) => handleInputChange('fatherPhone', e.target.value)}
                  />
                ) : (
                  <p className="fw-bold text-dark">{profileData.fatherPhone}</p>
                )}
              </div>
              <div className="mb-3">
                <small className="text-secondary fw-semibold">Mother's Name</small>
                {isEditing ? (
                  <Form.Control
                    type="text"
                    value={profileData.motherName}
                    onChange={(e) => handleInputChange('motherName', e.target.value)}
                  />
                ) : (
                  <p className="fw-bold text-dark">{profileData.motherName}</p>
                )}
              </div>
              <div>
                <small className="text-secondary fw-semibold">Mother's Phone</small>
                {isEditing ? (
                  <Form.Control
                    type="tel"
                    value={profileData.motherPhone}
                    onChange={(e) => handleInputChange('motherPhone', e.target.value)}
                  />
                ) : (
                  <p className="fw-bold text-dark">{profileData.motherPhone}</p>
                )}
              </div>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </div>
  );
}

// Timetable Tab
// NOTE: Students can ONLY VIEW the timetable (read-only)
// Teachers can EDIT the timetable (through TeacherPortal)
// Data is loaded from backend API - NO HARDCODED DATA
function TimetableTab() {
  const [timetable, setTimetable] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [classId, setClassId] = useState(null);
  const [groupLabel, setGroupLabel] = useState('');
  const [sessionInfo, setSessionInfo] = useState('');

  // Load timetable data when component mounts
  React.useEffect(() => {
    const loadTimetable = async () => {
      try {
        setLoading(true);
        setError(null);

        // Get current user using authService helper
        const currentUser = authService.getCurrentUser();

        if (!currentUser) {
          setError('User information not found. Please log in again.');
          setLoading(false);
          return;
        }

        console.log('Current User:', currentUser);

        // Try multiple field names for user ID (id, userId, user_id)
        const userId = currentUser?.id ||
                       currentUser?.userId ||
                       currentUser?.user_id ||
                       currentUser?.username;

        if (!userId) {
          console.error('No userId found in user object:', currentUser);
          setError('User ID not found. Please log in again.');
          setLoading(false);
          return;
        }

        console.log('Retrieved UserId:', userId, 'Type:', typeof userId);

        // Fetch student data to get class information
        const response = await studentService.getByUserId(userId);
        const student = response.data;

        if (!student) {
          setError('Student information not found.');
          setLoading(false);
          return;
        }

        console.log('Student Data:', student);

        // Determine class ID from student data
        // Try multiple possible field names
        const className = student?.className || student?.class_name || '10';
        const section = student?.section || 'A';

        // Map class_name and section to classId
        let studentClassId = 1; // Default to Class 10A

        const classKey = `${className}-${section}`;
        const classMapping = {
          '10-A': 1,
          '10-B': 2,
        };
        studentClassId = classMapping[classKey] || 1;

        console.log('Class Mapping - ClassName:', className, 'Section:', section, 'ClassID:', studentClassId, 'Type:', typeof studentClassId);

        // Ensure classId is a number
        if (typeof studentClassId !== 'number' || isNaN(studentClassId)) {
          console.error('Invalid classId:', studentClassId);
          setError('Invalid class ID. Please contact administrator.');
          setLoading(false);
          return;
        }

        // Get timetable for the student's class
        const timetableResponse = await timetableService.getByClass(studentClassId, '2024-2025');
        const timetableData = timetableResponse.data;

        if (!timetableData || timetableData.length === 0) {
          setError('No timetable data available for your class.');
          setTimetable([]);
          setLoading(false);
          return;
        }

        // Determine student's session type from timetable data
        const firstEntry = timetableData[0];
        const sessionType = firstEntry?.sessionType || 'MORNING';

        // Set group label and session info based on data
        if (sessionType === 'MORNING') {
          setGroupLabel('Group A - Morning');
          setSessionInfo('07:00 - 12:00');
        } else {
          setGroupLabel('Group B - Afternoon');
          setSessionInfo('13:00 - 18:00');
        }

        setTimetable(timetableData);
        console.log('Timetable loaded successfully:', timetableData.length, 'entries');
      } catch (err) {
        console.error('Error caught:', err);
        console.error('Error type:', typeof err);
        console.error('Error string:', String(err));

        // Build comprehensive error message
        let errorMsg = '';

        try {
          // Try all possible error message sources
          if (err?.response?.data?.message) {
            errorMsg = String(err.response.data.message).trim();
          } else if (err?.response?.data?.error) {
            errorMsg = String(err.response.data.error).trim();
          } else if (err?.response?.statusText) {
            errorMsg = `Error ${err.response.status}: ${err.response.statusText}`;
          } else if (err?.message) {
            errorMsg = String(err.message).trim();
          } else if (typeof err === 'string') {
            errorMsg = err.trim();
          } else if (err?.toString && typeof err.toString === 'function') {
            errorMsg = err.toString();
          }
        } catch (e) {
          console.error('Error extracting message:', e);
        }

        // Ensure errorMsg is valid
        if (!errorMsg || errorMsg === '' || errorMsg === 'Error' || errorMsg === '[object Object]') {
          errorMsg = 'Failed to load timetable. Please try again.';
        }

        console.error('Final error message:', errorMsg);
        setError(errorMsg);
        setTimetable([]);
      } finally {
        setLoading(false);
      }
    };

    loadTimetable();
  }, []);

  // Group timetable entries by day and session
  const groupedTimetable = {};
  if (timetable && timetable.length > 0) {
    timetable.forEach((entry) => {
      const key = `${entry.dayOfWeek} - ${entry.sessionType}`;
      if (!groupedTimetable[key]) {
        groupedTimetable[key] = [];
      }
      groupedTimetable[key].push(entry);
    });

    // Sort each day's lessons by time slot
    Object.keys(groupedTimetable).forEach((key) => {
      groupedTimetable[key].sort((a, b) => a.timeSlot - b.timeSlot);
    });
  }

  // Format time for display
  const formatTime = (time) => {
    if (!time) return '';
    // If time is in HH:mm:ss format, keep as is. If in other format, adjust as needed
    return time.substring(0, 5); // Get HH:mm part
  };

  return (
    <div className="tab-content py-4">
      {/* Loading State */}
      {loading && (
        <Alert variant="info">
          <div className="text-center">
            <div className="spinner-border spinner-border-sm me-2" role="status">
              <span className="visually-hidden">Loading...</span>
            </div>
            Loading timetable data...
          </div>
        </Alert>
      )}

      {/* Error State */}
      {error && !loading && (
        <Alert variant="danger" onClose={() => setError(null)} dismissible>
          <FiAlertTriangle className="me-2" />
          <strong>Error:</strong> {error || 'An unexpected error occurred. Please try again.'}
        </Alert>
      )}

      {/* Group Information */}
      {!loading && timetable.length > 0 && (
        <Alert variant="primary" className="mb-4">
          <strong>📚 Your Class Schedule: {groupLabel}</strong>
          <br />
          Session Time: <strong>{sessionInfo}</strong>
          <br />
          <small className="text-secondary">
            Classroom: <strong>
              {timetable.length > 0 ? `Room ${timetable[0].classroom}` : 'Room A'}
            </strong> (Shared with all students)
          </small>
        </Alert>
      )}

      {/* Timetable Display */}
      {!loading && timetable.length > 0 && (
        <div>
          {Object.keys(groupedTimetable).map((session) => (
            <Card key={session} className="content-card mb-3">
              <Card.Header className="bg-light">
                <Card.Title className="mb-0">{session}</Card.Title>
                <small className="text-secondary">
                  {groupedTimetable[session].length} lessons × 45 minutes each
                </small>
              </Card.Header>
              <Card.Body className="p-0">
                <ListGroup variant="flush">
                  {groupedTimetable[session].map((item, idx) => (
                    <ListGroup.Item key={idx}>
                      <Row className="align-items-center">
                        <Col md={2}>
                          <small className="text-secondary fw-semibold">
                            {formatTime(item.startTime)} - {formatTime(item.endTime)}
                          </small>
                          <br />
                          <small className="text-muted">45 min</small>
                        </Col>
                        <Col md={4}>
                          <h6 className="mb-0 text-dark">{item.subject}</h6>
                          {item.subjectTeacherName && (
                            <small className="text-secondary">
                              👨‍🏫 {item.subjectTeacherName}
                            </small>
                          )}
                        </Col>
                        <Col md={3}>
                          {item.subjectTeacherEmail && (
                            <small className="text-muted d-block">
                              📧 {item.subjectTeacherEmail}
                            </small>
                          )}
                          {item.subjectTeacherPhone && (
                            <small className="text-muted d-block">
                              📞 {item.subjectTeacherPhone}
                            </small>
                          )}
                        </Col>
                        <Col md={3} className="text-end">
                          <Badge bg="info" text="white">Room {item.classroom}</Badge>
                        </Col>
                      </Row>
                    </ListGroup.Item>
                  ))}
                </ListGroup>
              </Card.Body>
            </Card>
          ))}
        </div>
      )}

      {/* No Data State */}
      {!loading && timetable.length === 0 && !error && (
        <Alert variant="warning">
          <FiInfo className="me-2" />
          No timetable data available. Please contact your school administrator.
        </Alert>
      )}

      {/* Information and Rules */}
      {!loading && (
        <Alert variant="info" className="mt-3">
          <FiInfo className="me-2" />
          <strong>Schedule Information:</strong>
          <ul className="mb-0 mt-2">
            <li>Full school week: <strong>Monday to Saturday</strong></li>
            <li>All lessons held in: <strong>Room A</strong> (one shared classroom)</li>
            <li>Each lesson lasts: <strong>45 minutes</strong></li>
            <li>You study either <strong>morning (07:00-12:00) OR afternoon (13:00-18:00)</strong> for entire week</li>
            <li>Maximum <strong>5 lessons per day</strong></li>
            <li>Breaks: <strong>15 minutes</strong> between lessons</li>
            <li><strong>Teachers</strong> can edit the timetable through the Teacher Portal</li>
          </ul>
        </Alert>
      )}
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

