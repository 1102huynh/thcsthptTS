import React, { useState, useEffect } from 'react';
import { FiCalendar, FiPlus, FiEdit, FiTrash2, FiCheck, FiClock, FiArrowRight } from 'react-icons/fi';
import api from '../services/api';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';

function AcademicYearPage() {
    const [academicYears, setAcademicYears] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [showModal, setShowModal] = useState(false);
    const [selectedYear, setSelectedYear] = useState(null);
    const [formData, setFormData] = useState({
        yearName: '',
        startDate: '',
        endDate: '',
        semester1Start: '',
        semester1End: '',
        semester2Start: '',
        semester2End: '',
        isActive: true,
        isCurrent: false,
        description: ''
    });

    useEffect(() => {
        fetchAcademicYears();
    }, []);

    const fetchAcademicYears = async () => {
        try {
            setLoading(true);
            const response = await api.get('/api/academic-years');
            setAcademicYears(response.data || []);
        } catch (err) {
            setError('Failed to load academic years');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const handleAddNew = () => {
        setSelectedYear(null);
        setFormData({
            yearName: '',
            startDate: '',
            endDate: '',
            semester1Start: '',
            semester1End: '',
            semester2Start: '',
            semester2End: '',
            isActive: true,
            isCurrent: false,
            description: ''
        });
        setShowModal(true);
    };

    const handleEdit = (year) => {
        setSelectedYear(year);
        setFormData({
            yearName: year.yearName,
            startDate: year.startDate,
            endDate: year.endDate,
            semester1Start: year.semester1Start || '',
            semester1End: year.semester1End || '',
            semester2Start: year.semester2Start || '',
            semester2End: year.semester2End || '',
            isActive: year.isActive,
            isCurrent: year.isCurrent,
            description: year.description || ''
        });
        setShowModal(true);
    };

    const handleSave = async () => {
        try {
            if (selectedYear) {
                await api.put(`/api/academic-years/${selectedYear.id}`, formData);
            } else {
                await api.post('/api/academic-years', formData);
            }
            setShowModal(false);
            fetchAcademicYears();
            setError('');
        } catch (err) {
            setError('Failed to save: ' + (err.response?.data?.message || err.message));
        }
    };

    const handleDelete = async (id) => {
        if (window.confirm('Are you sure you want to delete this academic year?')) {
            try {
                await api.delete(`/api/academic-years/${id}`);
                fetchAcademicYears();
            } catch (err) {
                setError('Failed to delete: ' + (err.response?.data?.message || err.message));
            }
        }
    };

    const handleSetCurrent = async (id) => {
        if (window.confirm('Set this as the current academic year?')) {
            try {
                await api.put(`/api/academic-years/${id}/set-current`);
                fetchAcademicYears();
                setError('');
            } catch (err) {
                setError('Failed to set current year: ' + (err.response?.data?.message || err.message));
            }
        }
    };

    const formatDate = (dateString) => {
        if (!dateString) return 'Not set';
        return new Date(dateString).toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        });
    };

    const currentYear = academicYears.find(y => y.isCurrent);
    const otherYears = academicYears.filter(y => !y.isCurrent);

    return (
        <div className="min-h-screen bg-gradient-to-br from-slate-50 via-indigo-50 to-purple-50 p-6">
            <div className="max-w-7xl mx-auto">
                {/* Header */}
                <div className="mb-8 flex items-center justify-between">
                    <div>
                        <h1 className="text-3xl font-bold text-gray-900 mb-2 flex items-center gap-2">
                            <FiCalendar className="text-indigo-600" />
                            Academic Year Management
                        </h1>
                        <p className="text-gray-600">Manage school years, semesters, and transitions</p>
                    </div>
                    <Button
                        onClick={handleAddNew}
                        className="bg-gradient-to-r from-indigo-600 to-purple-600"
                    >
                        <FiPlus className="w-4 h-4 mr-2" />
                        Add Academic Year
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
                        <div className="w-12 h-12 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin"></div>
                    </div>
                ) : (
                    <div className="space-y-6">
                        {/* Current Academic Year */}
                        {currentYear && (
                            <Card className="border-2 border-indigo-500 shadow-xl">
                                <CardHeader className="bg-gradient-to-r from-indigo-50 to-purple-50">
                                    <div className="flex items-center justify-between">
                                        <div>
                                            <CardTitle className="flex items-center gap-2">
                                                <FiCheck className="text-green-600" />
                                                Current Academic Year
                                            </CardTitle>
                                            <CardDescription className="text-lg font-bold text-indigo-900 mt-1">
                                                {currentYear.yearName}
                                            </CardDescription>
                                        </div>
                                        <span className="px-4 py-2 bg-green-100 text-green-700 rounded-full text-sm font-semibold">
                                            Active
                                        </span>
                                    </div>
                                </CardHeader>
                                <CardContent className="pt-6">
                                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                                        {/* Year Period */}
                                        <div>
                                            <h4 className="text-sm font-semibold text-gray-700 mb-2">Academic Year Period</h4>
                                            <div className="flex items-center gap-2">
                                                <FiCalendar className="text-gray-400" />
                                                <span className="text-sm">{formatDate(currentYear.startDate)}</span>
                                                <FiArrowRight className="text-gray-400" />
                                                <span className="text-sm">{formatDate(currentYear.endDate)}</span>
                                            </div>
                                        </div>

                                        {/* Semester 1 */}
                                        <div>
                                            <h4 className="text-sm font-semibold text-gray-700 mb-2">Semester 1</h4>
                                            <div className="flex items-center gap-2">
                                                <FiClock className="text-blue-500" />
                                                <span className="text-sm">{formatDate(currentYear.semester1Start)}</span>
                                                <FiArrowRight className="text-gray-400" />
                                                <span className="text-sm">{formatDate(currentYear.semester1End)}</span>
                                            </div>
                                        </div>

                                        {/* Semester 2 */}
                                        <div>
                                            <h4 className="text-sm font-semibold text-gray-700 mb-2">Semester 2</h4>
                                            <div className="flex items-center gap-2">
                                                <FiClock className="text-purple-500" />
                                                <span className="text-sm">{formatDate(currentYear.semester2Start)}</span>
                                                <FiArrowRight className="text-gray-400" />
                                                <span className="text-sm">{formatDate(currentYear.semester2End)}</span>
                                            </div>
                                        </div>
                                    </div>

                                    {currentYear.description && (
                                        <div className="mt-4 p-3 bg-indigo-50 rounded-lg">
                                            <p className="text-sm text-gray-700">{currentYear.description}</p>
                                        </div>
                                    )}

                                    <div className="mt-4 flex gap-3">
                                        <Button
                                            size="sm"
                                            variant="outline"
                                            onClick={() => handleEdit(currentYear)}
                                            className="hover:bg-blue-50"
                                        >
                                            <FiEdit className="w-4 h-4 mr-2" />
                                            Edit
                                        </Button>
                                    </div>
                                </CardContent>
                            </Card>
                        )}

                        {/* Other Academic Years */}
                        {otherYears.length > 0 && (
                            <Card className="shadow-xl">
                                <CardHeader className="bg-gradient-to-r from-gray-50 to-slate-50">
                                    <CardTitle>Academic Year History</CardTitle>
                                    <CardDescription>{otherYears.length} previous/upcoming years</CardDescription>
                                </CardHeader>
                                <CardContent className="pt-6">
                                    <div className="space-y-4">
                                        {otherYears.map((year) => (
                                            <div key={year.id} className="border rounded-lg p-4 hover:bg-gray-50 transition-colors">
                                                <div className="flex items-center justify-between">
                                                    <div className="flex-1">
                                                        <div className="flex items-center gap-3 mb-2">
                                                            <h3 className="font-bold text-lg">{year.yearName}</h3>
                                                            <span className={`px-3 py-1 rounded-full text-xs font-semibold ${year.isActive
                                                                    ? 'bg-blue-100 text-blue-700'
                                                                    : 'bg-gray-100 text-gray-600'
                                                                }`}>
                                                                {year.isActive ? 'Active' : 'Inactive'}
                                                            </span>
                                                        </div>
                                                        <div className="grid grid-cols-1 md:grid-cols-3 gap-3 text-sm text-gray-600">
                                                            <div>
                                                                <span className="font-medium">Year:</span> {formatDate(year.startDate)} - {formatDate(year.endDate)}
                                                            </div>
                                                            <div>
                                                                <span className="font-medium">Sem 1:</span> {formatDate(year.semester1Start)} - {formatDate(year.semester1End)}
                                                            </div>
                                                            <div>
                                                                <span className="font-medium">Sem 2:</span> {formatDate(year.semester2Start)} - {formatDate(year.semester2End)}
                                                            </div>
                                                        </div>
                                                        {year.description && (
                                                            <p className="text-sm text-gray-500 mt-2">{year.description}</p>
                                                        )}
                                                    </div>
                                                    <div className="flex gap-2 ml-4">
                                                        <Button
                                                            size="sm"
                                                            variant="outline"
                                                            onClick={() => handleSetCurrent(year.id)}
                                                            className="hover:bg-green-50 hover:text-green-700"
                                                        >
                                                            <FiCheck className="w-4 h-4 mr-1" />
                                                            Set Current
                                                        </Button>
                                                        <Button
                                                            size="sm"
                                                            variant="outline"
                                                            onClick={() => handleEdit(year)}
                                                            className="hover:bg-blue-50"
                                                        >
                                                            <FiEdit className="w-4 h-4" />
                                                        </Button>
                                                        <Button
                                                            size="sm"
                                                            variant="outline"
                                                            onClick={() => handleDelete(year.id)}
                                                            className="hover:bg-red-50 hover:text-red-700"
                                                        >
                                                            <FiTrash2 className="w-4 h-4" />
                                                        </Button>
                                                    </div>
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                </CardContent>
                            </Card>
                        )}

                        {!currentYear && academicYears.length === 0 && (
                            <Card className="shadow-xl">
                                <CardContent className="py-12 text-center text-gray-500">
                                    <FiCalendar className="w-16 h-16 mx-auto mb-4 text-gray-300" />
                                    <p className="text-lg mb-2">No academic years configured</p>
                                    <p className="text-sm">Click "Add Academic Year" to create one</p>
                                </CardContent>
                            </Card>
                        )}
                    </div>
                )}

                {/* Modal */}
                {showModal && (
                    <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
                        <Card className="w-full max-w-2xl max-h-[90vh] overflow-y-auto">
                            <CardHeader>
                                <CardTitle>{selectedYear ? 'Edit Academic Year' : 'Add Academic Year'}</CardTitle>
                                <CardDescription>Configure year and semester dates</CardDescription>
                            </CardHeader>
                            <CardContent className="space-y-4">
                                <div className="space-y-2">
                                    <Label>Year Name *</Label>
                                    <Input
                                        placeholder="e.g., 2024-2025"
                                        value={formData.yearName}
                                        onChange={(e) => setFormData({ ...formData, yearName: e.target.value })}
                                    />
                                </div>

                                <div className="grid grid-cols-2 gap-4">
                                    <div className="space-y-2">
                                        <Label>Start Date *</Label>
                                        <Input
                                            type="date"
                                            value={formData.startDate}
                                            onChange={(e) => setFormData({ ...formData, startDate: e.target.value })}
                                        />
                                    </div>
                                    <div className="space-y-2">
                                        <Label>End Date *</Label>
                                        <Input
                                            type="date"
                                            value={formData.endDate}
                                            onChange={(e) => setFormData({ ...formData, endDate: e.target.value })}
                                        />
                                    </div>
                                </div>

                                <div className="border-t pt-4">
                                    <h4 className="font-semibold mb-3">Semester 1</h4>
                                    <div className="grid grid-cols-2 gap-4">
                                        <div className="space-y-2">
                                            <Label>Start Date</Label>
                                            <Input
                                                type="date"
                                                value={formData.semester1Start}
                                                onChange={(e) => setFormData({ ...formData, semester1Start: e.target.value })}
                                            />
                                        </div>
                                        <div className="space-y-2">
                                            <Label>End Date</Label>
                                            <Input
                                                type="date"
                                                value={formData.semester1End}
                                                onChange={(e) => setFormData({ ...formData, semester1End: e.target.value })}
                                            />
                                        </div>
                                    </div>
                                </div>

                                <div className="border-t pt-4">
                                    <h4 className="font-semibold mb-3">Semester 2</h4>
                                    <div className="grid grid-cols-2 gap-4">
                                        <div className="space-y-2">
                                            <Label>Start Date</Label>
                                            <Input
                                                type="date"
                                                value={formData.semester2Start}
                                                onChange={(e) => setFormData({ ...formData, semester2Start: e.target.value })}
                                            />
                                        </div>
                                        <div className="space-y-2">
                                            <Label>End Date</Label>
                                            <Input
                                                type="date"
                                                value={formData.semester2End}
                                                onChange={(e) => setFormData({ ...formData, semester2End: e.target.value })}
                                            />
                                        </div>
                                    </div>
                                </div>

                                <div className="space-y-2">
                                    <Label>Description</Label>
                                    <textarea
                                        className="flex min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                                        placeholder="Optional description or notes"
                                        value={formData.description}
                                        onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                                    />
                                </div>

                                <div className="flex items-center gap-4">
                                    <label className="flex items-center gap-2">
                                        <input
                                            type="checkbox"
                                            checked={formData.isActive}
                                            onChange={(e) => setFormData({ ...formData, isActive: e.target.checked })}
                                            className="w-4 h-4 rounded border-gray-300"
                                        />
                                        <span className="text-sm">Active</span>
                                    </label>
                                    <label className="flex items-center gap-2">
                                        <input
                                            type="checkbox"
                                            checked={formData.isCurrent}
                                            onChange={(e) => setFormData({ ...formData, isCurrent: e.target.checked })}
                                            className="w-4 h-4 rounded border-gray-300"
                                        />
                                        <span className="text-sm">Set as Current Year</span>
                                    </label>
                                </div>

                                <div className="flex justify-end gap-3 pt-4">
                                    <Button variant="outline" onClick={() => setShowModal(false)}>Cancel</Button>
                                    <Button
                                        onClick={handleSave}
                                        className="bg-gradient-to-r from-indigo-600 to-purple-600"
                                    >
                                        <FiPlus className="w-4 h-4 mr-2" />
                                        {selectedYear ? 'Update' : 'Create'}
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

export default AcademicYearPage;
