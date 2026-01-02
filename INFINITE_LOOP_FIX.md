# 🐛 INFINITE LOOP FIX - Backend API Calls

## Vấn Đề
Frontend đang gọi backend API liên tục (infinite loop) với các requests:
- `GET /admissions?page=0&size=10`
- `GET /news?page=0&size=3`
- `GET /error?page=0&size=10`

## Nguyên Nhân
1. **PrincipalHomePage** component load khi user chưa login
2. useEffect gọi API `newsService.getPublishedNews()` và `admissionService.getOpenAdmissions()`
3. Khi API fail (401 hoặc error khác), error handler vẫn cố retry
4. Component re-render trigger useEffect lại → infinite loop

## Đã Fix
✅ **File: `frontend/src/services/api.js`** (Line 85-90)
- Vô hiệu hoá auto-redirect khi 401 error  
- Prevent infinite loop by not reloading page

## Cần Làm Thêm

### 1. Đảm Bảo Backend Chạy
```bash
cd backend
java -jar target/school-management-system-1.0.0.jar
```

### 2. Kiểm Tra Endpoints
Backend PHẢI có các endpoints public này:
- `GET /api/news` - Public (no auth required) ✅ Đã config  
- `GET /api/admissions` - Public (no auth required) ✅ Đã config

### 3. Test API Trực Tiếp
```bash
# Test news endpoint
curl http://localhost:8080/api/news?page=0&size=3

# Test admissions endpoint  
curl http://localhost:8080/api/admissions?page=0&size=10
```

### 4. Nếu Vẫn Còn Loop

**Option A**: Tắt tạm PrincipalHomePage
```javascript
// In App.js line 104, thay vì:
<Route path="/" element={<PrincipalHomePage />} />

// Đổi thành:
<Route path="/" element={<Navigate to="/login" />} />
```

**Option B**: Sử dụng static data
- PrincipalHomePage đã có fallback data tĩnh
- Nếu API fail, sẽ dùng data mẫu thay vì retry

## Kiểm Tra Fix
1. Mở browser console (F12)
2. Reload page (Ctrl+R)
3. Kiểm tra Network tab - KHÔNG nên thấy requests lặp lại liên tục
4. Nếu thấy "/error" requests → Backend có vấn đề với endpoints

## Debug Commands
```bash
# Kill all node processes nếu cần
taskkill /F /IM node.exe

# Restart frontend
cd frontend
npm start

# Check backend logs
# Xem console của backend server
```

---

**Status**: ✅ Fixed in api.js  
**Next**: Test và verify không còn infinite loop
