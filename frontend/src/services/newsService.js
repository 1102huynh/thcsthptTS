import api from './api';

/**
 * News Service - Handles all news-related API calls
 */
const newsService = {
  /**
   * Get all published news (public)
   * @param {number} page - Page number (default: 0)
   * @param {number} size - Page size (default: 10)
   */
  getPublishedNews: async (page = 0, size = 10) => {
    try {
      const response = await api.get(`/api/news?page=${page}&size=${size}`);
      return response.data;
    } catch (error) {
      console.error('Error fetching published news:', error);
      throw error;
    }
  },

  /**
   * Get news by category (public)
   * @param {string} category - News category
   * @param {number} page - Page number
   * @param {number} size - Page size
   */
  getNewsByCategory: async (category, page = 0, size = 10) => {
    try {
      const response = await api.get(`/api/news/category/${category}?page=${page}&size=${size}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching news by category ${category}:`, error);
      throw error;
    }
  },

  /**
   * Get featured news (public)
   * @param {number} page - Page number
   * @param {number} size - Page size
   */
  getFeaturedNews: async (page = 0, size = 5) => {
    try {
      const response = await api.get(`/api/news/featured?page=${page}&size=${size}`);
      return response.data;
    } catch (error) {
      console.error('Error fetching featured news:', error);
      throw error;
    }
  },

  /**
   * Get recent news (public) - Returns top 5
   */
  getRecentNews: async () => {
    try {
      const response = await api.get('/api/news/recent');
      return response.data;
    } catch (error) {
      console.error('Error fetching recent news:', error);
      throw error;
    }
  },

  /**
   * Search news (public)
   * @param {string} keyword - Search keyword
   * @param {number} page - Page number
   * @param {number} size - Page size
   */
  searchNews: async (keyword, page = 0, size = 10) => {
    try {
      const response = await api.get(`/api/news/search?keyword=${encodeURIComponent(keyword)}&page=${page}&size=${size}`);
      return response.data;
    } catch (error) {
      console.error('Error searching news:', error);
      throw error;
    }
  },

  /**
   * Get news by ID (public)
   * @param {number} id - News ID
   */
  getNewsById: async (id) => {
    try {
      const response = await api.get(`/api/news/${id}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching news ${id}:`, error);
      throw error;
    }
  },

  /**
   * Get all news including drafts (admin only)
   * @param {number} page - Page number
   * @param {number} size - Page size
   */
  getAllNewsForAdmin: async (page = 0, size = 10) => {
    try {
      const response = await api.get(`/api/news/admin/all?page=${page}&size=${size}`);
      return response.data;
    } catch (error) {
      console.error('Error fetching all news for admin:', error);
      throw error;
    }
  },

  /**
   * Create new news (admin only)
   * @param {object} newsData - News data
   */
  createNews: async (newsData) => {
    try {
      const response = await api.post('/api/news', newsData);
      return response.data;
    } catch (error) {
      console.error('Error creating news:', error);
      throw error;
    }
  },

  /**
   * Update news (admin only)
   * @param {number} id - News ID
   * @param {object} newsData - Updated news data
   */
  updateNews: async (id, newsData) => {
    try {
      const response = await api.put(`/api/news/${id}`, newsData);
      return response.data;
    } catch (error) {
      console.error(`Error updating news ${id}:`, error);
      throw error;
    }
  },

  /**
   * Delete news (admin only)
   * @param {number} id - News ID
   */
  deleteNews: async (id) => {
    try {
      const response = await api.delete(`/api/news/${id}`);
      return response.data;
    } catch (error) {
      console.error(`Error deleting news ${id}:`, error);
      throw error;
    }
  },

  /**
   * Publish news (admin only)
   * @param {number} id - News ID
   */
  publishNews: async (id) => {
    try {
      const response = await api.put(`/api/news/${id}/publish`);
      return response.data;
    } catch (error) {
      console.error(`Error publishing news ${id}:`, error);
      throw error;
    }
  },

  /**
   * Archive news (admin only)
   * @param {number} id - News ID
   */
  archiveNews: async (id) => {
    try {
      const response = await api.put(`/api/news/${id}/archive`);
      return response.data;
    } catch (error) {
      console.error(`Error archiving news ${id}:`, error);
      throw error;
    }
  },
};

export default newsService;

