import React, { useState, useEffect } from 'react';
import { FiBarChart2, FiDownload, FiUsers, FiBook, FiAward, FiTrendingUp } from 'react-icons/fi';
import api from '../services/api';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card';
import { Button } from '../components/ui/button';

function ReportsPage() {
    const [loading, setLoading] = useState(true);
    const [reportData, setReportData] = useState({
        classes: [],
        subjects: [],
        staff: [],
        assignments: [],
        students: []
    });
    const [selectedReport, setSelectedReport] = useState('overview');
    const [error, setError] = useState('');

    useEffect(() => {
        fetchData();
    }, []);

    const fetchData = async () => {
        try {
            setLoading(true);
            const [classesRes, subjectsRes, staffRes, assignmentsRes, studentsRes] = await Promise.all([
                api.get('/api/classes'),
                api.get('/api/subjects'),
                api.get('/v1/staff'),
                api.get('/api/assignments'),
                api.get('/v1/students')
            ]);

            setReportData({
                classes: classesRes.data || [],
                subjects: subjectsRes.data || [],
                staff: staffRes.data || [],
                assignments: assignmentsRes.data || [],
                students: studentsRes.data || []
            });
        } catch (err) {
            setError('Failed to load report data');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    // Calculate teacher workload
    const getTeacherWorkload = () => {
        const teachers = reportData.staff.filter(s => s.position === 'TEACHER');
        return teachers.map(teacher => {
            const teacherAssignments = reportData.assignments.filter(a => a.teacher?.id === teacher.id);
            const totalPeriods = teacherAssignments.reduce((sum, a) => sum + (a.periodsPerWeek || 0), 0);
            const totalClasses = teacherAssignments.length;
            return {
                teacher,
                totalClasses,
                totalPeriods,
                workloadLevel: totalPeriods > 25 ? 'Heavy' : totalPeriods > 15 ? 'Normal' : 'Light'
            };
        }).sort((a, b) => b.totalPeriods - a.totalPeriods);
    };

    // Calculate class statistics
    const getClassStatistics = () => {
        return reportData.classes.map(cls => {
            const classAssignments = reportData.assignments.filter(a => a.schoolClass?.id === cls.id);
            const subjectCount = classAssignments.length;
            return {
                class: cls,
                studentCount: cls.currentStudentCount || 0,
                capacity: cls.maxCapacity || 0,
                subjectCount,
                utilization: cls.maxCapacity ? Math.round((cls.currentStudentCount / cls.maxCapacity) * 100) : 0
            };
        }).sort((a, b) => b.utilization - a.utilization);
    };

    // Calculate subject analytics
    const getSubjectAnalytics = () => {
        const subjectStats = {};
        reportData.assignments.forEach(assignment => {
            const subjectName = assignment.subject?.subjectName || 'Unknown';
            if (!subjectStats[subjectName]) {
                subjectStats[subjectName] = {
                    name: subjectName,
                    classCount: 0,
                    totalPeriods: 0,
                    teachers: new Set()
                };
            }
            subjectStats[subjectName].classCount++;
            subjectStats[subjectName].totalPeriods += assignment.periodsPerWeek || 0;
            if (assignment.teacher?.id) {
                subjectStats[subjectName].teachers.add(assignment.teacher.id);
            }
        });

        return Object.values(subjectStats).map(stat => ({
            ...stat,
            teacherCount: stat.teachers.size
        })).sort((a, b) => b.classCount - a.classCount);
    };

    const handleExport = (reportType) => {
        // Simple CSV export
        let csvContent = '';
        let filename = '';

        if (reportType === 'teacher') {
            const workload = getTeacherWorkload();
            csvContent = 'Teacher,Classes,Hours/Week,Workload\n';
            workload.forEach(w => {
                csvContent += `${w.teacher.user?.firstName} ${w.teacher.user?.lastName},${w.totalClasses},${w.totalPeriods},${w.workloadLevel}\n`;
            });
            filename = 'teacher_workload_report.csv';
        } else if (reportType === 'class') {
            const stats = getClassStatistics();
            csvContent = 'Class,Students,Capacity,Utilization %,Subjects\n';
            stats.forEach(s => {
                csvContent += `${s.class.className},${s.studentCount},${s.capacity},${s.utilization}%,${s.subjectCount}\n`;
            });
            filename = 'class_statistics_report.csv';
        } else if (reportType === 'subject') {
            const analytics = getSubjectAnalytics();
            csvContent = 'Subject,Classes,Total Hours,Teachers\n';
            analytics.forEach(a => {
                csvContent += `${a.name},${a.classCount},${a.totalPeriods},${a.teacherCount}\n`;
            });
            filename = 'subject_analytics_report.csv';
        }

        // Download CSV
        const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
        const link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = filename;
        link.click();
    };

    const renderOverview = () => {
        const teacherWorkload = getTeacherWorkload();
        const classStats = getClassStatistics();
        const subjectAnalytics = getSubjectAnalytics();

        return (
            <div className="space-y-6">
                {/* Summary Cards */}
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                    <Card className="border-l-4 border-l-blue-500">
                        <CardHeader className="pb-2">
                            <CardDescription>Total Classes</CardDescription>
                            <CardTitle className="text-3xl">{reportData.classes.length}</CardTitle>
                        </CardHeader>
                    </Card>
                    <Card className="border-l-4 border-l-green-500">
                        <CardHeader className="pb-2">
                            <CardDescription>Total Teachers</CardDescription>
                            <CardTitle className="text-3xl">{teacherWorkload.length}</CardTitle>
                        </CardHeader>
                    </Card>
                    <Card className="border-l-4 border-l-purple-500">
                        <CardHeader className="pb-2">
                            <CardDescription>Total Subjects</CardDescription>
                            <CardTitle className="text-3xl">{reportData.subjects.length}</CardTitle>
                        </CardHeader>
                    </Card>
                    <Card className="border-l-4 border-l-orange-500">
                        <CardHeader className="pb-2">
                            <CardDescription>Total Assignments</CardDescription>
                            <CardTitle className="text-3xl">{reportData.assignments.length}</CardTitle>
                        </CardHeader>
                    </Card>
                </div>

                {/* Top 5 Insights */}
                <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                    {/* Busiest Teachers */}
                    <Card>
                        <CardHeader>
                            <CardTitle className="text-lg">Top 5 Busiest Teachers</CardTitle>
                        </CardHeader>
                        <CardContent>
                            <div className="space-y-3">
                                {teacherWorkload.slice(0, 5).map((w, idx) => (
                                    <div key={idx} className="flex justify-between items-center">
                                        <div>
                                            <p className="font-medium">{w.teacher.user?.firstName} {w.teacher.user?.lastName}</p>
                                            <p className="text-sm text-gray-500">{w.totalClasses} classes</p>
                                        </div>
                                        <div className="text-right">
                                            <p className="font-bold">{w.totalPeriods}h/week</p>
                                            <p className={`text-xs ${w.workloadLevel === 'Heavy' ? 'text-red-600' : w.workloadLevel === 'Normal' ? 'text-yellow-600' : 'text-green-600'}`}>
                                                {w.workloadLevel}
                                            </p>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </CardContent>
                    </Card>

                    {/* Fullest Classes */}
                    <Card>
                        <CardHeader>
                            <CardTitle className="text-lg">Most Utilized Classes</CardTitle>
                        </CardHeader>
                        <CardContent>
                            <div className="space-y-3">
                                {classStats.slice(0, 5).map((s, idx) => (
                                    <div key={idx} className="flex justify-between items-center">
                                        <div>
                                            <p className="font-medium">{s.class.className}</p>
                                            <p className="text-sm text-gray-500">{s.studentCount}/{s.capacity} students</p>
                                        </div>
                                        <div className="text-right">
                                            <p className="font-bold">{s.utilization}%</p>
                                            <div className="w-20 h-2 bg-gray-200 rounded-full mt-1">
                                                <div
                                                    className={`h-2 rounded-full ${s.utilization > 90 ? 'bg-red-500' : s.utilization > 70 ? 'bg-yellow-500' : 'bg-green-500'}`}
                                                    style={{ width: `${Math.min(s.utilization, 100)}%` }}
                                                ></div>
                                            </div>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </CardContent>
                    </Card>

                    {/* Popular Subjects */}
                    <Card>
                        <CardHeader>
                            <CardTitle className="text-lg">Most Taught Subjects</CardTitle>
                        </CardHeader>
                        <CardContent>
                            <div className="space-y-3">
                                {subjectAnalytics.slice(0, 5).map((a, idx) => (
                                    <div key={idx} className="flex justify-between items-center">
                                        <div>
                                            <p className="font-medium">{a.name}</p>
                                            <p className="text-sm text-gray-500">{a.teacherCount} teachers</p>
                                        </div>
                                        <div className="text-right">
                                            <p className="font-bold">{a.classCount} classes</p>
                                            <p className="text-xs text-gray-500">{a.totalPeriods}h/week</p>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </CardContent>
                    </Card>
                </div>
            </div>
        );
    };

    const renderTeacherReport = () => {
        const workload = getTeacherWorkload();

        return (
            <Card>
                <CardHeader className="flex flex-row items-center justify-between">
                    <div>
                        <CardTitle>Teacher Workload Report</CardTitle>
                        <CardDescription>{workload.length} teachers</CardDescription>
                    </div>
                    <Button onClick={() => handleExport('teacher')} className="bg-green-600">
                        <FiDownload className="w-4 h-4 mr-2" />
                        Export CSV
                    </Button>
                </CardHeader>
                <CardContent>
                    <div className="overflow-x-auto">
                        <table className="w-full">
                            <thead className="bg-gray-50">
                                <tr>
                                    <th className="text-left p-3 font-semibold">#</th>
                                    <th className="text-left p-3 font-semibold">Teacher</th>
                                    <th className="text-left p-3 font-semibold">Department</th>
                                    <th className="text-center p-3 font-semibold">Classes</th>
                                    <th className="text-center p-3 font-semibold">Hours/Week</th>
                                    <th className="text-center p-3 font-semibold">Workload</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y">
                                {workload.map((w, idx) => (
                                    <tr key={idx} className="hover:bg-gray-50">
                                        <td className="p-3">{idx + 1}</td>
                                        <td className="p-3">
                                            <div className="font-medium">{w.teacher.user?.firstName} {w.teacher.user?.lastName}</div>
                                            <div className="text-sm text-gray-500">{w.teacher.employeeId}</div>
                                        </td>
                                        <td className="p-3">{w.teacher.department}</td>
                                        <td className="p-3 text-center">{w.totalClasses}</td>
                                        <td className="p-3 text-center font-bold">{w.totalPeriods}</td>
                                        <td className="p-3 text-center">
                                            <span className={`px-3 py-1 rounded-full text-xs font-semibold ${w.workloadLevel === 'Heavy' ? 'bg-red-100 text-red-700' :
                                                    w.workloadLevel === 'Normal' ? 'bg-yellow-100 text-yellow-700' :
                                                        'bg-green-100 text-green-700'
                                                }`}>
                                                {w.workloadLevel}
                                            </span>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </CardContent>
            </Card>
        );
    };

    const renderClassReport = () => {
        const stats = getClassStatistics();

        return (
            <Card>
                <CardHeader className="flex flex-row items-center justify-between">
                    <div>
                        <CardTitle>Class Statistics Report</CardTitle>
                        <CardDescription>{stats.length} classes</CardDescription>
                    </div>
                    <Button onClick={() => handleExport('class')} className="bg-green-600">
                        <FiDownload className="w-4 h-4 mr-2" />
                        Export CSV
                    </Button>
                </CardHeader>
                <CardContent>
                    <div className="overflow-x-auto">
                        <table className="w-full">
                            <thead className="bg-gray-50">
                                <tr>
                                    <th className="text-left p-3 font-semibold">Class</th>
                                    <th className="text-left p-3 font-semibold">Grade Level</th>
                                    <th className="text-center p-3 font-semibold">Students</th>
                                    <th className="text-center p-3 font-semibold">Capacity</th>
                                    <th className="text-center p-3 font-semibold">Utilization</th>
                                    <th className="text-center p-3 font-semibold">Subjects</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y">
                                {stats.map((s, idx) => (
                                    <tr key={idx} className="hover:bg-gray-50">
                                        <td className="p-3 font-medium">{s.class.className}</td>
                                        <td className="p-3">{s.class.gradeLevel?.levelName}</td>
                                        <td className="p-3 text-center">{s.studentCount}</td>
                                        <td className="p-3 text-center">{s.capacity}</td>
                                        <td className="p-3">
                                            <div className="flex items-center justify-center gap-2">
                                                <span className="font-bold">{s.utilization}%</span>
                                                <div className="w-24 h-2 bg-gray-200 rounded-full">
                                                    <div
                                                        className={`h-2 rounded-full ${s.utilization > 90 ? 'bg-red-500' : s.utilization > 70 ? 'bg-yellow-500' : 'bg-green-500'}`}
                                                        style={{ width: `${Math.min(s.utilization, 100)}%` }}
                                                    ></div>
                                                </div>
                                            </div>
                                        </td>
                                        <td className="p-3 text-center">{s.subjectCount}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </CardContent>
            </Card>
        );
    };

    const renderSubjectReport = () => {
        const analytics = getSubjectAnalytics();

        return (
            <Card>
                <CardHeader className="flex flex-row items-center justify-between">
                    <div>
                        <CardTitle>Subject Analytics Report</CardTitle>
                        <CardDescription>{analytics.length} subjects</CardDescription>
                    </div>
                    <Button onClick={() => handleExport('subject')} className="bg-green-600">
                        <FiDownload className="w-4 h-4 mr-2" />
                        Export CSV
                    </Button>
                </CardHeader>
                <CardContent>
                    <div className="overflow-x-auto">
                        <table className="w-full">
                            <thead className="bg-gray-50">
                                <tr>
                                    <th className="text-left p-3 font-semibold">Subject</th>
                                    <th className="text-center p-3 font-semibold">Classes</th>
                                    <th className="text-center p-3 font-semibold">Total Hours/Week</th>
                                    <th className="text-center p-3 font-semibold">Teachers</th>
                                    <th className="text-center p-3 font-semibold">Avg Hours/Class</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y">
                                {analytics.map((a, idx) => (
                                    <tr key={idx} className="hover:bg-gray-50">
                                        <td className="p-3 font-medium">{a.name}</td>
                                        <td className="p-3 text-center">{a.classCount}</td>
                                        <td className="p-3 text-center font-bold">{a.totalPeriods}</td>
                                        <td className="p-3 text-center">{a.teacherCount}</td>
                                        <td className="p-3 text-center">{(a.totalPeriods / a.classCount).toFixed(1)}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </CardContent>
            </Card>
        );
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-slate-50 via-emerald-50 to-teal-50 p-6">
            <div className="max-w-7xl mx-auto">
                {/* Header */}
                <div className="mb-8">
                    <h1 className="text-3xl font-bold text-gray-900 mb-2 flex items-center gap-2">
                        <FiBarChart2 className="text-emerald-600" />
                        Reports & Analytics
                    </h1>
                    <p className="text-gray-600">Comprehensive insights and statistics</p>
                </div>

                {/* Report Tabs */}
                <div className="mb-6 flex flex-wrap gap-3">
                    <Button
                        variant={selectedReport === 'overview' ? 'default' : 'outline'}
                        onClick={() => setSelectedReport('overview')}
                        className={selectedReport === 'overview' ? 'bg-gradient-to-r from-emerald-600 to-teal-600' : ''}
                    >
                        <FiTrendingUp className="w-4 h-4 mr-2" />
                        Overview
                    </Button>
                    <Button
                        variant={selectedReport === 'teacher' ? 'default' : 'outline'}
                        onClick={() => setSelectedReport('teacher')}
                        className={selectedReport === 'teacher' ? 'bg-gradient-to-r from-emerald-600 to-teal-600' : ''}
                    >
                        <FiUsers className="w-4 h-4 mr-2" />
                        Teacher Workload
                    </Button>
                    <Button
                        variant={selectedReport === 'class' ? 'default' : 'outline'}
                        onClick={() => setSelectedReport('class')}
                        className={selectedReport === 'class' ? 'bg-gradient-to-r from-emerald-600 to-teal-600' : ''}
                    >
                        <FiBook className="w-4 h-4 mr-2" />
                        Class Statistics
                    </Button>
                    <Button
                        variant={selectedReport === 'subject' ? 'default' : 'outline'}
                        onClick={() => setSelectedReport('subject')}
                        className={selectedReport === 'subject' ? 'bg-gradient-to-r from-emerald-600 to-teal-600' : ''}
                    >
                        <FiAward className="w-4 h-4 mr-2" />
                        Subject Analytics
                    </Button>
                </div>

                {/* Error Alert */}
                {error && (
                    <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg">
                        <p className="text-sm text-red-800">{error}</p>
                    </div>
                )}

                {/* Loading */}
                {loading ? (
                    <div className="flex justify-center items-center py-12">
                        <div className="w-12 h-12 border-4 border-emerald-600 border-t-transparent rounded-full animate-spin"></div>
                    </div>
                ) : (
                    <div>
                        {selectedReport === 'overview' && renderOverview()}
                        {selectedReport === 'teacher' && renderTeacherReport()}
                        {selectedReport === 'class' && renderClassReport()}
                        {selectedReport === 'subject' && renderSubjectReport()}
                    </div>
                )}
            </div>
        </div>
    );
}

export default ReportsPage;
