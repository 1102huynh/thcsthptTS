import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Badge, Spinner, Alert, Button } from 'react-bootstrap';
import { useParams, useNavigate } from 'react-router-dom';
import { FiCalendar, FiArrowLeft, FiEye, FiUser } from 'react-icons/fi';
import newsService from '../services/newsService';
import '../styles/NewsDetailPage.css';

function NewsDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [news, setNews] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchNewsDetail();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const fetchNewsDetail = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await newsService.getNewsById(id);
      setNews(response);
    } catch (err) {
      console.error('Error fetching news detail:', err);
      setError('Failed to load news details. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <Container className="news-detail-page py-5">
        <div className="text-center py-5">
          <Spinner animation="border" variant="primary" />
          <p className="mt-3">Loading news...</p>
        </div>
      </Container>
    );
  }

  if (error || !news) {
    return (
      <Container className="news-detail-page py-5">
        <Alert variant="danger" className="mb-4">
          {error || 'News not found'}
        </Alert>
        <Button variant="primary" onClick={() => navigate('/')}>
          <FiArrowLeft className="me-2" /> Back to Home
        </Button>
      </Container>
    );
  }

  return (
    <div className="news-detail-page">
      {/* Header with back button */}
      <div className="news-detail-header py-4 bg-light">
        <Container>
          <Button
            variant="link"
            className="text-decoration-none p-0 mb-3"
            onClick={() => navigate('/')}
          >
            <FiArrowLeft className="me-2" size={18} />
            Back to Home
          </Button>
        </Container>
      </div>

      {/* Main Content */}
      <Container className="py-5">
        <Row className="justify-content-center">
          <Col lg={10} xl={8}>
            <article className="news-article">
              {/* News Header */}
              <div className="news-article-header mb-4">
                <div className="d-flex align-items-center mb-3 flex-wrap gap-2">
                  <Badge bg="primary" className="modern-badge">
                    {news.category}
                  </Badge>
                  {news.featured && (
                    <Badge bg="warning" text="dark" className="modern-badge">
                      ⭐ Featured
                    </Badge>
                  )}
                </div>

                <h1 className="news-article-title mb-4">
                  {news.title}
                </h1>

                <div className="news-article-meta d-flex align-items-center text-muted flex-wrap gap-3">
                  <div className="d-flex align-items-center">
                    <FiCalendar size={16} className="me-2" />
                    <span>
                      {new Date(news.publishedDate).toLocaleDateString('en-US', {
                        year: 'numeric',
                        month: 'long',
                        day: 'numeric'
                      })}
                    </span>
                  </div>
                  {news.authorName && (
                    <div className="d-flex align-items-center">
                      <FiUser size={16} className="me-2" />
                      <span>{news.authorName}</span>
                    </div>
                  )}
                  {news.viewCount > 0 && (
                    <div className="d-flex align-items-center">
                      <FiEye size={16} className="me-2" />
                      <span>{news.viewCount} views</span>
                    </div>
                  )}
                </div>
              </div>

              {/* News Image/Icon */}
              {news.image && (
                <div className="news-article-image text-center mb-5">
                  <div className="news-icon-large-display">
                    {news.image}
                  </div>
                </div>
              )}

              {/* News Content */}
              <Card className="news-content-card">
                <Card.Body className="p-4 p-md-5">
                  <div className="news-article-content">
                    {news.content.split('\n\n').map((paragraph, index) => (
                      <p key={index} className="news-paragraph">
                        {paragraph}
                      </p>
                    ))}
                  </div>
                </Card.Body>
              </Card>

              {/* Footer Actions */}
              <div className="news-article-footer mt-4">
                <div className="d-flex justify-content-between align-items-center">
                  <Button
                    variant="outline-primary"
                    onClick={() => navigate('/')}
                  >
                    <FiArrowLeft className="me-2" />
                    Back to All News
                  </Button>

                  <div className="text-muted small">
                    Published on {new Date(news.publishedDate).toLocaleDateString('en-US', {
                      year: 'numeric',
                      month: 'long',
                      day: 'numeric'
                    })}
                  </div>
                </div>
              </div>
            </article>
          </Col>
        </Row>
      </Container>
    </div>
  );
}

export default NewsDetailPage;

