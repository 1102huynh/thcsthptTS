# 🔍 TIMETABLE - DETAILED DEBUGGING FOR TOKEN ISSUE

**Issue**: 401 Unauthorized on timetable request (but student request works)
**Status**: 🔍 Enhanced logging added to diagnose

---

## 📊 What The Server Logs Show

```
Authorized method invocation: getStudentByUserId ✅ SUCCESS
Set SecurityContextHolder to anonymous SecurityContext ⚠️ LOST AUTHENTICATION
Securing GET /v1/timetables/class/1 ❌ NOW NO TOKEN
Secured GET /v1/timetables/class/1 ❌ FAILS - REDIRECTS TO /error
```

### Translation:
1. ✅ First request (getStudentByUserId) → Works fine, has token
2. ❌ Between requests → Token gets lost or becomes invalid
3. ❌ Second request (getTimetable) → No token, becomes anonymous
4. ❌ Server returns 401 redirect to error page

---

## 🎯 Root Causes

### Possibility 1: Token Not Being Sent
The Authorization header might not be included in the timetable request

### Possibility 2: Token Format Wrong
Token might be stored without "Bearer " prefix

### Possibility 3: Token Expires Between Requests
First request uses valid token, second request uses expired token

### Possibility 4: CORS/Headers Issue
Headers being stripped by browser or backend

---

## 🧪 How to Debug NOW

### Step 1: Clear Cache & Login Fresh
```javascript
// In console:
localStorage.clear()
sessionStorage.clear()
```
Then hard refresh (Ctrl+F5) and login again.

### Step 2: Open DevTools Network Tab
```
F12 → Network tab
```

### Step 3: Go to Timetable Tab
```
Click Timetable in StudentPortal
This triggers both API requests
```

### Step 4: Check Console Logs
You should see these detailed logs now:

```javascript
// FIRST REQUEST (Student)
API Request: {
  method: "get",
  url: "/v1/students/user/1",
  hasToken: true,
  tokenLength: [some number],
  tokenPrefix: "Bearer eyJhbGci..."
}
Authorization header set: "Bearer eyJhbGci..."

// SECOND REQUEST (Timetable)
API Request: {
  method: "get",
  url: "/v1/timetables/class/1?academicYear=2024-2025",
  hasToken: true or false ← THIS IS KEY!
  tokenLength: [number or 0],
  tokenPrefix: "Bearer..." or "NO TOKEN"
}
```

### Step 5: Check Network Tab
For EACH request, click it and check:

#### Student Request:
```
Headers → Request Headers
Authorization: Bearer [token]? ✅ Should be present
```

#### Timetable Request:
```
Headers → Request Headers  
Authorization: Bearer [token]? ✅ Should be present
                            ❌ OR MISSING?
```

---

## 📋 What to Look For in Console

### ✅ GOOD SCENARIO
```javascript
// Student request
API Request: {
  hasToken: true,
  tokenLength: 150,
  tokenPrefix: "Bearer eyJhbGci..."
}
Authorization header set: "Bearer eyJhbGci..."

API Response Success: {
  status: 200,
  statusText: "OK",
  dataLength: 200
}

// Timetable request  
API Request: {
  hasToken: true,
  tokenLength: 150,
  tokenPrefix: "Bearer eyJhbGci..."
}
Authorization header set: "Bearer eyJhbGci..."

API Response Success: {
  status: 200,
  statusText: "OK",
  dataLength: 1200
}
```

### ❌ BAD SCENARIO (What's happening now)
```javascript
// Student request
API Request: {
  hasToken: true,
  tokenLength: 150,
  tokenPrefix: "Bearer eyJhbGci..."
}
Authorization header set: "Bearer eyJhbGci..."

API Response Success: {
  status: 200,
  ...
}

// Timetable request
API Request: {
  hasToken: false ← ⚠️ TOKEN GONE!
  tokenLength: 0,
  tokenPrefix: "NO TOKEN"
}

NO TOKEN FOUND IN LOCALSTORAGE ← ⚠️ THIS IS THE PROBLEM!

API Error Response: {
  status: 401,
  statusText: "Unauthorized",
  authHeader: "MISSING" ← ⚠️ CONFIRMED
  ...
}
```

---

## 🔧 What to Report

When you test, take note of:

1. **After login**, does localStorage have accessToken?
   ```javascript
   // In console:
   localStorage.getItem('accessToken')
   // Should show: "eyJhbGci..." (some token)
   // If shows: null → TOKEN NOT BEING SAVED!
   ```

2. **For Student request**, is token in Authorization header?
   - Network tab → student request → Headers
   - Should see: `Authorization: Bearer ...`

3. **For Timetable request**, is token in Authorization header?
   - Network tab → timetable request → Headers
   - Should see: `Authorization: Bearer ...`
   - OR is it missing?

4. **Console logs** - share the exact output

---

## 📞 Share These Details

When you test and see the error, please share:

1. **Console log output** for:
   - Student API Request
   - Timetable API Request
   - Any error messages

2. **Network tab screenshot** showing:
   - Student request headers (has Authorization?)
   - Timetable request headers (has Authorization?)

3. **localStorage contents**:
   ```javascript
   Object.keys(localStorage).map(k => k + ': ' + localStorage.getItem(k).substring(0, 30))
   ```

---

## 🎯 Possible Fixes Based on What We Find

### If token is missing from localStorage:
- Fix: Check login response
- Make sure login endpoint returns accessToken
- Make sure it's being saved correctly

### If token is in localStorage but not sent:
- Fix: May need to restart browser
- Or there's an issue with request interceptor timing

### If token is sent but 401 still happens:
- Fix: Token validation on backend
- Or token format is wrong
- Or backend doesn't recognize token source

---

## 🚀 Do This Now

1. **Clear everything**:
   ```javascript
   localStorage.clear()
   ```

2. **Hard refresh**: Ctrl+F5

3. **Login fresh**

4. **Open Network + Console tabs** (F12)

5. **Go to Timetable tab**

6. **Copy these logs**:
   - All console API logs
   - Network tab Authorization headers
   - localStorage keys

7. **Share the output**

Then I can pinpoint exactly why the token is missing! 🔍

