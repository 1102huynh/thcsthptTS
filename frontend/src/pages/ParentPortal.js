import React, { useState, useEffect } from 'react';
export default ParentPortal;

};
    );
        </div>
            </div>
                </div>
                    </Card>
                        </div>
                            )}
                                <p className="text-muted text-center">No upcoming meetings.</p>
                            ) : (
                                </div>
                                    ))}
                                        </div>
                                            <span className="badge bg-success mt-2">{meeting.status}</span>
                                            </p>
                                                📅 {new Date(meeting.meetingDate).toLocaleString()}
                                            <p className="text-muted small mb-0">
                                            <p className="text-muted small mb-1">Teacher: {meeting.teacherName}</p>
                                            <p className="fw-bold mb-1">{meeting.purpose}</p>
                                        <div key={meeting.id} className="meeting-item p-3 mb-2 border rounded bg-light">
                                    {upcomingMeetings.slice(0, 3).map((meeting) => (
                                <div className="meetings-list">
                            {upcomingMeetings && upcomingMeetings.length > 0 ? (
                            <h6 className="fw-bold mt-3 mb-3">📅 Upcoming Meetings</h6>

                            <hr />

                            )}
                                <p className="text-muted text-center mb-3">No unread messages.</p>
                            ) : (
                                </div>
                                    ))}
                                        </div>
                                            </p>
                                                {new Date(message.createdAt).toLocaleDateString()}
                                            <p className="text-muted small mb-0">
                                            <p className="text-muted small mb-1">From: {message.teacherName}</p>
                                            <h6 className="fw-bold mb-1">{message.subject}</h6>
                                        <div key={message.id} className="message-item p-3 mb-2 border rounded">
                                    {unreadMessages.slice(0, 3).map((message) => (
                                <div className="messages-list">
                            {unreadMessages && unreadMessages.length > 0 ? (
                        <div className="card-body">
                        </div>
                            </Button>
                                View All
                            >
                                onClick={() => navigate('/parent/messages')}
                                variant="light"
                                size="sm"
                            <Button
                            <h5 className="mb-0">💬 Unread Messages</h5>
                        <div className="card-header bg-warning text-white d-flex justify-content-between align-items-center">
                    <Card className="h-100">
                <div className="col-lg-6 mb-4">
                {/* Unread Messages & Upcoming Meetings */}

                </div>
                    </Card>
                        </div>
                            )}
                                <p className="text-muted text-center">No announcements available.</p>
                            ) : (
                                </div>
                                    ))}
                                        </div>
                                            </div>
                                                </div>
                                                    </span>
                                                        {announcement.priority}
                                                    <span className="badge bg-info">
                                                    </p>
                                                        {announcement.content.substring(0, 100)}...
                                                    <p className="text-muted small mb-2">
                                                    <h6 className="fw-bold mb-1">{announcement.title}</h6>
                                                <div>
                                            <div className="d-flex justify-content-between align-items-start">
                                        <div key={announcement.id} className="announcement-item p-3 mb-2 border rounded">
                                    {recentAnnouncements.slice(0, 5).map((announcement) => (
                                <div className="announcements-list">
                            {recentAnnouncements && recentAnnouncements.length > 0 ? (
                        <div className="card-body">
                        </div>
                            </Button>
                                View All
                            >
                                onClick={() => navigate('/parent/announcements')}
                                variant="light"
                                size="sm"
                            <Button
                            <h5 className="mb-0">📢 Recent Announcements</h5>
                        <div className="card-header bg-info text-white d-flex justify-content-between align-items-center">
                    <Card className="h-100">
                <div className="col-lg-6 mb-4">
                {/* Recent Announcements */}
            <div className="row">

            </div>
                </div>
                    </Card>
                        </div>
                            )}
                                <p className="text-muted text-center">No children linked to your account.</p>
                            ) : (
                                </div>
                                    ))}
                                        </div>
                                            </Card>
                                                </div>
                                                    </div>
                                                        </Button>
                                                            📅 Attendance
                                                        >
                                                            onClick={() => navigate(`/parent/child/${child.id}/attendance`)}
                                                            variant="outline-success"
                                                            size="sm"
                                                        <Button
                                                        </Button>
                                                            📊 Grades
                                                        >
                                                            onClick={() => navigate(`/parent/child/${child.id}/grades`)}
                                                            variant="outline-primary"
                                                            size="sm"
                                                        <Button
                                                    <div className="d-flex gap-2 mt-3">
                                                    <p className="text-muted small mb-2">Class: {child.className || child.gradeLevel}</p>
                                                    <p className="text-muted small mb-2">Roll: {child.rollNumber}</p>
                                                    <h6 className="fw-bold">{child.firstName} {child.lastName}</h6>
                                                <div className="card-body">
                                            <Card className="child-card h-100">
                                        <div key={child.id} className="col-md-6 col-lg-4 mb-3">
                                    {parent.children.map((child) => (
                                <div className="row">
                            {parent?.children && parent.children.length > 0 ? (
                        <div className="card-body">
                        </div>
                            <h5 className="mb-0">👦 My Children</h5>
                        <div className="card-header bg-primary text-white">
                    <Card>
                <div className="col-12">
            <div className="row mb-4">
            {/* Children Cards */}

            </div>
                </div>
                    </Card>
                        </div>
                            <p className="text-muted mb-0">Announcements</p>
                            <h3 className="fw-bold">{stats?.activeAnnouncements || 0}</h3>
                            </div>
                                <i className="bi bi-megaphone-fill fs-2"></i>
                            <div className="stat-icon bg-info text-white mb-2">
                        <div className="card-body text-center">
                    <Card className="stat-card h-100">
                <div className="col-md-3 col-sm-6 mb-3">
                </div>
                    </Card>
                        </div>
                            <p className="text-muted mb-0">Upcoming Meetings</p>
                            <h3 className="fw-bold">{stats?.upcomingMeetings || 0}</h3>
                            </div>
                                <i className="bi bi-calendar-event fs-2"></i>
                            <div className="stat-icon bg-success text-white mb-2">
                        <div className="card-body text-center">
                    <Card className="stat-card h-100">
                <div className="col-md-3 col-sm-6 mb-3">
                </div>
                    </Card>
                        </div>
                            <p className="text-muted mb-0">Unread Messages</p>
                            <h3 className="fw-bold">{stats?.unreadMessages || 0}</h3>
                            </div>
                                <i className="bi bi-envelope-fill fs-2"></i>
                            <div className="stat-icon bg-warning text-white mb-2">
                        <div className="card-body text-center">
                    <Card className="stat-card h-100">
                <div className="col-md-3 col-sm-6 mb-3">
                </div>
                    </Card>
                        </div>
                            <p className="text-muted mb-0">Children</p>
                            <h3 className="fw-bold">{stats?.totalChildren || 0}</h3>
                            </div>
                                <i className="bi bi-people-fill fs-2"></i>
                            <div className="stat-icon bg-primary text-white mb-2">
                        <div className="card-body text-center">
                    <Card className="stat-card h-100">
                <div className="col-md-3 col-sm-6 mb-3">
            <div className="row mb-4">
            {/* Stats Cards */}

            </div>
                </div>
                    <p className="text-muted">Welcome back, {parent?.firstName} {parent?.lastName}!</p>
                    <h2 className="fw-bold">👨‍👩‍👧‍👦 Parent Portal</h2>
                <div className="col-12">
            <div className="row mb-4">
            {/* Header */}
        <div className="container-fluid parent-portal mt-4">
    return (

    const { parent, stats, recentAnnouncements, upcomingMeetings, unreadMessages } = dashboard || {};

    }
        );
            </div>
                </div>
                    {error}
                <div className="alert alert-danger" role="alert">
            <div className="container mt-4">
        return (
    if (error) {

    }
        );
            </div>
                </div>
                    <span className="visually-hidden">Loading...</span>
                <div className="spinner-border text-primary" role="status">
            <div className="d-flex justify-content-center align-items-center" style={{ minHeight: '400px' }}>
        return (
    if (loading) {

    };
        }
            setLoading(false);
        } finally {
            setError('Failed to load dashboard. Please try again.');
            console.error('Error loading parent dashboard:', err);
        } catch (err) {
            setError(null);
            setDashboard(response.data);
            const response = await parentService.getDashboard(userId);
            const userId = localStorage.getItem('userId');
            setLoading(true);
        try {
    const loadDashboard = async () => {

    }, []);
        loadDashboard();
    useEffect(() => {

    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(true);
    const [dashboard, setDashboard] = useState(null);
    const navigate = useNavigate();
const ParentPortal = () => {

import './ParentPortal.css';
import { Button } from '../components/ui/button';
import { Card } from '../components/ui/card';
import parentService from '../services/parentService';
import { useNavigate } from 'react-router-dom';

