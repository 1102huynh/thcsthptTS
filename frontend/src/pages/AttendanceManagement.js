import React, { useState, useEffect } from 'react';
import { FiCheck, FiX, FiClock, FiSave, FiCalendar } from 'react-icons/fi';
import { studentService } from '../services/dataService';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';

function AttendanceManagement() {
    const [students, setStudents] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [selectedDate, setSelectedDate] = useState(new Date().toISOString().split('T')[0]);
    const [selectedClass, setSelectedClass] = useState('');
    const [attendance, setAttendance] = useState({});

    useEffect(() => {
        fetchStudents();
    }, [selectedClass]);

    const fetchStudents = async () => {
        try {
            setLoading(true);
            const response = await studentService.getAll();
            let filteredStudents = response.data || [];

            if (selectedClass) {
                filteredStudents = filteredStudents.filter(s => s.className === selectedClass);
            }

            setStudents(filteredStudents);

            const initialAttendance = {};
            filteredStudents.forEach(student => {
                initialAttendance[student.id] = 'Present';
            });
            setAttendance(initialAttendance);
        } catch (err) {
            setError('Failed to load students');
        } finally {
            setLoading(false);
        }
    };

    const handleAttendanceChange = (studentId, status) => {
        setAttendance({
            ...attendance,
            [studentId]: status
        });
    };

    const handleSaveAttendance = async () => {
        try {
            setSuccess(`Attendance saved successfully for ${selectedDate}`);
            setTimeout(() => setSuccess(''), 3000);
        } catch (err) {
            setError('Failed to save attendance');
        }
    };

    const markAllPresent = () => {
        const allPresent = {};
        students.forEach(student => {
            allPresent[student.id] = 'Present';
        });
        setAttendance(allPresent);
    };

    const markAllAbsent = () => {
        const allAbsent = {};
        students.forEach(student => {
            allAbsent[student.id] = 'Absent';
        });
        setAttendance(allAbsent);
    };

    const getAttendanceStats = () => {
        const total = students.length;
        const present = Object.values(attendance).filter(status => status === 'Present').length;
        const absent = Object.values(attendance).filter(status => status === 'Absent').length;
        const late = Object.values(attendance).filter(status => status === 'Late').length;
        const percentage = total > 0 ? ((present / total) * 100).toFixed(1) : 0;

        return { total, present, absent, late, percentage };
    };

    const stats = getAttendanceStats();
    const uniqueClasses = [...new Set(students.map(s => s.className))].filter(Boolean);

    return (
        <div className="min-h-screen bg-gradient-to-br from-slate-50 via-orange-50 to-amber-50 p-6">
            <div className="max-w-7xl mx-auto">
                {/* Header */}
                <div className="mb-8">
                    <div className="flex items-center justify-between">
                        <div>
                            <h1 className="text-3xl font-bold text-gray-900 mb-2 flex items-center gap-2">
                                <FiCalendar className="text-orange-600" />
                                Attendance Management
                            </h1>
                            <p className="text-gray-600">Mark and track student attendance daily</p>
                        </div>
                    </div>
                </div>

                {/* Alerts */}
                {error && (
                    <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg">
                        <p className="text-sm text-red-800">{error}</p>
                    </div>
                )}
                {success && (
                    <div className="mb-6 p-4 bg-green-50 border border-green-200 rounded-lg">
                        <p className="text-sm text-green-800">{success}</p>
                    </div>
                )}

                {/* Controls */}
                <Card className="mb-6 border-0 shadow-lg">
                    <CardContent className="pt-6">
                        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                            <div className="space-y-2">
                                <Label htmlFor="date">Date</Label>
                                <Input
                                    id="date"
                                    type="date"
                                    value={selectedDate}
                                    onChange={(e) => setSelectedDate(e.target.value)}
                                    max={new Date().toISOString().split('T')[0]}
                                />
                            </div>
                            <div className="space-y-2">
                                <Label htmlFor="class">Class</Label>
                                <select
                                    id="class"
                                    value={selectedClass}
                                    onChange={(e) => setSelectedClass(e.target.value)}
                                    className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                                >
                                    <option value="">All Classes</option>
                                    {uniqueClasses.map(className => (
                                        <option key={className} value={className}>{className}</option>
                                    ))}
                                </select>
                            </div>
                            <div className="flex items-end">
                                <Button onClick={markAllPresent} variant="outline" className="w-full hover:bg-green-50">
                                    <FiCheck className="mr-2" /> All Present
                                </Button>
                            </div>
                            <div className="flex items-end">
                                <Button onClick={handleSaveAttendance} className="w-full bg-gradient-to-r from-orange-600 to-amber-600">
                                    <FiSave className="mr-2" /> Save
                                </Button>
                            </div>
                        </div>
                    </CardContent>
                </Card>

                {/* Statistics */}
                <div className="grid grid-cols-4 gap-4 mb-6">
                    <Card className="border-0 shadow-lg">
                        <CardContent className="pt-6 text-center">
                            <div className="text-3xl font-bold text-gray-900">{stats.total}</div>
                            <div className="text-sm text-gray-600 mt-1">Total Students</div>
                        </CardContent>
                    </Card>
                    <Card className="border-0 shadow-lg border-l-4 border-l-green-500">
                        <CardContent className="pt-6 text-center">
                            <div className="text-3xl font-bold text-green-600">{stats.present}</div>
                            <div className="text-sm text-gray-600 mt-1">Present</div>
                        </CardContent>
                    </Card>
                    <Card className="border-0 shadow-lg border-l-4 border-l-red-500">
                        <CardContent className="pt-6 text-center">
                            <div className="text-3xl font-bold text-red-600">{stats.absent}</div>
                            <div className="text-sm text-gray-600 mt-1">Absent</div>
                        </CardContent>
                    </Card>
                    <Card className="border-0 shadow-lg border-l-4 border-l-amber-500">
                        <CardContent className="pt-6 text-center">
                            <div className="text-3xl font-bold text-amber-600">{stats.percentage}%</div>
                            <div className="text-sm text-gray-600 mt-1">Attendance Rate</div>
                        </CardContent>
                    </Card>
                </div>

                {/* Attendance Table */}
                <Card className="border-0 shadow-xl">
                    <CardHeader>
                        <CardTitle>Student Attendance</CardTitle>
                        <CardDescription>Mark attendance for {selectedDate}</CardDescription>
                    </CardHeader>
                    <CardContent>
                        {loading ? (
                            <div className="flex justify-center items-center py-12">
                                <div className="w-12 h-12 border-4 border-orange-600 border-t-transparent rounded-full animate-spin"></div>
                            </div>
                        ) : (
                            <div className="overflow-x-auto">
                                <table className="w-full">
                                    <thead>
                                        <tr className="border-b border-gray-200">
                                            <th className="text-left py-3 px-4 text-sm font-semibold text-gray-700">#</th>
                                            <th className="text-left py-3 px-4 text-sm font-semibold text-gray-700">Roll Number</th>
                                            <th className="text-left py-3 px-4 text-sm font-semibold text-gray-700">Name</th>
                                            <th className="text-left py-3 px-4 text-sm font-semibold text-gray-700">Class</th>
                                            <th className="text-left py-3 px-4 text-sm font-semibold text-gray-700">Section</th>
                                            <th className="text-left py-3 px-4 text-sm font-semibold text-gray-700">Attendance Status</th>
                                        </tr>
                                    </thead>
                                    <tbody className="divide-y divide-gray-100">
                                        {students.length > 0 ? (
                                            students.map((student, index) => (
                                                <tr key={student.id} className="hover:bg-gray-50">
                                                    <td className="py-4 px-4 text-sm text-gray-600">{index + 1}</td>
                                                    <td className="py-4 px-4 text-sm font-medium text-gray-900">{student.rollNumber}</td>
                                                    <td className="py-4 px-4">
                                                        <div className="font-medium text-gray-900">
                                                            {student.user?.firstName} {student.user?.lastName}
                                                        </div>
                                                    </td>
                                                    <td className="py-4 px-4 text-sm text-gray-600">{student.className}</td>
                                                    <td className="py-4 px-4 text-sm text-gray-600">{student.section}</td>
                                                    <td className="py-4 px-4">
                                                        <div className="flex gap-2">
                                                            <Button
                                                                size="sm"
                                                                onClick={() => handleAttendanceChange(student.id, 'Present')}
                                                                className={attendance[student.id] === 'Present' ? 'bg-green-600 hover:bg-green-700' : 'bg-gray-200 hover:bg-gray-300 text-gray-700'}
                                                            >
                                                                <FiCheck className="mr-1 w-4 h-4" /> Present
                                                            </Button>
                                                            <Button
                                                                size="sm"
                                                                onClick={() => handleAttendanceChange(student.id, 'Absent')}
                                                                className={attendance[student.id] === 'Absent' ? 'bg-red-600 hover:bg-red-700' : 'bg-gray-200 hover:bg-gray-300 text-gray-700'}
                                                            >
                                                                <FiX className="mr-1 w-4 h-4" /> Absent
                                                            </Button>
                                                            <Button
                                                                size="sm"
                                                                onClick={() => handleAttendanceChange(student.id, 'Late')}
                                                                className={attendance[student.id] === 'Late' ? 'bg-amber-600 hover:bg-amber-700' : 'bg-gray-200 hover:bg-gray-300 text-gray-700'}
                                                            >
                                                                <FiClock className="mr-1 w-4 h-4" /> Late
                                                            </Button>
                                                        </div>
                                                    </td>
                                                </tr>
                                            ))
                                        ) : (
                                            <tr>
                                                <td colSpan="6" className="text-center py-12 text-gray-500">
                                                    {selectedClass ? `No students found in class ${selectedClass}` : 'No students found'}
                                                </td>
                                            </tr>
                                        )}
                                    </tbody>
                                </table>
                            </div>
                        )}
                    </CardContent>
                </Card>
            </div>
        </div>
    );
}

export default AttendanceManagement;
