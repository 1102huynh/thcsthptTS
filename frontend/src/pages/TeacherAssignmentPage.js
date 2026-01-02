import React, { useState, useEffect } from 'react';
import { FiPlus, FiEdit, FiTrash2, FiUsers, FiCalendar, FiAward } from 'react-icons/fi';
import api from '../services/api';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';

function TeacherAssignmentPage() {
    const [assignments, setAssignments] = useState([]);
    const [classes, setClasses] = useState([]);
    const [subjects, setSubjects] = useState([]);
    const [staff, setStaff] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [showModal, setShowModal] = useState(false);
    const [selectedAssignment, setSelectedAssignment] = useState(null);
    const [filterSemester, setFilterSemester] = useState(1);
    const [formData, setFormData] = useState({
        classId: '',
        subjectId: '',
        teacherId: '',
        academicYear: '2024-2025',
        semester: 1,
        periodsPerWeek: 0
    });

    useEffect(() => {
        fetchData();
    }, []);

    const fetchData = async () => {
        try {
            setLoading(true);
            const [assignmentsRes, classesRes, subjectsRes, staffRes] = await Promise.all([
                api.get('/api/assignments'),
                api.get('/api/classes'),
                api.get('/api/subjects'),
                api.get('/v1/staff')  // Use /v1/staff to match backend
            ]);

            setAssignments(assignmentsRes.data || []);
            setClasses(classesRes.data || []);
            setSubjects(subjectsRes.data || []);
            setStaff(staffRes.data || []);
        } catch (err) {
            setError('Failed to load data: ' + err.message);
        } finally {
            setLoading(false);
        }
    };

    const handleAddNew = () => {
        setSelectedAssignment(null);
        setFormData({
            classId: '',
            subjectId: '',
            teacherId: '',
            academicYear: '2024-2025',
            semester: filterSemester,
            periodsPerWeek: 0
        });
        setShowModal(true);
    };

    const handleEdit = (assignment) => {
        setSelectedAssignment(assignment);
        setFormData({
            classId: assignment.schoolClass?.id || '',
            subjectId: assignment.subject?.id || '',
            teacherId: assignment.teacher?.id || '',
            academicYear: assignment.academicYear || '2024-2025',
            semester: assignment.semester || 1,
            periodsPerWeek: assignment.periodsPerWeek || 0
        });
        setShowModal(true);
    };

    const handleSave = async () => {
        try {
            if (!formData.classId || !formData.subjectId || !formData.teacherId) {
                setError('Please fill in all required fields');
                return;
            }

            const payload = {
                schoolClass: { id: parseInt(formData.classId) },
                subject: { id: parseInt(formData.subjectId) },
                teacher: { id: parseInt(formData.teacherId) },
                academicYear: formData.academicYear,
                semester: parseInt(formData.semester),
                periodsPerWeek: parseInt(formData.periodsPerWeek),
                status: 'ACTIVE'
            };

            if (selectedAssignment) {
                await api.put(`/api/assignments/${selectedAssignment.id}`, payload);
            } else {
                await api.post('/api/assignments', payload);
            }

            setShowModal(false);
            setError('');
            fetchData();
        } catch (err) {
            setError('Failed to save assignment: ' + (err.response?.data?.message || err.message));
        }
    };

    const handleDelete = async (id) => {
        if (window.confirm('Are you sure you want to delete this assignment?')) {
            try {
                await api.delete(`/api/assignments/${id}`);
                fetchData();
            } catch (err) {
                setError('Failed to delete assignment: ' + (err.response?.data?.message || err.message));
            }
        }
    };

    // Filter assignments by semester
    const filteredAssignments = assignments.filter(a => a.semester === filterSemester);

    // Group by class
    const groupedByClass = {};
    filteredAssignments.forEach(assignment => {
        const className = assignment.schoolClass?.className || 'Unknown';
        if (!groupedByClass[className]) {
            groupedByClass[className] = [];
        }
        groupedByClass[className].push(assignment);
    });

    // Calculate teacher workload
    const teacherWorkload = {};
    filteredAssignments.forEach(assignment => {
        const teacherId = assignment.teacher?.id;
        if (teacherId) {
            if (!teacherWorkload[teacherId]) {
                teacherWorkload[teacherId] = {
                    name: `${assignment.teacher.user?.firstName || ''} ${assignment.teacher.user?.lastName || ''}`.trim(),
                    totalPeriods: 0,
                    classCount: 0
                };
            }
            teacherWorkload[teacherId].totalPeriods += assignment.periodsPerWeek || 0;
            teacherWorkload[teacherId].classCount += 1;
        }
    });

    return (
        <div className="min-h-screen bg-gradient-to-br from-slate-50 via-green-50 to-emerald-50 p-6">
            <div className="max-w-7xl mx-auto">
                {/* Header */}
                <div className="mb-8">
                    <div className="flex items-center justify-between">
                        <div>
                            <h1 className="text-3xl font-bold text-gray-900 mb-2 flex items-center gap-2">
                                <FiUsers className="text-green-600" />
                                Teacher Assignment
                            </h1>
                            <p className="text-gray-600">Manage teacher-subject-class assignments</p>
                        </div>
                        <Button onClick={handleAddNew} className="bg-gradient-to-r from-green-600 to-emerald-600">
                            <FiPlus className="w-4 h-4 mr-2" />
                            New Assignment
                        </Button>
                    </div>
                </div>

                {/* Semester Filter */}
                <div className="mb-6 flex gap-2">
                    {[1, 2].map(sem => (
                        <Button
                            key={sem}
                            onClick={() => setFilterSemester(sem)}
                            variant={filterSemester === sem ? 'default' : 'outline'}
                            className={filterSemester === sem ? 'bg-gradient-to-r from-green-600 to-emerald-600' : ''}
                        >
                            <FiCalendar className="w-4 h-4 mr-2" />
                            Semester {sem} (HK{sem})
                        </Button>
                    ))}
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
                        <div className="w-12 h-12 border-4 border-green-600 border-t-transparent rounded-full animate-spin"></div>
                    </div>
                ) : (
                    <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                        {/* Left: Teacher Workload Summary */}
                        <div className="lg:col-span-1">
                            <Card className="border-0 shadow-xl sticky top-6">
                                <CardHeader className="bg-gradient-to-r from-green-50 to-emerald-50">
                                    <CardTitle className="flex items-center gap-2">
                                        <FiAward className="text-green-600" />
                                        Teacher Workload
                                    </CardTitle>
                                    <CardDescription>Semester {filterSemester} (HK{filterSemester})</CardDescription>
                                </CardHeader>
                                <CardContent className="pt-6">
                                    <div className="space-y-3 max-h-[600px] overflow-y-auto">
                                        {Object.entries(teacherWorkload).map(([id, data]) => (
                                            <div key={id} className="p-3 bg-gray-50 rounded-lg">
                                                <div className="font-semibold text-gray-900">{data.name}</div>
                                                <div className="text-sm text-gray-600 mt-1">
                                                    {data.totalPeriods} periods/week
                                                </div>
                                                <div className="text-xs text-gray-500">
                                                    {data.classCount} {data.classCount === 1 ? 'class' : 'classes'}
                                                </div>
                                                {/* Workload status */}
                                                <div className="mt-2">
                                                    <div className="w-full bg-gray-200 rounded-full h-2">
                                                        <div
                                                            className={`h-2 rounded-full ${data.totalPeriods > 20 ? 'bg-red-500' :
                                                                data.totalPeriods > 15 ? 'bg-yellow-500' :
                                                                    'bg-green-500'
                                                                }`}
                                                            style={{ width: `${Math.min((data.totalPeriods / 25) * 100, 100)}%` }}
                                                        ></div>
                                                    </div>
                                                    <p className="text-xs text-gray-500 mt-1">
                                                        {data.totalPeriods > 20 ? 'Heavy' : data.totalPeriods > 15 ? 'Normal' : 'Light'}
                                                    </p>
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                </CardContent>
                            </Card>
                        </div>

                        {/* Right: Assignments by Class */}
                        <div className="lg:col-span-2">
                            {Object.keys(groupedByClass).length > 0 ? (
                                Object.entries(groupedByClass).sort().map(([className, classAssignments]) => (
                                    <Card key={className} className="mb-6 border-0 shadow-xl">
                                        <CardHeader className="bg-gradient-to-r from-green-50 to-emerald-50">
                                            <CardTitle>Class {className}</CardTitle>
                                            <CardDescription>
                                                {classAssignments.length} subject{classAssignments.length !== 1 ? 's' : ''} assigned
                                            </CardDescription>
                                        </CardHeader>
                                        <CardContent className="pt-6">
                                            <div className="space-y-3">
                                                {classAssignments.map((assignment) => (
                                                    <div key={assignment.id} className="flex items-center justify-between p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors">
                                                        <div className="flex-1">
                                                            <div className="font-semibold text-gray-900">
                                                                {assignment.subject?.subjectName || 'Unknown Subject'}
                                                            </div>
                                                            <div className="text-sm text-gray-600 mt-1">
                                                                Teacher: {assignment.teacher?.user?.firstName || ''} {assignment.teacher?.user?.lastName || 'Not assigned'}
                                                            </div>
                                                            <div className="text-xs text-gray-500 mt-1">
                                                                {assignment.periodsPerWeek || 0} periods/week
                                                            </div>
                                                        </div>
                                                        <div className="flex gap-2 ml-4">
                                                            <Button size="sm" variant="outline" onClick={() => handleEdit(assignment)}>
                                                                <FiEdit className="w-4 h-4" />
                                                            </Button>
                                                            <Button size="sm" variant="outline" onClick={() => handleDelete(assignment.id)} className="hover:bg-red-50">
                                                                <FiTrash2 className="w-4 h-4 text-red-600" />
                                                            </Button>
                                                        </div>
                                                    </div>
                                                ))}
                                            </div>
                                        </CardContent>
                                    </Card>
                                ))
                            ) : (
                                <Card className="border-0 shadow-xl">
                                    <CardContent className="py-12 text-center text-gray-500">
                                        No assignments for Semester {filterSemester}. Click "New Assignment" to add one.
                                    </CardContent>
                                </Card>
                            )}
                        </div>
                    </div>
                )}

                {/* Modal */}
                {showModal && (
                    <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
                        <Card className="w-full max-w-2xl">
                            <CardHeader>
                                <CardTitle>{selectedAssignment ? 'Edit Assignment' : 'New Assignment'}</CardTitle>
                                <CardDescription>Assign teacher to teach subject in class</CardDescription>
                            </CardHeader>
                            <CardContent className="space-y-4">
                                <div className="grid grid-cols-2 gap-4">
                                    <div className="space-y-2">
                                        <Label htmlFor="classId">Class *</Label>
                                        <select
                                            id="classId"
                                            value={formData.classId}
                                            onChange={(e) => setFormData({ ...formData, classId: e.target.value })}
                                            className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                                        >
                                            <option value="">Select Class</option>
                                            {classes.map(c => (
                                                <option key={c.id} value={c.id}>{c.className}</option>
                                            ))}
                                        </select>
                                    </div>
                                    <div className="space-y-2">
                                        <Label htmlFor="subjectId">Subject *</Label>
                                        <select
                                            id="subjectId"
                                            value={formData.subjectId}
                                            onChange={(e) => setFormData({ ...formData, subjectId: e.target.value })}
                                            className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                                        >
                                            <option value="">Select Subject</option>
                                            {subjects.map(s => (
                                                <option key={s.id} value={s.id}>{s.subjectName}</option>
                                            ))}
                                        </select>
                                    </div>
                                </div>

                                <div className="space-y-2">
                                    <Label htmlFor="teacherId">Teacher *</Label>
                                    <select
                                        id="teacherId"
                                        value={formData.teacherId}
                                        onChange={(e) => setFormData({ ...formData, teacherId: e.target.value })}
                                        className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                                    >
                                        <option value="">Select Teacher</option>
                                        {staff.map(s => (
                                            <option key={s.id} value={s.id}>
                                                {s.user?.firstName || ''} {s.user?.lastName || ''} - {s.position}
                                            </option>
                                        ))}
                                    </select>
                                </div>

                                <div className="grid grid-cols-3 gap-4">
                                    <div className="space-y-2">
                                        <Label htmlFor="semester">Semester *</Label>
                                        <select
                                            id="semester"
                                            value={formData.semester}
                                            onChange={(e) => setFormData({ ...formData, semester: e.target.value })}
                                            className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                                        >
                                            <option value="1">HK1 (Semester 1)</option>
                                            <option value="2">HK2 (Semester 2)</option>
                                        </select>
                                    </div>
                                    <div className="space-y-2">
                                        <Label htmlFor="periodsPerWeek">Periods/Week *</Label>
                                        <Input
                                            id="periodsPerWeek"
                                            type="number"
                                            value={formData.periodsPerWeek}
                                            onChange={(e) => setFormData({ ...formData, periodsPerWeek: e.target.value })}
                                        />
                                    </div>
                                    <div className="space-y-2">
                                        <Label htmlFor="academicYear">Academic Year</Label>
                                        <Input
                                            id="academicYear"
                                            value={formData.academicYear}
                                            onChange={(e) => setFormData({ ...formData, academicYear: e.target.value })}
                                            disabled
                                        />
                                    </div>
                                </div>

                                <div className="flex justify-end gap-3 pt-4">
                                    <Button variant="outline" onClick={() => setShowModal(false)}>Cancel</Button>
                                    <Button onClick={handleSave} className="bg-gradient-to-r from-green-600 to-emerald-600">
                                        <FiPlus className="w-4 h-4 mr-2" />
                                        {selectedAssignment ? 'Update' : 'Save'}
                                    </Button>
                                </div>
                            </CardContent>
                        </Card>
                    </div>
                )}
            </div>
        </div>
    );
}

export default TeacherAssignmentPage;
