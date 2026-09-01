// Creates student accounts through the real REST API (POST /v1/users then
// POST /v1/students) - a raw SQL insert can't produce a correctly
// BCrypt-hashed password, so this has to go through the actual service.
// See README.md.
import fs from 'fs';

const BASE = process.env.API_BASE_URL || 'http://localhost:8080/api';
const ADMIN_USERNAME = 'admin';
const ADMIN_PASSWORD = 'Test@123'; // edit if your local admin password differs

// Each entry: fill this className/section up to targetCount students,
// starting rollNumber/admissionNumber after whatever already exists.
// existingCount/nextAdmissionSeq must match your current data - check via
// GET /v1/classes and GET /v1/students first, or this will create
// duplicate roll/admission numbers and the API will reject them.
const PLAN = [
  { className: '10', section: 'A', existingCount: 3, targetCount: 25, birthYear: 2009 },
  { className: '10', section: 'B', existingCount: 3, targetCount: 25, birthYear: 2009 },
  { className: '9', section: 'A', existingCount: 0, targetCount: 25, birthYear: 2010 },
  { className: '9', section: 'B', existingCount: 0, targetCount: 25, birthYear: 2010 },
];
const STARTING_ADMISSION_SEQ = 7; // next unused ADM### number

const HO = ['Nguyễn', 'Trần', 'Lê', 'Phạm', 'Hoàng', 'Huỳnh', 'Phan', 'Vũ', 'Võ', 'Đặng', 'Bùi', 'Đỗ', 'Hồ', 'Ngô', 'Dương', 'Lý'];
const DEM_MALE = ['Văn', 'Đức', 'Minh', 'Quốc', 'Hữu', 'Công', 'Anh', 'Thành'];
const DEM_FEMALE = ['Thị', 'Ngọc', 'Thanh', 'Thu', 'Kim', 'Diễm', 'Hồng', 'Mỹ'];
const TEN_MALE = ['Anh', 'Bảo', 'Dũng', 'Đạt', 'Hùng', 'Khang', 'Long', 'Minh', 'Nam', 'Phong', 'Quang', 'Sơn', 'Tài', 'Thắng', 'Tuấn', 'Việt', 'Vinh', 'Hải', 'Huy', 'Khôi'];
const TEN_FEMALE = ['An', 'Chi', 'Diệp', 'Giang', 'Hà', 'Hoa', 'Huyền', 'Lan', 'Linh', 'Mai', 'My', 'Nga', 'Ngọc', 'Nhi', 'Phương', 'Quyên', 'Thảo', 'Trang', 'Vy', 'Yến'];
const CITIES = ['Hà Nội', 'Đà Nẵng', 'TP. Hồ Chí Minh', 'Hải Phòng', 'Cần Thơ'];
const OCCUPATIONS = ['Kỹ sư', 'Giáo viên', 'Bác sĩ', 'Kinh doanh', 'Công nhân', 'Kế toán', 'Nông dân', 'Luật sư'];

function pick(arr) { return arr[Math.floor(Math.random() * arr.length)]; }
function pad(n, len) { return String(n).padStart(len, '0'); }

function randomName(isMale) {
  const ho = pick(HO);
  const dem = isMale ? pick(DEM_MALE) : pick(DEM_FEMALE);
  const ten = isMale ? pick(TEN_MALE) : pick(TEN_FEMALE);
  return { firstName: ho, lastName: `${dem} ${ten}` };
}

function randomPhone() {
  return '09' + Array.from({ length: 8 }, () => Math.floor(Math.random() * 10)).join('');
}

