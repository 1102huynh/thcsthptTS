# 🔍 TIMETABLE - Debugging "No message available" Error

**Problem**: Still seeing "Error: No message available"
**Status**: 🔍 INVESTIGATING - Enhanced debugging

---

## 🎯 What We've Improved

### 1. Enhanced Error Extraction (StudentPortal.js)
Now logs:
```javascript
console.error('Error caught:', err);           // Full error object
console.error('Error type:', typeof err);      // Type of error
console.error('Error string:', String(err));   // String representation
```

And checks:
- ✅ err?.response?.data?.message
- ✅ err?.response?.data?.error
- ✅ err?.response?.statusText
- ✅ err?.message
- ✅ String conversion
- ✅ toString() method

### 2. Enhanced API Interceptor (api.js)
Now logs complete error details:
```javascript
console.error('API Error:', {
  status: error.response?.status,
  statusText: error.response?.statusText,
  data: error.response?.data,
  message: error.message,
  errorString: String(error),
});
```

And sets meaningful message from response if available.

---

## 🧪 How to Debug

### Step 1: Open Browser Console
- Press F12
- Click "Console" tab

### Step 2: Clear Console
- Right-click → Clear console

### Step 3: Trigger the Error
```
1. Make sure backend is STOPPED
2. Refresh the page
3. Login
4. Navigate to Timetable tab
```

### Step 4: Check Console Output
You should see multiple logs:

```javascript
// From API interceptor:
API Error: {
  status: [some number],
  statusText: "[error description]",
  data: { ... },
  message: "[error message]",
  errorString: "[error as string]"
}

// From TimetableTab:
Error caught: [Error object]
Error type: object
Error string: [string representation]

Final error message: [extracted message]
```

---

## 📋 What to Look For

### Console Logs Priority

1. **Check API Error log first**
   ```
   API Error: { status: 500, statusText: "...", data: {...} }
   ```
   - What status code?
   - What statusText?
   - What data?

2. **Check Error caught log**
   ```
   Error caught: Error: [something]
   Error type: object
   Error string: [something]
   ```
   - What does the error object contain?
   - What's the string representation?

3. **Check Final error message log**
   ```
   Final error message: [what user sees]
   ```
   - What message is being set?

---

## 🔍 Common Issues & Solutions

### Issue 1: Backend Returns 404 or 500
```
API Error: {
  status: 404,
  statusText: "Not Found",
  data: { error: "Resource not found" }
}
```
**Solution**: Make sure:
- Backend is running on port 8080
- API endpoint exists: `/api/v1/timetables/class/{id}`
- Student with that ID exists in database

### Issue 2: Backend Returns Error in Wrong Format
```
API Error: {
  status: 400,
  statusText: "Bad Request",
  data: { error_details: "Invalid input" }  // NOT "message"
}
```
**Solution**: Check what field backend uses for error message
- Is it `message`?
- Is it `error`?
- Is it something else?

### Issue 3: Network Error (No Response)
```
API Error: {
  status: undefined,
  statusText: undefined,
  data: undefined,
  message: "Network Error" or "ECONNREFUSED"
}
```
**Solution**: Make sure:
- Backend is running
- Port 8080 is accessible
- No firewall blocking requests

### Issue 4: CORS Error
```
API Error: {
  status: undefined,
  statusText: undefined,
  data: undefined,
  message: "Network Error"
}
```
**Solution**: Check CORS configuration in backend

---

## 📊 Console Output Examples

### ✅ Successful Load
```
Current User: { id: 1, username: 'student1', ... }
Retrieved UserId: 1 Type: number
Student Data: { className: '10', section: 'A', ... }
Class Mapping - ClassName: 10 Section: A ClassID: 1 Type: number
Timetable loaded successfully: 30 entries
```

### ❌ Error During Load
```
Error caught: Error: [something]
Error type: object
Error string: [string]

API Error: {
  status: [number or undefined],
  statusText: "[text]",
  data: { ... },
  message: "[message]",
  errorString: "[string]"
}

Final error message: [extracted message should appear here]
```

---

## 🎯 What to Report

When sharing the error, please include:

1. **Full console output** (screenshot or copy-paste all logs)
2. **Network tab** (DevTools → Network)
   - What request was made?
   - What was the response?
   - What status code?
3. **Backend console**
   - Is backend running?
   - Any errors on backend side?

---

## 🧪 Testing Steps

### Test 1: Check if Backend is Reachable
```javascript
// In browser console, type:
fetch('http://localhost:8080/api/v1/timetables/class/1')
  .then(r => r.json())
  .then(d => console.log('Success:', d))
  .catch(e => console.log('Error:', e))
```

Expected:
- ✅ Shows data if backend running
- ❌ Shows error if backend stopped

### Test 2: Check Student Data is Loading
Look for:
```
Student Data: { className: '10', section: 'A', ... }
```

If this log appears → Student data loading works ✅
If not → Issue is before timetable API call

### Test 3: Check Timetable API Call
Look for either:
```
Timetable loaded successfully: 30 entries  ✅
```
OR:
```
API Error: { status: ... }  ❌
```

---

## 🔧 Next Steps

1. **Open browser console (F12)**
2. **Clear it**
3. **Stop backend** (or keep it running)
4. **Navigate to Timetable**
5. **Copy ALL console logs**
6. **Share them here**

Based on the logs, I can identify exactly where the error message is being lost!

---

## 📞 Questions to Answer

When you see the error, please check:

- [ ] Is backend running?
- [ ] What's the API status code? (404, 500, etc. or no connection?)
- [ ] What do the console logs show?
- [ ] Can you fetch the API manually from console?
- [ ] Does student data load successfully?

---

**Status**: 🔍 Waiting for console logs to debug further!

Share the console output and I'll identify the exact issue! 🔍

