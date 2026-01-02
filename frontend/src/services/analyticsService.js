import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api';

const getAuthHeader = () => {
    const token = localStorage.getItem('token');
    return token ? { 'Authorization': `Bearer ${token}` } : {};
};

const analyticsService = {
    // Student Performance
    getStudentPerformance: (studentId) =>
        axios.get(`${API_BASE_URL}/v1/analytics/student/${studentId}/performance`, { headers: getAuthHeader() }),

    // Class Analytics
    getClassAnalytics: (classId) =>
        axios.get(`${API_BASE_URL}/v1/analytics/class/${classId}/analytics`, { headers: getAuthHeader() }),

    // Attendance Analytics
    getAttendanceAnalytics: (studentId, months = 6) =>
        axios.get(`${API_BASE_URL}/v1/analytics/student/${studentId}/attendance?months=${months}`, { headers: getAuthHeader() }),

    // Grade Distribution
    getGradeDistribution: (classId) =>
        axios.get(`${API_BASE_URL}/v1/analytics/class/${classId}/grade-distribution`, { headers: getAuthHeader() }),

    // Performance Prediction
    getPrediction: (studentId) =>
        axios.get(`${API_BASE_URL}/v1/analytics/student/${studentId}/prediction`, { headers: getAuthHeader() }),
};

export default analyticsService;

