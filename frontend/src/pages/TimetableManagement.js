import React, { useState, useEffect } from 'react';
import { FiCalendar, FiPlus, FiEdit, FiTrash2, FiClock, FiUser, FiBook, FiPrinter } from 'react-icons/fi';
import api from '../services/api';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';

function TimetableManagement() {
    const [timetables, setTimetables] = useState([]);
    const [classes, setClasses] = useState([]);
    const [subjects, setSubjects] = useState([]);
    const [staff, setStaff] = useState([]);
    const [selectedClass, setSelectedClass] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [showModal, setShowModal] = useState(false);
    const [editingEntry, setEditingEntry] = useState(null);
    const [formData, setFormData] = useState({
        dayOfWeek: 'MONDAY',
        sessionType: 'MORNING',
        timeSlot: 1,
        startTime: '07:00',
        endTime: '07:45',
        subject: '',
        subjectTeacherId: '',
        classroom: 'A',
        academicYear: '2024-2025',
        status: 'ACTIVE'
    });

    const daysOfWeek = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'];
    const timeSlots = {
        MORNING: [
            { slot: 1, startTime: '07:00', endTime: '07:45' },
            { slot: 2, startTime: '07:50', endTime: '08:35' },
            { slot: 3, startTime: '08:40', endTime: '09:25' },
            { slot: 4, startTime: '09:30', endTime: '10:15' },
            { slot: 5, startTime: '10:20', endTime: '11:05' },
        ],
        AFTERNOON: [
            { slot: 1, startTime: '13:00', endTime: '13:45' },
            { slot: 2, startTime: '13:50', endTime: '14:35' },
            { slot: 3, startTime: '14:40', endTime: '15:25' },
            { slot: 4, startTime: '15:30', endTime: '16:15' },
            { slot: 5, startTime: '16:20', endTime: '17:05' },
        ]
    };

    useEffect(() => {
        fetchData();
    }, []);

    useEffect(() => {
        if (selectedClass) {
            fetchTimetables();
        }
    }, [selectedClass]);

    const fetchData = async () => {
        try {
            setLoading(true);
            const [classesRes, subjectsRes, staffRes] = await Promise.all([
                api.get('/api/classes'),
                api.get('/api/subjects'),
                api.get('/v1/staff')
            ]);
            setClasses(classesRes.data || []);
            setSubjects(subjectsRes.data || []);
            setStaff(staffRes.data || []);
        } catch (err) {
            setError('Failed to load data');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const fetchTimetables = async () => {
        if (!selectedClass) return;
        try {
            const response = await api.get(`/api/v1/timetables/class/${selectedClass.id}`);
            setTimetables(response.data || []);
        } catch (err) {
            console.error('Failed to load timetables:', err);
            setTimetables([]);
        }
    };

    const getTimetableEntry = (day, session, slot) => {
        return timetables.find(t =>
            t.dayOfWeek === day &&
            t.sessionType === session &&
            t.timeSlot === slot
        );
    };

    const handleAddEntry = (day, session, slot) => {
        const timeSlotInfo = timeSlots[session].find(ts => ts.slot === slot);
        setEditingEntry(null);
        setFormData({
            dayOfWeek: day,
            sessionType: session,
            timeSlot: slot,
            startTime: timeSlotInfo.startTime,
            endTime: timeSlotInfo.endTime,
            subject: '',
            subjectTeacherId: '',
            classroom: 'A',
            academicYear: '2024-2025',
            status: 'ACTIVE'
        });
        setShowModal(true);
    };

    const handleEditEntry = (entry) => {
        setEditingEntry(entry);
        setFormData({
            dayOfWeek: entry.dayOfWeek,
            sessionType: entry.sessionType,
            timeSlot: entry.timeSlot,
            startTime: entry.startTime,
            endTime: entry.endTime,
            subject: entry.subject,
            subjectTeacherId: entry.subjectTeacher?.id || '',
            classroom: entry.classroom,
            academicYear: entry.academicYear,
            status: entry.status
        });
        setShowModal(true);
    };

    const handleSave = async () => {
        try {
            if (!selectedClass) {
                setError('Please select a class first');
                return;
            }

            const payload = {
                schoolClass: { id: selectedClass.id },
                dayOfWeek: formData.dayOfWeek,
                sessionType: formData.sessionType,
                timeSlot: formData.timeSlot,
                startTime: formData.startTime,
                endTime: formData.endTime,
                subject: formData.subject,
                subjectTeacher: formData.subjectTeacherId ? { id: formData.subjectTeacherId } : null,
                classroom: formData.classroom,
                academicYear: formData.academicYear,
                status: formData.status
            };

            if (editingEntry) {
                await api.put(`/api/v1/timetables/${editingEntry.id}`, payload);
            } else {
                await api.post('/api/v1/timetables', payload);
            }

            setShowModal(false);
            fetchTimetables();
            setError('');
        } catch (err) {
            setError('Failed to save: ' + (err.response?.data?.message || err.message));
        }
    };

    const handleDelete = async (id) => {
        if (window.confirm('Are you sure you want to delete this entry?')) {
            try {
                await api.delete(`/api/v1/timetables/${id}`);
                fetchTimetables();
            } catch (err) {
                setError('Failed to delete entry');
            }
        }
    };

    const handlePrint = () => {
        window.print();
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-slate-50 via-purple-50 to-pink-50 p-6">
            <div className="max-w-7xl mx-auto">
                {/* Header */}
                <div className="mb-8 flex items-center justify-between">
                    <div>
                        <h1 className="text-3xl font-bold text-gray-900 mb-2 flex items-center gap-2">
                            <FiCalendar className="text-purple-600" />
                            Timetable Management
                        </h1>
                        <p className="text-gray-600">Manage class schedules and periods</p>
                    </div>
                    <Button
                        onClick={handlePrint}
                        className="bg-gradient-to-r from-purple-600 to-pink-600"
                        disabled={!selectedClass}
                    >
                        <FiPrinter className="w-4 h-4 mr-2" />
                        Print Timetable
                    </Button>
                </div>

                {/* Class Selector */}
                <Card className="mb-6 border-0 shadow-xl">
                    <CardHeader className="bg-gradient-to-r from-purple-50 to-pink-50">
                        <CardTitle>Select Class</CardTitle>
                    </CardHeader>
                    <CardContent className="pt-6">
                        <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-3">
                            {classes.map(cls => (
                                <Button
                                    key={cls.id}
                                    variant={selectedClass?.id === cls.id ? 'default' : 'outline'}
                                    className={selectedClass?.id === cls.id ?
                                        'bg-gradient-to-r from-purple-600 to-pink-600' : ''
                                    }
                                    onClick={() => setSelectedClass(cls)}
                                >
                                    {cls.className}
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

                {/* Timetable Grid */}
                {selectedClass && (
                    <>
                        {/* Morning Session */}
                        <Card className="mb-6 border-0 shadow-xl">
                            <CardHeader className="bg-gradient-to-r from-amber-50 to-yellow-50">
                                <CardTitle className="flex items-center gap-2">
                                    <FiClock className="text-amber-600" />
                                    Morning Session (07:00 - 11:05)
                                </CardTitle>
                                <CardDescription>Class: {selectedClass.className}</CardDescription>
                            </CardHeader>
                            <CardContent className="pt-6">
                                <div className="overflow-x-auto">
                                    <table className="w-full border-collapse">
                                        <thead>
                                            <tr className="bg-gray-50">
                                                <th className="border p-2 text-sm font-semibold">Period</th>
                                                {daysOfWeek.map(day => (
                                                    <th key={day} className="border p-2 text-sm font-semibold">
                                                        {day}
                                                    </th>
                                                ))}
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {timeSlots.MORNING.map(ts => (
                                                <tr key={ts.slot}>
                                                    <td className="border p-2 bg-gray-50 text-center">
                                                        <div className="text-sm font-medium">Period {ts.slot}</div>
                                                        <div className="text-xs text-gray-500">
                                                            {ts.startTime} - {ts.endTime}
                                                        </div>
                                                    </td>
                                                    {daysOfWeek.map(day => {
                                                        const entry = getTimetableEntry(day, 'MORNING', ts.slot);
                                                        return (
                                                            <td key={day} className="border p-2 min-w-[120px]">
                                                                {entry ? (
                                                                    <div className="relative group">
                                                                        <div className="bg-purple-50 hover:bg-purple-100 p-2 rounded cursor-pointer transition-colors">
                                                                            <div className="font-medium text-sm text-purple-900">
                                                                                {entry.subject}
                                                                            </div>
                                                                            {entry.subjectTeacher && (
                                                                                <div className="text-xs text-gray-600 mt-1 flex items-center gap-1">
                                                                                    <FiUser className="w-3 h-3" />
                                                                                    {entry.subjectTeacher.user?.firstName}
                                                                                </div>
                                                                            )}
                                                                        </div>
                                                                        <div className="absolute top-1 right-1 hidden group-hover:flex gap-1">
                                                                            <button
                                                                                onClick={() => handleEditEntry(entry)}
                                                                                className="p-1 bg-white rounded shadow-sm hover:bg-blue-50"
                                                                            >
                                                                                <FiEdit className="w-3 h-3 text-blue-600" />
                                                                            </button>
                                                                            <button
                                                                                onClick={() => handleDelete(entry.id)}
                                                                                className="p-1 bg-white rounded shadow-sm hover:bg-red-50"
                                                                            >
                                                                                <FiTrash2 className="w-3 h-3 text-red-600" />
                                                                            </button>
                                                                        </div>
                                                                    </div>
                                                                ) : (
                                                                    <button
                                                                        onClick={() => handleAddEntry(day, 'MORNING', ts.slot)}
                                                                        className="w-full p-2 text-gray-400 hover:bg-gray-50 hover:text-gray-600 rounded transition-colors flex items-center justify-center gap-1 text-sm"
                                                                    >
                                                                        <FiPlus className="w-4 h-4" />
                                                                        Add
                                                                    </button>
                                                                )}
                                                            </td>
                                                        );
                                                    })}
                                                </tr>
                                            ))}
                                        </tbody>
                                    </table>
                                </div>
                            </CardContent>
                        </Card>

                        {/* Afternoon Session */}
                        <Card className="border-0 shadow-xl">
                            <CardHeader className="bg-gradient-to-r from-blue-50 to-indigo-50">
                                <CardTitle className="flex items-center gap-2">
                                    <FiClock className="text-blue-600" />
                                    Afternoon Session (13:00 - 17:05)
                                </CardTitle>
                                <CardDescription>Class: {selectedClass.className}</CardDescription>
                            </CardHeader>
                            <CardContent className="pt-6">
                                <div className="overflow-x-auto">
                                    <table className="w-full border-collapse">
                                        <thead>
                                            <tr className="bg-gray-50">
                                                <th className="border p-2 text-sm font-semibold">Period</th>
                                                {daysOfWeek.map(day => (
                                                    <th key={day} className="border p-2 text-sm font-semibold">
                                                        {day}
                                                    </th>
                                                ))}
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {timeSlots.AFTERNOON.map(ts => (
                                                <tr key={ts.slot}>
                                                    <td className="border p-2 bg-gray-50 text-center">
                                                        <div className="text-sm font-medium">Period {ts.slot}</div>
                                                        <div className="text-xs text-gray-500">
                                                            {ts.startTime} - {ts.endTime}
                                                        </div>
                                                    </td>
                                                    {daysOfWeek.map(day => {
                                                        const entry = getTimetableEntry(day, 'AFTERNOON', ts.slot);
                                                        return (
                                                            <td key={day} className="border p-2 min-w-[120px]">
                                                                {entry ? (
                                                                    <div className="relative group">
                                                                        <div className="bg-blue-50 hover:bg-blue-100 p-2 rounded cursor-pointer transition-colors">
                                                                            <div className="font-medium text-sm text-blue-900">
                                                                                {entry.subject}
                                                                            </div>
                                                                            {entry.subjectTeacher && (
                                                                                <div className="text-xs text-gray-600 mt-1 flex items-center gap-1">
                                                                                    <FiUser className="w-3 h-3" />
                                                                                    {entry.subjectTeacher.user?.firstName}
                                                                                </div>
                                                                            )}
                                                                        </div>
                                                                        <div className="absolute top-1 right-1 hidden group-hover:flex gap-1">
                                                                            <button
                                                                                onClick={() => handleEditEntry(entry)}
                                                                                className="p-1 bg-white rounded shadow-sm hover:bg-blue-50"
                                                                            >
                                                                                <FiEdit className="w-3 h-3 text-blue-600" />
                                                                            </button>
                                                                            <button
                                                                                onClick={() => handleDelete(entry.id)}
                                                                                className="p-1 bg-white rounded shadow-sm hover:bg-red-50"
                                                                            >
                                                                                <FiTrash2 className="w-3 h-3 text-red-600" />
                                                                            </button>
                                                                        </div>
                                                                    </div>
                                                                ) : (
                                                                    <button
                                                                        onClick={() => handleAddEntry(day, 'AFTERNOON', ts.slot)}
                                                                        className="w-full p-2 text-gray-400 hover:bg-gray-50 hover:text-gray-600 rounded transition-colors flex items-center justify-center gap-1 text-sm"
                                                                    >
                                                                        <FiPlus className="w-4 h-4" />
                                                                        Add
                                                                    </button>
                                                                )}
                                                            </td>
                                                        );
                                                    })}
                                                </tr>
                                            ))}
                                        </tbody>
                                    </table>
                                </div>
                            </CardContent>
                        </Card>
                    </>
                )}

                {!selectedClass && !loading && (
                    <Card className="border-0 shadow-xl">
                        <CardContent className="py-12 text-center text-gray-500">
                            <FiCalendar className="w-16 h-16 mx-auto mb-4 text-gray-300" />
                            <p className="text-lg">Please select a class to view or edit its timetable</p>
                        </CardContent>
                    </Card>
                )}

                {/* Modal */}
                {showModal && (
                    <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
                        <Card className="w-full max-w-2xl">
                            <CardHeader>
                                <CardTitle>{editingEntry ? 'Edit Period' : 'Add Period'}</CardTitle>
                                <CardDescription>
                                    {formData.dayOfWeek} - {formData.sessionType} - Period {formData.timeSlot}
                                </CardDescription>
                            </CardHeader>
                            <CardContent className="space-y-4">
                                <div className="grid grid-cols-2 gap-4">
                                    <div className="space-y-2">
                                        <Label>Subject *</Label>
                                        <select
                                            value={formData.subject}
                                            onChange={(e) => setFormData({ ...formData, subject: e.target.value })}
                                            className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                                        >
                                            <option value="">Select Subject</option>
                                            {subjects.map(s => (
                                                <option key={s.id} value={s.subjectName}>{s.subjectName}</option>
                                            ))}
                                        </select>
                                    </div>
                                    <div className="space-y-2">
                                        <Label>Teacher</Label>
                                        <select
                                            value={formData.subjectTeacherId}
                                            onChange={(e) => setFormData({ ...formData, subjectTeacherId: e.target.value })}
                                            className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                                        >
                                            <option value="">Select Teacher</option>
                                            {staff.filter(s => s.position === 'TEACHER').map(s => (
                                                <option key={s.id} value={s.id}>
                                                    {s.user?.firstName} {s.user?.lastName}
                                                </option>
                                            ))}
                                        </select>
                                    </div>
                                </div>

                                <div className="flex justify-end gap-3 pt-4">
                                    <Button variant="outline" onClick={() => setShowModal(false)}>Cancel</Button>
                                    <Button
                                        onClick={handleSave}
                                        className="bg-gradient-to-r from-purple-600 to-pink-600"
                                    >
                                        <FiPlus className="w-4 h-4 mr-2" />
                                        {editingEntry ? 'Update' : 'Save'}
                                    </Button>
                                </div>
                            </CardContent>
                        </Card>
                    </div>
                )}
            </div>

            {/* Print Styles */}
            <style jsx>{`
                @media print {
                    .no-print {
                        display: none;
                    }
                    body {
                        print-color-adjust: exact;
                        -webkit-print-color-adjust: exact;
                    }
                }
            `}</style>
        </div>
    );
}

export default TimetableManagement;
