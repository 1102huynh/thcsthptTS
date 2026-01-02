import axios from 'axios';
export default parentService;

};
        axios.get(`${API_BASE_URL}/v1/attendance/student/${studentId}`, { headers: getAuthHeader() }),
    getStudentAttendance: (studentId) => 
    
        axios.get(`${API_BASE_URL}/v1/grades/student/${studentId}`, { headers: getAuthHeader() }),
    getStudentGrades: (studentId) => 
    // Student Data (for parent view)
    
        axios.put(`${API_BASE_URL}/v1/meetings/${meetingId}/cancel`, {}, { headers: getAuthHeader() }),
    cancelMeeting: (meetingId) => 
    
        axios.put(`${API_BASE_URL}/v1/meetings/${meetingId}/confirm`, {}, { headers: getAuthHeader() }),
    confirmMeeting: (meetingId) => 
    
        axios.post(`${API_BASE_URL}/v1/meetings`, meeting, { headers: getAuthHeader() }),
    scheduleMeeting: (meeting) => 
    
        axios.get(`${API_BASE_URL}/v1/meetings/parent/${parentId}/upcoming`, { headers: getAuthHeader() }),
    getUpcomingMeetings: (parentId) => 
    
        axios.get(`${API_BASE_URL}/v1/meetings/parent/${parentId}`, { headers: getAuthHeader() }),
    getMeetings: (parentId) => 
    // Meetings
    
        axios.get(`${API_BASE_URL}/v1/announcements/${id}`, { headers: getAuthHeader() }),
    getAnnouncementById: (id) => 
    
        axios.get(`${API_BASE_URL}/v1/announcements/active/PARENTS`, { headers: getAuthHeader() }),
    getActiveAnnouncements: () => 
    // Announcements
    
        axios.delete(`${API_BASE_URL}/v1/messages/${messageId}`, { headers: getAuthHeader() }),
    deleteMessage: (messageId) => 
    
        axios.put(`${API_BASE_URL}/v1/messages/${messageId}/read`, {}, { headers: getAuthHeader() }),
    markMessageAsRead: (messageId) => 
    
        axios.post(`${API_BASE_URL}/v1/messages`, message, { headers: getAuthHeader() }),
    sendMessage: (message) => 
    
        axios.get(`${API_BASE_URL}/v1/messages/parent/${parentId}/unread`, { headers: getAuthHeader() }),
    getUnreadMessages: (parentId) => 
    
        axios.get(`${API_BASE_URL}/v1/messages/parent/${parentId}`, { headers: getAuthHeader() }),
    getMessages: (parentId) => 
    // Messages
    
        axios.get(`${API_BASE_URL}/v1/parents/dashboard/user/${userId}`, { headers: getAuthHeader() }),
    getDashboard: (userId) => 
    // Dashboard
    
        axios.delete(`${API_BASE_URL}/v1/parents/${parentId}/children/${studentId}`, { headers: getAuthHeader() }),
    removeChild: (parentId, studentId) => 
    
        axios.post(`${API_BASE_URL}/v1/parents/${parentId}/children/${studentId}`, {}, { headers: getAuthHeader() }),
    addChild: (parentId, studentId) => 
    
        axios.delete(`${API_BASE_URL}/v1/parents/${id}`, { headers: getAuthHeader() }),
    deleteParent: (id) => 
    
        axios.put(`${API_BASE_URL}/v1/parents/${id}`, parent, { headers: getAuthHeader() }),
    updateParent: (id, parent) => 
    
        axios.post(`${API_BASE_URL}/v1/parents`, parent, { headers: getAuthHeader() }),
    createParent: (parent) => 
    
        axios.get(`${API_BASE_URL}/v1/parents`, { headers: getAuthHeader() }),
    getAllParents: () => 
    
        axios.get(`${API_BASE_URL}/v1/parents/${id}`, { headers: getAuthHeader() }),
    getParentById: (id) => 
    
        axios.get(`${API_BASE_URL}/v1/parents/user/${userId}`, { headers: getAuthHeader() }),
    getParentByUserId: (userId) => 
    // Parent CRUD
const parentService = {

};
    return token ? { 'Authorization': `Bearer ${token}` } : {};
    const token = localStorage.getItem('token');
const getAuthHeader = () => {

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api';


