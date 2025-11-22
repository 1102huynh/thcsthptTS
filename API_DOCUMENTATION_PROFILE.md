# API Documentation - Student Profile Update

## New Endpoints Added

### 1. Get Student by User ID
**Endpoint**: `GET /api/v1/students/user/{userId}`

**Authorization**: Required
- Roles: ADMIN, PRINCIPAL, TEACHER, STUDENT

**Path Parameters**:
- `userId` (Long): The ID of the user associated with the student

**Response** (200 OK):
```json
{
  "id": 1,
  "rollNumber": "STU001",
  "admissionNumber": "ADM001",
  "user": {
    "id": 5,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@school.com",
    "username": "johndoe",
    "role": "STUDENT"
  },
  "dateOfBirth": "2009-01-15",
  "gender": "Male",
  "bloodGroup": "O+",
  "className": "10A",
  "section": "A",
  "dateOfAdmission": "2022-06-15",
  "status": "ACTIVE",
  "fatherName": "Mr. Parent Name",
  "fatherPhone": "+84 900 000 001",
  "fatherOccupation": "Engineer",
  "motherName": "Mrs. Parent Name",
  "motherPhone": "+84 900 000 002",
  "motherOccupation": "Teacher",
  "address": "123 Main Street, City",
  "city": "Ho Chi Minh",
  "state": "Ho Chi Minh",
  "postalCode": "70000",
  "emergencyContactName": "Emergency Contact",
  "emergencyContactPhone": "+84 900 000 003",
  "emergencyContactRelation": "Uncle",
  "createdAt": "2022-06-15T10:30:00",
  "updatedAt": "2024-11-22T15:45:00"
}
```

**Error Response** (404 Not Found):
```json
{
  "message": "Student not found with user id: 999"
}
```

### 2. Update Student Profile
**Endpoint**: `PUT /api/v1/students/{id}`

**Authorization**: Required
- Roles: ADMIN, PRINCIPAL (for updating other students), STUDENT (updating own record)

**Path Parameters**:
- `id` (Long): The ID of the student to update

**Request Body**:
```json
{
  "dateOfBirth": "2009-01-15",
  "gender": "Male",
  "bloodGroup": "O+",
  "className": "10A",
  "section": "A",
  "fatherName": "Mr. Parent Name",
  "fatherPhone": "+84 900 000 001",
  "fatherOccupation": "Engineer",
  "motherName": "Mrs. Parent Name",
  "motherPhone": "+84 900 000 002",
  "motherOccupation": "Teacher",
  "address": "123 Main Street, City",
  "city": "Ho Chi Minh",
  "state": "Ho Chi Minh",
  "postalCode": "70000",
  "emergencyContactName": "Emergency Contact",
  "emergencyContactPhone": "+84 900 000 003",
  "emergencyContactRelation": "Uncle"
}
```

**Response** (200 OK):
```json
{
  "id": 1,
  "rollNumber": "STU001",
  "admissionNumber": "ADM001",
  "user": {
    "id": 5,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@school.com",
    "username": "johndoe",
    "role": "STUDENT"
  },
  "dateOfBirth": "2009-01-15",
  "gender": "Male",
  "bloodGroup": "O+",
  "className": "10A",
  "section": "A",
  "dateOfAdmission": "2022-06-15",
  "status": "ACTIVE",
  "fatherName": "Mr. Parent Name",
  "fatherPhone": "+84 900 000 001",
  "fatherOccupation": "Engineer",
  "motherName": "Mrs. Parent Name",
  "motherPhone": "+84 900 000 002",
  "motherOccupation": "Teacher",
  "address": "123 Main Street, City",
  "city": "Ho Chi Minh",
  "state": "Ho Chi Minh",
  "postalCode": "70000",
  "emergencyContactName": "Emergency Contact",
  "emergencyContactPhone": "+84 900 000 003",
  "emergencyContactRelation": "Uncle",
  "createdAt": "2022-06-15T10:30:00",
  "updatedAt": "2024-11-22T16:00:00"
}
```

**Error Responses**:
- 404 Not Found: Student with given ID doesn't exist
- 400 Bad Request: Invalid data provided
- 403 Forbidden: User doesn't have permission to update this student

## Frontend Integration

### JavaScript Service Methods

#### Get Student by User ID
```javascript
import { studentService } from '../services/dataService';

// Usage
const response = await studentService.getByUserId(userId);
const studentData = response.data;
```

#### Update Student Profile
```javascript
import { studentService } from '../services/dataService';

// Prepare update data
const updateData = {
  dateOfBirth: '2009-01-15',
  gender: 'Male',
  address: '123 Main Street, City',
  fatherName: 'Mr. Parent Name',
  fatherPhone: '+84 900 000 001',
  motherName: 'Mrs. Parent Name',
  motherPhone: '+84 900 000 002'
};

// Usage
await studentService.update(studentId, updateData);
```

## Implementation Details

### Database Changes
- No schema changes were required
- Added repository method to query by userId (JPA will handle the SQL)

### API Route Order
Routes are ordered from most specific to least specific:
1. `/user/{userId}` - Get by user ID
2. `/roll/{rollNumber}` - Get by roll number
3. `/{id}` - Get by student ID
4. `` - Get all students

This order prevents route conflicts.

### Security
- All endpoints require authentication
- Authorization checks are performed based on user role
- Students can update their own profile
- Admins/Principals can update any student profile

### Data Validation
- Non-editable fields (rollNumber, admissionNumber, className, section, dateOfAdmission) are not updated
- Duplicate roll numbers and admission numbers are prevented
- All input is validated at the service layer

## Changelog

### Version 1.0 (2024-11-22)
- Added `GET /api/v1/students/user/{userId}` endpoint
- Added `getStudentByUserId()` method to StudentService
- Added `findByUserId()` method to StudentRepository
- Added `getByUserId()` method to frontend studentService
- Implemented profile save functionality in StudentPortal component
- Added error handling and user feedback

