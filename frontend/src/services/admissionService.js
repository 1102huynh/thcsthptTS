import api from './api';

/**
 * Admission Service - Handles all admission-related API calls
 */
const admissionService = {
  /**
   * Get all open admissions (public)
   */
  getOpenAdmissions: async (page = 0, size = 10) => {
    try {
      const response = await api.get(`/api/admissions?page=${page}&size=${size}`);
      return response.data;
    } catch (error) {
      console.error('Error fetching open admissions:', error);
      throw error;
    }
  },

  /**
   * Get admission by ID (public)
   */
  getAdmissionById: async (id) => {
    try {
      const response = await api.get(`/api/admissions/${id}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching admission ${id}:`, error);
      throw error;
    }
  },

  /**
   * Get all admissions including all statuses (admin only)
   */
  getAllAdmissionsForAdmin: async (page = 0, size = 10) => {
    try {
      const response = await api.get(`/api/admissions/admin/all?page=${page}&size=${size}`);
      return response.data;
    } catch (error) {
      console.error('Error fetching all admissions for admin:', error);
      throw error;
    }
  },

  /**
   * Create new admission (admin only)
   */
  createAdmission: async (admissionData) => {
    try {
      const response = await api.post('/api/admissions', admissionData);
      return response.data;
    } catch (error) {
      console.error('Error creating admission:', error);
      throw error;
    }
  },

  /**
   * Update admission (admin only)
   */
  updateAdmission: async (id, admissionData) => {
    try {
      const response = await api.put(`/api/admissions/${id}`, admissionData);
      return response.data;
    } catch (error) {
      console.error(`Error updating admission ${id}:`, error);
      throw error;
    }
  },

  /**
   * Delete admission (admin only)
   */
  deleteAdmission: async (id) => {
    try {
      const response = await api.delete(`/api/admissions/${id}`);
      return response.data;
    } catch (error) {
      console.error(`Error deleting admission ${id}:`, error);
      throw error;
    }
  },

  /**
   * Update admission status (admin only)
   */
  updateStatus: async (id, status) => {
    try {
      const response = await api.put(`/api/admissions/${id}/status?status=${status}`);
      return response.data;
    } catch (error) {
      console.error(`Error updating admission ${id} status:`, error);
      throw error;
    }
  },
};

export default admissionService;

