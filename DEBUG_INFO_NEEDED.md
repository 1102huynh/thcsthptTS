# 🔍 DEBUG CHECKLIST - Cần thông tin từ bạn!

## **HÃY TRẢ LỜI CÁC CÂU HỎI SAU:**

### **1. Lỗi ở đâu?**
- [ ] Trang cũ (Staff, Students, Dashboard)?
- [ ] Trang mới (Classes, Subjects, Assignments)?
- [ ] Tất cả trang?
- [ ] Một trang cụ thể nào?

**Trang nào bị lỗi:**
```
http://localhost:3000/???
```

---

### **2. Lỗi gì?**
- [ ] "Failed to load data"?
- [ ] "No message available"?
- [ ] 401 Unauthorized?
- [ ] 404 Not Found?
- [ ] 500 Server Error?
- [ ] Lỗi khác?

**Error message cụ thể:**
```
(Copy error message ở đây)
```

---

### **3. Backend có chạy không?**
```bash
# Check xem có process ở port 8080 không
netstat -ano | findstr :8080
```

**Có thấy process không:**
- [ ] Có (có số PID)
- [ ] Không (rỗng)

---

### **4. Backend console hiển thị gì?**

**Khi access trang bị lỗi, backend terminal hiển thị:**
```
(Copy 10-20 dòng cuối từ backend terminal)
```

**Có thấy error AFTER restart không:**
- [ ] Có - Copy error message
- [ ] Không - Backend chạy bình thường

---

### **5. Browser console (F12) hiển thị gì?**

**Mở DevTools (F12) → Console tab:**
```
(Copy error messages từ console)
```

**Mở DevTools (F12) → Network tab:**
- Request nào failed?
- Status code là gì? (401/404/500?)
- Response body là gì?

---

### **6. Health Check kết quả:**

**Vào:** `http://localhost:3000/health`

**Kết quả:**
- [ ] Tất cả GREEN
- [ ] Tất cả YELLOW (401)
- [ ] Có RED - Endpoint nào?
- [ ] Network Error

**Screenshot kết quả health check!**

---

## **QUICK TESTS:**

### **Test 1: Direct backend call**
```bash
curl http://localhost:8080/api/classes
```
**Kết quả:** (paste response)

### **Test 2: Auth endpoint**
```bash
curl http://localhost:8080/v1/staff
```
**Kết quả:** (paste response)

### **Test 3: Check authentication**
**Trong browser console:**
```javascript
localStorage.getItem('accessToken')
```
**Có token không:**
- [ ] Có (hiển thị token)
- [ ] Không (null)

---

## **RESTART CHECKLIST:**

**Bạn đã làm những bước sau chưa:**
- [ ] Stop backend (Ctrl+C)
- [ ] Restart backend (mvn spring-boot:run)
- [ ] Đợi thấy "Started SchoolManagementApplication"
- [ ] Clear browser cache (Ctrl+Shift+R)
- [ ] Logout và login lại

---

## **CUNG CẤP THÔNG TIN:**

**Để tôi fix được, cần:**

1. ✅ **Trang nào bị lỗi** (URL cụ thể)
2. ✅ **Error message** (từ browser hoặc backend)
3. ✅ **Backend logs** (10-20 dòng cuối)
4. ✅ **Browser console errors** (F12 → Console)
5. ✅ **Network tab** (F12 → Network → failed requests)

**Hoặc:**
- Screenshot trang bị lỗi
- Screenshot backend terminal
- Screenshot browser console (F12)

---

**HÃY GỬI THÔNG TIN TRÊN CHO TÔI!** 🔍
