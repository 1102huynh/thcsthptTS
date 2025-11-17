# 🏫 School Management System

A comprehensive full-stack application for managing school operations built with Spring Boot backend and React frontend.

## 🚀 Quick Start

1. **Backend Setup** - See [`backend/QUICKSTART.md`](./backend/QUICKSTART.md)
2. **Frontend Setup** - See [`frontend/QUICKSTART.md`](./frontend/QUICKSTART.md)
3. **Database Setup** - See [`backend/DATABASE_SETUP_INDEX.md`](./backend/DATABASE_SETUP_INDEX.md)

## 📁 Project Structure

```
thcsthptTS/
├── backend/           # Spring Boot API
│   ├── src/           # Java source code
│   ├── pom.xml        # Maven dependencies
│   └── *.md          # Backend documentation
├── frontend/          # React application
│   ├── src/           # React source code
│   ├── package.json   # NPM dependencies
│   └── *.md          # Frontend documentation
└── *.bat             # Quick start scripts
```

## 🔧 Technology Stack

### Backend
- **Java 17** with Spring Boot 3.1.5
- **Spring Security** with JWT authentication
- **PostgreSQL** database (Aiven Cloud)
- **Maven** for dependency management
- **Swagger/OpenAPI** for API documentation

### Frontend
- **React 18** with TypeScript
- **Material-UI** for components
- **Axios** for HTTP requests
- **React Router** for navigation
- **NPM** for package management

## 📋 Features

- 🔐 **Authentication & Authorization** - JWT-based security
- 👥 **User Management** - Admin, Principal, Teachers, Students
- 🏛️ **Staff Management** - Employee records and roles
- 🎓 **Student Management** - Student profiles and academic records
- 📚 **Library Management** - Book catalog and borrowing system
- 📊 **Attendance Tracking** - Daily attendance management
- 🎯 **Grade Management** - Student grades and assessments
- 💰 **Fee Management** - Fee collection and payment tracking
- 📈 **Reports** - Various academic and administrative reports

## 🎯 Quick Actions

### Start the Application
```bash
# Start backend
cd backend
mvn spring-boot:run

# Start frontend (in another terminal)
cd frontend
npm start
```

### Import Test Data
```bash
# Import sample data to PostgreSQL
psql -U avnadmin -h school-clinicbooking.c.aivencloud.com -p 14143 -d defaultdb -f backend/TEST_DATA_CORRECTED.sql
```

### Test Login
- **URL:** http://localhost:3000
- **Admin:** admin / Test@123
- **Principal:** principal / Test@123
- **Teacher:** teacher1 / Test@123

## 📚 Documentation

| Component | Documentation |
|-----------|--------------|
| **Backend API** | [`backend/README.md`](./backend/README.md) |
| **Database Setup** | [`backend/DATABASE_SETUP_INDEX.md`](./backend/DATABASE_SETUP_INDEX.md) |
| **API Testing** | [`backend/API_TESTING_GUIDE.md`](./backend/API_TESTING_GUIDE.md) |
| **Architecture** | [`backend/ARCHITECTURE.md`](./backend/ARCHITECTURE.md) |
| **Frontend App** | [`frontend/README.md`](./frontend/README.md) |
| **Development** | [`backend/DEVELOPMENT_GUIDE.md`](./backend/DEVELOPMENT_GUIDE.md) |

## 🌐 Application URLs

- **Frontend:** http://localhost:3000
- **Backend API:** http://localhost:8080/api
- **Swagger UI:** http://localhost:8080/api/swagger-ui.html
- **API Docs:** http://localhost:8080/api/v3/api-docs

## 🗄️ Database

- **Type:** PostgreSQL (Aiven Cloud)
- **Host:** school-clinicbooking.c.aivencloud.com
- **Port:** 14143
- **Database:** defaultdb
- **Test Data:** `backend/TEST_DATA_CORRECTED.sql`

## 🚦 Status

✅ **Backend:** Fully functional with JWT authentication  
✅ **Database:** PostgreSQL configured with test data  
✅ **Frontend:** React dashboard with login  
✅ **Documentation:** Clean and organized

---

