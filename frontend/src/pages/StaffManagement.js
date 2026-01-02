import React, { useState, useEffect } from 'react';
import { FiEdit, FiTrash2, FiPlus, FiUsers } from 'react-icons/fi';
import { staffService } from '../services/dataService';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';

function StaffManagement() {
    const [staff, setStaff] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [showModal, setShowModal] = useState(false);
    const [selectedStaff, setSelectedStaff] = useState(null);
    const [formData, setFormData] = useState({
        firstName: '',
        lastName: '',
        email: '',
        position: '',
        department: '',
        phone: ''
    });

    useEffect(() => {
        fetchStaff();
    }, []);

    const fetchStaff = async () => {
        try {
            setLoading(true);
            const response = await staffService.getAll();
            setStaff(response.data || []);
        } catch (err) {
            setError('Failed to load staff members');
        } finally {
            setLoading(false);
        }
    };

    const handleDelete = async (id) => {
        if (window.confirm('Are you sure you want to delete this staff member?')) {
            try {
                await staffService.delete(id);
                setStaff(staff.filter(s => s.id !== id));
            } catch (err) {
                setError('Failed to delete staff member');
            }
        }
    };

    const handleAddNew = () => {
        setSelectedStaff(null);
        setFormData({
            firstName: '',
            lastName: '',
            email: '',
            position: '',
            department: '',
            phone: ''
        });
        setShowModal(true);
    };

    const handleEdit = (member) => {
        setSelectedStaff(member);
        setFormData({
            firstName: member.user?.firstName || '',
            lastName: member.user?.lastName || '',
            email: member.user?.email || '',
            position: member.position || '',
            department: member.department || '',
            phone: member.user?.phone || ''
        });
        setShowModal(true);
    };

    const handleSave = async () => {
        try {
            if (!formData.firstName || !formData.lastName || !formData.email || !formData.position) {
                setError('Please fill in all required fields');
                return;
            }

            if (selectedStaff) {
                await staffService.update(selectedStaff.id, formData);
            } else {
                await staffService.create(formData);
            }
            setShowModal(false);
            setError('');
            fetchStaff();
        } catch (err) {
            setError('Failed to save staff member: ' + (err.message || 'Unknown error'));
        }
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-purple-50 p-6">
            <div className="max-w-7xl mx-auto">
                {/* Header */}
                <div className="mb-8">
                    <div className="flex items-center justify-between">
                        <div>
                            <h1 className="text-3xl font-bold text-gray-900 mb-2 flex items-center gap-2">
                                <FiUsers className="text-blue-600" />
                                Staff Management
                            </h1>
                            <p className="text-gray-600">Manage and track your teaching and administrative staff</p>
                        </div>
                        <Button onClick={handleAddNew} className="bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700">
                            <FiPlus className="w-4 h-4 mr-2" />
                            Add Staff
                        </Button>
                    </div>
                </div>

                {/* Error Alert */}
                {error && (
                    <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg">
                        <p className="text-sm text-red-800">{error}</p>
                    </div>
                )}

                {/* Staff Table */}
                <Card className="border-0 shadow-xl">
                    <CardHeader>
                        <CardTitle>Staff Members ({staff.length})</CardTitle>
                        <CardDescription>View and manage all staff members</CardDescription>
                    </CardHeader>
                    <CardContent>
                        {loading ? (
                            <div className="flex justify-center items-center py-12">
                                <div className="w-12 h-12 border-4 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
                            </div>
                        ) : (
                            <div className="overflow-x-auto">
                                <table className="w-full">
                                    <thead>
                                        <tr className="border-b border-gray-200">
                                            <th className="text-left py-3 px-4 text-sm font-semibold text-gray-700">Employee ID</th>
                                            <th className="text-left py-3 px-4 text-sm font-semibold text-gray-700">Name</th>
                                            <th className="text-left py-3 px-4 text-sm font-semibold text-gray-700">Position</th>
                                            <th className="text-left py-3 px-4 text-sm font-semibold text-gray-700">Department</th>
                                            <th className="text-left py-3 px-4 text-sm font-semibold text-gray-700">Email</th>
                                            <th className="text-left py-3 px-4 text-sm font-semibold text-gray-700">Status</th>
                                            <th className="text-right py-3 px-4 text-sm font-semibold text-gray-700">Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody className="divide-y divide-gray-100">
                                        {staff.length > 0 ? (
                                            staff.map((member) => (
                                                <tr key={member.id} className="hover:bg-gray-50 transition-colors">
                                                    <td className="py-4 px-4 text-sm text-gray-900">{member.employeeId}</td>
                                                    <td className="py-4 px-4">
                                                        <div className="font-medium text-gray-900">
                                                            {member.user?.firstName} {member.user?.lastName}
                                                        </div>
                                                    </td>
                                                    <td className="py-4 px-4 text-sm text-gray-600">{member.position}</td>
                                                    <td className="py-4 px-4 text-sm text-gray-600">{member.department}</td>
                                                    <td className="py-4 px-4 text-sm text-gray-600">{member.user?.email}</td>
                                                    <td className="py-4 px-4">
                                                        <span className={`px-3 py-1 rounded-full text-xs font-semibold ${member.status === 'ACTIVE'
                                                                ? 'bg-green-100 text-green-700'
                                                                : 'bg-red-100 text-red-700'
                                                            }`}>
                                                            {member.status}
                                                        </span>
                                                    </td>
                                                    <td className="py-4 px-4">
                                                        <div className="flex justify-end gap-2">
                                                            <Button
                                                                size="sm"
                                                                variant="outline"
                                                                onClick={() => handleEdit(member)}
                                                                className="hover:bg-blue-50 hover:text-blue-700"
                                                            >
                                                                <FiEdit className="w-4 h-4" />
                                                            </Button>
                                                            <Button
                                                                size="sm"
                                                                variant="outline"
                                                                onClick={() => handleDelete(member.id)}
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
                                                <td colSpan="7" className="text-center py-12 text-gray-500">
                                                    No staff members found
                                                </td>
                                            </tr>
                                        )}
                                    </tbody>
                                </table>
                            </div>
                        )}
                    </CardContent>
                </Card>

                {/* Modal */}
                {showModal && (
                    <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
                        <Card className="w-full max-w-2xl max-h-[90vh] overflow-y-auto">
                            <CardHeader>
                                <CardTitle>{selectedStaff ? 'Edit Staff Member' : 'Add New Staff Member'}</CardTitle>
                                <CardDescription>Fill in the staff member details</CardDescription>
                            </CardHeader>
                            <CardContent className="space-y-4">
                                <div className="grid grid-cols-2 gap-4">
                                    <div className="space-y-2">
                                        <Label htmlFor="firstName">First Name *</Label>
                                        <Input
                                            id="firstName"
                                            value={formData.firstName}
                                            onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                                            placeholder="Enter first name"
                                        />
                                    </div>
                                    <div className="space-y-2">
                                        <Label htmlFor="lastName">Last Name *</Label>
                                        <Input
                                            id="lastName"
                                            value={formData.lastName}
                                            onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                                            placeholder="Enter last name"
                                        />
                                    </div>
                                </div>

                                <div className="space-y-2">
                                    <Label htmlFor="email">Email *</Label>
                                    <Input
                                        id="email"
                                        type="email"
                                        value={formData.email}
                                        onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                                        placeholder="Enter email"
                                    />
                                </div>

                                <div className="space-y-2">
                                    <Label htmlFor="phone">Phone</Label>
                                    <Input
                                        id="phone"
                                        value={formData.phone}
                                        onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                                        placeholder="Enter phone number"
                                    />
                                </div>

                                <div className="grid grid-cols-2 gap-4">
                                    <div className="space-y-2">
                                        <Label htmlFor="position">Position *</Label>
                                        <select
                                            id="position"
                                            value={formData.position}
                                            onChange={(e) => setFormData({ ...formData, position: e.target.value })}
                                            className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                                        >
                                            <option value="">Select Position</option>
                                            <option value="PRINCIPAL">Principal</option>
                                            <option value="TEACHER">Teacher</option>
                                            <option value="LIBRARIAN">Librarian</option>
                                            <option value="ACCOUNTANT">Accountant</option>
                                            <option value="ADMIN_STAFF">Admin Staff</option>
                                        </select>
                                    </div>
                                    <div className="space-y-2">
                                        <Label htmlFor="department">Department</Label>
                                        <Input
                                            id="department"
                                            value={formData.department}
                                            onChange={(e) => setFormData({ ...formData, department: e.target.value })}
                                            placeholder="Enter department"
                                        />
                                    </div>
                                </div>

                                <div className="flex justify-end gap-3 pt-4">
                                    <Button variant="outline" onClick={() => setShowModal(false)}>
                                        Cancel
                                    </Button>
                                    <Button onClick={handleSave} className="bg-gradient-to-r from-blue-600 to-purple-600">
                                        <FiPlus className="w-4 h-4 mr-2" />
                                        {selectedStaff ? 'Update' : 'Save'}
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

export default StaffManagement;
