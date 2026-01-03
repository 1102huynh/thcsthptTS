import React, { useState, useEffect } from 'react';
import { FiUser, FiPlus, FiEdit, FiTrash2, FiSearch, FiFilter, FiDownload, FiPrinter } from 'react-icons/fi';
import api from '../services/api';
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/card';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import StudentVNForm from '../components/StudentVNForm';

function StudentVNManagement() {
    const [students, setStudents] = useState([]);
    const [classes, setClasses] = useState([]);
    const [gradeLevels, setGradeLevels] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [showForm, setShowForm] = useState(false);
    const [selectedStudent, setSelectedStudent] = useState(null);

    // Filters
    const [filters, setFilters] = useState({
        classId: '',
        gradeLevelId: '',
        status: '',
        searchName: ''
    });

    useEffect(() => {
        fetchData();
    }, []);

    const fetchData = async () => {
        try {
            setLoading(true);
            const [studentsRes, classesRes, gradeLevelsRes] = await Promise.all([
                api.get('/api/vn/students'),
                api.get('/api/classes'),
                api.get('/api/grade-levels')
            ]);
            setStudents(studentsRes.data || []);
            setClasses(classesRes.data || []);
            setGradeLevels(gradeLevelsRes.data || []);
        } catch (err) {
            setError('Failed to load data');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const handleAddNew = () => {
        setSelectedStudent(null);
        setShowForm(true);
    };

    const handleEdit = (student) => {
        setSelectedStudent(student);
        setShowForm(true);
    };

    const handleDelete = async (id) => {
        if (window.confirm('Are you sure you want to delete this student?')) {
            try {
                await api.delete(`/api/vn/students/${id}`);
                fetchData();
            } catch (err) {
                setError('Failed to delete student');
            }
        }
    };

    const handleFormClose = (success) => {
        setShowForm(false);
        setSelectedStudent(null);
        if (success) {
            fetchData();
        }
    };

    const handleSearch = () => {
        // Apply filters
        let filtered = students;

        if (filters.classId) {
            filtered = filtered.filter(s => s.schoolClass?.id === parseInt(filters.classId));
        }
        if (filters.gradeLevelId) {
            filtered = filtered.filter(s => s.gradeLevel?.id === parseInt(filters.gradeLevelId));
        }
        if (filters.status) {
            filtered = filtered.filter(s => s.status === filters.status);
        }
        if (filters.searchName) {
            const search = filters.searchName.toLowerCase();
            filtered = filtered.filter(s =>
                (s.firstName?.toLowerCase().includes(search)) ||
                (s.lastName?.toLowerCase().includes(search)) ||
                (s.studentCode?.toLowerCase().includes(search))
            );
        }

        return filtered;
    };

    const exportToExcel = () => {
        // TODO: Implement Excel export
        alert('Excel export feature is in development');
    };

    const printList = () => {
        window.print();
    };

    const filteredStudents = handleSearch();

    const getStatusBadge = (status) => {
        const colors = {
            'ACTIVE': 'bg-green-100 text-green-700',
            'ON_LEAVE': 'bg-yellow-100 text-yellow-700',
            'TRANSFERRED': 'bg-blue-100 text-blue-700',
            'DROPPED': 'bg-red-100 text-red-700',
            'GRADUATED': 'bg-purple-100 text-purple-700'
        };
        const labels = {
            'ACTIVE': 'Active',
            'ON_LEAVE': 'On Leave',
            'TRANSFERRED': 'Transferred',
            'DROPPED': 'Dropped',
            'GRADUATED': 'Graduated'
        };
        return (
            <span className={`px-2 py-1 rounded-full text-xs font-semibold ${colors[status] || 'bg-gray-100 text-gray-700'}`}>
                {labels[status] || status}
            </span>
        );
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-slate-50 via-violet-50 to-purple-50 p-6">
            <div className="max-w-7xl mx-auto">
                {/* Header */}
                <div className="mb-8 flex items-center justify-between">
                    <div>
                        <h1 className="text-3xl font-bold text-gray-900 mb-2 flex items-center gap-2">
                            Student Management
                        </h1>
                        <p className="text-gray-600">Vietnam Ministry of Education Standard</p>
                    </div>
                    <div className="flex gap-3">
                        <Button
                            variant="outline"
                            onClick={exportToExcel}
                            className="hover:bg-green-50"
                        >
                            <FiDownload className="w-4 h-4 mr-2" />
                            Export Excel
                        </Button>
                        <Button
                            variant="outline"
                            onClick={printList}
                            className="hover:bg-blue-50 no-print"
                        >
                            <FiPrinter className="w-4 h-4 mr-2" />
                            Print List
                        </Button>
                        <Button
                            onClick={handleAddNew}
                            className="bg-gradient-to-r from-violet-600 to-purple-600"
                        >
                            <FiPlus className="w-4 h-4 mr-2" />
                            Add Student
                        </Button>
                    </div>
                </div>

                {/* Filters */}
                <Card className="mb-6 border-0 shadow-xl no-print">
                    <CardHeader>
                        <CardTitle className="flex items-center gap-2">
                            <FiFilter />
                            Search Filters
                        </CardTitle>
                    </CardHeader>
                    <CardContent>
                        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                            <div>
                                <label className="block text-sm font-medium mb-2">Search</label>
                                <div className="relative">
                                    <FiSearch className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
                                    <Input
                                        placeholder="Name, student code..."
                                        value={filters.searchName}
                                        onChange={(e) => setFilters({ ...filters, searchName: e.target.value })}
                                        className="pl-10"
                                    />
                                </div>
                            </div>
                            <div>
                                <label className="block text-sm font-medium mb-2">Class</label>
                                <select
                                    value={filters.classId}
                                    onChange={(e) => setFilters({ ...filters, classId: e.target.value })}
                                    className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                                >
                                    <option value="">All</option>
                                    {classes.map(cls => (
                                        <option key={cls.id} value={cls.id}>{cls.className}</option>
                                    ))}
                                </select>
                            </div>
                            <div>
                                <label className="block text-sm font-medium mb-2">Grade</label>
                                <select
                                    value={filters.gradeLevelId}
                                    onChange={(e) => setFilters({ ...filters, gradeLevelId: e.target.value })}
                                    className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                                >
                                    <option value="">All</option>
                                    {gradeLevels.map(gl => (
                                        <option key={gl.id} value={gl.id}>Grade {gl.levelName}</option>
                                    ))}
                                </select>
                            </div>
                            <div>
                                <label className="block text-sm font-medium mb-2">Status</label>
                                <select
                                    value={filters.status}
                                    onChange={(e) => setFilters({ ...filters, status: e.target.value })}
                                    className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                                >
                                    <option value="">All</option>
                                    <option value="ACTIVE">Active</option>
                                    <option value="ON_LEAVE">On Leave</option>
                                    <option value="TRANSFERRED">Transferred</option>
                                    <option value="DROPPED">Dropped</option>
                                    <option value="GRADUATED">Graduated</option>
                                </select>
                            </div>
                        </div>
                        <div className="mt-4 flex items-center gap-3 text-sm text-gray-600">
                            <span className="font-semibold">Results:</span>
                            <span>{filteredStudents.length} students</span>
                            {filters.searchName || filters.classId || filters.gradeLevelId || filters.status ? (
                                <Button
                                    variant="outline"
                                    size="sm"
                                    onClick={() => setFilters({ classId: '', gradeLevelId: '', status: '', searchName: '' })}
                                >
                                    Clear Filters
                                </Button>
                            ) : null}
                        </div>
                    </CardContent>
                </Card>

                {/* Error Alert */}
                {error && (
                    <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg no-print">
                        <p className="text-sm text-red-800">{error}</p>
                    </div>
                )}

                {/* Student List */}
                {loading ? (
                    <div className="flex justify-center items-center py-12">
                        <div className="w-12 h-12 border-4 border-violet-600 border-t-transparent rounded-full animate-spin"></div>
                    </div>
                ) : (
                    <Card className="border-0 shadow-xl">
                        <CardContent className="pt-6">
                            <div className="overflow-x-auto">
                                <table className="w-full">
                                    <thead className="bg-gradient-to-r from-violet-50 to-purple-50">
                                        <tr>
                                            <th className="text-left p-3 font-semibold">Student ID</th>
                                            <th className="text-left p-3 font-semibold">Full Name</th>
                                            <th className="text-left p-3 font-semibold">Date of Birth</th>
                                            <th className="text-left p-3 font-semibold">Gender</th>
                                            <th className="text-left p-3 font-semibold">Class</th>
                                            <th className="text-left p-3 font-semibold">Status</th>
                                            <th className="text-center p-3 font-semibold no-print">Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody className="divide-y">
                                        {filteredStudents.length > 0 ? (
                                            filteredStudents.map((student) => (
                                                <tr key={student.id} className="hover:bg-violet-50">
                                                    <td className="p-3 font-mono text-sm">{student.studentCode}</td>
                                                    <td className="p-3">
                                                        <div className="flex items-center gap-2">
                                                            <FiUser className="text-violet-500" />
                                                            <span className="font-medium">
                                                                {student.lastName} {student.firstName}
                                                            </span>
                                                        </div>
                                                    </td>
                                                    <td className="p-3 text-sm">
                                                        {student.dateOfBirth ? new Date(student.dateOfBirth).toLocaleDateString('vi-VN') : '-'}
                                                    </td>
                                                    <td className="p-3 text-sm">{student.gender}</td>
                                                    <td className="p-3 text-sm">{student.schoolClass?.className || '-'}</td>
                                                    <td className="p-3">{getStatusBadge(student.status)}</td>
                                                    <td className="p-3 text-center no-print">
                                                        <div className="flex gap-2 justify-center">
                                                            <Button
                                                                size="sm"
                                                                variant="outline"
                                                                onClick={() => handleEdit(student)}
                                                                className="hover:bg-blue-50"
                                                            >
                                                                <FiEdit className="w-4 h-4" />
                                                            </Button>
                                                            <Button
                                                                size="sm"
                                                                variant="outline"
                                                                onClick={() => handleDelete(student.id)}
                                                                className="hover:bg-red-50 hover:text-red-700"
                                                            >
                                                                <FiTrash2 className="w-4 h-4" />
                                                            </Button>
                                                        </div>
                                                    </td>
                                                </tr>
                                            ))
                                        ) : (
                                            <tr>
                                                <td colSpan="7" className="p-12 text-center text-gray-500">
                                                    <FiUser className="w-16 h-16 mx-auto mb-4 text-gray-300" />
                                                    <p className="text-lg">No students found</p>
                                                    <p className="text-sm">Add new student or change filters</p>
                                                </td>
                                            </tr>
                                        )}
                                    </tbody>
                                </table>
                            </div>
                        </CardContent>
                    </Card>
                )}

                {/* Form Modal */}
                {showForm && (
                    <StudentVNForm
                        student={selectedStudent}
                        onClose={handleFormClose}
                    />
                )}
            </div>

            {/* Print Styles */}
            <style jsx>{`
                @media print {
                    .no-print {
                        display: none !important;
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

export default StudentVNManagement;
