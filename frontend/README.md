# 🚀 School Management System - Frontend

Modern React application for school management with Material-UI components.

## 🚀 Quick Start

```bash
# Install dependencies
npm install

# Start development server
npm start

# Access application
http://localhost:3000
```

## 🔧 Technology Stack

- **React 18** - UI framework
- **TypeScript** - Type safety
- **Material-UI** - Component library
- **React Router** - Navigation
- **Axios** - HTTP client
- **CSS Modules** - Styling

## 📁 Project Structure

```
frontend/
├── public/          # Static assets
├── src/
│   ├── components/  # Reusable UI components
│   │   ├── Navbar.js
│   │   └── Sidebar.js
│   ├── pages/       # Page components
│   │   ├── LoginPage.js
│   │   ├── Dashboard.js
│   │   ├── StaffManagement.js
│   │   └── StudentManagement.js
│   ├── services/    # API services
│   │   ├── api.js
│   │   ├── authService.js
│   │   └── dataService.js
│   ├── utils/       # Utilities
│   ├── styles/      # Global styles
│   └── App.js       # Main app component
├── package.json
└── README.md
```

## 🔑 Authentication

**Test Credentials:**
- **Admin:** admin / Test@123
- **Principal:** principal / Test@123
- **Teacher:** teacher1 / Test@123
- **Student:** student1 / Test@123

## 🎯 Features

- 🔐 **Login System** - JWT-based authentication
- 📊 **Dashboard** - Role-based dashboard views
- 👥 **User Management** - Admin user controls
- 🏛️ **Staff Management** - Employee management
- 🎓 **Student Management** - Student records
- 📚 **Library System** - Book management
- 📈 **Reports** - Various management reports

## 🌐 API Integration

**Backend API:** http://localhost:8080/api

**Key Services:**
- `authService.js` - Authentication logic
- `dataService.js` - CRUD operations
- `api.js` - Axios configuration

## 🎨 UI Components

**Navigation:**
- Responsive navbar
- Collapsible sidebar
- Role-based menu items

**Pages:**
- Login with validation
- Dashboard with cards and charts
- Management pages with data tables
- Modal forms for CRUD operations

## 🔧 Development

### Available Scripts

```bash
npm start      # Development server (port 3000)
npm build      # Production build
npm test       # Run tests
npm run eject  # Eject from Create React App
```

### Environment Variables

Create `.env` file:
```env
REACT_APP_API_URL=http://localhost:8080/api
REACT_APP_API_TIMEOUT=5000
```

### Code Style

- **ESLint** - Code linting
- **Prettier** - Code formatting
- **TypeScript** - Type checking

## 📱 Responsive Design

- **Desktop:** Full sidebar navigation
- **Tablet:** Collapsible sidebar
- **Mobile:** Hamburger menu

## 🚦 Status

✅ **Authentication:** Login/logout working  
✅ **Routing:** React Router configured  
✅ **API Integration:** Backend connected  
✅ **UI Components:** Material-UI implemented  
✅ **Responsive:** Mobile-friendly design  

## 🔗 Related Documentation

- **Backend API:** [../backend/README.md](../backend/README.md)
- **Quick Start:** [QUICKSTART.md](./QUICKSTART.md)
- **Project Index:** [../PROJECT_INDEX.md](../PROJECT_INDEX.md)

---

**Port:** 3000  
**Build Tool:** Create React App  
**Version:** 1.0.0  
**Last Updated:** November 17, 2025

```
frontend/
├── public/
│   ├── index.html
│   └── favicon.ico
├── src/
│   ├── components/
│   │   ├── Navbar.js
│   │   ├── Navbar.css
│   │   ├── Sidebar.js
│   │   └── Sidebar.css
│   ├── pages/
│   │   ├── LoginPage.js
│   │   ├── LoginPage.css
│   │   ├── Dashboard.js
│   │   ├── Dashboard.css
│   │   ├── StaffManagement.js
│   │   ├── StudentManagement.js
│   │   ├── ManagementPages.js
│   │   └── Management.css
│   ├── services/
│   │   ├── api.js
│   │   ├── authService.js
│   │   └── dataService.js
│   ├── utils/
│   ├── styles/
│   ├── App.js
│   ├── App.css
│   ├── index.js
│   └── index.css
├── package.json
├── .gitignore
└── README.md
```

---

## 🔧 Installation & Setup

### Prerequisites
- Node.js (v14+)
- npm or yarn

### Steps

1. **Navigate to frontend folder**
```bash
cd D:\learn\thcsthptTS\frontend
```

2. **Install dependencies**
```bash
npm install
```

3. **Start development server**
```bash
npm start
```

The app will open at `http://localhost:3000`

---

## 🎯 Features Implemented

### ✅ Authentication
- Login page with form validation
- JWT token management
- Session persistence
- Logout functionality

### ✅ Layout Components
- Responsive Navbar with user dropdown
- Collapsible Sidebar with role-based menu
- Main content area
- Footer (ready to add)

