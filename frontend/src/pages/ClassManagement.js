import React, { useState, useEffect } from 'react';
import { FiPlus, FiEdit, FiTrash2, FiUsers, FiBook } from 'react-icons/fi';
import api from '../services/api';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';

function ClassManagement() {
    const [classes, setClasses] = useState([]);
    const [gradeLevels, setGradeLevels] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [showModal, setShowModal] = useState(false);
    const [selectedClass, setSelectedClass] = useState(null);
    const [formData, setFormData] = useState({
        className: '',
        fullName: '',
        gradeLevelId: '',
        academicYear: '2024-2025',
        maxStudents: 40,
        roomNumber: '',
        status: 'ACTIVE'
    });

    useEffect(() => {
        fetchData();
    }, []);

    const fetchData = async () => {
        try {
            setLoading(true);
            const [classesRes, gradeLevelsRes] = await Promise.all([
                api.get('/api/classes'),
                api.get('/api/grade-levels/current')
            ]);
            setClasses(classesRes.data || []);
            setGradeLevels(gradeLevelsRes.data || []);
            setError(''); // Clear any previous errors
        } catch (err) {
            console.error('Fetch error:', err);
            const errorMsg = err.response?.data?.message
                || err.response?.statusText
                || err.message
                || 'Unknown error occurred';
            setError(`Failed to load data: ${errorMsg} (Status: ${err.response?.status || 'Network Error'})`);
        } finally {
            setLoading(false);
        }
    };

    const handleAddNew = () => {
        setSelectedClass(null);
        setFormData({
            className: '',
            fullName: '',
            gradeLevelId: '',
            academicYear: '2024-2025',
            maxStudents: 40,
            roomNumber: '',
            status: 'ACTIVE'
        });
        setShowModal(true);
    };

    const handleEdit = (classItem) => {
        setSelectedClass(classItem);
        setFormData({
            className: classItem.className || '',
            fullName: classItem.fullName || '',
            gradeLevelId: classItem.gradeLevel?.id || '',
            academicYear: classItem.academicYear || '2024-2025',
            maxStudents: classItem.maxStudents || 40,
            roomNumber: classItem.roomNumber || '',
            status: classItem.status || 'ACTIVE'
        });
        setShowModal(true);
    };

    const handleSave = async () => {
        try {
            if (!formData.className || !formData.gradeLevelId) {
                setError('Please fill in all required fields');
                return;
            }

            const payload = {
                className: formData.className,
                fullName: formData.fullName || `Class ${formData.className}`,
                gradeLevel: { id: parseInt(formData.gradeLevelId) },
                academicYear: formData.academicYear,
                maxStudents: parseInt(formData.maxStudents),
                currentStudents: 0,
                roomNumber: formData.roomNumber,
                status: formData.status
            };

            if (selectedClass) {
                await api.put(`/api/classes/${selectedClass.id}`, payload);
            } else {
                await api.post('/api/classes', payload);
            }

            setShowModal(false);
            setError('');
            fetchData();
        } catch (err) {
            setError('Failed to save class: ' + (err.response?.data?.message || err.message));
        }
    };

    const handleDelete = async (id) => {
        if (window.confirm('Are you sure you want to delete this class?')) {
            try {
                await api.delete(`/api/classes/${id}`);
                fetchData();
            } catch (err) {
                setError('Failed to delete class: ' + (err.response?.data?.message || err.message));
            }
        }
    };

    // Group classes by grade level
    const groupedClasses = gradeLevels.map(gradeLevel => ({
        gradeLevel,
        classes: classes.filter(c => c.gradeLevel?.id === gradeLevel.id)
    }));

    return (
        <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50 p-6">
            <div className="max-w-7xl mx-auto">
                {/* Header */}
                <div className="mb-8">
                    <div className="flex items-center justify-between">
                        <div>
                            <h1 className="text-3xl font-bold text-gray-900 mb-2 flex items-center gap-2">
                                <FiBook className="text-blue-600" />
                                Class Management
                            </h1>
                            <p className="text-gray-600">Manage school classes and student capacity</p>
                        </div>
                        <Button onClick={handleAddNew} className="bg-gradient-to-r from-blue-600 to-indigo-600">
                            <FiPlus className="w-4 h-4 mr-2" />
                            Add Class
                        </Button>
                    </div>
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
                        <div className="w-12 h-12 border-4 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
                    </div>
                ) : (
                    <>
                        {/* Grade Level Groups */}
                        {groupedClasses.map(({ gradeLevel, classes: levelClasses }) => (
                            <Card key={gradeLevel.id} className="mb-6 border-0 shadow-xl">
                                <CardHeader className="bg-gradient-to-r from-blue-50 to-indigo-50">
                                    <CardTitle>{gradeLevel.levelName} ({gradeLevel.schoolType})</CardTitle>
                                    <CardDescription>{levelClasses.length} classes</CardDescription>
                                </CardHeader>
                                <CardContent className="pt-6">
                                    {levelClasses.length > 0 ? (
                                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                                            {levelClasses.map((classItem) => (
                                                <Card key={classItem.id} className="hover:shadow-lg transition-shadow">
                                                    <CardContent className="pt-6">
                                                        <div className="flex justify-between items-start mb-4">
                                                            <div>
                                                                <h3 className="text-xl font-bold text-gray-900">{classItem.className}</h3>
                                                                <p className="text-sm text-gray-600">{classItem.roomNumber || 'No room'}</p>
                                                            </div>
                                                            <div className="flex gap-2">
                                                                <Button size="sm" variant="outline" onClick={() => handleEdit(classItem)}>
                                                                    <FiEdit className="w-4 h-4" />
                                                                </Button>
                                                                <Button size="sm" variant="outline" onClick={() => handleDelete(classItem.id)} className="hover:bg-red-50">
                                                                    <FiTrash2 className="w-4 h-4 text-red-600" />
                                                                </Button>
                                                            </div>
                                                        </div>

                                                        <div className="space-y-2">
                                                            <div className="flex items-center gap-2 text-sm">
                                                                <FiUsers className="text-blue-600" />
                                                                <span>{classItem.currentStudents || 0} / {classItem.maxStudents} students</span>
                                                            </div>

                                                            {/* Progress bar */}
                                                            <div className="w-full bg-gray-200 rounded-full h-2">
                                                                <div
                                                                    className="bg-blue-600 h-2 rounded-full transition-all"
                                                                    style={{ width: `${((classItem.currentStudents || 0) / classItem.maxStudents) * 100}%` }}
                                                                ></div>
                                                            </div>

                                                            <div className="flex justify-between items-center pt-2">
                                                                <span className={`px-2 py-1 rounded text-xs font-semibold ${classItem.status === 'ACTIVE' ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-700'
                                                                    }`}>
                                                                    {classItem.status}
                                                                </span>
                                                                <span className="text-xs text-gray-500">
                                                                    {Math.round(((classItem.currentStudents || 0) / classItem.maxStudents) * 100)}% full
                                                                </span>
                                                            </div>
                                                        </div>
                                                    </CardContent>
                                                </Card>
                                            ))}
                                        </div>
                                    ) : (
                                        <p className="text-center text-gray-500 py-8">No classes in this grade level</p>
                                    )}
                                </CardContent>
                            </Card>
                        ))}
                    </>
                )}

                {/* Modal */}
                {showModal && (
                    <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
                        <Card className="w-full max-w-2xl">
                            <CardHeader>
                                <CardTitle>{selectedClass ? 'Edit Class' : 'Add New Class'}</CardTitle>
                                <CardDescription>Fill in the class details</CardDescription>
                            </CardHeader>
                            <CardContent className="space-y-4">
                                <div className="grid grid-cols-2 gap-4">
                                    <div className="space-y-2">
                                        <Label htmlFor="className">Class Name *</Label>
                                        <Input
                                            id="className"
                                            value={formData.className}
                                            onChange={(e) => setFormData({ ...formData, className: e.target.value })}
                                            placeholder="6A, 10A1, etc."
                                        />
                                    </div>
                                    <div className="space-y-2">
                                        <Label htmlFor="gradeLevelId">Grade Level *</Label>
                                        <select
                                            id="gradeLevelId"
                                            value={formData.gradeLevelId}
                                            onChange={(e) => setFormData({ ...formData, gradeLevelId: e.target.value })}
                                            className="flex h-10 w-full rounded-md border border-input bg-back ground px-3 py-2 text-sm"
                                        >
                                            <option value="">Select Grade Level</option>
                                            {gradeLevels.map(gl => (
                                                <option key={gl.id} value={gl.id}>{gl.levelName}</option>
                                            ))}
                                        </select>
                                    </div>
                                </div>

                                <div className="grid grid-cols-2 gap-4">
                                    <div className="space-y-2">
                                        <Label htmlFor="roomNumber">Room Number</Label>
                                        <Input
                                            id="roomNumber"
                                            value={formData.roomNumber}
                                            onChange={(e) => setFormData({ ...formData, roomNumber: e.target.value })}
                                            placeholder="A101, B205"
                                        />
                                    </div>
                                    <div className="space-y-2">
                                        <Label htmlFor="maxStudents">Max Students</Label>
                                        <Input
                                            id="maxStudents"
                                            type="number"
                                            value={formData.maxStudents}
                                            onChange={(e) => setFormData({ ...formData, maxStudents: e.target.value })}
                                        />
                                    </div>
                                </div>

                                <div className="flex justify-end gap-3 pt-4">
                                    <Button variant="outline" onClick={() => setShowModal(false)}>Cancel</Button>
                                    <Button onClick={handleSave} className="bg-gradient-to-r from-blue-600 to-indigo-600">
                                        <FiPlus className="w-4 h-4 mr-2" />
                                        {selectedClass ? 'Update' : 'Save'}
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

export default ClassManagement;