async function login() {
  const res = await fetch(`${BASE}/v1/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username: ADMIN_USERNAME, password: ADMIN_PASSWORD }),
  });
  if (!res.ok) throw new Error(`login failed: ${res.status} ${await res.text()}`);
  const data = await res.json();
  return data.accessToken;
}

async function createStudent(token, { className, section, gradeYearBirth, rollSeq, admissionSeq }) {
  const isMale = Math.random() < 0.5;
  const { firstName, lastName } = randomName(isMale);
  const username = `hs${pad(admissionSeq, 3)}`;
  const email = `${username}@school.edu.vn`;
  const rollNumber = `${className}${section}${pad(rollSeq, 3)}`;
  const admissionNumber = `ADM${pad(admissionSeq, 3)}`;

  const userRes = await fetch(`${BASE}/v1/users`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
    body: JSON.stringify({
      username, email, password: 'Test@123', firstName, lastName,
      phoneNumber: randomPhone(), role: 'STUDENT',
    }),
  });
  if (!userRes.ok) throw new Error(`create user ${username} failed: ${userRes.status} ${await userRes.text()}`);
  const { userId } = await userRes.json();

  const dob = `${gradeYearBirth}-${pad(1 + Math.floor(Math.random() * 12), 2)}-${pad(1 + Math.floor(Math.random() * 28), 2)}`;
  const father = randomName(true);
  const mother = randomName(false);

  const studentRes = await fetch(`${BASE}/v1/students`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
    body: JSON.stringify({
      user: { id: userId },
      rollNumber,
      admissionNumber,
      dateOfBirth: dob,
      // Matches backend/../frontend/src/lib/enumLabels.js's GENDER_LABELS
      // values (MALE/FEMALE) so these edit correctly through the UI later.
      gender: isMale ? 'MALE' : 'FEMALE',
      bloodGroup: pick(['A+', 'A-', 'B+', 'B-', 'O+', 'O-', 'AB+', 'AB-']),
      className,
      section,
      dateOfAdmission: '2024-08-15',
      status: 'ACTIVE',
      fatherName: `${father.firstName} ${father.lastName}`,
      fatherPhone: randomPhone(),
      fatherOccupation: pick(OCCUPATIONS),
      motherName: `${mother.firstName} ${mother.lastName}`,
      motherPhone: randomPhone(),
      motherOccupation: pick(OCCUPATIONS),
      address: `Số ${1 + Math.floor(Math.random() * 200)} đường ${pick(['Lê Lợi', 'Trần Phú', 'Nguyễn Huệ', 'Hai Bà Trưng', 'Lý Thường Kiệt'])}`,
      city: pick(CITIES),
      state: pick(CITIES),
      postalCode: String(100000 + Math.floor(Math.random() * 900000)),
      emergencyContactName: `${father.firstName} ${father.lastName}`,
      emergencyContactPhone: randomPhone(),
      emergencyContactRelation: 'Cha',
    }),
  });
  if (!studentRes.ok) throw new Error(`create student ${rollNumber} failed: ${studentRes.status} ${await studentRes.text()}`);
  const student = await studentRes.json();
  return { ...student, username };
}

async function main() {
  const token = await login();
  console.log('logged in');

  let admissionSeq = STARTING_ADMISSION_SEQ;
  const created = [];

  for (const cls of PLAN) {
    const toCreate = cls.targetCount - cls.existingCount;
    console.log(`\n${cls.className}${cls.section}: creating ${toCreate} students...`);
    for (let i = 0; i < toCreate; i++) {
      const rollSeq = cls.existingCount + i + 1;
      try {
        const student = await createStudent(token, { ...cls, gradeYearBirth: cls.birthYear, rollSeq, admissionSeq });
        created.push({ ...student, className: cls.className, section: cls.section });
        process.stdout.write('.');
      } catch (err) {
        console.error(`\n  FAILED (roll ${cls.className}${cls.section}${rollSeq}):`, err.message);
      }
      admissionSeq++;
    }
  }

  console.log(`\n\nCreated ${created.length} students.`);
  fs.writeFileSync('created-students.json', JSON.stringify(created, null, 2));
  console.log('Saved to created-students.json - run 02-seed-related-data.mjs next.');
}

main().catch((err) => { console.error('FATAL:', err); process.exit(1); });
