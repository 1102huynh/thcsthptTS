import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Tab, Tabs, Badge, Pagination, Spinner, Alert } from 'react-bootstrap';
import { FiFileText, FiUsers, FiCalendar, FiArrowRight, FiPhone, FiMail, FiMapPin, FiAward, FiTrendingUp } from 'react-icons/fi';
import { FaFacebookF, FaTwitter, FaInstagram, FaLinkedinIn } from 'react-icons/fa';
import { Link } from 'react-router-dom';
import newsService from '../services/newsService';
import admissionService from '../services/admissionService';
import '../styles/PrincipalHomePage.css';

function PrincipalHomePage() {
  const [isVisible, setIsVisible] = useState(false);
  const [news, setNews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [totalPages, setTotalPages] = useState(0);
  const [currentNewsPage, setCurrentNewsPage] = useState(1);
  const [admissions, setAdmissions] = useState([]);
  const [admissionsLoading, setAdmissionsLoading] = useState(true);
  const [admissionsError, setAdmissionsError] = useState(null);

  const fetchNews = async (page) => {
    console.log('Fetching news for page:', page);
    try {
      setLoading(true);
      setError(null);
      const response = await newsService.getPublishedNews(page, 3);
      console.log('News API response:', response);
      setNews(response.content || []);
      setTotalPages(response.totalPages || 0);
      setCurrentNewsPage(page + 1); // Convert to 1-based for display
      console.log('News loaded successfully:', response.content?.length || 0, 'items');
    } catch (err) {
      console.error('Error fetching news:', err);
      console.error('Error details:', err.response?.data || err.message);
      setError('Failed to load news from server. Showing sample news instead.');
      // Fallback to static data if API fails
      setNews([
        {
          id: 1,
          title: 'Annual Sports Day 2025',
          content: 'Join us for the exciting Annual Sports Day on December 15, 2025. Various sports competitions and cultural programs will be held.',
          publishedDate: '2025-11-16',
          category: 'Event',
          image: '🏆'
        },
        {
          id: 2,
          title: 'New Computer Lab Inauguration',
          content: 'Our state-of-the-art computer lab with 50 high-end computers is now operational. Students can now use the latest technology for learning.',
          publishedDate: '2025-11-15',
          category: 'Infrastructure',
          image: '💻'
        },
        {
          id: 3,
          title: 'Excellence Awards Ceremony',
          content: 'Annual Excellence Awards ceremony will be held on November 30, 2025 to recognize and honor outstanding students and staff members.',
          publishedDate: '2025-11-14',
          category: 'Achievement',
          image: '🎖️'
        },
      ]);
      setTotalPages(1);
      console.log('Fallback news data loaded');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    console.log('PrincipalHomePage mounted, loading initial data...');
    setIsVisible(true);
    fetchNews(0);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []); // Only run once on mount

  const fetchAdmissions = async () => {
    console.log('Fetching admissions...');
    try {
      setAdmissionsLoading(true);
      setAdmissionsError(null);
      const response = await admissionService.getOpenAdmissions(0, 10);
      console.log('Admissions API response:', response);
      setAdmissions(response.content || []);
      console.log('Admissions loaded successfully:', response.content?.length || 0, 'items');
    } catch (err) {
      console.error('Error fetching admissions:', err);
      console.error('Error details:', err.response?.data || err.message);
      setAdmissionsError('Failed to load admissions from server.');
      setAdmissions([]);
    } finally {
      setAdmissionsLoading(false);
    }
  };

  useEffect(() => {
    console.log('PrincipalHomePage mounted, loading admissions data...');
    fetchAdmissions();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handlePageChange = (pageNumber) => {
    fetchNews(pageNumber - 1); // Convert to 0-based for API
  };

  const statistics = [
    { icon: FiUsers, value: '5000+', label: 'Students Enrolled', color: 'primary', gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
    { icon: FiAward, value: '150+', label: 'Expert Faculty', color: 'success', gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' },
    { icon: FiCalendar, value: '25+', label: 'Years Excellence', color: 'info', gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' },
    { icon: FiTrendingUp, value: '100%', label: 'Success Rate', color: 'warning', gradient: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)' },
  ];

  return (
    <div className="principal-home-page">
      {/* Hero Header Section */}
      <div className="principal-hero-header">
        <Container fluid className="h-100">
          <div className={`hero-content ${isVisible ? 'fade-in' : ''}`}>
            <div className="hero-overlay"></div>
            <Container className="position-relative">
              <Row className="align-items-center min-vh-70">
                <Col lg={8} className="text-white">
                  <div className="hero-badge mb-3">
                    <Badge bg="light" text="dark" className="px-3 py-2">
                      🏆 Ranked #1 School in the Region
                    </Badge>
                  </div>
                  <h1 className="display-3 fw-bold mb-4 hero-title">
                    Tay Son Secondary<br />& High School
                  </h1>
                  <p className="lead mb-4 hero-subtitle">
                    Empowering minds, shaping futures. Quality education for a brighter tomorrow.
                  </p>
                  <div className="hero-actions">
                    <Link to="/login" className="btn btn-light btn-lg px-4 me-3 btn-hover-lift">
                      <FiArrowRight className="me-2" /> Access Portal
                    </Link>
                    <button className="btn btn-outline-light btn-lg px-4 btn-hover-lift">
                      <FiPhone className="me-2" /> Contact Us
                    </button>
                  </div>
                </Col>
              </Row>
            </Container>
          </div>
        </Container>
      </div>

      {/* Statistics Section */}
      <Container className="statistics-section">
        <Row className="g-4">
          {statistics.map((stat, index) => (
            <Col md={6} lg={3} key={index}>
              <Card className={`stat-card stat-card-${index} hover-lift`}>
                <div className="stat-gradient" style={{ background: stat.gradient }}></div>
                <Card.Body className="text-center position-relative">
                  <div className="stat-icon-wrapper mb-3">
                    <stat.icon size={48} className="stat-icon" />
                  </div>
                  <h2 className="stat-value mb-2">{stat.value}</h2>
                  <p className="stat-label mb-0">{stat.label}</p>
                </Card.Body>
              </Card>
            </Col>
          ))}
        </Row>
      </Container>

      {/* Main Content Section */}
      <Container className="content-section py-5">
        <Tabs defaultActiveKey="news" id="principal-tabs" className="modern-tabs mb-5">
          {/* News Tab */}
          <Tab eventKey="news" title={
            <span className="tab-title">
              <FiFileText className="me-2" />
              <span>News & Updates</span>
            </span>
          }>
            <div className="news-section">
              <div className="section-header mb-5 text-center">
                <h2 className="section-title">Latest News & Announcements</h2>
                <p className="section-subtitle text-muted">Stay updated with our school activities and achievements</p>
              </div>

              {loading && (
                <div className="text-center py-5">
                  <Spinner animation="border" variant="primary" />
                  <p className="mt-3">Loading news...</p>
                </div>
              )}

              {error && (
                <Alert variant="warning" className="mb-4">
                  {error}
                </Alert>
              )}

              {!loading && !error && (
                <>
                  <Row className="g-4">
                    {news.map((item, index) => (
                  <Col lg={12} key={item.id}>
                    <Card className={`modern-news-card hover-lift fade-in-up delay-${index}`}>
                      <Card.Body>
                        <Row className="align-items-center">
                          <Col md={2} className="text-center">
                            <div className="news-icon-large">
                              {item.image}
                            </div>
                          </Col>
                          <Col md={10}>
                            <div className="news-content">
                              <div className="d-flex align-items-center mb-2">
                                <Badge bg="primary" className="me-2 modern-badge">
                                  {item.category}
                                </Badge>
                                <small className="text-muted d-flex align-items-center">
                                  <FiCalendar size={14} className="me-1" />
                                  {new Date(item.publishedDate).toLocaleDateString('en-US', {
                                    year: 'numeric',
                                    month: 'long',
                                    day: 'numeric'
                                  })}
                                </small>
                              </div>
                              <h4 className="news-title mb-2">{item.title}</h4>
                              <p className="news-description text-muted mb-3">
                                {item.content.length > 200
                                  ? `${item.content.substring(0, 200)}...`
                                  : item.content}
                              </p>
                              <Link
                                to={`/news/${item.id}`}
                                className="btn btn-outline-primary btn-sm d-inline-flex align-items-center"
                              >
                                Read More <FiArrowRight className="ms-2" size={14} />
                              </Link>
                            </div>
                          </Col>
                        </Row>
                      </Card.Body>
                    </Card>
                  </Col>
                ))}
              </Row>

              {/* Pagination */}
              {totalPages > 1 && (
                <div className="d-flex justify-content-center mt-5">
                  <Pagination className="modern-pagination">
                    <Pagination.First
                      disabled={currentNewsPage === 1}
                      onClick={() => handlePageChange(1)}
                    />
                    <Pagination.Prev
                      disabled={currentNewsPage === 1}
                      onClick={() => handlePageChange(currentNewsPage - 1)}
                    />
                    {[...Array(totalPages)].map((_, i) => (
                      <Pagination.Item
                        key={i + 1}
                        active={i + 1 === currentNewsPage}
                        onClick={() => handlePageChange(i + 1)}
                      >
                        {i + 1}
                      </Pagination.Item>
                    ))}
                    <Pagination.Next
                      disabled={currentNewsPage === totalPages}
                      onClick={() => handlePageChange(currentNewsPage + 1)}
                    />
                    <Pagination.Last
                      disabled={currentNewsPage === totalPages}
                      onClick={() => handlePageChange(totalPages)}
                    />
                  </Pagination>
                </div>
              )}
              </>
              )}
            </div>
          </Tab>

          {/* Admissions Tab */}
          <Tab eventKey="admissions" title={
            <span className="tab-title">
              <FiUsers className="me-2" />
              <span>Admissions</span>
            </span>
          }>
            <div className="admissions-section">
              <div className="section-header mb-5 text-center">
                <h2 className="section-title">Admission Information</h2>
                <p className="section-subtitle text-muted">Join our community of excellence</p>
              </div>

              {admissionsLoading && (
                <div className="text-center py-5">
                  <Spinner animation="border" variant="primary" />
                  <p className="mt-3">Loading admissions...</p>
                </div>
              )}

              {admissionsError && (
                <Alert variant="warning" className="mb-4">
                  {admissionsError}
                </Alert>
              )}

              {!admissionsLoading && admissions.length === 0 && (
                <Alert variant="info" className="text-center">
                  No admission information available at this time.
                </Alert>
              )}

              {!admissionsLoading && admissions.length > 0 && (
                <Row className="g-4">
                  {admissions.map((admission, index) => (
                  <Col lg={12} key={admission.id}>
                    <Card className={`modern-admission-card hover-lift fade-in-up delay-${index}`}>
                      <Card.Body>
                        <div className="admission-header-modern mb-4">
                          <div className="d-flex justify-content-between align-items-start">
                            <div>
                              <h4 className="admission-title mb-2">{admission.title}</h4>
                              <div className="d-flex align-items-center gap-2 flex-wrap">
                                <Badge
                                  bg={admission.status === 'Open' ? 'success' : 'warning'}
                                  className="modern-badge"
                                >
                                  {admission.status}
                                </Badge>
                                <Badge bg="info" className="modern-badge">
                                  {admission.grade}
                                </Badge>
                              </div>
                            </div>
                            <div className="deadline-badge">
                              <FiCalendar size={20} />
                            </div>
                          </div>
                        </div>

                        <p className="admission-description text-muted mb-4">{admission.description}</p>

                        {/* Key Information */}
                        <Row className="mb-4">
                          <Col md={12} className="mb-3">
                            <div className="info-box p-3 bg-light rounded">
                              <small className="text-muted d-block mb-1">📚 Eligibility - Transfer Students</small>
                              <p className="mb-0 small fw-semibold">{admission.eligibility}</p>
                            </div>
                          </Col>
                          <Col md={6} className="mb-3">
                            <div className="info-box p-3 bg-primary bg-gradient text-white rounded">
                              <small className="d-block mb-1 opacity-75">🎓 Total Available Seats</small>
                              <p className="mb-0 fw-bold fs-5">{admission.seats}</p>
                            </div>
                          </Col>
                          <Col md={6} className="mb-3">
                            <div className="info-box p-3 bg-info bg-gradient text-white rounded">
                              <small className="d-block mb-1 opacity-75">🏫 Class Structure</small>
                              <p className="mb-1 small fw-semibold">{admission.classStructure}</p>
                              <p className="mb-0 small opacity-90">{admission.studentsPerClass}</p>
                            </div>
                          </Col>
                          <Col md={6} className="mb-3">
                            <div className="info-box p-3 bg-light rounded border border-primary">
                              <small className="text-muted d-block mb-1">📝 Entrance Exam Date</small>
                              <p className="mb-0 small fw-semibold text-primary">
                                {new Date(admission.examDate).toLocaleDateString('en-US', {
                                  year: 'numeric',
                                  month: 'long',
                                  day: 'numeric'
                                })}
                              </p>
                            </div>
                          </Col>
                          <Col md={6} className="mb-3">
                            <div className="info-box p-3 bg-light rounded border border-danger">
                              <small className="text-muted d-block mb-1">⏰ Application Deadline</small>
                              <p className="mb-0 small fw-semibold text-danger">
                                {new Date(admission.deadline).toLocaleDateString('en-US', {
                                  year: 'numeric',
                                  month: 'long',
                                  day: 'numeric'
                                })}
                              </p>
                            </div>
                          </Col>
                        </Row>

                        <div className="requirements-section mb-4">
                          <h6 className="requirements-title mb-3">
                            <FiFileText className="me-2" />
                            Required Documents
                          </h6>
                          <Row>
                            {admission.requirements.map((req, idx) => (
                              <Col md={6} key={idx} className="mb-2">
                                <div className="requirement-item">
                                  <span className="requirement-bullet">✓</span>
                                  {req}
                                </div>
                              </Col>
                            ))}
                          </Row>
                        </div>

                        <div className="admission-footer-modern">
                          <Row className="align-items-center">
                            <Col md={6}>
                              <div className="d-flex align-items-center gap-2">
                                <Badge bg="primary" className="px-3 py-2">
                                  Apply Now
                                </Badge>
                                <small className="text-muted">
                                  Application Process Online
                                </small>
                              </div>
                            </Col>
                            <Col md={6} className="text-md-end mt-3 mt-md-0">
                              <a href={`mailto:${admission.contact}`} className="contact-link">
                                <FiMail size={16} className="me-2" />
                                {admission.contact}
                              </a>
                            </Col>
                          </Row>
                        </div>
                      </Card.Body>
                    </Card>
                  </Col>
                  ))}
                </Row>
              )}
            </div>
          </Tab>

          {/* About Tab */}
          <Tab eventKey="about" title={
            <span className="tab-title">
              <FiFileText className="me-2" />
              <span>About Us</span>
            </span>
          }>
            <div className="about-section">
              <div className="section-header mb-5 text-center">
                <h2 className="section-title">About Our Institution</h2>
                <p className="section-subtitle text-muted">Building tomorrow's leaders today</p>
              </div>

              <Row className="g-4">
                <Col lg={8}>
                  <Card className="modern-about-card mb-4 hover-lift">
                    <Card.Body className="p-4">
                      <h5 className="about-subtitle mb-3">🎯 Our Mission</h5>
                      <p className="about-text">
                        To provide quality education that develops critical thinking, creativity, and character
                        in our students, preparing them to be responsible global citizens who can contribute
                        meaningfully to society.
                      </p>
                    </Card.Body>
                  </Card>

                  <Card className="modern-about-card mb-4 hover-lift">
                    <Card.Body className="p-4">
                      <h5 className="about-subtitle mb-3">🔭 Our Vision</h5>
                      <p className="about-text">
                        To be a leading educational institution that nurtures excellence, fosters innovation,
                        and promotes holistic development of every individual.
                      </p>
                    </Card.Body>
                  </Card>

                  <Card className="modern-about-card hover-lift">
                    <Card.Body className="p-4">
                      <h5 className="about-subtitle mb-3">💎 Our Values</h5>
                      <Row className="g-3">
                        {[
                          { icon: '🎯', title: 'Integrity', desc: 'Honesty and ethical behavior' },
                          { icon: '⭐', title: 'Excellence', desc: 'Pursuing highest standards' },
                          { icon: '🤝', title: 'Inclusion', desc: 'Embracing diversity' },
                          { icon: '💡', title: 'Innovation', desc: 'Encouraging creativity' },
                          { icon: '❤️', title: 'Compassion', desc: 'Empathy and care' },
                        ].map((value, idx) => (
                          <Col md={6} key={idx}>
                            <div className="value-item">
                              <span className="value-icon">{value.icon}</span>
                              <div>
                                <strong className="d-block">{value.title}</strong>
                                <small className="text-muted">{value.desc}</small>
                              </div>
                            </div>
                          </Col>
                        ))}
                      </Row>
                    </Card.Body>
                  </Card>
                </Col>

                <Col lg={4}>
                  <Card className="modern-contact-card hover-lift sticky-top" style={{ top: '20px' }}>
                    <div className="contact-card-header">
                      <h5 className="text-white mb-0">📞 Contact Information</h5>
                    </div>
                    <Card.Body className="p-4">
                      <div className="contact-item mb-4">
                        <div className="contact-icon-wrapper">
                          <FiMapPin size={20} />
                        </div>
                        <div className="contact-details">
                          <small className="text-muted d-block">Address</small>
                          <p className="mb-0">
                            Tay Son Secondary and High School<br />
                            Tay Son District<br />
                            Vietnam
                          </p>
                        </div>
                      </div>

                      <div className="contact-item mb-4">
                        <div className="contact-icon-wrapper">
                          <FiPhone size={20} />
                        </div>
                        <div className="contact-details">
                          <small className="text-muted d-block">Phone</small>
                          <p className="mb-0">+84 (123) 456-7890</p>
                        </div>
                      </div>

                      <div className="contact-item mb-4">
                        <div className="contact-icon-wrapper">
                          <FiMail size={20} />
                        </div>
                        <div className="contact-details">
                          <small className="text-muted d-block">Email</small>
                          <p className="mb-0">info@taysonsecondary.edu</p>
                        </div>
                      </div>

                      <hr className="my-4" />

                      <div className="social-links">
                        <small className="text-muted d-block mb-3">Follow Us</small>
                        <div className="d-flex gap-2">
                          {[FaFacebookF, FaTwitter, FaInstagram, FaLinkedinIn].map((Icon, idx) => (
                            <a href="#" key={idx} className="social-icon-link">
                              <Icon size={18} />
                            </a>
                          ))}
                        </div>
                      </div>
                    </Card.Body>
                  </Card>
                </Col>
              </Row>
            </div>
          </Tab>
        </Tabs>
      </Container>

      {/* Modern Footer */}
      <footer className="modern-footer">
        <Container>
          <Row className="py-5">
            <Col lg={4} className="mb-4">
              <h5 className="footer-title mb-4">Tay Son School</h5>
              <p className="footer-text">
                Empowering students with quality education and preparing them for a successful future.
              </p>
              <div className="social-icons mt-4">
                {[
                  { Icon: FaFacebookF, label: 'Facebook' },
                  { Icon: FaTwitter, label: 'Twitter' },
                  { Icon: FaInstagram, label: 'Instagram' },
                  { Icon: FaLinkedinIn, label: 'LinkedIn' }
                ].map((social, idx) => (
                  <a href="#" key={idx} className="footer-social-link" aria-label={social.label}>
                    <social.Icon size={16} />
                  </a>
                ))}
              </div>
            </Col>

            <Col lg={2} md={6} className="mb-4">
              <h6 className="footer-heading mb-4">Quick Links</h6>
              <ul className="footer-links">
                <li><Link to="/login">Login Portal</Link></li>
                <li><a href="#admissions">Admissions</a></li>
                <li><a href="#news">News & Events</a></li>
                <li><a href="#about">About Us</a></li>
              </ul>
            </Col>

            <Col lg={3} md={6} className="mb-4">
              <h6 className="footer-heading mb-4">Contact Info</h6>
              <ul className="footer-contact">
                <li>
                  <FiMapPin size={16} />
                  <span>123 Tay Son Street, City</span>
                </li>
                <li>
                  <FiPhone size={16} />
                  <span>+84 (123) 456-7890</span>
                </li>
                <li>
                  <FiMail size={16} />
                  <span>info@taysonsecondary.edu</span>
                </li>
              </ul>
            </Col>

            <Col lg={3} className="mb-4">
              <h6 className="footer-heading mb-4">Office Hours</h6>
              <div className="office-hours">
                <p className="mb-2">Monday - Friday</p>
                <p className="text-primary fw-semibold mb-3">8:00 AM - 4:00 PM</p>
                <p className="mb-2">Saturday</p>
                <p className="text-primary fw-semibold">8:00 AM - 12:00 PM</p>
              </div>
            </Col>
          </Row>

          <hr className="footer-divider" />

          <div className="footer-bottom py-4">
            <Row className="align-items-center">
              <Col md={6} className="text-center text-md-start mb-3 mb-md-0">
                <small className="footer-copyright">
                  © 2025 Tay Son Secondary and High School. All rights reserved.
                </small>
              </Col>
              <Col md={6} className="text-center text-md-end">
                <small className="footer-links-inline">
                  <a href="#privacy">Privacy Policy</a>
                  <span className="mx-2">•</span>
                  <a href="#terms">Terms of Service</a>
                  <span className="mx-2">•</span>
                  <a href="#sitemap">Sitemap</a>
                </small>
              </Col>
            </Row>
          </div>
        </Container>
      </footer>
    </div>
  );
}

export default PrincipalHomePage;