**Last Updated:** November 17, 2025  
**Version:** 1.0.0  
**Status:** Production Ready
**Folder**: `D:\learn\thcsthptTS\backend\`

#### Quick Start
```bash
cd D:\learn\thcsthptTS\backend
java -jar target/school-management-system-1.0.0.jar
```
**Runs on**: http://localhost:8080

#### Important Files
- `TEST_DATA_CORRECTED.sql` - Test data import
- `pom.xml` - Maven dependencies
- `src/` - All Java source code
- `target/` - Compiled JAR file

#### Documentation
- `README.md` - Complete backend guide
- `BUILD_SUCCESS.md` - Build information
- `DEVELOPMENT_GUIDE.md` - Developer guide
- `API_TESTING_GUIDE.md` - API testing

---

### Frontend (React)
**Folder**: `D:\learn\thcsthptTS\frontend\`

#### Quick Start
```bash
cd D:\learn\thcsthptTS\frontend
npm install
npm start
```
**Runs on**: http://localhost:3000

#### Important Files
- `package.json` - Dependencies
- `src/App.js` - Main app
- `src/pages/` - Page components
- `src/components/` - UI components
- `src/services/` - API services

#### Documentation
- `README.md` - Complete guide
- `QUICKSTART.md` - 5-minute setup
- `FRONTEND_SETUP.md` - Detailed setup
- `FRONTEND_FILE_INVENTORY.md` - File listing

---

## 🚀 GETTING STARTED

### 1. Setup Backend
```bash
cd D:\learn\thcsthptTS\backend
mvn -s settings.xml clean install
# OR just run the JAR
java -jar target/school-management-system-1.0.0.jar
```

### 2. Setup Frontend
```bash
cd D:\learn\thcsthptTS\frontend
npm install
npm start
```

### 3. Login
Open http://localhost:3000
```
Username: admin
Password: Test@123
```

---

## 📊 AVAILABLE TEST CREDENTIALS

**All passwords**: Test@123

| Username | Role | Access |
|----------|------|--------|
| admin | ADMIN | Full system |
| principal | PRINCIPAL | School admin |
| teacher1 | TEACHER | Classes |
| student1 | STUDENT | Student records |
| librarian | LIBRARIAN | Library |
| accountant | ACCOUNTANT | Finance |

---

## 📚 DOCUMENTATION INDEX

### Backend Documentation
**Location**: `D:\learn\thcsthptTS\backend\`

| File | Purpose |
|------|---------|
| README.md | Complete backend guide |
| BUILD_SUCCESS.md | Build fix & status |
| DEVELOPMENT_GUIDE.md | Developer handbook |
| API_TESTING_GUIDE.md | API testing methods |
| TEST_LOGIN_ENDPOINT.md | Login testing |
| LOGIN_TEST_EXAMPLES.md | Test examples |
| TEST_DATA_DELIVERY.md | Test data info |
| QUICKSTART.md | Quick setup |

### Frontend Documentation
**Location**: `D:\learn\thcsthptTS\frontend\`

| File | Purpose |
|------|---------|
| README.md | Complete frontend guide |
| QUICKSTART.md | 5-minute setup |
| FRONTEND_SETUP.md | Detailed setup |
| FRONTEND_FILE_INVENTORY.md | File listing |

### Project Documentation
**Location**: `D:\learn\thcsthptTS\`

| File | Purpose |
|------|---------|
| COMPLETE_SYSTEM_SUMMARY.md | Overall summary |
| PROJECT_INDEX.md | This file |

---

## 🔗 QUICK LINKS

### Backend API
```
Base URL: http://localhost:8080/api/v1

Popular Endpoints:
- POST   /auth/login              (Authentication)
- GET    /staff                   (List staff)
- GET    /students                (List students)
- GET    /library/books           (List books)
- GET    /attendance/student/{id} (Student attendance)
- GET    /grades/student/{id}     (Student grades)
- GET    /fees/student/{id}       (Student fees)

Swagger UI: http://localhost:8080/api/swagger-ui.html
```

### Frontend Pages
```
http://localhost:3000/                    (Dashboard)
http://localhost:3000/staff               (Staff Management)
http://localhost:3000/students            (Student Management)
http://localhost:3000/library             (Library Management)
http://localhost:3000/attendance          (Attendance)
http://localhost:3000/grades              (Grades)
http://localhost:3000/fees                (Fees)
```

---

## 🎯 WHAT'S INCLUDED

### Backend (Java Spring Boot)
- ✅ 54+ Java classes
- ✅ 53+ REST API endpoints
- ✅ 8 complete modules
- ✅ JWT authentication
- ✅ Role-based access control
- ✅ Database integration
- ✅ Complete documentation

### Frontend (React)
- ✅ 24 files
- ✅ 1,000+ lines of code
- ✅ 8 page modules
- ✅ Responsive design
- ✅ API integration
- ✅ Complete documentation

### Database (MySQL)
- ✅ 10 tables
- ✅ Complete schema
- ✅ Test data included
- ✅ All relationships

---

## 🛠️ TECH STACK

```
Backend:
  - Java 17
  - Spring Boot 3.1.5
  - Spring Security 6.x
  - MySQL 8.0
  - Maven

Frontend:
  - React 18.2
  - React Router 6.14
  - Bootstrap 5.3
  - Axios 1.4
  - React Icons

