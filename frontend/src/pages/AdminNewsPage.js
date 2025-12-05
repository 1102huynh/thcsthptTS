import React, { useState, useEffect } from 'react';
import {
  Container,
  Row,
  Col,
  Card,
  Button,
  Table,
  Modal,
  Form,
  Badge,
  Spinner,
  Alert,
  Pagination
} from 'react-bootstrap';
import { FiPlus, FiEdit, FiTrash2, FiCheck, FiArchive } from 'react-icons/fi';
import newsService from '../services/newsService';
import '../styles/AdminNews.css';

function AdminNewsPage() {
  const [news, setNews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(0);

  const [showModal, setShowModal] = useState(false);
  const [modalMode, setModalMode] = useState('create'); // 'create' or 'edit'
  const [selectedNews, setSelectedNews] = useState(null);

  const [formData, setFormData] = useState({
    title: '',
    content: '',
    category: 'Event',
    image: '📰',
    status: 'DRAFT',
    featured: false,
    authorName: ''
  });

  useEffect(() => {
    fetchAllNews(0);
  }, []);

  const fetchAllNews = async (page) => {
    try {
      setLoading(true);
      setError(null);
      const response = await newsService.getAllNewsForAdmin(page, 10);
      setNews(response.content || []);
      setTotalPages(response.totalPages || 0);
      setCurrentPage(page + 1);
    } catch (err) {
      console.error('Error fetching news:', err);
      setError('Failed to load news. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handlePageChange = (pageNumber) => {
    fetchAllNews(pageNumber - 1);
  };

  const handleShowModal = (mode, newsItem = null) => {
    setModalMode(mode);
    if (mode === 'edit' && newsItem) {
      setSelectedNews(newsItem);
      setFormData({
        title: newsItem.title,
        content: newsItem.content,
        category: newsItem.category,
        image: newsItem.image || '📰',
        status: newsItem.status,
        featured: newsItem.featured || false,
        authorName: newsItem.authorName || ''
      });
    } else {
      setFormData({
        title: '',
        content: '',
        category: 'Event',
        image: '📰',
        status: 'DRAFT',
        featured: false,
        authorName: ''
      });
    }
    setShowModal(true);
  };

  const handleCloseModal = () => {
    setShowModal(false);
    setSelectedNews(null);
    setFormData({
      title: '',
      content: '',
      category: 'Event',
      image: '📰',
      status: 'DRAFT',
      featured: false,
      authorName: ''
    });
  };

  const handleInputChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      setError(null);
      if (modalMode === 'create') {
        await newsService.createNews(formData);
        setSuccess('News created successfully!');
      } else {
        await newsService.updateNews(selectedNews.id, formData);
        setSuccess('News updated successfully!');
      }
      handleCloseModal();
      fetchAllNews(currentPage - 1);
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      console.error('Error saving news:', err);
      setError(err.response?.data?.message || 'Failed to save news. Please try again.');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this news?')) {
      return;
    }
    try {
      setError(null);
      await newsService.deleteNews(id);
      setSuccess('News deleted successfully!');
      fetchAllNews(currentPage - 1);
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      console.error('Error deleting news:', err);
      setError('Failed to delete news. Please try again.');
    }
  };

  const handlePublish = async (id) => {
    try {
      setError(null);
      await newsService.publishNews(id);
      setSuccess('News published successfully!');
      fetchAllNews(currentPage - 1);
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      console.error('Error publishing news:', err);
      setError('Failed to publish news. Please try again.');
    }
  };

  const handleArchive = async (id) => {
    try {
      setError(null);
      await newsService.archiveNews(id);
      setSuccess('News archived successfully!');
      fetchAllNews(currentPage - 1);
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      console.error('Error archiving news:', err);
      setError('Failed to archive news. Please try again.');
    }
  };

  const getStatusBadge = (status) => {
    const variants = {
      PUBLISHED: 'success',
      DRAFT: 'warning',
      ARCHIVED: 'secondary'
    };
    return <Badge bg={variants[status] || 'secondary'}>{status}</Badge>;
  };

  const categories = [
    'Event',
    'Achievement',
    'Infrastructure',
    'Admission',
    'Extracurricular',
    'Meeting',
    'Library',
    'Other'
  ];

  const emojis = ['📰', '🏆', '💻', '🎖️', '🎉', '📚', '🎯', '🔬', '👨‍👩‍👧‍👦', '📖', '⚽', '🎭', '🎨'];

  return (
    <Container fluid className="admin-news-page">
      <div className="page-header mb-4">
        <Row className="align-items-center">
          <Col>
            <h2 className="mb-0">News Management</h2>
            <p className="text-muted mb-0">Create, edit, and manage school news</p>
          </Col>
          <Col xs="auto">
            <Button
              variant="primary"
              onClick={() => handleShowModal('create')}
              className="d-flex align-items-center"
            >
              <FiPlus className="me-2" />
              Create News
            </Button>
          </Col>
        </Row>
      </div>

      {success && (
        <Alert variant="success" dismissible onClose={() => setSuccess(null)}>
          {success}
        </Alert>
      )}

      {error && (
        <Alert variant="danger" dismissible onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      <Card>
        <Card.Body>
          {loading ? (
            <div className="text-center py-5">
              <Spinner animation="border" variant="primary" />
              <p className="mt-3">Loading news...</p>
            </div>
          ) : (
            <>
              <div className="table-responsive">
                <Table hover>
                  <thead>
                    <tr>
                      <th style={{ width: '50px' }}>Icon</th>
                      <th>Title</th>
                      <th>Category</th>
                      <th>Status</th>
                      <th>Featured</th>
                      <th>Views</th>
                      <th>Published Date</th>
                      <th style={{ width: '200px' }}>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {news.length === 0 ? (
                      <tr>
                        <td colSpan="8" className="text-center py-4">
                          No news found. Create your first news article!
                        </td>
                      </tr>
                    ) : (
                      news.map((item) => (
                        <tr key={item.id}>
                          <td className="text-center fs-4">{item.image}</td>
                          <td>
                            <strong>{item.title}</strong>
                            <br />
                            <small className="text-muted">
                              {item.content.substring(0, 60)}...
                            </small>
                          </td>
                          <td>
                            <Badge bg="info">{item.category}</Badge>
                          </td>
                          <td>{getStatusBadge(item.status)}</td>
                          <td>
                            {item.featured ? (
                              <Badge bg="warning">Featured</Badge>
                            ) : (
                              <span className="text-muted">-</span>
                            )}
                          </td>
                          <td>{item.viewCount || 0}</td>
                          <td>
                            <small>
                              {new Date(item.publishedDate).toLocaleDateString('en-US', {
                                year: 'numeric',
                                month: 'short',
                                day: 'numeric'
                              })}
                            </small>
                          </td>
                          <td>
                            <div className="d-flex gap-2">
                              <Button
                                variant="outline-primary"
                                size="sm"
                                onClick={() => handleShowModal('edit', item)}
                                title="Edit"
                              >
                                <FiEdit />
                              </Button>
                              {item.status === 'DRAFT' && (
                                <Button
                                  variant="outline-success"
                                  size="sm"
                                  onClick={() => handlePublish(item.id)}
                                  title="Publish"
                                >
                                  <FiCheck />
                                </Button>
                              )}
                              {item.status === 'PUBLISHED' && (
                                <Button
                                  variant="outline-warning"
                                  size="sm"
                                  onClick={() => handleArchive(item.id)}
                                  title="Archive"
                                >
                                  <FiArchive />
                                </Button>
                              )}
                              <Button
                                variant="outline-danger"
                                size="sm"
                                onClick={() => handleDelete(item.id)}
                                title="Delete"
                              >
                                <FiTrash2 />
                              </Button>
                            </div>
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </Table>
              </div>

              {totalPages > 1 && (
                <div className="d-flex justify-content-center mt-4">
                  <Pagination>
                    <Pagination.First
                      disabled={currentPage === 1}
                      onClick={() => handlePageChange(1)}
                    />
                    <Pagination.Prev
                      disabled={currentPage === 1}
                      onClick={() => handlePageChange(currentPage - 1)}
                    />
                    {[...Array(totalPages)].map((_, i) => (
                      <Pagination.Item
                        key={i + 1}
                        active={i + 1 === currentPage}
                        onClick={() => handlePageChange(i + 1)}
                      >
                        {i + 1}
                      </Pagination.Item>
                    ))}
                    <Pagination.Next
                      disabled={currentPage === totalPages}
                      onClick={() => handlePageChange(currentPage + 1)}
                    />
                    <Pagination.Last
                      disabled={currentPage === totalPages}
                      onClick={() => handlePageChange(totalPages)}
                    />
                  </Pagination>
                </div>
              )}
            </>
          )}
        </Card.Body>
      </Card>

      {/* Create/Edit Modal */}
      <Modal show={showModal} onHide={handleCloseModal} size="lg">
        <Modal.Header closeButton>
          <Modal.Title>
            {modalMode === 'create' ? 'Create New News' : 'Edit News'}
          </Modal.Title>
        </Modal.Header>
        <Form onSubmit={handleSubmit}>
          <Modal.Body>
            <Row>
              <Col md={12}>
                <Form.Group className="mb-3">
                  <Form.Label>Title *</Form.Label>
                  <Form.Control
                    type="text"
                    name="title"
                    value={formData.title}
                    onChange={handleInputChange}
                    required
                    placeholder="Enter news title"
                  />
                </Form.Group>
              </Col>

              <Col md={12}>
                <Form.Group className="mb-3">
                  <Form.Label>Content *</Form.Label>
                  <Form.Control
                    as="textarea"
                    rows={6}
                    name="content"
                    value={formData.content}
                    onChange={handleInputChange}
                    required
                    placeholder="Enter news content"
                  />
                </Form.Group>
              </Col>

              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>Category *</Form.Label>
                  <Form.Select
                    name="category"
                    value={formData.category}
                    onChange={handleInputChange}
                    required
                  >
                    {categories.map(cat => (
                      <option key={cat} value={cat}>{cat}</option>
                    ))}
                  </Form.Select>
                </Form.Group>
              </Col>

              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>Icon/Emoji</Form.Label>
                  <Form.Select
                    name="image"
                    value={formData.image}
                    onChange={handleInputChange}
                  >
                    {emojis.map(emoji => (
                      <option key={emoji} value={emoji}>{emoji}</option>
                    ))}
                  </Form.Select>
                </Form.Group>
              </Col>

              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>Status</Form.Label>
                  <Form.Select
                    name="status"
                    value={formData.status}
                    onChange={handleInputChange}
                  >
                    <option value="DRAFT">Draft</option>
                    <option value="PUBLISHED">Published</option>
                    <option value="ARCHIVED">Archived</option>
                  </Form.Select>
                </Form.Group>
              </Col>

              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>Author Name</Form.Label>
                  <Form.Control
                    type="text"
                    name="authorName"
                    value={formData.authorName}
                    onChange={handleInputChange}
                    placeholder="Leave blank for auto"
                  />
                </Form.Group>
              </Col>

              <Col md={12}>
                <Form.Group className="mb-3">
                  <Form.Check
                    type="checkbox"
                    name="featured"
                    checked={formData.featured}
                    onChange={handleInputChange}
                    label="Featured News (Show prominently on homepage)"
                  />
                </Form.Group>
              </Col>
            </Row>
          </Modal.Body>
          <Modal.Footer>
            <Button variant="secondary" onClick={handleCloseModal}>
              Cancel
            </Button>
            <Button variant="primary" type="submit">
              {modalMode === 'create' ? 'Create' : 'Update'} News
            </Button>
          </Modal.Footer>
        </Form>
      </Modal>
    </Container>
  );
}

export default AdminNewsPage;

