import React, { useState, useEffect } from 'react';
import { FiDollarSign, FiCheck, FiX, FiSearch, FiClock } from 'react-icons/fi';
import { studentService } from '../services/dataService';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';

function FeeManagement() {
    const [students, setStudents] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [showModal, setShowModal] = useState(false);
    const [selectedStudent, setSelectedStudent] = useState(null);
    const [searchTerm, setSearchTerm] = useState('');
    const [filterStatus, setFilterStatus] = useState('All');
    const [feeData, setFeeData] = useState({});
    const [paymentAmount, setPaymentAmount] = useState(0);

    const feeStructure = {
        tuitionFee: 25000,
        libraryFee: 2000,
        labFee: 3000,
        sportsFee: 1500,
        examFee: 2500,
        total: 34000
    };

    useEffect(() => {
        fetchStudents();
    }, []);

    const fetchStudents = async () => {
        try {
            setLoading(true);
            const response = await studentService.getAll();
            const studentsList = response.data || [];
            setStudents(studentsList);

            const initialFeeData = {};
            studentsList.forEach(student => {
                const paid = Math.random() > 0.3;
                const amount = paid ? feeStructure.total : Math.floor(Math.random() * feeStructure.total);
                initialFeeData[student.id] = {
                    totalDue: feeStructure.total,
                    paid: amount,
                    pending: feeStructure.total - amount,
                    status: amount >= feeStructure.total ? 'Paid' : amount > 0 ? 'Partial' : 'Pending',
                    lastPayment: paid ? new Date().toISOString().split('T')[0] : null
                };
            });
            setFeeData(initialFeeData);
        } catch (err) {
            setError('Failed to load students');
        } finally {
            setLoading(false);
        }
    };

    const handlePayment = (student) => {
        setSelectedStudent(student);
        setPaymentAmount(feeData[student.id]?.pending || 0);
        setShowModal(true);
    };

    const handleSavePayment = async () => {
        if (!selectedStudent || paymentAmount <= 0) {
            setError('Please enter a valid payment amount');
            return;
        }

        try {
            const studentFee = feeData[selectedStudent.id];
            const newPaid = studentFee.paid + paymentAmount;
            const newPending = feeStructure.total - newPaid;

            setFeeData({
                ...feeData,
                [selectedStudent.id]: {
                    ...studentFee,
                    paid: newPaid,
                    pending: newPending,
                    status: newPending === 0 ? 'Paid' : 'Partial',
                    lastPayment: new Date().toISOString().split('T')[0]
                }
            });

            setSuccess(`Payment of ₹${paymentAmount} recorded successfully`);
            setShowModal(false);
            setTimeout(() => setSuccess(''), 3000);
        } catch (err) {
            setError('Failed to record payment');
        }
    };

    const getFilteredStudents = () => {
        let filtered = students;

        if (searchTerm) {
            filtered = filtered.filter(student =>
                student.user?.firstName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
                student.user?.lastName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
                student.rollNumber?.includes(searchTerm)
            );
        }

        if (filterStatus !== 'All') {
            filtered = filtered.filter(student => feeData[student.id]?.status === filterStatus);
        }

        return filtered;
    };

    const getStats = () => {
        const total = students.length;
        const paid = students.filter(s => feeData[s.id]?.status === 'Paid').length;
        const partial = students.filter(s => feeData[s.id]?.status === 'Partial').length;
        const pending = students.filter(s => feeData[s.id]?.status === 'Pending').length;

        const totalRevenue = students.reduce((sum, s) => sum + (feeData[s.id]?.paid || 0), 0);
        const totalPending = students.reduce((sum, s) => sum + (feeData[s.id]?.pending || 0), 0);
        const collectionRate = total > 0 ? ((paid / total) * 100).toFixed(1) : 0;

        return { total, paid, partial, pending, totalRevenue, totalPending, collectionRate };
    };

    const stats = getStats();
    const filteredStudents = getFilteredStudents();

    return (
        <div className="min-h-screen bg-gradient-to-br from-slate-50 via-rose-50 to-pink-50 p-6">
            <div className="max-w-7xl mx-auto">
                {/* Header */}
                <div className="mb-8">
                    <div className="flex items-center justify-between">
                        <div>
                            <h1 className="text-3xl font-bold text-gray-900 mb-2 flex items-center gap-2">
                                <FiDollarSign className="text-rose-600" />
                                Fee Management
                            </h1>
                            <p className="text-gray-600">Track and manage student fee payments</p>
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

                {/* Statistics */}
                <div className="grid grid-cols-4 gap-4 mb-6">
                    <Card className="border-0 shadow-lg border-l-4 border-l-green-500">
                        <CardContent className="pt-6 text-center">
                            <div className="text-3xl font-bold text-green-600">₹{stats.totalRevenue.toLocaleString()}</div>
                            <div className="text-sm text-gray-600 mt-1">Total Revenue</div>
                        </CardContent>
                    </Card>
                    <Card className="border-0 shadow-lg border-l-4 border-l-red-500">
                        <CardContent className="pt-6 text-center">
                            <div className="text-3xl font-bold text-red-600">₹{stats.totalPending.toLocaleString()}</div>
                            <div className="text-sm text-gray-600 mt-1">Total Pending</div>
                        </CardContent>
                    </Card>
                    <Card className="border-0 shadow-lg border-l-4 border-l-rose-500">
                        <CardContent className="pt-6 text-center">
                            <div className="text-3xl font-bold text-rose-600">{stats.paid}/{stats.total}</div>
                            <div className="text-sm text-gray-600 mt-1">Fully Paid</div>
                        </CardContent>
                    </Card>
                    <Card className="border-0 shadow-lg border-l-4 border-l-blue-500">
                        <CardContent className="pt-6 text-center">
                            <div className="text-3xl font-bold text-blue-600">{stats.collectionRate}%</div>
                            <div className="text-sm text-gray-600 mt-1">Collection Rate</div>
                        </CardContent>
                    </Card>
                </div>

                {/* Filters */}
                <Card className="mb-6 border-0 shadow-lg">
                    <CardContent className="pt-6">
                        <div className="grid grid-cols-2 gap-4">
                            <div className="relative">
                                <FiSearch className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                                <Input
                                    placeholder="Search by name or roll number..."
                                    value={searchTerm}
                                    onChange={(e) => setSearchTerm(e.target.value)}
                                    className="pl-10"
                                />
                            </div>
                            <select
                                value={filterStatus}
                                onChange={(e) => setFilterStatus(e.target.value)}
                                className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                            >
                                <option value="All">All Status</option>
                                <option value="Paid">Paid</option>
                                <option value="Partial">Partially Paid</option>
                                <option value="Pending">Pending</option>
                            </select>
                        </div>
                    </CardContent>
                </Card>

                {/* Fee Table */}
                <Card className="border-0 shadow-xl">
                    <CardHeader>
                        <CardTitle>Fee Collection</CardTitle>
                        <CardDescription>Manage student fee payments and track collection</CardDescription>
                    </CardHeader>
                    <CardContent>
                        {loading ? (
                            <div className="flex justify-center items-center py-12">
                                <div className="w-12 h-12 border-4 border-rose-600 border-t-transparent rounded-full animate-spin"></div>
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
                                            <th className="text-right py-3 px-4 text-sm font-semibold text-gray-700">Total Fee</th>
                                            <th className="text-right py-3 px-4 text-sm font-semibold text-gray-700">Paid</th>
                                            <th className="text-right py-3 px-4 text-sm font-semibold text-gray-700">Pending</th>
                                            <th className="text-left py-3 px-4 text-sm font-semibold text-gray-700">Status</th>
                                            <th className="text-right py-3 px-4 text-sm font-semibold text-gray-700">Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody className="divide-y divide-gray-100">
                                        {filteredStudents.length > 0 ? (
                                            filteredStudents.map((student, index) => {
                                                const fee = feeData[student.id];
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
                                                        <td className="py-4 px-4 text-right text-sm text-gray-900">₹{fee?.totalDue.toLocaleString()}</td>
                                                        <td className="py-4 px-4 text-right text-sm text-green-600 font-medium">₹{fee?.paid.toLocaleString()}</td>
                                                        <td className="py-4 px-4 text-right text-sm text-red-600 font-medium">₹{fee?.pending.toLocaleString()}</td>
                                                        <td className="py-4 px-4">
                                                            <span className={`inline-flex items-center gap-1 px-3 py-1 rounded-full text-xs font-semibold ${fee?.status === 'Paid' ? 'bg-green-100 text-green-700' :
                                                                    fee?.status === 'Partial' ? 'bg-yellow-100 text-yellow-700' :
                                                                        'bg-red-100 text-red-700'
                                                                }`}>
                                                                {fee?.status === 'Paid' && <FiCheck className="w-3 h-3" />}
                                                                {fee?.status === 'Pending' && <FiX className="w-3 h-3" />}
                                                                {fee?.status === 'Partial' && <FiClock className="w-3 h-3" />}
                                                                {fee?.status}
                                                            </span>
                                                        </td>
                                                        <td className="py-4 px-4">
                                                            <div className="flex justify-end">
                                                                <Button
                                                                    size="sm"
                                                                    onClick={() => handlePayment(student)}
                                                                    disabled={fee?.status === 'Paid'}
                                                                    className="bg-gradient-to-r from-rose-600 to-pink-600 hover:from-rose-700 hover:to-pink-700 disabled:opacity-50"
                                                                >
                                                                    <FiDollarSign className="w-4 h-4 mr-1" /> Pay
                                                                </Button>
                                                            </div>
                                                        </td>
                                                    </tr>
                                                );
                                            })
                                        ) : (
                                            <tr>
                                                <td colSpan="9" className="text-center py-12 text-gray-500">
                                                    {searchTerm || filterStatus !== 'All' ? 'No students match the filters' : 'No students found'}
                                                </td>
                                            </tr>
                                        )}
                                    </tbody>
                                </table>
                            </div>
                        )}
                    </CardContent>
                </Card>

                {/* Payment Modal */}
                {showModal && selectedStudent && (
                    <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
                        <Card className="w-full max-w-md">
                            <CardHeader>
                                <CardTitle>Record Payment</CardTitle>
                                <CardDescription>
                                    {selectedStudent.user?.firstName} {selectedStudent.user?.lastName} - {selectedStudent.rollNumber}
                                </CardDescription>
                            </CardHeader>
                            <CardContent className="space-y-4">
                                <Card className="bg-gray-50">
                                    <CardContent className="pt-4 space-y-2">
                                        <div className="flex justify-between text-sm">
                                            <span className="text-gray-600">Total Fee:</span>
                                            <span className="font-semibold">₹{feeData[selectedStudent.id]?.totalDue.toLocaleString()}</span>
                                        </div>
                                        <div className="flex justify-between text-sm text-green-600">
                                            <span>Already Paid:</span>
                                            <span className="font-semibold">₹{feeData[selectedStudent.id]?.paid.toLocaleString()}</span>
                                        </div>
                                        <div className="flex justify-between text-sm text-red-600">
                                            <span>Pending:</span>
                                            <span className="font-semibold">₹{feeData[selectedStudent.id]?.pending.toLocaleString()}</span>
                                        </div>
                                    </CardContent>
                                </Card>

                                <div className="space-y-2">
                                    <Label htmlFor="paymentAmount">Payment Amount</Label>
                                    <div className="relative">
                                        <span className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500">₹</span>
                                        <Input
                                            id="paymentAmount"
                                            type="number"
                                            value={paymentAmount}
                                            onChange={(e) => setPaymentAmount(parseFloat(e.target.value) || 0)}
                                            min="0"
                                            max={feeData[selectedStudent.id]?.pending}
                                            className="pl-8"
                                        />
                                    </div>
                                    <p className="text-xs text-gray-500">Maximum: ₹{feeData[selectedStudent.id]?.pending.toLocaleString()}</p>
                                </div>

                                <div className="flex justify-end gap-3 pt-4">
                                    <Button variant="outline" onClick={() => setShowModal(false)}>
                                        Cancel
                                    </Button>
                                    <Button onClick={handleSavePayment} className="bg-gradient-to-r from-rose-600 to-pink-600">
                                        <FiCheck className="w-4 h-4 mr-2" /> Record Payment
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

export default FeeManagement;
