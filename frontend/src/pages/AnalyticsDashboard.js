import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import analyticsService from '../services/analyticsService';
import { Card } from '../components/ui/card';
import { Button } from '../components/ui/button';
import {
    Chart as ChartJS,
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    BarElement,
    ArcElement,
    Title,
    Tooltip,
    Legend
} from 'chart.js';
import { Line, Bar, Doughnut } from 'react-chartjs-2';
import './AnalyticsDashboard.css';

// Register ChartJS components
ChartJS.register(
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    BarElement,
    ArcElement,
    Title,
    Tooltip,
    Legend
);

const AnalyticsDashboard = () => {
    const { studentId } = useParams();
    const [performance, setPerformance] = useState(null);
    const [attendance, setAttendance] = useState(null);
    const [prediction, setPrediction] = useState(null);
    const [loading, setLoading] = useState(true);
    const [selectedMonths, setSelectedMonths] = useState(6);

    useEffect(() => {
        if (studentId) {
            loadAnalytics();
        }
    }, [studentId, selectedMonths]);

    const loadAnalytics = async () => {
        try {
            setLoading(true);
            const [perfResp, attResp, predResp] = await Promise.all([
                analyticsService.getStudentPerformance(studentId),
                analyticsService.getAttendanceAnalytics(studentId, selectedMonths),
                analyticsService.getPrediction(studentId).catch(() => ({ data: null }))
            ]);

            setPerformance(perfResp.data);
            setAttendance(attResp.data);
            setPrediction(predResp.data);
        } catch (err) {
            console.error('Error loading analytics:', err);
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <div className="d-flex justify-content-center align-items-center" style={{ minHeight: '400px' }}>
                <div className="spinner-border text-primary" role="status">
                    <span className="visually-hidden">Loading...</span>
                </div>
            </div>
        );
    }

    // Prepare chart data
    const subjectPerformanceData = performance?.subjectAverages ? {
        labels: Object.keys(performance.subjectAverages),
        datasets: [{
            label: 'Average Marks',
            data: Object.values(performance.subjectAverages),
            backgroundColor: 'rgba(54, 162, 235, 0.6)',
            borderColor: 'rgba(54, 162, 235, 1)',
            borderWidth: 2
        }]
    } : null;

    const attendanceTrendData = attendance?.trends ? {
        labels: attendance.trends.map(t => t.month),
        datasets: [{
            label: 'Attendance Rate (%)',
            data: attendance.trends.map(t => t.attendanceRate),
            borderColor: 'rgb(75, 192, 192)',
            backgroundColor: 'rgba(75, 192, 192, 0.2)',
            tension: 0.4,
            fill: true
        }]
    } : null;

    const attendanceDistributionData = attendance ? {
        labels: ['Present', 'Absent', 'Late'],
        datasets: [{
            data: [attendance.totalPresent, attendance.totalAbsent, attendance.totalLate],
            backgroundColor: [
                'rgba(75, 192, 192, 0.6)',
                'rgba(255, 99, 132, 0.6)',
                'rgba(255, 206, 86, 0.6)'
            ],
            borderColor: [
                'rgba(75, 192, 192, 1)',
                'rgba(255, 99, 132, 1)',
                'rgba(255, 206, 86, 1)'
            ],
            borderWidth: 2
        }]
    } : null;

    const chartOptions = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                position: 'top',
            }
        },
        scales: {
            y: {
                beginAtZero: true,
                max: 100
            }
        }
    };

    return (
        <div className="container-fluid analytics-dashboard mt-4">
            {/* Header */}
            <div className="row mb-4">
                <div className="col-12">
                    <h2 className="fw-bold">📊 Analytics Dashboard</h2>
                    {performance && (
                        <p className="text-muted">
                            Student: {performance.studentName} | Class: {performance.className}
                        </p>
                    )}
                </div>
            </div>

            {/* Performance Stats */}
            <div className="row mb-4">
                <div className="col-md-4 mb-3">
                    <Card className="stat-card h-100 bg-primary text-white">
                        <div className="card-body text-center">
                            <i className="bi bi-graph-up fs-1 mb-2"></i>
                            <h3 className="fw-bold">{performance?.averageGrade?.toFixed(2) || 'N/A'}</h3>
                            <p className="mb-0">Average Grade</p>
                        </div>
                    </Card>
                </div>
                <div className="col-md-4 mb-3">
                    <Card className="stat-card h-100 bg-success text-white">
                        <div className="card-body text-center">
                            <i className="bi bi-calendar-check fs-1 mb-2"></i>
                            <h3 className="fw-bold">{attendance?.overallAttendanceRate?.toFixed(2) || 'N/A'}%</h3>
                            <p className="mb-0">Attendance Rate</p>
                        </div>
                    </Card>
                </div>
                <div className="col-md-4 mb-3">
                    <Card className={`stat-card h-100 text-white ${
                        performance?.performanceTrend === 'IMPROVING' ? 'bg-success' :
                        performance?.performanceTrend === 'DECLINING' ? 'bg-danger' : 'bg-secondary'
                    }`}>
                        <div className="card-body text-center">
                            <i className={`bi ${
                                performance?.performanceTrend === 'IMPROVING' ? 'bi-arrow-up-circle' :
                                performance?.performanceTrend === 'DECLINING' ? 'bi-arrow-down-circle' :
                                'bi-dash-circle'
                            } fs-1 mb-2`}></i>
                            <h3 className="fw-bold">{performance?.performanceTrend || 'N/A'}</h3>
                            <p className="mb-0">Performance Trend</p>
                        </div>
                    </Card>
                </div>
            </div>

            {/* Charts Row 1 */}
            <div className="row mb-4">
                {/* Subject Performance */}
                <div className="col-lg-8 mb-4">
                    <Card>
                        <div className="card-header bg-primary text-white">
                            <h5 className="mb-0">📚 Subject Performance</h5>
                        </div>
                        <div className="card-body">
                            <div style={{ height: '300px' }}>
                                {subjectPerformanceData ? (
                                    <Bar data={subjectPerformanceData} options={chartOptions} />
                                ) : (
                                    <p className="text-center text-muted">No subject data available</p>
                                )}
                            </div>
                        </div>
                    </Card>
                </div>

                {/* Attendance Distribution */}
                <div className="col-lg-4 mb-4">
                    <Card>
                        <div className="card-header bg-success text-white">
                            <h5 className="mb-0">📊 Attendance Distribution</h5>
                        </div>
                        <div className="card-body">
                            <div style={{ height: '300px' }}>
                                {attendanceDistributionData ? (
                                    <Doughnut data={attendanceDistributionData} options={{
                                        responsive: true,
                                        maintainAspectRatio: false,
                                        plugins: {
                                            legend: {
                                                position: 'bottom'
                                            }
                                        }
                                    }} />
                                ) : (
                                    <p className="text-center text-muted">No attendance data available</p>
                                )}
                            </div>
                        </div>
                    </Card>
                </div>
            </div>

            {/* Charts Row 2 */}
            <div className="row mb-4">
                {/* Attendance Trend */}
                <div className="col-lg-12 mb-4">
                    <Card>
                        <div className="card-header bg-info text-white d-flex justify-content-between align-items-center">
                            <h5 className="mb-0">📈 Attendance Trend</h5>
                            <div className="btn-group">
                                <Button
                                    size="sm"
                                    variant={selectedMonths === 3 ? 'light' : 'outline-light'}
                                    onClick={() => setSelectedMonths(3)}
                                >
                                    3 Months
                                </Button>
                                <Button
                                    size="sm"
                                    variant={selectedMonths === 6 ? 'light' : 'outline-light'}
                                    onClick={() => setSelectedMonths(6)}
                                >
                                    6 Months
                                </Button>
                                <Button
                                    size="sm"
                                    variant={selectedMonths === 12 ? 'light' : 'outline-light'}
                                    onClick={() => setSelectedMonths(12)}
                                >
                                    12 Months
                                </Button>
                            </div>
                        </div>
                        <div className="card-body">
                            <div style={{ height: '300px' }}>
                                {attendanceTrendData ? (
                                    <Line data={attendanceTrendData} options={{
                                        ...chartOptions,
                                        scales: {
                                            y: {
                                                beginAtZero: true,
                                                max: 100
                                            }
                                        }
                                    }} />
                                ) : (
                                    <p className="text-center text-muted">No attendance trend data available</p>
                                )}
                            </div>
                        </div>
                    </Card>
                </div>
            </div>

            {/* Performance Prediction */}
            {prediction && (
                <div className="row mb-4">
                    <div className="col-12">
                        <Card>
                            <div className="card-header bg-warning text-white">
                                <h5 className="mb-0">🎯 Performance Prediction & Recommendations</h5>
                            </div>
                            <div className="card-body">
                                <div className="row">
                                    <div className="col-md-4">
                                        <div className="prediction-stat">
                                            <h6 className="text-muted">Current Average</h6>
                                            <h3 className="fw-bold text-primary">
                                                {prediction.currentAverage.toFixed(2)}
                                            </h3>
                                        </div>
                                    </div>
                                    <div className="col-md-4">
                                        <div className="prediction-stat">
                                            <h6 className="text-muted">Predicted Average</h6>
                                            <h3 className="fw-bold text-info">
                                                {prediction.predictedAverage.toFixed(2)}
                                            </h3>
                                        </div>
                                    </div>
                                    <div className="col-md-4">
                                        <div className="prediction-stat">
                                            <h6 className="text-muted">Risk Level</h6>
                                            <h3 className={`fw-bold ${
                                                prediction.riskLevel === 'LOW' ? 'text-success' :
                                                prediction.riskLevel === 'MEDIUM' ? 'text-warning' : 'text-danger'
                                            }`}>
                                                {prediction.riskLevel}
                                            </h3>
                                        </div>
                                    </div>
                                </div>
                                <hr />
                                <h6 className="fw-bold mb-3">📝 Recommendations:</h6>
                                <ul className="recommendations-list">
                                    {prediction.recommendations.map((rec, index) => (
                                        <li key={index} className="mb-2">
                                            <i className="bi bi-check-circle text-success me-2"></i>
                                            {rec}
                                        </li>
                                    ))}
                                </ul>
                            </div>
                        </Card>
                    </div>
                </div>
            )}

            {/* Recent Exams */}
            {performance?.recentExams && performance.recentExams.length > 0 && (
                <div className="row mb-4">
                    <div className="col-12">
                        <Card>
                            <div className="card-header bg-secondary text-white">
                                <h5 className="mb-0">📝 Recent Exam Results</h5>
                            </div>
                            <div className="card-body">
                                <div className="table-responsive">
                                    <table className="table table-hover">
                                        <thead>
                                            <tr>
                                                <th>Exam</th>
                                                <th>Subject</th>
                                                <th>Score</th>
                                                <th>Max Score</th>
                                                <th>Percentage</th>
                                                <th>Date</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {performance.recentExams.map((exam, index) => (
                                                <tr key={index}>
                                                    <td>{exam.examName}</td>
                                                    <td>{exam.subject}</td>
                                                    <td>{exam.score}</td>
                                                    <td>{exam.maxScore}</td>
                                                    <td>
                                                        <span className={`badge ${
                                                            exam.percentage >= 75 ? 'bg-success' :
                                                            exam.percentage >= 60 ? 'bg-warning' : 'bg-danger'
                                                        }`}>
                                                            {exam.percentage.toFixed(1)}%
                                                        </span>
                                                    </td>
                                                    <td>{exam.date}</td>
                                                </tr>
                                            ))}
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </Card>
                    </div>
                </div>
            )}
        </div>
    );
};

export default AnalyticsDashboard;

