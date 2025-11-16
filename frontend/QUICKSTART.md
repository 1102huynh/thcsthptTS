# 🚀 Frontend Quick Start Guide

## ⚡ Get Started in 5 Minutes

### Step 1: Install Dependencies
```bash
cd D:\learn\thcsthptTS\frontend
npm install
```

⏱️ **Time**: 3-5 minutes (first time only)

---

### Step 2: Ensure Backend is Running
Make sure your backend is running:
```bash
# In another terminal, from backend folder
java -jar target/school-management-system-1.0.0.jar
```

Backend should be accessible at: `http://localhost:8080`

---

### Step 3: Start Frontend Dev Server
```bash
npm start
```

⏱️ **Time**: 30 seconds

The app will automatically open in your browser at `http://localhost:3000`

---

## 🎯 First Login

### Test Credentials
```
Username: admin
Password: Test@123
```

### Other Available Users
```
principal / Test@123
teacher1 / Test@123
student1 / Test@123
librarian / Test@123
accountant / Test@123
```

---

## 📁 Project Structure Overview

```
src/
├── components/       # Reusable UI components
│   ├── Navbar.js
│   └── Sidebar.js
├── pages/            # Page components
│   ├── LoginPage.js
│   ├── Dashboard.js
│   ├── StaffManagement.js
│   └── ...
├── services/         # API services
│   ├── api.js        # Axios instance
│   ├── authService.js
│   └── dataService.js
├── App.js            # Main app component
└── index.js          # Entry point
```

---

## 🔄 Main Features

### ✅ Authentication
- Login page with credentials
- Session persistence
- Auto-logout on token expiry

### ✅ Dashboard
- Statistics (staff, students, books count)
- Quick actions
- System info

### ✅ Management Pages
- Staff management (view, add, edit, delete)
- Student management
- Library, Attendance, Grades, Fees (placeholders ready)

### ✅ Layout
- Responsive navbar
- Collapsible sidebar with role-based menu
- Mobile-friendly design

---

## 🛠️ Available Scripts

### Development
```bash
npm start          # Start dev server on port 3000
npm test           # Run tests
npm run build      # Build for production
npm run eject      # Eject from Create React App (irreversible!)
```

---

## 📡 Backend Connection

Frontend connects to backend at:
```
http://localhost:8080/api/v1
```

### API Endpoints Used
- POST `/auth/login` - User login
- GET `/staff` - List all staff
- GET `/students` - List all students
- GET `/library/books` - List all books
- And more...

---

## 🎨 Styling

- **CSS Framework**: Bootstrap 5
- **Component Library**: React Bootstrap
- **Icons**: React Icons
- **Custom Styling**: CSS files per component

### Key Colors
- Primary: #667eea (Purple)
- Success: #52c41a (Green)
- Info: #1890ff (Blue)

---

## 📱 Responsive Design

✅ Mobile: < 576px  
✅ Tablet: 576px - 768px  
✅ Desktop: 768px+  

All components are responsive and mobile-friendly.

---

## 🔐 Security

### Implemented
- JWT token authentication
- Token stored in localStorage
- Auto-logout on 401 errors
- CORS enabled

### To Add (Future)
- Refresh token mechanism
- .env for sensitive config
- Rate limiting
- Input validation

---

## 🐛 Common Issues

| Issue | Solution |
|-------|----------|
| "Cannot reach backend" | Ensure backend running on :8080 |
| "Blank page after login" | Check browser console for errors |
| "Styles not loading" | Clear cache: Ctrl+Shift+Del |
| "404 Not Found" | Check URL spelling |

---

## 📚 Next Steps

1. ✅ Start frontend: `npm start`
2. ✅ Login with test credentials
3. ✅ Explore dashboard
4. ✅ Test staff management
5. ✅ Review code structure
6. ✅ Implement additional features

---

## 🚀 Ready?

Start your frontend now:
```bash
cd D:\learn\thcsthptTS\frontend
npm install
npm start
```

**Then login with**: `admin` / `Test@123`

---

**Happy coding!** 🎉

