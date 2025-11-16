package com.schoolmanagement.entity;

public enum Permission {
    // Staff Management
    CREATE_STAFF,
    READ_STAFF,
    UPDATE_STAFF,
    DELETE_STAFF,

    // Student Management
    CREATE_STUDENT,
    READ_STUDENT,
    UPDATE_STUDENT,
    DELETE_STUDENT,

    // Library Management
    CREATE_BOOK,
    READ_BOOK,
    UPDATE_BOOK,
    DELETE_BOOK,
    BORROW_BOOK,
    RETURN_BOOK,

    // Class Management
    CREATE_CLASS,
    READ_CLASS,
    UPDATE_CLASS,
    DELETE_CLASS,
    ASSIGN_TEACHER,

    // Grade Management
    CREATE_GRADE,
    READ_GRADE,
    UPDATE_GRADE,
    DELETE_GRADE,

    // Attendance Management
    CREATE_ATTENDANCE,
    READ_ATTENDANCE,
    UPDATE_ATTENDANCE,
    DELETE_ATTENDANCE,

    // Fee Management
    CREATE_FEE,
    READ_FEE,
    UPDATE_FEE,
    DELETE_FEE,
    PROCESS_PAYMENT,

    // Report Generation
    GENERATE_REPORT,
    VIEW_REPORT,

    // System Administration
    MANAGE_USERS,
    MANAGE_ROLES,
    VIEW_LOGS,
    SYSTEM_CONFIG
}