### ✅ Pages
- Login Page
- Dashboard with statistics
- Staff Management (view, add, edit, delete)
- Student Management
- Library Management (placeholder)
- Attendance Management (placeholder)
- Grade Management (placeholder)
- Fee Management (placeholder)

### ✅ Services
- API integration with backend
- Authentication service
- Data services (staff, student, library, etc.)
- Axios interceptors for token handling

### ✅ Styling
- Bootstrap 5 integration
- Custom CSS for each component
- Responsive design
- Smooth transitions and animations

---

## 🔑 Test Credentials

All test credentials have password: **Test@123**

```
admin (Admin - Full Access)
principal (Principal - School Admin)
teacher1 (Teacher)
student1 (Student)
librarian (Librarian)
accountant (Accountant)
```

---

## 📡 Backend Connection

The frontend connects to the backend API at:
```
http://localhost:8080/api/v1
```

Make sure the backend is running before starting the frontend.

---

## 📦 Dependencies

### Core
- `react` - UI library
- `react-dom` - DOM rendering
- `react-router-dom` - Client-side routing
- `bootstrap` - CSS framework
- `react-bootstrap` - Bootstrap components

### API & Auth
- `axios` - HTTP client
- `jwt-decode` - JWT token decoding

### State Management
- `zustand` - Light-weight state management (optional)

### Icons & Utils
- `react-icons` - Icon library
- `date-fns` - Date utilities

---

## 🧩 Component Details

### Navbar
- Shows user info and role
- Dropdown menu with user options
- Logout button
- Toggle sidebar button

### Sidebar
- Role-based menu items
- Navigation links
- Responsive design
- Smooth animations

### LoginPage
- Username and password fields
- Error handling
- Loading state
- Test credentials display

### Dashboard
- Statistics cards (staff, students, books)
- Quick actions panel
- System information
- Fetches data from backend

---

## 🚀 Running the Application

### Development Mode
```bash
npm start
```

### Build for Production
```bash
npm run build
```

### Run Tests
```bash
npm test
```

---

## 🔄 API Integration

### Authentication Flow
1. User enters credentials
2. Frontend calls POST `/auth/login`
3. Backend returns JWT tokens
4. Frontend stores tokens in localStorage
5. All subsequent requests include token in Authorization header

### Data Flow
1. Component mounts
2. useEffect fetches data from API
3. Data stored in component state
4. Component renders with data
5. User interactions trigger CRUD operations

---

## 🎨 Styling System

### Colors
- Primary: #667eea (Purple)
- Secondary: #764ba2
- Success: #52c41a (Green)
- Danger: #ff4d4f (Red)
- Warning: #faad14 (Yellow)

### Spacing
- Uses Bootstrap spacing utilities
- Custom padding/margin for consistency
- Responsive breakpoints

### Animations
- Smooth transitions (0.3s)
- Hover effects
- Transform animations
- Shadow effects

---

## 🔐 Security Features

### Implemented
- JWT token-based auth
- Token stored in localStorage
- Automatic logout on 401 response
- Protected routes
- Environment variables for API URL (to be added)

### To Add
- .env file for configuration
- Token refresh mechanism
- HTTPS enforcement
- CORS configuration
- Rate limiting

---

## 📱 Responsive Design

### Breakpoints
- Mobile: < 576px
- Tablet: 576px - 768px
- Desktop: 768px - 1200px
- Large: > 1200px

### Mobile Features
- Collapsible sidebar
- Touch-friendly buttons
- Optimized table layout
- Mobile-first CSS

---

## 🐛 Troubleshooting

### Issue: Connection refused
**Solution**: Ensure backend is running on port 8080

### Issue: CORS error
**Solution**: Backend has CORS configured, check backend logs

### Issue: Page blank after login
**Solution**: Check browser console for errors, verify backend is responding

### Issue: Styling not loading
**Solution**: Clear cache and reinstall: `rm -rf node_modules && npm install`

---

## 📚 Next Steps

1. Implement complete Staff Management CRUD
2. Implement complete Student Management CRUD
3. Implement Library, Attendance, Grade, Fee management pages
4. Add form validation
5. Add error boundaries
6. Add loading skeletons
7. Add pagination and filtering
8. Add export to PDF/Excel
9. Add real-time notifications
10. Add dark mode

---

## 🚀 Deployment

### Deploy to Vercel
```bash
npm install -g vercel
vercel
```

### Deploy to Netlify
```bash
npm install -g netlify-cli
netlify deploy --prod --dir=build
```

### Docker
See `Dockerfile` (to be created)

---

## 📝 Environment Variables

Create `.env` file:
```
REACT_APP_API_URL=http://localhost:8080/api/v1
REACT_APP_ENV=development
```

---

**Created**: November 16, 2025  
**Version**: 1.0.0  
**Status**: ✅ Ready for Development

