import React, { useState, useEffect } from 'react';
import { FiAward, FiPlus, FiEdit, FiUser, FiTrendingUp, FiCheckCircle, FiXCircle } from 'react-icons/fi';
import api from '../services/api';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';

function ExamResultsPage() {
    const [exams, setExams] = useState([]);
    const [selectedExam, setSelectedExam] = useState(null);
    const [results, setResults] = useState([]);
    const [students, setStudents] = useState([]);
    const [statistics, setStatistics] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [showModal, setShowModal] = useState(false);
    const [selectedResult, setSelectedResult] = useState(null);
    const [formData, setFormData] = useState({
        studentId: '',
        marksObtained: '',
        remarks: ''
    });

    useEffect(() => {
        fetchExams();
    }, []);

    useEffect(() => {
        if (selectedExam) {
            fetchResults();
            fetchStatistics();
        }
    }, [selectedExam]);

    const fetchExams = async () => {
        try {
            setLoading(true);
            const response = await api.get('/api/exams');
            setExams(response.data || []);
        } catch (err) {
            setError('Failed to load exams');
        } finally {
            setLoading(false);
        }
    };

    const fetchResults = async () => {
        if (!selectedExam) return;
        try {
            const [resultsRes, studentsRes] = await Promise.all([
                api.get(`/api/exam-results/exam/${selectedExam.id}`),
                api.get('/v1/students')
            ]);
            setResults(resultsRes.data || []);
            setStudents(studentsRes.data || []);
        } catch (err) {
            setError('Failed to load results');
        }
    };

    const fetchStatistics = async () => {
        if (!selectedExam) return;
        try {
            const response = await api.get(`/api/exam-results/exam/${selectedExam.id}/statistics`);
            setStatistics(response.data);
        } catch (err) {
            console.error('Failed to load statistics');
        }
    };

    const handleAddResult = () => {
        setSelectedResult(null);
        setFormData({
            studentId: '',
            marksObtained: '',
            remarks: ''
        });
        setShowModal(true);
    };

    const handleEditResult = (result) => {
        setSelectedResult(result);
        setFormData({
            studentId: result.student?.id || '',
            marksObtained: result.marksObtained || '',
            remarks: result.remarks || ''
        });
        setShowModal(true);
    };

    const handleSave = async () => {
        try {
            const payload = {
                exam: { id: selectedExam.id },
                student: { id: formData.studentId },
                marksObtained: parseFloat(formData.marksObtained),
                remarks: formData.remarks,
                gradedById: 1 // You should get this from auth context
            };

            if (selectedResult) {
                await api.put(`/api/exam-results/${selectedResult.id}`, payload);
            } else {
                await api.post('/api/exam-results', payload);
            }

            setShowModal(false);
            fetchResults();
            fetchStatistics();
            setError('');
        } catch (err) {
            setError('Failed to save: ' + (err.response?.data?.message || err.message));
        }
    };

    const getGradeColor = (grade) => {
        switch (grade) {
            case 'A+': return 'bg-green-100 text-green-700 border-green-300';
            case 'A': return 'bg-green-50 text-green-600 border-green-200';
            case 'B+': return 'bg-blue-100 text-blue-700 border-blue-300';
            case 'B': return 'bg-blue-50 text-blue-600 border-blue-200';
            case 'C': return 'bg-yellow-100 text-yellow-700 border-yellow-300';
            case 'D': return 'bg-orange-100 text-orange-700 border-orange-300';
            case 'F': return 'bg-red-100 text-red-700 border-red-300';
            default: return 'bg-gray-100 text-gray-700 border-gray-300';
        }
    };

    const getStatusIcon = (status) => {
        switch (status) {
            case 'PASS': return <FiCheckCircle className="text-green-600" />;
            case 'FAIL': return <FiXCircle className="text-red-600" />;
            default: return null;
        }
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50 p-6">
            <div className="max-w-7xl mx-auto">
                {/* Header */}
                <div className="mb-8">
                    <h1 className="text-3xl font-bold text-gray-900 mb-2 flex items-center gap-2">
                        <FiAward className="text-blue-600" />
                        Exam Results Management
                    </h1>
                    <p className="text-gray-600">Enter and manage exam results</p>
                </div>

                {/* Exam Selector */}
                <Card className="mb-6 border-0 shadow-xl">
                    <CardHeader>
                        <CardTitle>Select Exam</CardTitle>
                        <CardDescription>Choose an exam to view or enter results</CardDescription>
                    </CardHeader>
                    <CardContent>
                        <select
                            value={selectedExam?.id || ''}
                            onChange={(e) => {
                                const exam = exams.find(ex => ex.id === parseInt(e.target.value));
                                setSelectedExam(exam);
                            }}
                            className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                        >
                            <option value="">Select an exam</option>
                            {exams.map(exam => (
                                <option key={exam.id} value={exam.id}>
                                    {exam.examName} - {exam.subject?.subjectName} ({new Date(exam.examDate).toLocaleDateString()})
                                </option>
                            ))}
                        </select>
                    </CardContent>
                </Card>

                {/* Error Alert */}
                {error && (
                    <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg">
                        <p className="text-sm text-red-800">{error}</p>
                    </div>
                )}

                {selectedExam && (
                    <>
                        {/* Statistics */}
                        {statistics && (
                            <div className="grid grid-cols-1 md:grid-cols-5 gap-4 mb-6">
                                <Card className="border-l-4 border-l-purple-500">
                                    <CardHeader className="pb-2">
                                        <CardDescription>Total Students</CardDescription>
                                        <CardTitle className="text-3xl">{statistics.totalCount}</CardTitle>
                                    </CardHeader>
                                </Card>
                                <Card className="border-l-4 border-l-green-500">
                                    <CardHeader className="pb-2">
                                        <CardDescription>Passed</CardDescription>
                                        <CardTitle className="text-3xl text-green-600">{statistics.passedCount}</CardTitle>
                                    </CardHeader>
                                </Card>
                                <Card className="border-l-4 border-l-red-500">
                                    <CardHeader className="pb-2">
                                        <CardDescription>Failed</CardDescription>
                                        <CardTitle className="text-3xl text-red-600">{statistics.failedCount}</CardTitle>
                                    </CardHeader>
                                </Card>
                                <Card className="border-l-4 border-l-blue-500">
                                    <CardHeader className="pb-2">
                                        <CardDescription>Average Score</CardDescription>
                                        <CardTitle className="text-3xl text-blue-600">{statistics.averageScore?.toFixed(1)}</CardTitle>
                                    </CardHeader>
                                </Card>
                                <Card className="border-l-4 border-l-orange-500">
                                    <CardHeader className="pb-2">
                                        <CardDescription>Pass Rate</CardDescription>
                                        <CardTitle className="text-3xl text-orange-600">{statistics.passPercentage?.toFixed(1)}%</CardTitle>
                                    </CardHeader>
                                </Card>
                            </div>
                        )}

                        {/* Results Table */}
                        <Card className="border-0 shadow-xl">
                            <CardHeader className="flex flex-row items-center justify-between">
                                <div>
                                    <CardTitle>Exam Results</CardTitle>
                                    <CardDescription>{results.length} results entered</CardDescription>
                                </div>
                                <Button
                                    onClick={handleAddResult}
                                    className="bg-gradient-to-r from-blue-600 to-indigo-600"
                                >
                                    <FiPlus className="w-4 h-4 mr-2" />
                                    Add Result
                                </Button>
                            </CardHeader>
                            <CardContent>
                                {results.length > 0 ? (
                                    <div className="overflow-x-auto">
                                        <table className="w-full">
                                            <thead className="bg-gray-50">
                                                <tr>
                                                    <th className="text-left p-3 font-semibold">#</th>
                                                    <th className="text-left p-3 font-semibold">Student</th>
                                                    <th className="text-center p-3 font-semibold">Marks</th>
                                                    <th className="text-center p-3 font-semibold">Percentage</th>
                                                    <th className="text-center p-3 font-semibold">Grade</th>
                                                    <th className="text-center p-3 font-semibold">Status</th>
                                                    <th className="text-left p-3 font-semibold">Remarks</th>
                                                    <th className="text-center p-3 font-semibold">Actions</th>
                                                </tr>
                                            </thead>
                                            <tbody className="divide-y">
                                                {results.map((result, idx) => (
                                                    <tr key={result.id} className="hover:bg-gray-50">
                                                        <td className="p-3">{idx + 1}</td>
                                                        <td className="p-3">
                                                            <div className="flex items-center gap-2">
                                                                <FiUser className="text-gray-400" />
                                                                <div>
                                                                    <p className="font-medium">
                                                                        {result.student?.user?.firstName} {result.student?.user?.lastName}
                                                                    </p>
                                                                    <p className="text-xs text-gray-500">{result.student?.studentId}</p>
                                                                </div>
                                                            </div>
                                                        </td>
                                                        <td className="p-3 text-center font-bold">
                                                            {result.marksObtained}/{selectedExam.totalMarks}
                                                        </td>
                                                        <td className="p-3 text-center">
                                                            {result.percentage?.toFixed(1)}%
                                                        </td>
                                                        <td className="p-3 text-center">
                                                            <span className={`inline-flex px-3 py-1 rounded-full text-sm font-bold border-2 ${getGradeColor(result.grade)}`}>
                                                                {result.grade}
                                                            </span>
                                                        </td>
                                                        <td className="p-3 text-center">
                                                            <div className="flex items-center justify-center gap-1">
                                                                {getStatusIcon(result.status)}
                                                                <span className={`font-semibold ${result.status === 'PASS' ? 'text-green-600' : 'text-red-600'}`}>
                                                                    {result.status}
                                                                </span>
                                                            </div>
                                                        </td>
                                                        <td className="p-3 text-sm text-gray-600">{result.remarks}</td>
                                                        <td className="p-3 text-center">
                                                            <Button
                                                                size="sm"
                                                                variant="outline"
                                                                onClick={() => handleEditResult(result)}
                                                                className="hover:bg-blue-50"
                                                            >
                                                                <FiEdit className="w-4 h-4" />
                                                            </Button>
                                                        </td>
                                                    </tr>
                                                ))}
                                            </tbody>
                                        </table>
                                    </div>
                                ) : (
                                    <div className="py-12 text-center text-gray-500">
                                        <FiAward className="w-16 h-16 mx-auto mb-4 text-gray-300" />
                                        <p className="text-lg">No results entered yet</p>
                                        <p className="text-sm">Click "Add Result" to start entering marks</p>
                                    </div>
                                )}
                            </CardContent>
                        </Card>

                        {/* Grade Distribution */}
                        {results.length > 0 && (
                            <Card className="mt-6 border-0 shadow-xl">
                                <CardHeader>
                                    <CardTitle className="flex items-center gap-2">
                                        <FiTrendingUp />
                                        Grade Distribution
                                    </CardTitle>
                                </CardHeader>
                                <CardContent>
                                    <div className="grid grid-cols-7 gap-3">
                                        {['A+', 'A', 'B+', 'B', 'C', 'D', 'F'].map(grade => {
                                            const count = results.filter(r => r.grade === grade).length;
                                            const percentage = results.length > 0 ? (count / results.length) * 100 : 0;
                                            return (
                                                <div key={grade} className="text-center">
                                                    <div className={`p-4 rounded-lg border-2 ${getGradeColor(grade)}`}>
                                                        <div className="text-2xl font-bold">{count}</div>
                                                        <div className="text-xs font-semibold">{grade}</div>
                                                    </div>
                                                    <div className="w-full h-2 bg-gray-200 rounded-full mt-2">
                                                        <div
                                                            className={`h-2 rounded-full ${grade.includes('A') ? 'bg-green-500' : grade.includes('B') ? 'bg-blue-500' : grade === 'C' ? 'bg-yellow-500' : grade === 'D' ? 'bg-orange-500' : 'bg-red-500'}`}
                                                            style={{ width: `${percentage}%` }}
                                                        ></div>
                                                    </div>
                                                    <div className="text-xs text-gray-500 mt-1">{percentage.toFixed(0)}%</div>
                                                </div>
                                            );
                                        })}
                                    </div>
                                </CardContent>
                            </Card>
                        )}
                    </>
                )}

                {/* Modal */}
                {showModal && (
                    <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
                        <Card className="w-full max-w-md">
                            <CardHeader>
                                <CardTitle>{selectedResult ? 'Edit Result' : 'Add Result'}</CardTitle>
                                <CardDescription>
                                    {selectedExam?.examName} (Total: {selectedExam?.totalMarks} marks)
                                </CardDescription>
                            </CardHeader>
                            <CardContent className="space-y-4">
                                {!selectedResult && (
                                    <div className="space-y-2">
                                        <Label>Student *</Label>
                                        <select
                                            value={formData.studentId}
                                            onChange={(e) => setFormData({ ...formData, studentId: e.target.value })}
                                            className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                                        >
                                            <option value="">Select Student</option>
                                            {students
                                                .filter(s => !results.some(r => r.student?.id === s.id))
                                                .map(student => (
                                                    <option key={student.id} value={student.id}>
                                                        {student.user?.firstName} {student.user?.lastName} ({student.studentId})
                                                    </option>
                                                ))
                                            }
                                        </select>
                                    </div>
                                )}

                                <div className="space-y-2">
                                    <Label>Marks Obtained *</Label>
                                    <Input
                                        type="number"
                                        min="0"
                                        max={selectedExam?.totalMarks}
                                        value={formData.marksObtained}
                                        onChange={(e) => setFormData({ ...formData, marksObtained: e.target.value })}
                                        placeholder={`Out of ${selectedExam?.totalMarks}`}
                                    />
                                    {formData.marksObtained && (
                                        <p className="text-sm text-gray-600">
                                            Percentage: {((formData.marksObtained / selectedExam?.totalMarks) * 100).toFixed(1)}%
                                        </p>
                                    )}
                                </div>

                                <div className="space-y-2">
                                    <Label>Remarks</Label>
                                    <textarea
                                        className="flex min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                                        value={formData.remarks}
                                        onChange={(e) => setFormData({ ...formData, remarks: e.target.value })}
                                        placeholder="Optional remarks..."
                                    />
                                </div>

                                <div className="flex justify-end gap-3 pt-4">
                                    <Button variant="outline" onClick={() => setShowModal(false)}>Cancel</Button>
                                    <Button
                                        onClick={handleSave}
                                        className="bg-gradient-to-r from-blue-600 to-indigo-600"
                                    >
                                        {selectedResult ? 'Update' : 'Save'} Result
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

export default ExamResultsPage;