Database:
  - MySQL 8.0+
  - JDBC driver
```

---

## 📱 FEATURES

### User Features
- ✅ Secure authentication
- ✅ Dashboard with statistics
- ✅ Staff management
- ✅ Student management
- ✅ Library management
- ✅ Attendance tracking
- ✅ Grade management
- ✅ Fee management

### Technical Features
- ✅ REST API architecture
- ✅ JWT token authentication
- ✅ Role-based authorization
- ✅ Responsive UI
- ✅ API error handling
- ✅ Database persistence

---

## 🐛 TROUBLESHOOTING

| Issue | Solution |
|-------|----------|
| Cannot connect to backend | Check if running on :8080 |
| Cannot connect to frontend | Check if running on :3000 |
| npm install fails | Clear cache: `npm cache clean --force` |
| Port already in use | Kill process or use different port |
| Database error | Import TEST_DATA_CORRECTED.sql |
| Blank page | Check browser console for errors |

---

## ✅ QUICK COMMANDS

### Backend
```bash
cd D:\learn\thcsthptTS\backend

# Build
mvn -s settings.xml clean install

# Run
java -jar target/school-management-system-1.0.0.jar

# View API docs
# Open: http://localhost:8080/api/swagger-ui.html
```

### Frontend
```bash
cd D:\learn\thcsthptTS\frontend

# Install
npm install

# Start dev server
npm start

# Build for production
npm run build

# Run tests
npm test
```

### Database
```bash
# Import test data
mysql -u root -p school_management < TEST_DATA_CORRECTED.sql

# Connect to MySQL
mysql -u root -p school_management

# View tables
SHOW TABLES;

# Count records
SELECT COUNT(*) FROM users;
```

---

## 📊 PROJECT STATISTICS

| Metric | Value |
|--------|-------|
| Total Files | 100+ |
| Java Classes | 54+ |
| React Components | 10+ |
| CSS Files | 6 |
| Documentation Files | 20+ |
| API Endpoints | 53+ |
| Database Tables | 10 |
| Test Users | 13 |
| Lines of Code | 6,000+ |
| Lines of Docs | 3,000+ |

---

## 🚀 DEPLOYMENT

### Development
```bash
# Terminal 1
java -jar backend/target/school-management-system-1.0.0.jar

# Terminal 2
cd frontend && npm start
```

### Production
```bash
# Build frontend
cd frontend && npm run build

# Run backend with JAR
java -jar backend/target/school-management-system-1.0.0.jar

# Deploy built files
# Copy frontend/build to web server
```

### Docker (Optional)
```bash
# Build backend Docker image
docker build -t school-management:backend backend/

# Build frontend Docker image  
docker build -t school-management:frontend frontend/

# Run with docker-compose
docker-compose up
```

---

## 🎓 LEARNING PATH

1. **Understand Architecture**
   - Read: `COMPLETE_SYSTEM_SUMMARY.md`

2. **Setup Backend**
   - Follow: `backend/README.md`
   - Run: `java -jar target/...jar`

3. **Setup Frontend**
   - Follow: `frontend/QUICKSTART.md`
   - Run: `npm install && npm start`

4. **Test API**
   - Open: Swagger UI
   - Try endpoints

5. **Explore Code**
   - Review backend controllers
   - Review React components

6. **Extend & Customize**
   - Add new features
   - Modify UI
   - Add business logic

---

## ✅ VERIFICATION

Run this checklist:

- [ ] Backend running on :8080
- [ ] Frontend running on :3000
- [ ] Can login with admin/Test@123
- [ ] Dashboard shows statistics
- [ ] Staff page loads data
- [ ] Student page loads data
- [ ] No console errors
- [ ] No network errors
- [ ] All navigation works

---

## 🎉 YOU'RE ALL SET!

Your complete School Management System is ready!

### To Start Right Now:
```bash
# Terminal 1: Backend
cd D:\learn\thcsthptTS\backend
java -jar target/school-management-system-1.0.0.jar

# Terminal 2: Frontend
cd D:\learn\thcsthptTS\frontend
npm install && npm start
```

### Then:
1. Open http://localhost:3000
2. Login with: admin / Test@123
3. Explore the system!

---

## 📞 NEED HELP?

1. **Backend Issues**: Check `backend/README.md`
2. **Frontend Issues**: Check `frontend/QUICKSTART.md`
3. **API Issues**: Check `backend/API_TESTING_GUIDE.md`
4. **Database Issues**: Import `TEST_DATA_CORRECTED.sql`

---

**System Created**: November 16, 2025  
**Status**: ✅ Complete & Ready  
**Version**: 1.0.0

---

# 🚀 Happy Coding! Enjoy Your School Management System! 🎊

