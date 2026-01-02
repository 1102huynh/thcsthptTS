import React, { useState, useEffect } from 'react';
import { FiPlus, FiEdit, FiTrash2, FiBookOpen, FiAward } from 'react-icons/fi';
import api from '../services/api';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';

function SubjectManagement() {
    const [subjects, setSubjects] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [showModal, setShowModal] = useState(false);
    const [selectedSubject, setSelectedSubject] = useState(null);
    const [filterType, setFilterType] = useState('ALL');
    const [formData, setFormData] = useState({
        subjectCode: '',
        subjectName: '',
        subjectNameEn: '',
        schoolType: 'BOTH',
        category: '',
        totalPeriodsPerWeek: 0,
        coefficient: 1.0,
        isRequired: true,
        status: 'ACTIVE'
    });

    useEffect(() => {
        fetchSubjects();
    }, []);

    const fetchSubjects = async () => {
        try {
            setLoading(true);
            const response = await api.get('/api/subjects');
            setSubjects(response.data || []);
        } catch (err) {
            setError('Failed to load subjects: ' + err.message);
        } finally {
            setLoading(false);
        }
    };

    const handleAddNew = () => {
        setSelectedSubject(null);
        setFormData({
            subjectCode: '',
            subjectName: '',
            subjectNameEn: '',
            schoolType: 'BOTH',
            category: '',
            totalPeriodsPerWeek: 0,
            coefficient: 1.0,
            isRequired: true,
            status: 'ACTIVE'
        });
        setShowModal(true);
    };

    const handleEdit = (subject) => {
        setSelectedSubject(subject);
        setFormData({
            subjectCode: subject.subjectCode || '',
            subjectName: subject.subjectName || '',
            subjectNameEn: subject.subjectNameEn || '',
            schoolType: subject.schoolType || 'BOTH',
            category: subject.category || '',
            totalPeriodsPerWeek: subject.totalPeriodsPerWeek || 0,
            coefficient: subject.coefficient || 1.0,
            isRequired: subject.isRequired !== undefined ? subject.isRequired : true,
            status: subject.status || 'ACTIVE'
        });
        setShowModal(true);
    };

    const handleSave = async () => {
        try {
            if (!formData.subjectCode || !formData.subjectName) {
                setError('Please fill in all required fields');
                return;
            }

            const payload = {
                subjectCode: formData.subjectCode.toUpperCase(),
                subjectName: formData.subjectName,
                subjectNameEn: formData.subjectNameEn,
                schoolType: formData.schoolType,
                category: formData.category,
                totalPeriodsPerWeek: parseInt(formData.totalPeriodsPerWeek),
                coefficient: parseFloat(formData.coefficient),
                isRequired: formData.isRequired,
                status: formData.status
            };

            if (selectedSubject) {
                await api.put(`/api/subjects/${selectedSubject.id}`, payload);
            } else {
                await api.post('/api/subjects', payload);
            }

            setShowModal(false);
            setError('');
            fetchSubjects();
        } catch (err) {
            setError('Failed to save subject: ' + (err.response?.data?.message || err.message));
        }
    };

    const handleDelete = async (id) => {
        if (window.confirm('Are you sure you want to delete this subject?')) {
            try {
                await api.delete(`/api/subjects/${id}`);
                fetchSubjects();
            } catch (err) {
                setError('Failed to delete subject: ' + (err.response?.data?.message || err.message));
            }
        }
    };

    // Filter subjects
    const filteredSubjects = subjects.filter(subject => {
        if (filterType === 'ALL') return true;
        if (filterType === 'THCS') return subject.schoolType === 'THCS' || subject.schoolType === 'BOTH';
        if (filterType === 'THPT') return subject.schoolType === 'THPT' || subject.schoolType === 'BOTH';
        return true;
    });

    // Group by category
    const categories = [...new Set(filteredSubjects.map(s => s.category || 'Other'))];

    return (
        <div className="min-h-screen bg-gradient-to-br from-slate-50 via-purple-50 to-pink-50 p-6">
            <div className="max-w-7xl mx-auto">
                {/* Header */}
                <div className="mb-8">
                    <div className="flex items-center justify-between">
                        <div>
                            <h1 className="text-3xl font-bold text-gray-900 mb-2 flex items-center gap-2">
                                <FiBookOpen className="text-purple-600" />
                                Subject Management
                            </h1>
                            <p className="text-gray-600">Manage school subjects and curriculum</p>
                        </div>
                        <Button onClick={handleAddNew} className="bg-gradient-to-r from-purple-600 to-pink-600">
                            <FiPlus className="w-4 h-4 mr-2" />
                            Add Subject
                        </Button>
                    </div>
                </div>

                {/* Filter Tabs */}
                <div className="mb-6 flex gap-2">
                    {['ALL', 'THCS', 'THPT'].map(type => (
                        <Button
                            key={type}
                            onClick={() => setFilterType(type)}
                            variant={filterType === type ? 'default' : 'outline'}
                            className={filterType === type ? 'bg-gradient-to-r from-purple-600 to-pink-600' : ''}
                        >
                            {type === 'ALL' ? 'All Subjects' : type === 'THCS' ? 'Middle School (THCS)' : 'High School (THPT)'}
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
                        <div className="w-12 h-12 border-4 border-purple-600 border-t-transparent rounded-full animate-spin"></div>
                    </div>
                ) : (
                    <>
                        {/* Subjects Grid by Category */}
                        {categories.map(category => {
                            const categorySubjects = filteredSubjects.filter(s => (s.category || 'Other') === category);
                            if (categorySubjects.length === 0) return null;

                            return (
                                <Card key={category} className="mb-6 border-0 shadow-xl">
                                    <CardHeader className="bg-gradient-to-r from-purple-50 to-pink-50">
                                        <CardTitle>{category}</CardTitle>
                                        <CardDescription>{categorySubjects.length} subjects</CardDescription>
                                    </CardHeader>
                                    <CardContent className="pt-6">
                                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                                            {categorySubjects.map((subject) => (
                                                <Card key={subject.id} className="hover:shadow-lg transition-shadow">
                                                    <CardContent className="pt-6">
                                                        <div className="flex justify-between items-start mb-3">
                                                            <div>
                                                                <h3 className="text-lg font-bold text-gray-900">{subject.subjectName}</h3>
                                                                <p className="text-sm text-gray-600">{subject.subjectCode}</p>
                                                            </div>
                                                            <div className="flex gap-2">
                                                                <Button size="sm" variant="outline" onClick={() => handleEdit(subject)}>
                                                                    <FiEdit className="w-4 h-4" />
                                                                </Button>
                                                                <Button size="sm" variant="outline" onClick={() => handleDelete(subject.id)} className="hover:bg-red-50">
                                                                    <FiTrash2 className="w-4 h-4 text-red-600" />
                                                                </Button>
                                                            </div>
                                                        </div>

                                                        <div className="space-y-2">
                                                            <div className="flex items-center gap-2 text-sm">
                                                                <FiAward className={subject.coefficient > 1 ? 'text-yellow-600' : 'text-gray-600'} />
                                                                <span>Coefficient: {subject.coefficient}</span>
                                                            </div>

                                                            <div className="flex items-center gap-2 text-sm text-gray-600">
                                                                <span>{subject.totalPeriodsPerWeek} periods/week</span>
                                                            </div>

                                                            <div className="flex justify-between items-center pt-2">
                                                                <span className={`px-2 py-1 rounded text-xs font-semibold ${subject.schoolType === 'BOTH' ? 'bg-blue-100 text-blue-700' :
                                                                    subject.schoolType === 'THCS' ? 'bg-green-100 text-green-700' :
                                                                        'bg-purple-100 text-purple-700'
                                                                    }`}>
                                                                    {subject.schoolType}
                                                                </span>
                                                                <span className={`px-2 py-1 rounded text-xs font-semibold ${subject.isRequired ? 'bg-red-100 text-red-700' : 'bg-gray-100 text-gray-700'
                                                                    }`}>
                                                                    {subject.isRequired ? 'Required' : 'Optional'}
                                                                </span>
                                                            </div>
                                                        </div>
                                                    </CardContent>
                                                </Card>
                                            ))}
                                        </div>
                                    </CardContent>
                                </Card>
                            );
                        })}
                    </>
                )}

                {/* Modal */}
                {showModal && (
                    <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
                        <Card className="w-full max-w-2xl max-h-[90vh] overflow-y-auto">
                            <CardHeader>
                                <CardTitle>{selectedSubject ? 'Edit Subject' : 'Add New Subject'}</CardTitle>
                                <CardDescription>Fill in the subject details</CardDescription>
                            </CardHeader>
                            <CardContent className="space-y-4">
                                <div className="grid grid-cols-2 gap-4">
                                    <div className="space-y-2">
                                        <Label htmlFor="subjectCode">Subject Code *</Label>
                                        <Input
                                            id="subjectCode"
                                            value={formData.subjectCode}
                                            onChange={(e) => setFormData({ ...formData, subjectCode: e.target.value.toUpperCase() })}
                                            placeholder="MATH, LIT, ENG"
                                            disabled={selectedSubject}
                                        />
                                    </div>
                                    <div className="space-y-2">
                                        <Label htmlFor="coefficient">Coefficient *</Label>
                                        <Input
                                            id="coefficient"
                                            type="number"
                                            step="0.1"
                                            value={formData.coefficient}
                                            onChange={(e) => setFormData({ ...formData, coefficient: e.target.value })}
                                        />
                                    </div>
                                </div>

                                <div className="space-y-2">
                                    <Label htmlFor="subjectName">Subject Name *</Label>
                                    <Input
                                        id="subjectName"
                                        value={formData.subjectName}
                                        onChange={(e) => setFormData({ ...formData, subjectName: e.target.value })}
                                        placeholder="Mathematics, Literature"
                                    />
                                </div>

                                <div className="space-y-2">
                                    <Label htmlFor="subjectNameEn">Subject Name (English)</Label>
                                    <Input
                                        id="subjectNameEn"
                                        value={formData.subjectNameEn}
                                        onChange={(e) => setFormData({ ...formData, subjectNameEn: e.target.value })}
                                        placeholder="Mathematics, Literature"
                                    />
                                </div>

                                <div className="grid grid-cols-2 gap-4">
                                    <div className="space-y-2">
                                        <Label htmlFor="schoolType">School Type *</Label>
                                        <select
                                            id="schoolType"
                                            value={formData.schoolType}
                                            onChange={(e) => setFormData({ ...formData, schoolType: e.target.value })}
                                            className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                                        >
                                            <option value="BOTH">Both (THCS & THPT)</option>
                                            <option value="THCS">THCS only</option>
                                            <option value="THPT">THPT only</option>
                                        </select>
                                    </div>
                                    <div className="space-y-2">
                                        <Label htmlFor="totalPeriodsPerWeek">Periods/Week *</Label>
                                        <Input
                                            id="totalPeriodsPerWeek"
                                            type="number"
                                            value={formData.totalPeriodsPerWeek}
                                            onChange={(e) => setFormData({ ...formData, totalPeriodsPerWeek: e.target.value })}
                                        />
                                    </div>
                                </div>

                                <div className="space-y-2">
                                    <Label htmlFor="category">Category</Label>
                                    <Input
                                        id="category"
                                        value={formData.category}
                                        onChange={(e) => setFormData({ ...formData, category: e.target.value })}
                                        placeholder="Natural Sciences, Social Studies, etc."
                                    />
                                </div>

                                <div className="flex items-center gap-2">
                                    <input
                                        type="checkbox"
                                        id="isRequired"
                                        checked={formData.isRequired}
                                        onChange={(e) => setFormData({ ...formData, isRequired: e.target.checked })}
                                        className="w-4 h-4"
                                    />
                                    <Label htmlFor="isRequired">Required Subject</Label>
                                </div>

                                <div className="flex justify-end gap-3 pt-4">
                                    <Button variant="outline" onClick={() => setShowModal(false)}>Cancel</Button>
                                    <Button onClick={handleSave} className="bg-gradient-to-r from-purple-600 to-pink-600">
                                        <FiPlus className="w-4 h-4 mr-2" />
                                        {selectedSubject ? 'Update' : 'Save'}
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

export default SubjectManagement;
