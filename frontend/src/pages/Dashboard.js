import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
    Users,
    BookOpen,
    ClipboardCheck,
    TrendingUp,
    TrendingDown,
    Award,
    DollarSign,
    Calendar,
    Clock,
    CheckCircle,
    Activity,
    BarChart3,
    ArrowRight,
    Percent,
    UserCheck
} from 'lucide-react';
import { staffService, studentService, libraryService } from '../services/dataService';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card';
import { Button } from '../components/ui/button';

function Dashboard({ user }) {
    const [stats, setStats] = useState({
        staffCount: 0,
        studentCount: 0,
        bookCount: 0,
        attendanceRate: 85,
        totalRevenue: 0,
        loading: true,
        error: null,
    });

    const [recentActivity] = useState([
        { id: 1, type: 'student', message: 'New student registered', time: '2 hours ago', icon: Users, color: 'text-blue-600' },
        { id: 2, type: 'book', message: 'Book borrowed from library', time: '4 hours ago', icon: BookOpen, color: 'text-purple-600' },
        { id: 3, type: 'attendance', message: 'Attendance marked for class 10A', time: '6 hours ago', icon: CheckCircle, color: 'text-green-600' },
        { id: 4, type: 'fee', message: 'Fee payment received', time: '1 day ago', icon: DollarSign, color: 'text-amber-600' },
    ]);

    useEffect(() => {
        fetchStats();
    }, []);

    const fetchStats = async () => {
        try {
            setStats(prev => ({ ...prev, loading: true }));

            const staffRes = await staffService.getAll();
            const studentRes = await studentService.getAll();
            const bookRes = await libraryService.getBooks();

            setStats({
                staffCount: staffRes.data.length || 0,
                studentCount: studentRes.data.length || 0,
                bookCount: bookRes.data.length || 0,
                attendanceRate: 85,
                totalRevenue: 125000,
                loading: false,
                error: null,
            });
        } catch (err) {
            setStats(prev => ({
                ...prev,
                loading: false,
                error: 'Failed to load statistics',
            }));
        }
    };

    const statCards = [
        {
            title: 'Total Staff',
            value: stats.staffCount,
            icon: Users,
            trend: '+12%',
            trendUp: true,
            color: 'from-blue-500 to-blue-600',
            bgColor: 'bg-blue-50',
            iconColor: 'text-blue-600',
        },
        {
            title: 'Total Students',
            value: stats.studentCount,
            icon: UserCheck,
            trend: '+5%',
            trendUp: true,
            color: 'from-green-500 to-emerald-600',
            bgColor: 'bg-green-50',
            iconColor: 'text-green-600',
        },
        {
            title: 'Library Books',
            value: stats.bookCount,
            icon: BookOpen,
            trend: '-2%',
            trendUp: false,
            color: 'from-purple-500 to-purple-600',
            bgColor: 'bg-purple-50',
            iconColor: 'text-purple-600',
        },
        {
            title: 'Attendance Rate',
            value: `${stats.attendanceRate}%`,
            icon: Percent,
            trend: '+3%',
            trendUp: true,
            color: 'from-amber-500 to-orange-600',
            bgColor: 'bg-amber-50',
            iconColor: 'text-amber-600',
        },
    ];

    const quickActions = [
        { icon: Users, label: 'Manage Staff', to: '/staff', color: 'from-blue-600 to-blue-700' },
        { icon: UserCheck, label: 'Manage Students', to: '/students', color: 'from-green-600 to-green-700' },
        { icon: ClipboardCheck, label: 'Attendance', to: '/attendance', color: 'from-purple-600 to-purple-700' },
        { icon: Award, label: 'Manage Grades', to: '/grades', color: 'from-amber-600 to-amber-700' },
        { icon: BookOpen, label: 'Library', to: '/library', color: 'from-pink-600 to-pink-700' },
        { icon: DollarSign, label: 'Manage Fees', to: '/fees', color: 'from-rose-600 to-rose-700' },
        // Vietnamese Education System (NEW)
        { icon: BookOpen, label: 'Manage Classes', to: '/classes', color: 'from-indigo-600 to-indigo-700' },
        { icon: Award, label: 'Manage Subjects', to: '/subjects', color: 'from-violet-600 to-violet-700' },
        { icon: Users, label: 'Teacher Assignments', to: '/assignments', color: 'from-emerald-600 to-emerald-700' },
    ];

    const metrics = [
        { label: 'Student Enrollment', value: stats.studentCount, percentage: 75, trend: '+15%', color: 'bg-green-500' },
        { label: 'Attendance', value: '85%', percentage: 85, trend: '+3%', color: 'bg-blue-500' },
        { label: 'Fee Collection', value: '60%', percentage: 60, trend: '-5%', color: 'bg-amber-500' },
    ];

    return (
        <div className="min-h-screen bg-gradient-to-br from-slate-50 via-gray-50 to-slate-100 p-6">
            {/* Header */}
            <div className="mb-8">
                <div className="flex flex-col md:flex-row md:items-center md:justify-between mb-6">
                    <div>
                        <h1 className="text-3xl font-bold text-gray-900 mb-2">
                            Welcome back, <span className="bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">{user?.firstName}!</span>
                        </h1>
                        <p className="text-gray-600">Here's what's happening in your school today</p>
                    </div>
                    <div className="mt-4 md:mt-0 flex items-center gap-3">
                        <div className="px-4 py-2 bg-gradient-to-r from-blue-600 to-purple-600 text-white rounded-lg shadow-lg">
                            <span className="text-sm font-semibold">{user?.role}</span>
                        </div>
                        <span className="text-sm text-gray-600">{user?.email}</span>
                    </div>
                </div>
            </div>

            {stats.error && (
                <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg flex items-start gap-3">
                    <Activity className="w-5 h-5 text-red-600 mt-0.5 flex-shrink-0" />
                    <p className="text-sm text-red-800">{stats.error}</p>
                </div>
            )}

            {stats.loading ? (
                <div className="flex justify-center items-center py-20">
                    <div className="w-12 h-12 border-4 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
                </div>
            ) : (
                <>
                    {/* Stats Grid */}
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
                        {statCards.map((stat, index) => {
                            const Icon = stat.icon;
                            const TrendIcon = stat.trendUp ? TrendingUp : TrendingDown;
                            return (
                                <Card key={index} className="border-0 shadow-lg hover:shadow-2xl transition-all duration-300 hover:-translate-y-1 bg-white/80 backdrop-blur-sm">
                                    <CardContent className="p-6">
                                        <div className="flex items-center justify-between mb-4">
                                            <div className={`w-14 h-14 ${stat.bgColor} rounded-xl flex items-center justify-center`}>
                                                <Icon className={`w-7 h-7 ${stat.iconColor}`} />
                                            </div>
                                            <div className={`flex items-center gap-1 px-2 py-1 rounded-full text-xs font-semibold ${stat.trendUp ? 'bg-green-100 text-green-600' : 'bg-red-100 text-red-600'
                                                }`}>
                                                <TrendIcon className="w-3 h-3" />
                                                {stat.trend}
                                            </div>
                                        </div>
                                        <div>
                                            <p className="text-sm text-gray-600 mb-1">{stat.title}</p>
                                            <p className="text-3xl font-bold text-gray-900">{stat.value}</p>
                                            <p className="text-xs text-gray-500 mt-1">vs last month</p>
                                        </div>
                                    </CardContent>
                                </Card>
                            );
                        })}
                    </div>

                    {/* Main Content Grid */}
                    <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
                        {/* Quick Actions */}
                        <div className="lg:col-span-2">
                            <Card className="border-0 shadow-lg bg-white/80 backdrop-blur-sm">
                                <CardHeader>
                                    <CardTitle className="text-xl">Quick Actions</CardTitle>
                                    <CardDescription>Frequently used features</CardDescription>
                                </CardHeader>
                                <CardContent>
                                    <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
                                        {quickActions.map((action, index) => {
                                            const Icon = action.icon;
                                            return (
                                                <Link key={index} to={action.to}>
                                                    <Button
                                                        variant="outline"
                                                        className={`w-full h-24 flex flex-col items-center justify-center gap-2 border-2 hover:border-transparent bg-gradient-to-br ${action.color} text-white hover:shadow-xl transition-all duration-300 hover:scale-105`}
                                                    >
                                                        <Icon className="w-6 h-6" />
                                                        <span className="font-semibold text-sm">{action.label}</span>
                                                    </Button>
                                                </Link>
                                            );
                                        })}
                                    </div>
                                </CardContent>
                            </Card>
                        </div>

                        {/* System Overview */}
                        <Card className="border-0 shadow-lg bg-gradient-to-br from-blue-600 to-purple-600 text-white">
                            <CardHeader>
                                <CardTitle className="text-white">System Overview</CardTitle>
                            </CardHeader>
                            <CardContent className="space-y-4">
                                <div className="flex items-center justify-between py-3 border-b border-white/20">
                                    <div className="flex items-center gap-2">
                                        <Calendar className="w-5 h-5" />
                                        <span className="text-sm">Academic Year</span>
                                    </div>
                                    <span className="font-semibold">2024-2025</span>
                                </div>
                                <div className="flex items-center justify-between py-3 border-b border-white/20">
                                    <div className="flex items-center gap-2">
                                        <CheckCircle className="w-5 h-5" />
                                        <span className="text-sm">System Status</span>
                                    </div>
                                    <span className="px-3 py-1 bg-green-500 rounded-full text-xs font-semibold">Operational</span>
                                </div>
                                <div className="flex items-center justify-between py-3 border-b border-white/20">
                                    <div className="flex items-center gap-2">
                                        <Clock className="w-5 h-5" />
                                        <span className="text-sm">Last Backup</span>
                                    </div>
                                    <span className="font-semibold text-sm">Today 3:00 PM</span>
                                </div>
                                <div className="flex items-center justify-between py-3 border-b border-white/20">
                                    <div className="flex items-center gap-2">
                                        <Users className="w-5 h-5" />
                                        <span className="text-sm">Active Users</span>
                                    </div>
                                    <span className="px-3 py-1 bg-blue-500 rounded-full text-sm font-semibold">24</span>
                                </div>
                                <div className="flex items-center justify-between py-3">
                                    <div className="flex items-center gap-2">
                                        <DollarSign className="w-5 h-5" />
                                        <span className="text-sm">Total Revenue</span>
                                    </div>
                                    <span className="font-bold">₹{stats.totalRevenue.toLocaleString()}</span>
                                </div>
                            </CardContent>
                        </Card>
                    </div>

                    {/* Bottom Row */}
                    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                        {/* Recent Activity */}
                        <Card className="border-0 shadow-lg bg-white/80 backdrop-blur-sm">
                            <CardHeader>
                                <CardTitle className="text-xl">Recent Activity</CardTitle>
                                <CardDescription>Latest updates from your system</CardDescription>
                            </CardHeader>
                            <CardContent className="p-0">
                                <div className="divide-y divide-gray-100">
                                    {recentActivity.map((activity) => {
                                        const Icon = activity.icon;
                                        return (
                                            <div key={activity.id} className="flex items-start gap-4 p-6 hover:bg-gray-50 transition-colors">
                                                <div className={`w-10 h-10 rounded-lg ${activity.color.replace('text-', 'bg-').replace('600', '100')} flex items-center justify-center flex-shrink-0`}>
                                                    <Icon className={`w-5 h-5 ${activity.color}`} />
                                                </div>
                                                <div className="flex-1 min-w-0">
                                                    <p className="text-sm font-medium text-gray-900">{activity.message}</p>
                                                    <p className="text-xs text-gray-500 mt-1">{activity.time}</p>
                                                </div>
                                                <ArrowRight className="w-5 h-5 text-gray-400 flex-shrink-0" />
                                            </div>
                                        );
                                    })}
                                </div>
                            </CardContent>
                        </Card>

                        {/* Performance Metrics */}
                        <Card className="border-0 shadow-lg bg-white/80 backdrop-blur-sm">
                            <CardHeader>
                                <CardTitle className="text-xl">Performance Metrics</CardTitle>
                                <CardDescription>This month overview</CardDescription>
                            </CardHeader>
                            <CardContent className="space-y-6">
                                {metrics.map((metric, index) => (
                                    <div key={index}>
                                        <div className="flex items-center justify-between mb-2">
                                            <span className="text-sm font-medium text-gray-700">{metric.label}</span>
                                            <div className="flex items-center gap-2">
                                                <span className="text-xs font-semibold text-green-600">{metric.trend}</span>
                                                <span className="text-sm font-semibold text-gray-900">{metric.value}</span>
                                            </div>
                                        </div>
                                        <div className="w-full bg-gray-200 rounded-full h-2.5 overflow-hidden">
                                            <div
                                                className={`h-full ${metric.color} rounded-full transition-all duration-500`}
                                                style={{ width: `${metric.percentage}%` }}
                                            ></div>
                                        </div>
                                        <p className="text-xs text-gray-500 mt-1">
                                            {metric.label === 'Student Enrollment' && `${stats.studentCount} students`}
                                            {metric.label === 'Attendance' && '85% average'}
                                            {metric.label === 'Fee Collection' && '60% collected'}
                                        </p>
                                    </div>
                                ))}
                            </CardContent>
                        </Card>
                    </div>
                </>
            )}
        </div>
    );
}

export default Dashboard;
