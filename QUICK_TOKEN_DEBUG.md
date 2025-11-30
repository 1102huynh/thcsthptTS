# ⚡ QUICK ACTION - Find the Token Issue

**Problem**: Token works for 1st API call, fails for 2nd API call
**Solution**: Enhanced logging to find where token is lost

---

## 🚀 Do This Now

### Step 1: Clear & Login Fresh
```javascript
// In browser console:
localStorage.clear()
// Then hard refresh: Ctrl+F5
// Then login as student1
```

### Step 2: Open Console & Network
```
F12
Click "Console" tab
Also keep "Network" tab visible
```

### Step 3: Go to Timetable Tab
```
Click Timetable in StudentPortal
Both API calls will be triggered
```

### Step 4: Check Console Logs

Look for lines like:
```
API Request: { hasToken: true/false, ... }
```

**Key question**: Does the timetable request show `hasToken: true` or `false`?

### Step 5: Check Network Tab

For timetable API request:
```
Right-click request → "Inspect"
Go to "Headers" tab
Scroll to "Request Headers"
Look for: Authorization: Bearer [token]

Is it there? YES ✅ or NO ❌
```

### Step 6: Copy & Share

Copy these from console:
- Student API Request logs
- Timetable API Request logs
- Any error messages

And from Network:
- Does timetable request have Authorization header?

---

## 📊 What It Means

### ✅ hasToken: true + Authorization header present
Token is being sent correctly
→ Problem is on backend (token validation)

### ❌ hasToken: false + No Authorization header
Token is NOT being sent
→ Problem is token not in localStorage OR being cleared

### ✅ hasToken: true + No Authorization header
Token is in storage but not being added to header
→ Problem is request interceptor

---

## 🎯 Most Likely Cause

Based on server logs showing:
```
Set SecurityContextHolder to anonymous SecurityContext
```

My guess: **hasToken: false** for timetable request

This means token is being lost or cleared between requests.

---

## 📞 What to Share

Just run the above steps and tell me:

1. **Does console show hasToken: true or false** for timetable?
2. **Does Network tab show Authorization header** in timetable request?
3. **Copy the exact console output** for both requests

With this info, I can fix it immediately! ✅

---

## 💡 The Problem is Likely:

Token is stored successfully for first request, but then:
- Gets cleared somehow
- OR localStorage becomes unavailable
- OR request interceptor not running on second request

The enhanced logging will show exactly which! 🔍

