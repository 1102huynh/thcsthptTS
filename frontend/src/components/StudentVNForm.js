import React, { useState, useEffect } from 'react';
import { FiX, FiSave, FiRefreshCw } from 'react-icons/fi';
import api from '../services/api';
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/card';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';

function StudentVNForm({ student, onClose }) {
    const [activeTab, setActiveTab] = useState(0);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    // Dropdown data
    const [provinces, setProvinces] = useState([]);
    const [ethnicities, setEthnicities] = useState([]);
    const [religions, setReligions] = useState([]);
    const [priorityObjects, setPriorityObjects] = useState([]);
    const [genders, setGenders] = useState([]);
    const [bloodTypes, setBloodTypes] = useState([]);
    const [academicRanks, setAcademicRanks] = useState([]);
    const [conductRanks, setConductRanks] = useState([]);
    const [guardianRelationships, setGuardianRelationships] = useState([]);
    const [classes, setClasses] = useState([]);
    const [gradeLevels, setGradeLevels] = useState([]);

    // Form data
    const [formData, setFormData] = useState({
        // Tab 1: Basic info
        studentCode: '',
        studentId: '',
        lastName: '',
        firstName: '',
        dateOfBirth: '',
        gender: 'Nam',
        placeOfBirth: '',
        idNumber: '',
        idIssueDate: '',
        idIssuePlace: '',
        province: '',
        district: '',
        ward: '',
        detailedAddress: '',
        phoneNumber: '',
        ethnicity: 'Kinh',
        religion: 'Không',
        priorityObject: 'Không',
        gradeLevelId: '',
        schoolClassId: '',
        academicYear: '2024-2025',
        admissionYear: new Date().getFullYear(),
        status: 'ACTIVE',

        // Tab 2: Parents
        fatherName: '',
        fatherYearOfBirth: '',
        fatherOccupation: '',
        fatherWorkplace: '',
        fatherPhone: '',
        fatherEmail: '',
        motherName: '',
        motherYearOfBirth: '',
        motherOccupation: '',
        motherWorkplace: '',
        motherPhone: '',
        motherEmail: '',
        guardianName: '',
        guardianRelationship: '',
        guardianPhone: '',
        guardianAddress: '',

        // Tab 3: Academic history
        previousSchool: '',
        previousSchoolAddress: '',
        previousSchoolFrom: '',
        previousSchoolTo: '',
        transferReason: '',
        previousAcademicRank: '',
        previousConductRank: '',
        awards: '',

        // Tab 4: Health
        height: '',
        weight: '',
        bloodType: '',
        diseases: '',
        allergies: '',
        notes: ''
    });

    useEffect(() => {
        fetchConstants();
        fetchData();
        if (student) {
            populateForm(student);
        }
    }, [student]);

    const fetchConstants = async () => {
        try {
            const [
                provincesRes, ethnicitiesRes, religionsRes, priorityRes,
                gendersRes, bloodRes, academicRes, conductRes, guardianRes
            ] = await Promise.all([
                api.get('/api/vn/students/constants/provinces'),
                api.get('/api/vn/students/constants/ethnicities'),
                api.get('/api/vn/students/constants/religions'),
                api.get('/api/vn/students/constants/priority-objects'),
                api.get('/api/vn/students/constants/genders'),
                api.get('/api/vn/students/constants/blood-types'),
                api.get('/api/vn/students/constants/academic-ranks'),
                api.get('/api/vn/students/constants/conduct-ranks'),
                api.get('/api/vn/students/constants/guardian-relationships')
            ]);

            setProvinces(provincesRes.data || []);
            setEthnicities(ethnicitiesRes.data || []);
            setReligions(religionsRes.data || []);
            setPriorityObjects(priorityRes.data || []);
            setGenders(gendersRes.data || []);
            setBloodTypes(bloodRes.data || []);
            setAcademicRanks(academicRes.data || []);
            setConductRanks(conductRes.data || []);
            setGuardianRelationships(guardianRes.data || []);
        } catch (err) {
            console.error('Failed to load constants:', err);
        }
    };

    const fetchData = async () => {
        try {
            const [classesRes, gradeLevelsRes] = await Promise.all([
                api.get('/api/classes'),
                api.get('/api/grade-levels')
            ]);
            setClasses(classesRes.data || []);
            setGradeLevels(gradeLevelsRes.data || []);
        } catch (err) {
            console.error('Failed to load data:', err);
        }
    };

    const populateForm = (studentData) => {
        setFormData({
            studentCode: studentData.studentCode || '',
            studentId: studentData.studentId || '',
            lastName: studentData.lastName || '',
            firstName: studentData.firstName || '',
            dateOfBirth: studentData.dateOfBirth || '',
            gender: studentData.gender || 'Nam',
            placeOfBirth: studentData.placeOfBirth || '',
            idNumber: studentData.idNumber || '',
            idIssueDate: studentData.idIssueDate || '',
            idIssuePlace: studentData.idIssuePlace || '',
            province: studentData.province || '',
            district: studentData.district || '',
            ward: studentData.ward || '',
            detailedAddress: studentData.detailedAddress || '',
            phoneNumber: studentData.phoneNumber || '',
            ethnicity: studentData.ethnicity || 'Kinh',
            religion: studentData.religion || 'Không',
            priorityObject: studentData.priorityObject || 'Không',
            gradeLevelId: studentData.gradeLevel?.id || '',
            schoolClassId: studentData.schoolClass?.id || '',
            academicYear: studentData.academicYear || '2024-2025',
            admissionYear: studentData.admissionYear || new Date().getFullYear(),
            status: studentData.status || 'ACTIVE',
            fatherName: studentData.fatherName || '',
            fatherYearOfBirth: studentData.fatherYearOfBirth || '',
            fatherOccupation: studentData.fatherOccupation || '',
            fatherWorkplace: studentData.fatherWorkplace || '',
            fatherPhone: studentData.fatherPhone || '',
            fatherEmail: studentData.fatherEmail || '',
            motherName: studentData.motherName || '',
            motherYearOfBirth: studentData.motherYearOfBirth || '',
            motherOccupation: studentData.motherOccupation || '',
            motherWorkplace: studentData.motherWorkplace || '',
            motherPhone: studentData.motherPhone || '',
            motherEmail: studentData.motherEmail || '',
            guardianName: studentData.guardianName || '',
            guardianRelationship: studentData.guardianRelationship || '',
            guardianPhone: studentData.guardianPhone || '',
            guardianAddress: studentData.guardianAddress || '',
            previousSchool: studentData.previousSchool || '',
            previousSchoolAddress: studentData.previousSchoolAddress || '',
            previousSchoolFrom: studentData.previousSchoolFrom || '',
            previousSchoolTo: studentData.previousSchoolTo || '',
            transferReason: studentData.transferReason || '',
            previousAcademicRank: studentData.previousAcademicRank || '',
            previousConductRank: studentData.previousConductRank || '',
            awards: studentData.awards || '',
            height: studentData.height || '',
            weight: studentData.weight || '',
            bloodType: studentData.bloodType || '',
            diseases: studentData.diseases || '',
            allergies: studentData.allergies || '',
            notes: studentData.notes || ''
        });
    };

    const handleGenerateCode = async () => {
        if (!formData.gradeLevelId || !formData.admissionYear) {
            setError('Please select Grade and Admission Year first');
            return;
        }

        try {
            const gradeLevel = gradeLevels.find(g => g.id === parseInt(formData.gradeLevelId));
            const response = await api.post('/api/vn/students/generate-code', {
                admissionYear: formData.admissionYear,
                gradeLevel: { id: gradeLevel.id, levelNumber: gradeLevel.levelNumber }
            });
            setFormData({ ...formData, studentCode: response.data.studentCode });
            setError('');
        } catch (err) {
            setError('Cannot generate student code');
        }
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        try {
            // Prepare payload
            const payload = {
                studentCode: formData.studentCode,
                studentId: formData.studentId || formData.studentCode,
                lastName: formData.lastName,
                firstName: formData.firstName,
                dateOfBirth: formData.dateOfBirth,
                gender: formData.gender,
                placeOfBirth: formData.placeOfBirth,
                idNumber: formData.idNumber,
                idIssueDate: formData.idIssueDate || null,
                idIssuePlace: formData.idIssuePlace,
                province: formData.province,
                district: formData.district,
                ward: formData.ward,
                detailedAddress: formData.detailedAddress,
                phoneNumber: formData.phoneNumber,
                ethnicity: formData.ethnicity,
                religion: formData.religion,
                priorityObject: formData.priorityObject,
                gradeLevel: formData.gradeLevelId ? { id: parseInt(formData.gradeLevelId) } : null,
                schoolClass: formData.schoolClassId ? { id: parseInt(formData.schoolClassId) } : null,
                academicYear: formData.academicYear,
                admissionYear: parseInt(formData.admissionYear),
                status: formData.status,
                fatherName: formData.fatherName,
                fatherYearOfBirth: formData.fatherYearOfBirth ? parseInt(formData.fatherYearOfBirth) : null,
                fatherOccupation: formData.fatherOccupation,
                fatherWorkplace: formData.fatherWorkplace,
                fatherPhone: formData.fatherPhone,
                fatherEmail: formData.fatherEmail,
                motherName: formData.motherName,
                motherYearOfBirth: formData.motherYearOfBirth ? parseInt(formData.motherYearOfBirth) : null,
                motherOccupation: formData.motherOccupation,
                motherWorkplace: formData.motherWorkplace,
                motherPhone: formData.motherPhone,
                motherEmail: formData.motherEmail,
                guardianName: formData.guardianName,
                guardianRelationship: formData.guardianRelationship,
                guardianPhone: formData.guardianPhone,
                guardianAddress: formData.guardianAddress,
                previousSchool: formData.previousSchool,
                previousSchoolAddress: formData.previousSchoolAddress,
                previousSchoolFrom: formData.previousSchoolFrom || null,
                previousSchoolTo: formData.previousSchoolTo || null,
                transferReason: formData.transferReason,
                previousAcademicRank: formData.previousAcademicRank,
                previousConductRank: formData.previousConductRank,
                awards: formData.awards,
                height: formData.height ? parseInt(formData.height) : null,
                weight: formData.weight ? parseInt(formData.weight) : null,
                bloodType: formData.bloodType,
                diseases: formData.diseases,
                allergies: formData.allergies,
                notes: formData.notes,
                user: student?.user || { id: 1 } // TODO: Create user properly
            };

            if (student) {
                await api.put(`/api/vn/students/${student.id}`, payload);
            } else {
                await api.post('/api/vn/students', payload);
            }

            onClose(true);
        } catch (err) {
            setError(err.response?.data?.message || 'Error saving student');
        } finally {
            setLoading(false);
        }
    };

    const tabs = [
        { id: 0, label: 'Basic Information', icon: '📋' },
        { id: 1, label: 'Parents', icon: '👨‍👩‍👧' },
        { id: 2, label: 'Academic History', icon: '📚' },
        { id: 3, label: 'Health & Others', icon: '🏥' },
        { id: 4, label: 'Documents', icon: '📎' }
    ];

    return (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4 overflow-y-auto">
            <Card className="w-full max-w-5xl max-h-[90vh] overflow-y-auto">
                <CardHeader className="sticky top-0 bg-white z-10 border-b">
                    <div className="flex items-center justify-between">
                        <CardTitle className="text-2xl">
                            {student ? 'Update Student' : 'Add New Student'}
                        </CardTitle>
                        <Button variant="ghost" size="sm" onClick={() => onClose(false)}>
                            <FiX className="w-5 h-5" />
                        </Button>
                    </div>

                    {/* Tabs */}
                    <div className="flex gap-2 mt-4 overflow-x-auto pb-2">
                        {tabs.map(tab => (
                            <button
                                key={tab.id}
                                onClick={() => setActiveTab(tab.id)}
                                className={`px-4 py-2 rounded-lg whitespace-nowrap transition-all ${activeTab === tab.id
                                    ? 'bg-gradient-to-r from-violet-600 to-purple-600 text-white shadow-lg'
                                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                                    }`}
                            >
                                <span className="mr-2">{tab.icon}</span>
                                {tab.label}
                            </button>
                        ))}
                    </div>
                </CardHeader>

                <form onSubmit={handleSubmit}>
                    <CardContent className="pt-6">
                        {/* Error Alert */}
                        {error && (
                            <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-lg">
                                <p className="text-sm text-red-800">{error}</p>
                            </div>
                        )}

                        {/* Tab 1: Basic Info */}
                        {activeTab === 0 && (
                            <div className="space-y-6">
                                {/* Student Code */}
                                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                                    <div className="col-span-2">
                                        <Label>Student Code *</Label>
                                        <Input
                                            name="studentCode"
                                            value={formData.studentCode}
                                            onChange={handleChange}
                                            placeholder="0124061234"
                                            required
                                        />
                                    </div>
                                    <div>
                                        <Label>&nbsp;</Label>
                                        <Button
                                            type="button"
                                            variant="outline"
                                            onClick={handleGenerateCode}
                                            className="w-full"
                                        >
                                            <FiRefreshCw className="w-4 h-4 mr-2" />
                                            Auto Generate
                                        </Button>
                                    </div>
                                </div>

                                {/* Name */}
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                    <div>
                                        <Label>Last Name & Middle Name *</Label>
                                        <Input
                                            name="lastName"
                                            value={formData.lastName}
                                            onChange={handleChange}
                                            placeholder="Nguyen Van"
                                            required
                                        />
                                    </div>
                                    <div>
                                        <Label>First Name *</Label>
                                        <Input
                                            name="firstName"
                                            value={formData.firstName}
                                            onChange={handleChange}
                                            placeholder="An"
                                            required
                                        />
                                    </div>
                                </div>

                                {/* DOB, Gender, Place of Birth */}
                                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                                    <div>
                                        <Label>Date of Birth *</Label>
                                        <Input
                                            type="date"
                                            name="dateOfBirth"
                                            value={formData.dateOfBirth}
                                            onChange={handleChange}
                                            required
                                        />
                                    </div>
                                    <div>
                                        <Label>Gender *</Label>
                                        <select
                                            name="gender"
                                            value={formData.gender}
                                            onChange={handleChange}
                                            className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                                            required
                                        >
                                            {genders.map(g => <option key={g} value={g}>{g}</option>)}
                                        </select>
                                    </div>
                                    <div>
                                        <Label>Place of Birth</Label>
                                        <select
                                            name="placeOfBirth"
                                            value={formData.placeOfBirth}
                                            onChange={handleChange}
                                            className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                                        >
                                            <option value="">Chọn tỉnh/thành</option>
                                            {provinces.map(p => <option key={p} value={p}>{p}</option>)}
                                        </select>
                                    </div>
                                </div>

                                {/* ID Info */}
                                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                                    <div>
                                        <Label>ID Number (CMND/CCCD)</Label>
                                        <Input
                                            name="idNumber"
                                            value={formData.idNumber}
                                            onChange={handleChange}
                                            placeholder="001234567890"
                                        />
                                    </div>
                                    <div>
                                        <Label>Issue Date</Label>
                                        <Input
                                            type="date"
                                            name="idIssueDate"
                                            value={formData.idIssueDate}
                                            onChange={handleChange}
                                        />
                                    </div>
                                    <div>
                                        <Label>Issue Place</Label>
                                        <Input
                                            name="idIssuePlace"
                                            value={formData.idIssuePlace}
                                            onChange={handleChange}
                                            placeholder="Police Department..."
                                        />
                                    </div>
                                </div>

                                {/* Address */}
                                <div className="space-y-2">
                                    <Label className="text-lg font-semibold">Permanent Address</Label>
                                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                                        <div>
                                            <Label>Province/City</Label>
                                            <select
                                                name="province"
                                                value={formData.province}
                                                onChange={handleChange}
                                                className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                                            >
                                                <option value="">Select Province/City</option>
                                                {provinces.map(p => <option key={p} value={p}>{p}</option>)}
                                            </select>
                                        </div>
                                        <div>
                                            <Label>District</Label>
                                            <Input
                                                name="district"
                                                value={formData.district}
                                                onChange={handleChange}
                                                placeholder="District"
                                            />
                                        </div>
                                        <div>
                                            <Label>Ward/Commune</Label>
                                            <Input
                                                name="ward"
                                                value={formData.ward}
                                                onChange={handleChange}
                                                placeholder="Ward/Commune"
                                            />
                                        </div>
                                    </div>
                                    <div>
                                        <Label>Detailed Address</Label>
                                        <Input
                                            name="detailedAddress"
                                            value={formData.detailedAddress}
                                            onChange={handleChange}
                                            placeholder="House number, street..."
                                        />
                                    </div>
                                    <div>
                                        <Label>Số điện thoại</Label>
                                        <Input
                                            name="phoneNumber"
                                            value={formData.phoneNumber}
                                            onChange={handleChange}
                                            placeholder="0912345678"
                                        />
                                    </div>
                                </div>

                                {/* Ethnicity, Religion, Priority */}
                                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                                    <div>
                                        <Label>Ethnicity</Label>
                                        <select
                                            name="ethnicity"
                                            value={formData.ethnicity}
                                            onChange={handleChange}
                                            className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                                        >
                                            {ethnicities.map(e => <option key={e} value={e}>{e}</option>)}
                                        </select>
                                    </div>
                                    <div>
                                        <Label>Religion</Label>
                                        <select
                                            name="religion"
                                            value={formData.religion}
                                            onChange={handleChange}
                                            className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                                        >
                                            {religions.map(r => <option key={r} value={r}>{r}</option>)}
                                        </select>
                                    </div>
                                    <div>
                                        <Label>Priority Object</Label>
                                        <select
                                            name="priorityObject"
                                            value={formData.priorityObject}
                                            onChange={handleChange}
                                            className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                                        >
                                            {priorityObjects.map(p => <option key={p} value={p}>{p}</option>)}
                                        </select>
                                    </div>
                                </div>

                                {/* Academic Info */}
                                <div className="space-y-2">
                                    <Label className="text-lg font-semibold">Academic Information</Label>
                                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                        <div>
                                            <Label>Grade Level *</Label>
                                            <select
                                                name="gradeLevelId"
                                                value={formData.gradeLevelId}
                                                onChange={handleChange}
                                                className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                                                required
                                            >
                                                <option value="">Select Grade</option>
                                                {gradeLevels.map(g => (
                                                    <option key={g.id} value={g.id}>Grade {g.levelName}</option>
                                                ))}
                                            </select>
                                        </div>
                                        <div>
                                            <Label>Class</Label>
                                            <select
                                                name="schoolClassId"
                                                value={formData.schoolClassId}
                                                onChange={handleChange}
                                                className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                                            >
                                                <option value="">Select Class</option>
                                                {classes.map(c => (
                                                    <option key={c.id} value={c.id}>{c.className}</option>
                                                ))}
                                            </select>
                                        </div>
                                        <div>
                                            <Label>Academic Year</Label>
                                            <Input
                                                name="academicYear"
                                                value={formData.academicYear}
                                                onChange={handleChange}
                                                placeholder="2024-2025"
                                            />
                                        </div>
                                        <div>
                                            <Label>Admission Year *</Label>
                                            <Input
                                                type="number"
                                                name="admissionYear"
                                                value={formData.admissionYear}
                                                onChange={handleChange}
                                                required
                                            />
                                        </div>
                                        <div>
                                            <Label>Status</Label>
                                            <select
                                                name="status"
                                                value={formData.status}
                                                onChange={handleChange}
                                                className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                                            >
                                                <option value="ACTIVE">Active</option>
                                                <option value="ON_LEAVE">On Leave</option>
                                                <option value="TRANSFERRED">Transferred</option>
                                                <option value="DROPPED">Dropped</option>
                                                <option value="GRADUATED">Graduated</option>
                                            </select>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        )}

                        {/* Tab 2: Parents */}
                        {activeTab === 1 && (
                            <div className="space-y-6">
                                {/* Father */}
                                <div className="space-y-4 p-4 bg-blue-50 rounded-lg">
                                    <Label className="text-lg font-semibold">Father Information</Label>
                                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                        <div>
                                            <Label>Full Name</Label>
                                            <Input name="fatherName" value={formData.fatherName} onChange={handleChange} />
                                        </div>
                                        <div>
                                            <Label>Year of Birth</Label>
                                            <Input type="number" name="fatherYearOfBirth" value={formData.fatherYearOfBirth} onChange={handleChange} />
                                        </div>
                                        <div>
                                            <Label>Occupation</Label>
                                            <Input name="fatherOccupation" value={formData.fatherOccupation} onChange={handleChange} />
                                        </div>
                                        <div>
                                            <Label>Workplace</Label>
                                            <Input name="fatherWorkplace" value={formData.fatherWorkplace} onChange={handleChange} />
                                        </div>
                                        <div>
                                            <Label>Phone Number</Label>
                                            <Input name="fatherPhone" value={formData.fatherPhone} onChange={handleChange} />
                                        </div>
                                        <div>
                                            <Label>Email</Label>
                                            <Input type="email" name="fatherEmail" value={formData.fatherEmail} onChange={handleChange} />
                                        </div>
                                    </div>
                                </div>

                                {/* Mother */}
                                <div className="space-y-4 p-4 bg-pink-50 rounded-lg">
                                    <Label className="text-lg font-semibold">Mother Information</Label>
                                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                        <div>
                                            <Label>Full Name</Label>
                                            <Input name="motherName" value={formData.motherName} onChange={handleChange} />
                                        </div>
                                        <div>
                                            <Label>Year of Birth</Label>
                                            <Input type="number" name="motherYearOfBirth" value={formData.motherYearOfBirth} onChange={handleChange} />
                                        </div>
                                        <div>
                                            <Label>Occupation</Label>
                                            <Input name="motherOccupation" value={formData.motherOccupation} onChange={handleChange} />
                                        </div>
                                        <div>
                                            <Label>Workplace</Label>
                                            <Input name="motherWorkplace" value={formData.motherWorkplace} onChange={handleChange} />
                                        </div>
                                        <div>
                                            <Label>Phone Number</Label>
                                            <Input name="motherPhone" value={formData.motherPhone} onChange={handleChange} />
                                        </div>
                                        <div>
                                            <Label>Email</Label>
                                            <Input type="email" name="motherEmail" value={formData.motherEmail} onChange={handleChange} />
                                        </div>
                                    </div>
                                </div>

                                {/* Guardian */}
                                <div className="space-y-4 p-4 bg-purple-50 rounded-lg">
                                    <Label className="text-lg font-semibold">Guardian (if any)</Label>
                                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                        <div>
                                            <Label>Full Name</Label>
                                            <Input name="guardianName" value={formData.guardianName} onChange={handleChange} />
                                        </div>
                                        <div>
                                            <Label>Relationship</Label>
                                            <select
                                                name="guardianRelationship"
                                                value={formData.guardianRelationship}
                                                onChange={handleChange}
                                                className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                                            >
                                                <option value="">Select Relationship</option>
                                                {guardianRelationships.map(r => <option key={r} value={r}>{r}</option>)}
                                            </select>
                                        </div>
                                        <div>
                                            <Label>Số điện thoại</Label>
                                            <Input name="guardianPhone" value={formData.guardianPhone} onChange={handleChange} />
                                        </div>
                                        <div>
                                            <Label>Address</Label>
                                            <Input name="guardianAddress" value={formData.guardianAddress} onChange={handleChange} />
                                        </div>
                                    </div>
                                </div>
                            </div>
                        )}

                        {/* Tab 3: Academic History */}
                        {activeTab === 2 && (
                            <div className="space-y-6">
                                <div className="space-y-4">
                                    <Label className="text-lg font-semibold">Previous School</Label>
                                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                        <div className="md:col-span-2">
                                            <Label>School Name</Label>
                                            <Input name="previousSchool" value={formData.previousSchool} onChange={handleChange} />
                                        </div>
                                        <div className="md:col-span-2">
                                            <Label>Address</Label>
                                            <Input name="previousSchoolAddress" value={formData.previousSchoolAddress} onChange={handleChange} />
                                        </div>
                                        <div>
                                            <Label>From Date</Label>
                                            <Input type="date" name="previousSchoolFrom" value={formData.previousSchoolFrom} onChange={handleChange} />
                                        </div>
                                        <div>
                                            <Label>To Date</Label>
                                            <Input type="date" name="previousSchoolTo" value={formData.previousSchoolTo} onChange={handleChange} />
                                        </div>
                                        <div className="md:col-span-2">
                                            <Label>Transfer Reason</Label>
                                            <textarea
                                                name="transferReason"
                                                value={formData.transferReason}
                                                onChange={handleChange}
                                                className="flex min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                                            />
                                        </div>
                                    </div>
                                </div>

                                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                    <div>
                                        <Label>Academic Rank</Label>
                                        <select
                                            name="previousAcademicRank"
                                            value={formData.previousAcademicRank}
                                            onChange={handleChange}
                                            className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                                        >
                                            <option value="">Select Rank</option>
                                            {academicRanks.map(r => <option key={r} value={r}>{r}</option>)}
                                        </select>
                                    </div>
                                    <div>
                                        <Label>Conduct Rank</Label>
                                        <select
                                            name="previousConductRank"
                                            value={formData.previousConductRank}
                                            onChange={handleChange}
                                            className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                                        >
                                            <option value="">Select Rank</option>
                                            {conductRanks.map(r => <option key={r} value={r}>{r}</option>)}
                                        </select>
                                    </div>
                                </div>

                                <div>
                                    <Label>Awards</Label>
                                    <textarea
                                        name="awards"
                                        value={formData.awards}
                                        onChange={handleChange}
                                        placeholder="List of awards received..."
                                        className="flex min-h-[100px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                                    />
                                </div>
                            </div>
                        )}

                        {/* Tab 4: Health */}
                        {activeTab === 3 && (
                            <div className="space-y-6">
                                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                                    <div>
                                        <Label>Height (cm)</Label>
                                        <Input type="number" name="height" value={formData.height} onChange={handleChange} />
                                    </div>
                                    <div>
                                        <Label>Weight (kg)</Label>
                                        <Input type="number" name="weight" value={formData.weight} onChange={handleChange} />
                                    </div>
                                    <div>
                                        <Label>Blood Type</Label>
                                        <select
                                            name="bloodType"
                                            value={formData.bloodType}
                                            onChange={handleChange}
                                            className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                                        >
                                            <option value="">Select Blood Type</option>
                                            {bloodTypes.map(b => <option key={b} value={b}>{b}</option>)}
                                        </select>
                                    </div>
                                </div>

                                <div>
                                    <Label>Diseases (if any)</Label>
                                    <textarea
                                        name="diseases"
                                        value={formData.diseases}
                                        onChange={handleChange}
                                        className="flex min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                                    />
                                </div>

                                <div>
                                    <Label>Allergies (if any)</Label>
                                    <textarea
                                        name="allergies"
                                        value={formData.allergies}
                                        onChange={handleChange}
                                        className="flex min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                                    />
                                </div>

                                <div>
                                    <Label>Notes</Label>
                                    <textarea
                                        name="notes"
                                        value={formData.notes}
                                        onChange={handleChange}
                                        className="flex min-h-[100px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                                    />
                                </div>
                            </div>
                        )}

                        {/* Tab 5: Documents */}
                        {activeTab === 4 && (
                            <div className="space-y-6">
                                <div className="text-center py-12 text-gray-500">
                                    <p className="text-lg mb-2">📎 Document Upload Feature</p>
                                    <p className="text-sm">Will be developed in the next version</p>
                                    <p className="text-xs mt-4">Including: 3x4 Photo, Birth Certificate, Household Registration Book</p>
                                </div>
                            </div>
                        )}
                    </CardContent>

                    {/* Footer Actions */}
                    <div className="sticky bottom-0 bg-white border-t p-6 flex gap-3 justify-end">
                        <Button
                            type="button"
                            variant="outline"
                            onClick={() => onClose(false)}
                            disabled={loading}
                        >
                            Cancel
                        </Button>
                        <Button
                            type="submit"
                            className="bg-gradient-to-r from-violet-600 to-purple-600"
                            disabled={loading}
                        >
                            {loading ? (
                                <>
                                    <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin mr-2"></div>
                                    Saving...
                                </>
                            ) : (
                                <>
                                    <FiSave className="w-4 h-4 mr-2" />
                                    {student ? 'Update' : 'Save'}
                                </>
                            )}
                        </Button>
                    </div>
                </form>
            </Card>
        </div>
    );
}

export default StudentVNForm;
