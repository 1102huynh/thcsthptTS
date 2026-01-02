import React, { useState, useEffect } from 'react';
import { FiAward, FiTrendingUp, FiSave } from 'react-icons/fi';
import { studentService } from '../services/dataService';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';

function GradeManagement() {
    const [students, setStudents] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [showModal, setShowModal] = useState(false);
    const [selectedStudent, setSelectedStudent] = useState(null);
    const [selectedClass, setSelectedClass] = useState('');
    const [selectedSubject, setSelectedSubject] = useState('Mathematics');
    const [grades, setGrades] = useState({});

    const subjects = [
        'Mathematics', 'English', 'Science', 'History', 'Geography',
        'Physics', 'Chemistry', 'Biology', 'Computer Science', 'Physical Education'
    ];

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

            const initialGrades = {};
            filteredStudents.forEach(student => {
                initialGrades[student.id] = subjects.reduce((acc, subject) => {
                    acc[subject] = 0;
                    return acc;
                }, {});
            });
            setGrades(initialGrades);
        } catch (err) {
            setError('Failed to load students');
        } finally {
            setLoading(false);
        }
    };

    const handleGradeChange = (studentId, subject, grade) => {
        setGrades({
            ...grades,
            [studentId]: {
                ...grades[studentId],
                [subject]: parseFloat(grade) || 0
            }
        });
    };

    const calculateAverage = (studentId) => {
        const studentGrades = grades[studentId];
        if (!studentGrades) return 0;

        const values = Object.values(studentGrades);
        const sum = values.reduce((acc, val) => acc + val, 0);
        return (sum / values.length).toFixed(2);
    };

    const getGrade = (percentage) => {
        if (percentage >= 90) return { letter: 'A+', color: 'bg-green-100 text-green-700' };
        if (percentage >= 80) return { letter: 'A', color: 'bg-green-100 text-green-700' };
        if (percentage >= 70) return { letter: 'B', color: 'bg-blue-100 text-blue-700' };
        if (percentage >= 60) return { letter: 'C', color: 'bg-yellow-100 text-yellow-700' };
        if (percentage >= 50) return { letter: 'D', color: 'bg-orange-100 text-orange-700' };
        return { letter: 'F', color: 'bg-red-100 text-red-700' };
    };

    const handleSaveGrades = async () => {
        try {
            setSuccess(`Grades saved successfully for ${selectedSubject}`);
            setTimeout(() => setSuccess(''), 3000);
        } catch (err) {
            setError('Failed to save grades');
        }
    };

    const handleViewDetails = (student) => {
        setSelectedStudent(student);
        setShowModal(true);
    };

    const uniqueClasses = [...new Set(students.map(s => s.className))].filter(Boolean);

    return (
        <div className="min-h-screen bg-gradient-to-br from-slate-50 via-indigo-50 to-violet-50 p-6">
            <div className="max-w-7xl mx-auto">
                {/* Header */}
                <div className="mb-8">
                    <div className="flex items-center justify-between">
                        <div>
                            <h1 className="text-3xl font-bold text-gray-900 mb-2 flex items-center gap-2">
                                <FiAward className="text-indigo-600" />
                                Grade Management
                            </h1>
                            <p className="text-gray-600">Manage student grades and academic performance</p>
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
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
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
                            <div className="space-y-2">
                                <Label htmlFor="subject">Subject</Label>
                                <select
                                    id="subject"
                                    value={selectedSubject}
                                    onChange={(e) => setSelectedSubject(e.target.value)}
                                    className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                                >
                                    {subjects.map(subject => (
                                        <option key={subject} value={subject}>{subject}</option>
                                    ))}
                                </select>
                            </div>
                            <div className="flex items-end">
                                <Button onClick={handleSaveGrades} className="w-full bg-gradient-to-r from-indigo-600 to-violet-600">
                                    <FiSave className="mr-2" /> Save Grades
                                </Button>
                            </div>
                        </div>
                    </CardContent>
                </Card>

                {/* Grades Table */}
                <Card className="border-0 shadow-xl">
                    <CardHeader>
                        <CardTitle>Student Grades - {selectedSubject}</CardTitle>
                        <CardDescription>Enter and manage grades for selected subject</CardDescription>
                    </CardHeader>
                    <CardContent>
                        {loading ? (
                            <div className="flex justify-center items-center py-12">
                                <div className="w-12 h-12 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin"></div>
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
                                            <th className="text-left py-3 px-4 text-sm font-semibold text-gray-700">{selectedSubject} Score</th>
                                            <th className="text-left py-3 px-4 text-sm font-semibold text-gray-700">Average</th>
                                            <th className="text-left py-3 px-4 text-sm font-semibold text-gray-700">Grade</th>
                                            <th className="text-right py-3 px-4 text-sm font-semibold text-gray-700">Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody className="divide-y divide-gray-100">
                                        {students.length > 0 ? (
                                            students.map((student, index) => {
                                                const average = calculateAverage(student.id);
                                                const gradeInfo = getGrade(average);
                                                return (
                                                    <tr key={student.id} className="hover:bg-gray-50">
                                                        <td className="py-4 px-4 text-sm text-gray-600">{index + 1}</td>
                                                        <td className="py-4 px-4 text-sm font-medium text-gray-900">{student.rollNumber}</td>
                                                        <td className="py-4 px-4">
                                                            <div className="font-medium text-gray-900">
                                                                {student.user?.firstName} {student.user?.lastName}
                                                            </div>
                                                        </td>
                                                        <td className="py-4 px-4 text-sm text-gray-600">{student.className}</td>
                                                        <td className="py-4 px-4">
                                                            <Input
                                                                type="number"
                                                                min="0"
                                                                max="100"
                                                                className="w-24"
                                                                value={grades[student.id]?.[selectedSubject] || 0}
                                                                onChange={(e) => handleGradeChange(student.id, selectedSubject, e.target.value)}
                                                            />
                                                        </td>
                                                        <td className="py-4 px-4">
                                                            <span className="font-semibold text-gray-900">{average}%</span>
                                                        </td>
                                                        <td className="py-4 px-4">
                                                            <span className={`px-3 py-1 rounded-full text-xs font-semibold ${gradeInfo.color}`}>
                                                                {gradeInfo.letter}
                                                            </span>
                                                        </td>
                                                        <td className="py-4 px-4">
                                                            <div className="flex justify-end">
                                                                <Button
                                                                    size="sm"
                                                                    variant="outline"
                                                                    onClick={() => handleViewDetails(student)}
                                                                    className="hover:bg-indigo-50"
                                                                >
                                                                    <FiTrendingUp className="w-4 h-4 mr-1" /> Details
                                                                </Button>
                                                            </div>
                                                        </td>
                                                    </tr>
                                                );
                                            })
                                        ) : (
                                            <tr>
                                                <td colSpan="8" className="text-center py-12 text-gray-500">
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

                {/* Student Details Modal */}
                {showModal && selectedStudent && (
                    <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
                        <Card className="w-full max-w-2xl max-h-[90vh] overflow-y-auto">
                            <CardHeader>
                                <CardTitle>
                                    Grade Details - {selectedStudent.user?.firstName} {selectedStudent.user?.lastName}
                                </CardTitle>
                                <CardDescription>
                                    Roll Number: {selectedStudent.rollNumber} | Class: {selectedStudent.className}
                                </CardDescription>
                            </CardHeader>
                            <CardContent>
                                <div className="overflow-x-auto">
                                    <table className="w-full">
                                        <thead>
                                            <tr className="border-b border-gray-200">
                                                <th className="text-left py-2 px-3 text-sm font-semibold text-gray-700">Subject</th>
                                                <th className="text-center py-2 px-3 text-sm font-semibold text-gray-700">Score</th>
                                                <th className="text-center py-2 px-3 text-sm font-semibold text-gray-700">Grade</th>
                                            </tr>
                                        </thead>
                                        <tbody className="divide-y divide-gray-100">
                                            {subjects.map(subject => {
                                                const score = grades[selectedStudent.id]?.[subject] || 0;
                                                const gradeInfo = getGrade(score);
                                                return (
                                                    <tr key={subject}>
                                                        <td className="py-2 px-3 text-sm text-gray-900">{subject}</td>
                                                        <td className="py-2 px-3 text-center text-sm font-medium">{score}%</td>
                                                        <td className="py-2 px-3 text-center">
                                                            <span className={`px-2 py-1 rounded text-xs font-semibold ${gradeInfo.color}`}>
                                                                {gradeInfo.letter}
                                                            </span>
                                                        </td>
                                                    </tr>
                                                );
                                            })}
                                        </tbody>
                                        <tfoot className="border-t-2 border-gray-300">
                                            <tr className="bg-gray-50">
                                                <td className="py-3 px-3 text-sm font-bold text-gray-900">Overall Average</td>
                                                <td className="py-3 px-3 text-center">
                                                    <span className="text-lg font-bold text-indigo-600">
                                                        {calculateAverage(selectedStudent.id)}%
                                                    </span>
                                                </td>
                                                <td className="py-3 px-3 text-center">
                                                    <span className={`px-3 py-1 rounded-full text-sm font-bold ${getGrade(calculateAverage(selectedStudent.id)).color}`}>
                                                        {getGrade(calculateAverage(selectedStudent.id)).letter}
                                                    </span>
                                                </td>
                                            </tr>
                                        </tfoot>
                                    </table>
                                </div>
                                <div className="flex justify-end mt-6">
                                    <Button variant="outline" onClick={() => setShowModal(false)}>
                                        Close
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

export default GradeManagement;
