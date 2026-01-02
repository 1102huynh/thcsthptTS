import React, { useState, useEffect } from 'react';
import { FiCalendar, FiPlus, FiEdit, FiTrash2, FiClock, FiUser, FiBook, FiCheckCircle } from 'react-icons/fi';
import api from '../services/api';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';

function ExamManagement() {
    const [exams, setExams] = useState([]);
    const [subjects, setSubjects] = useState([]);
    const [gradeLevels, setGradeLevels] = useState([]);
    const [academicYears, setAcademicYears] = useState([]);
    const [staff, setStaff] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [showModal, setShowModal] = useState(false);
    const [selectedExam, setSelectedExam] = useState(null);
    const [filterStatus, setFilterStatus] = useState('ALL');
    const [formData, setFormData] = useState({
        examName: '',
        subjectId: '',
        gradeLevelId: '',
        examDate: '',
        startTime: '08:00',
        endTime: '10:00',
        durationMinutes: 120,
        totalMarks: 100,
        passingMarks: 40,
        roomNumber: '',
        invigilatorId: '',
        examType: 'MIDTERM',
        semester: 'SEMESTER_1',
        academicYearId: '',
        status: 'SCHEDULED',
        instructions: ''
    });

    useEffect(() => {
        fetchData();
    }, []);

    const fetchData = async () => {
        try {
            setLoading(true);
            const [examsRes, subjectsRes, gradeLevelsRes, yearsRes, staffRes] = await Promise.all([
                api.get('/api/exams'),
                api.get('/api/subjects'),
                api.get('/api/grade-levels'),
                api.get('/api/academic-years'),
                api.get('/v1/staff')
            ]);
            setExams(examsRes.data || []);
            setSubjects(subjectsRes.data || []);
            setGradeLevels(gradeLevelsRes.data || []);
            setAcademicYears(yearsRes.data || []);
            setStaff(staffRes.data?.filter(s => s.position === 'TEACHER') || []);
        } catch (err) {
            setError('Failed to load data');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const handleAddNew = () => {
        setSelectedExam(null);
        const currentYear = academicYears.find(y => y.isCurrent);
        setFormData({
            examName: '',
            subjectId: '',
            gradeLevelId: '',
            examDate: '',
            startTime: '08:00',
            endTime: '10:00',
            durationMinutes: 120,
            totalMarks: 100,
            passingMarks: 40,
            roomNumber: '',
            invigilatorId: '',
            examType: 'MIDTERM',
            semester: 'SEMESTER_1',
            academicYearId: currentYear?.id || '',
            status: 'SCHEDULED',
            instructions: ''
        });
        setShowModal(true);
    };

    const handleEdit = (exam) => {
        setSelectedExam(exam);
        setFormData({
            examName: exam.examName,
            subjectId: exam.subject?.id || '',
            gradeLevelId: exam.gradeLevel?.id || '',
            examDate: exam.examDate,
            startTime: exam.startTime,
            endTime: exam.endTime,
            durationMinutes: exam.durationMinutes,
            totalMarks: exam.totalMarks,
            passingMarks: exam.passingMarks,
            roomNumber: exam.roomNumber || '',
            invigilatorId: exam.invigilator?.id || '',
            examType: exam.examType,
            semester: exam.semester,
            academicYearId: exam.academicYear?.id || '',
            status: exam.status,
            instructions: exam.instructions || ''
        });
        setShowModal(true);
    };

    const handleSave = async () => {
        try {
            const payload = {
                examName: formData.examName,
                subject: formData.subjectId ? { id: formData.subjectId } : null,
                gradeLevel: formData.gradeLevelId ? { id: formData.gradeLevelId } : null,
                examDate: formData.examDate,
                startTime: formData.startTime,
                endTime: formData.endTime,
                durationMinutes: formData.durationMinutes,
                totalMarks: formData.totalMarks,
                passingMarks: formData.passingMarks,
                roomNumber: formData.roomNumber,
                invigilator: formData.invigilatorId ? { id: formData.invigilatorId } : null,
                examType: formData.examType,
                semester: formData.semester,
                academicYear: formData.academicYearId ? { id: formData.academicYearId } : null,
                status: formData.status,
                instructions: formData.instructions
            };

            if (selectedExam) {
                await api.put(`/api/exams/${selectedExam.id}`, payload);
            } else {
                await api.post('/api/exams', payload);
            }

            setShowModal(false);
            fetchData();
            setError('');
        } catch (err) {
            setError('Failed to save: ' + (err.response?.data?.message || err.message));
        }
    };

    const handleDelete = async (id) => {
        if (window.confirm('Are you sure you want to delete this exam?')) {
            try {
                await api.delete(`/api/exams/${id}`);
                fetchData();
            } catch (err) {
                setError('Failed to delete exam');
            }
        }
    };

    const formatDate = (dateString) => {
        if (!dateString) return '';
        return new Date(dateString).toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        });
    };

    const getStatusColor = (status) => {
        switch (status) {
            case 'SCHEDULED': return 'bg-blue-100 text-blue-700';
            case 'ONGOING': return 'bg-yellow-100 text-yellow-700';
            case 'COMPLETED': return 'bg-green-100 text-green-700';
            case 'CANCELLED': return 'bg-red-100 text-red-700';
            default: return 'bg-gray-100 text-gray-700';
        }
    };

    const filteredExams = filterStatus === 'ALL'
        ? exams
        : exams.filter(e => e.status === filterStatus);

    return (
        <div className="min-h-screen bg-gradient-to-br from-slate-50 via-orange-50 to-red-50 p-6">
            <div className="max-w-7xl mx-auto">
                {/* Header */}
                <div className="mb-8 flex items-center justify-between">
                    <div>
                        <h1 className="text-3xl font-bold text-gray-900 mb-2 flex items-center gap-2">
                            <FiCalendar className="text-orange-600" />
                            Exam Management
                        </h1>
                        <p className="text-gray-600">Schedule and manage exams</p>
                    </div>
                    <Button
                        onClick={handleAddNew}
                        className="bg-gradient-to-r from-orange-600 to-red-600"
                    >
                        <FiPlus className="w-4 h-4 mr-2" />
                        Schedule Exam
                    </Button>
                </div>

                {/* Filters */}
                <Card className="mb-6 border-0 shadow-xl">
                    <CardHeader>
                        <CardTitle>Filter Exams</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <div className="flex gap-3">
                            {['ALL', 'SCHEDULED', 'ONGOING', 'COMPLETED', 'CANCELLED'].map(status => (
                                <Button
                                    key={status}
                                    variant={filterStatus === status ? 'default' : 'outline'}
                                    onClick={() => setFilterStatus(status)}
                                    className={filterStatus === status ? 'bg-gradient-to-r from-orange-600 to-red-600' : ''}
                                >
                                    {status}
                                </Button>
                            ))}
                        </div>
                    </CardContent>
                </Card>

                {/* Error Alert */}
                {error && (
                    <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg">
                        <p className="text-sm text-red-800">{error}</p>
                    </div>
                )}

                {/* Loading */}
                {loading ? (
                    <div className="flex justify-center items-center py-12">
                        <div className="w-12 h-12 border-4 border-orange-600 border-t-transparent rounded-full animate-spin"></div>
                    </div>
                ) : (
                    <div className="grid grid-cols-1 gap-6">
                        {filteredExams.length > 0 ? (
                            filteredExams.map((exam) => (
                                <Card key={exam.id} className="border-0 shadow-xl hover:shadow-2xl transition-shadow">
                                    <CardContent className="pt-6">
                                        <div className="flex items-start justify-between">
                                            <div className="flex-1">
                                                <div className="flex items-center gap-3 mb-3">
                                                    <h3 className="text-xl font-bold text-gray-900">{exam.examName}</h3>
                                                    <span className={`px-3 py-1 rounded-full text-xs font-semibold ${getStatusColor(exam.status)}`}>
                                                        {exam.status}
                                                    </span>
                                                    <span className="px-3 py-1 rounded-full text-xs font-semibold bg-purple-100 text-purple-700">
                                                        {exam.examType}
                                                    </span>
                                                </div>

                                                <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-4">
                                                    <div className="flex items-center gap-2 text-gray-600">
                                                        <FiBook className="text-blue-500" />
                                                        <div>
                                                            <p className="text-xs text-gray-500">Subject</p>
                                                            <p className="text-sm font-medium">{exam.subject?.subjectName}</p>
                                                        </div>
                                                    </div>
                                                    <div className="flex items-center gap-2 text-gray-600">
                                                        <FiCalendar className="text-green-500" />
                                                        <div>
                                                            <p className="text-xs text-gray-500">Date</p>
                                                            <p className="text-sm font-medium">{formatDate(exam.examDate)}</p>
                                                        </div>
                                                    </div>
                                                    <div className="flex items-center gap-2 text-gray-600">
                                                        <FiClock className="text-orange-500" />
                                                        <div>
                                                            <p className="text-xs text-gray-500">Time</p>
                                                            <p className="text-sm font-medium">{exam.startTime} - {exam.endTime}</p>
                                                        </div>
                                                    </div>
                                                    <div className="flex items-center gap-2 text-gray-600">
                                                        <FiCheckCircle className="text-purple-500" />
                                                        <div>
                                                            <p className="text-xs text-gray-500">Marks</p>
                                                            <p className="text-sm font-medium">{exam.totalMarks} (Pass: {exam.passingMarks})</p>
                                                        </div>
                                                    </div>
                                                </div>

                                                <div className="flex items-center gap-6 text-sm text-gray-600">
                                                    <span>Grade: {exam.gradeLevel?.levelName}</span>
                                                    <span>Room: {exam.roomNumber}</span>
                                                    {exam.invigilator && (
                                                        <span className="flex items-center gap-1">
                                                            <FiUser />
                                                            {exam.invigilator.user?.firstName} {exam.invigilator.user?.lastName}
                                                        </span>
                                                    )}
                                                    <span>{exam.durationMinutes} minutes</span>
                                                </div>
                                            </div>

                                            <div className="flex gap-2">
                                                <Button
                                                    size="sm"
                                                    variant="outline"
                                                    onClick={() => handleEdit(exam)}
                                                    className="hover:bg-blue-50"
                                                >
                                                    <FiEdit className="w-4 h-4" />
                                                </Button>
                                                <Button
                                                    size="sm"
                                                    variant="outline"
                                                    onClick={() => handleDelete(exam.id)}
                                                    className="hover:bg-red-50 hover:text-red-700"
                                                >
                                                    <FiTrash2 className="w-4 h-4" />
                                                </Button>
                                            </div>
                                        </div>
                                    </CardContent>
                                </Card>
                            ))
                        ) : (
                            <Card className="border-0 shadow-xl">
                                <CardContent className="py-12 text-center text-gray-500">
                                    <FiCalendar className="w-16 h-16 mx-auto mb-4 text-gray-300" />
                                    <p className="text-lg">No exams found</p>
                                    <p className="text-sm">Click "Schedule Exam" to create one</p>
                                </CardContent>
                            </Card>
                        )}
                    </div>
                )}

                {/* Modal */}
                {showModal && (
                    <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
                        <Card className="w-full max-w-3xl max-h-[90vh] overflow-y-auto">
                            <CardHeader>
                                <CardTitle>{selectedExam ? 'Edit Exam' : 'Schedule New Exam'}</CardTitle>
                                <CardDescription>Configure exam details</CardDescription>
                            </CardHeader>
                            <CardContent className="space-y-4">
                                <div className="grid grid-cols-2 gap-4">
                                    <div className="col-span-2 space-y-2">
                                        <Label>Exam Name *</Label>
                                        <Input
                                            value={formData.examName}
                                            onChange={(e) => setFormData({ ...formData, examName: e.target.value })}
                                            placeholder="e.g., Mid-Term Exam - Mathematics"
                                        />
                                    </div>

                                    <div className="space-y-2">
                                        <Label>Subject *</Label>
                                        <select
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

                                    <div className="space-y-2">
                                        <Label>Grade Level *</Label>
                                        <select
                                            value={formData.gradeLevelId}
                                            onChange={(e) => setFormData({ ...formData, gradeLevelId: e.target.value })}
                                            className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                                        >
                                            <option value="">Select Grade</option>
                                            {gradeLevels.map(g => (
                                                <option key={g.id} value={g.id}>{g.levelName}</option>
                                            ))}
                                        </select>
                                    </div>

                                    <div className="space-y-2">
                                        <Label>Exam Date *</Label>
                                        <Input
                                            type="date"
                                            value={formData.examDate}
                                            onChange={(e) => setFormData({ ...formData, examDate: e.target.value })}
                                        />
                                    </div>

                                    <div className="space-y-2">
                                        <Label>Start Time *</Label>
                                        <Input
                                            type="time"
                                            value={formData.startTime}
                                            onChange={(e) => setFormData({ ...formData, startTime: e.target.value })}
                                        />
                                    </div>

                                    <div className="space-y-2">
                                        <Label>End Time *</Label>
                                        <Input
                                            type="time"
                                            value={formData.endTime}
                                            onChange={(e) => setFormData({ ...formData, endTime: e.target.value })}
                                        />
                                    </div>

                                    <div className="space-y-2">
                                        <Label>Duration (minutes)</Label>
                                        <Input
                                            type="number"
                                            value={formData.durationMinutes}
                                            onChange={(e) => setFormData({ ...formData, durationMinutes: parseInt(e.target.value) })}
                                        />
                                    </div>

                                    <div className="space-y-2">
                                        <Label>Total Marks *</Label>
                                        <Input
                                            type="number"
                                            value={formData.totalMarks}
                                            onChange={(e) => setFormData({ ...formData, totalMarks: parseInt(e.target.value) })}
                                        />
                                    </div>

                                    <div className="space-y-2">
                                        <Label>Passing Marks *</Label>
                                        <Input
                                            type="number"
                                            value={formData.passingMarks}
                                            onChange={(e) => setFormData({ ...formData, passingMarks: parseInt(e.target.value) })}
                                        />
                                    </div>

                                    <div className="space-y-2">
                                        <Label>Room Number</Label>
                                        <Input
                                            value={formData.roomNumber}
                                            onChange={(e) => setFormData({ ...formData, roomNumber: e.target.value })}
                                            placeholder="e.g., Room 101"
                                        />
                                    </div>

                                    <div className="space-y-2">
                                        <Label>Invigilator</Label>
                                        <select
                                            value={formData.invigilatorId}
                                            onChange={(e) => setFormData({ ...formData, invigilatorId: e.target.value })}
                                            className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                                        >
                                            <option value="">Select Teacher</option>
                                            {staff.map(s => (
                                                <option key={s.id} value={s.id}>
                                                    {s.user?.firstName} {s.user?.lastName}
                                                </option>
                                            ))}
                                        </select>
                                    </div>

                                    <div className="space-y-2">
                                        <Label>Exam Type *</Label>
                                        <select
                                            value={formData.examType}
                                            onChange={(e) => setFormData({ ...formData, examType: e.target.value })}
                                            className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                                        >
                                            <option value="MIDTERM">Midterm</option>
                                            <option value="FINAL">Final</option>
                                            <option value="QUIZ">Quiz</option>
                                            <option value="PRACTICE">Practice</option>
                                        </select>
                                    </div>

                                    <div className="space-y-2">
                                        <Label>Semester *</Label>
                                        <select
                                            value={formData.semester}
                                            onChange={(e) => setFormData({ ...formData, semester: e.target.value })}
                                            className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                                        >
                                            <option value="SEMESTER_1">Semester 1</option>
                                            <option value="SEMESTER_2">Semester 2</option>
                                        </select>
                                    </div>

                                    <div className="space-y-2">
                                        <Label>Status</Label>
                                        <select
                                            value={formData.status}
                                            onChange={(e) => setFormData({ ...formData, status: e.target.value })}
                                            className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                                        >
                                            <option value="SCHEDULED">Scheduled</option>
                                            <option value="ONGOING">Ongoing</option>
                                            <option value="COMPLETED">Completed</option>
                                            <option value="CANCELLED">Cancelled</option>
                                        </select>
                                    </div>

                                    <div className="col-span-2 space-y-2">
                                        <Label>Instructions</Label>
                                        <textarea
                                            className="flex min-h-[100px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                                            value={formData.instructions}
                                            onChange={(e) => setFormData({ ...formData, instructions: e.target.value })}
                                            placeholder="Exam instructions..."
                                        />
                                    </div>
                                </div>

                                <div className="flex justify-end gap-3 pt-4">
                                    <Button variant="outline" onClick={() => setShowModal(false)}>Cancel</Button>
                                    <Button
                                        onClick={handleSave}
                                        className="bg-gradient-to-r from-orange-600 to-red-600"
                                    >
                                        {selectedExam ? 'Update' : 'Create'} Exam
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

export default ExamManagement;
