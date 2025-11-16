import React, { useState } from 'react';
import { Container, Row, Col, Card, Tab, Tabs, Badge, Pagination } from 'react-bootstrap';
import { FiFileText, FiUsers, FiCalendar, FiArrowRight, FiPhone, FiMail, FiMapPin } from 'react-icons/fi';
import { Link } from 'react-router-dom';
import '../styles/PrincipalHomePage.css';

function PrincipalHomePage() {
  const [news] = useState([
    {
      id: 1,
      title: 'Annual Sports Day 2025',
      content: 'Join us for the exciting Annual Sports Day on December 15, 2025. Various sports competitions and cultural programs will be held.',
      date: '2025-11-16',
      category: 'Event',
      image: '🏆'
    },
    {
      id: 2,
      title: 'New Computer Lab Inauguration',
      content: 'Our state-of-the-art computer lab with 50 high-end computers is now operational. Students can now use the latest technology for learning.',
      date: '2025-11-15',
      category: 'Infrastructure',
      image: '💻'
    },
    {
      id: 3,
      title: 'Excellence Awards Ceremony',
      content: 'Annual Excellence Awards ceremony will be held on November 30, 2025 to recognize and honor outstanding students and staff members.',
      date: '2025-11-14',
      category: 'Achievement',
      image: '🎖️'
    },
    {
      id: 4,
      title: 'School Foundation Day',
      content: 'Join us on November 25, 2025 to celebrate our school\'s 25th anniversary with special programs, performances, and prizes.',
      date: '2025-11-13',
      category: 'Event',
      image: '🎉'
    },
  ]);

  const [admissions] = useState([
    {
      id: 1,
      title: 'Admission for Class I',
      description: 'We are accepting admissions for Class I for the academic year 2025-2026. Age requirement: 5+ years.',
      requirements: ['Birth Certificate', 'Vaccination Certificate', 'Transfer Certificate (if applicable)'],
      deadline: '2025-12-31',
      status: 'Open',
      contact: 'admission@taysonsecondary.edu'
    },
    {
      id: 2,
      title: 'Admission for Class VI',
      description: 'Entrance examination based admission for Class VI. Merit-based selection process.',
      requirements: ['Previous School Marksheet', 'Birth Certificate', 'Transfer Certificate'],
      deadline: '2025-12-15',
      status: 'Open',
      contact: 'admission@taysonsecondary.edu'
    },
    {
      id: 3,
      title: 'Senior Secondary (Class XI)',
      description: 'Entrance examination for admission to Class XI. Stream selection available: Science, Commerce, Arts.',
      requirements: ['Class X Marksheet', 'Entrance Exam Score', 'Birth Certificate'],
      deadline: '2026-05-31',
      status: 'Upcoming',
      contact: 'admission@taysonsecondary.edu'
    },
  ]);

  const [currentNewsPage, setCurrentNewsPage] = useState(1);
  const newsPerPage = 3;
  const totalNewsPages = Math.ceil(news.length / newsPerPage);
  const displayedNews = news.slice((currentNewsPage - 1) * newsPerPage, currentNewsPage * newsPerPage);

  return (
    <div className="principal-home-page">
      {/* Header Section */}
      <div className="principal-header">
        <Container fluid>
          <div className="header-content">
            <div className="header-text">
              <h1>📚 Tay Son Secondary and High School</h1>
              <p>Quality Education for Quality Future</p>
            </div>
            <div className="header-actions">
              <Link to="/login" className="btn btn-primary btn-lg">
                <FiArrowRight /> Login Now
              </Link>
            </div>
          </div>
        </Container>
      </div>

      {/* Info Section */}
      <Container className="py-5">
        <Row className="mb-5">
          <Col md={3} className="mb-4">
            <Card className="info-card">
              <Card.Body className="text-center">
                <FiUsers size={40} className="text-primary mb-3" />
                <h4>5000+</h4>
                <p>Students</p>
              </Card.Body>
            </Card>
          </Col>
          <Col md={3} className="mb-4">
            <Card className="info-card">
              <Card.Body className="text-center">
                <FiFileText size={40} className="text-success mb-3" />
                <h4>150+</h4>
                <p>Faculty Members</p>
              </Card.Body>
            </Card>
          </Col>
          <Col md={3} className="mb-4">
            <Card className="info-card">
              <Card.Body className="text-center">
                <FiCalendar size={40} className="text-info mb-3" />
                <h4>25+</h4>
                <p>Years of Excellence</p>
              </Card.Body>
            </Card>
          </Col>
          <Col md={3} className="mb-4">
            <Card className="info-card">
              <Card.Body className="text-center">
                <FiArrowRight size={40} className="text-warning mb-3" />
                <h4>100%</h4>
                <p>Success Rate</p>
              </Card.Body>
            </Card>
          </Col>
        </Row>

        {/* Tabs Section */}
        <Tabs defaultActiveKey="news" id="principal-tabs" className="mb-4">
          {/* News Tab */}
          <Tab eventKey="news" title={<><FiFileText className="me-2" /> News & Updates </>}>
            <div className="news-section py-4">
              <h2 className="mb-4">Latest School News</h2>

              {displayedNews.map((item) => (
                <Card key={item.id} className="news-card mb-3">
                  <Card.Body>
                    <div className="news-header">
                      <div className="news-title-section">
                        <span className="news-image">{item.image}</span>
                        <div>
                          <h5 className="mb-1">{item.title}</h5>
                          <Badge bg="primary" className="me-2">{item.category}</Badge>
                          <small className="text-muted">
                            <FiCalendar size={14} className="me-1" />
                            {new Date(item.date).toLocaleDateString('en-US', {
                              year: 'numeric',
                              month: 'long',
                              day: 'numeric'
                            })}
                          </small>
                        </div>
                      </div>
                    </div>
                    <p className="mt-3 mb-0">{item.content}</p>
                  </Card.Body>
                </Card>
              ))}

              {/* Pagination */}
              {totalNewsPages > 1 && (
                <div className="d-flex justify-content-center mt-4">
                  <Pagination>
                    <Pagination.First
                      disabled={currentNewsPage === 1}
                      onClick={() => setCurrentNewsPage(1)}
                    />
                    <Pagination.Prev
                      disabled={currentNewsPage === 1}
                      onClick={() => setCurrentNewsPage(currentNewsPage - 1)}
                    />
                    {[...Array(totalNewsPages)].map((_, i) => (
                      <Pagination.Item
                        key={i + 1}
                        active={i + 1 === currentNewsPage}
                        onClick={() => setCurrentNewsPage(i + 1)}
                      >
                        {i + 1}
                      </Pagination.Item>
                    ))}
                    <Pagination.Next
                      disabled={currentNewsPage === totalNewsPages}
                      onClick={() => setCurrentNewsPage(currentNewsPage + 1)}
                    />
                    <Pagination.Last
                      disabled={currentNewsPage === totalNewsPages}
                      onClick={() => setCurrentNewsPage(totalNewsPages)}
                    />
                  </Pagination>
                </div>
              )}
            </div>
          </Tab>

          {/* Admissions Tab */}
          <Tab eventKey="admissions" title={<><FiUsers className="me-2" /> Admissions </>}>
            <div className="admissions-section py-4">
              <h2 className="mb-4">Admission Information</h2>

              {admissions.map((admission) => (
                <Card key={admission.id} className="admission-card mb-3">
                  <Card.Body>
                    <div className="admission-header">
                      <div>
                        <h5 className="mb-1">{admission.title}</h5>
                        <Badge
                          bg={admission.status === 'Open' ? 'success' : 'warning'}
                          className="me-2"
                        >
                          {admission.status}
                        </Badge>
                      </div>
                    </div>

                    <p className="mt-3 text-muted">{admission.description}</p>

                    <div className="requirements">
                      <h6 className="mb-2">Required Documents:</h6>
                      <ul className="mb-3">
                        {admission.requirements.map((req, idx) => (
                          <li key={idx}>{req}</li>
                        ))}
                      </ul>
                    </div>

                    <div className="admission-footer">
                      <div className="deadline-contact">
                        <small>
                          <strong>Deadline:</strong> {new Date(admission.deadline).toLocaleDateString('en-US', {
                            year: 'numeric',
                            month: 'long',
                            day: 'numeric'
                          })}
                        </small>
                        <br />
                        <small>
                          <FiMail size={14} className="me-1" />
                          {admission.contact}
                        </small>
                      </div>
                    </div>
                  </Card.Body>
                </Card>
              ))}
            </div>
          </Tab>

          {/* About Tab */}
          <Tab eventKey="about" title={<><FiFileText className="me-2" /> About Us </>}>
            <div className="about-section py-4">
              <h2 className="mb-4">About Our School</h2>

              <Row>
                <Col md={8}>
                  <h5>Our Mission</h5>
                  <p>
                    To provide quality education that develops critical thinking, creativity, and character
                    in our students, preparing them to be responsible global citizens who can contribute
                    meaningfully to society.
                  </p>

                  <h5 className="mt-4">Our Vision</h5>
                  <p>
                    To be a leading educational institution that nurtures excellence, fosters innovation,
                    and promotes holistic development of every individual.
                  </p>

                  <h5 className="mt-4">Our Values</h5>
                  <ul>
                    <li><strong>Integrity:</strong> Honesty and ethical behavior in all our dealings</li>
                    <li><strong>Excellence:</strong> Pursuit of highest standards in everything we do</li>
                    <li><strong>Inclusion:</strong> Embracing diversity and ensuring equal opportunities</li>
                    <li><strong>Innovation:</strong> Encouraging creative thinking and new approaches</li>
                    <li><strong>Compassion:</strong> Empathy and care for all members of our community</li>
                  </ul>
                </Col>

                <Col md={4}>
                  <Card className="bg-light">
                    <Card.Body>
                      <h6>Contact Information</h6>
                      <div className="contact-details">
                        <p>
                          <FiMapPin size={16} className="me-2" />
                          Tay Son Secondary and High School<br />
                          Tay Son District<br />
                          Vietnam
                        </p>
                        <p>
                          <FiPhone size={16} className="me-2" />
                          +84 (123) 456-7890
                        </p>
                        <p>
                          <FiMail size={16} className="me-2" />
                          info@taysonsecondary.edu
                        </p>
                      </div>
                    </Card.Body>
                  </Card>
                </Col>
              </Row>
            </div>
          </Tab>
        </Tabs>
      </Container>

      {/* Footer Section */}
      <div className="principal-footer bg-dark text-white py-4">
        <Container>
          <Row>
            <Col md={4} className="mb-3">
              <h6>Quick Links</h6>
              <ul className="list-unstyled">
                <li><Link to="/login" className="text-white-50 text-decoration-none">Login</Link></li>
                <li><a href="#admissions" className="text-white-50 text-decoration-none">Admissions</a></li>
                <li><a href="#news" className="text-white-50 text-decoration-none">News</a></li>
              </ul>
            </Col>
            <Col md={4} className="mb-3">
              <h6>Contact Us</h6>
              <small className="text-white-50">
                Tay Son Secondary and High School<br />
                Email: info@taysonsecondary.edu<br />
                Phone: +84 (123) 456-7890<br />
                Address: 123 Tay Son Street, City
              </small>
            </Col>
            <Col md={4} className="text-end">
              <h6>Follow Us</h6>
              <small className="text-white-50">
                Facebook | Twitter | Instagram | LinkedIn
              </small>
            </Col>
          </Row>
          <hr className="my-3" />
          <div className="text-center">
            <small className="text-white-50">
              © 2025 Tay Son Secondary and High School. All rights reserved.
            </small>
          </div>
        </Container>
      </div>
    </div>
  );
}

export default PrincipalHomePage;

