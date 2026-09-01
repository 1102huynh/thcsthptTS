// Direct SQL (not the REST API) bulk-inserts Attendance/Fee/Grade rows for
// exactly the students 01-create-students.mjs just created, plus creates
// the ACTIVE academic year row if none exists. None of these tables need
// password hashing, and 1000+ rows one HTTP call at a time would be far
// too slow - see README.md.
import mysql from 'mysql2/promise';
import fs from 'fs';

const DB_CONFIG = {
  host: process.env.DB_HOST || 'localhost',
  port: Number(process.env.DB_PORT || 3306),
  user: process.env.DB_USERNAME || 'root',
  password: process.env.DB_PASSWORD || 'root',
  database: process.env.DB_NAME || 'school_management',
};
const ACADEMIC_YEAR = '2024-2025';
const ATTENDANCE_WEEKDAYS = 15;
const TUITION_AMOUNT = 5_000_000;
const INSURANCE_AMOUNT = 300_000;
const GRADE_SUBJECTS = ['Toán', 'Ngữ văn', 'Tiếng Anh'];
const CHUNK = 500;

function pick(arr) { return arr[Math.floor(Math.random() * arr.length)]; }
function fmtDate(d) { return d.toISOString().slice(0, 10); }

// `columnsPerRow` is just the data columns - every row also gets a
// trailing NOW(), NOW() for created_at/updated_at, appended here rather
// than passed as bind params.
async function insertChunked(conn, table, columns, rows) {
  const sql = `INSERT INTO ${table} (${columns.join(', ')}, created_at, updated_at) VALUES `;
  for (let i = 0; i < rows.length; i += CHUNK) {
    const chunk = rows.slice(i, i + CHUNK);
    const placeholders = chunk
      .map((row) => `(${row.map(() => '?').join(', ')}, NOW(), NOW())`)
      .join(', ');
    await conn.query(sql + placeholders, chunk.flat());
    process.stdout.write('.');
  }
  console.log('');
}

async function main() {
  const created = JSON.parse(fs.readFileSync('created-students.json', 'utf8'));
  const ids = created.map((s) => s.id);
  console.log(`Targeting ${ids.length} students from created-students.json`);

  const conn = await mysql.createConnection(DB_CONFIG);

  // --- Academic year ---
  const [existingYears] = await conn.execute('SELECT id FROM academic_years WHERE name = ?', [ACADEMIC_YEAR]);
  if (existingYears.length) {
    console.log(`Academic year ${ACADEMIC_YEAR} already exists`);
  } else {
    await conn.execute(
      'INSERT INTO academic_years (name, start_date, end_date, status, created_at, updated_at) VALUES (?, ?, ?, ?, NOW(), NOW())',
      [ACADEMIC_YEAR, '2024-08-15', '2025-05-31', 'ACTIVE']
    );
    console.log(`Created academic year ${ACADEMIC_YEAR}`);
  }

  const [teacherUsers] = await conn.execute(
    "SELECT s.id AS staff_id, u.id AS user_id FROM staff s JOIN users u ON u.id = s.user_id WHERE s.position = 'TEACHER'"
  );
  if (!teacherUsers.length) throw new Error('No TEACHER staff found - seed at least one teacher first');

  // --- Attendance: last N weekdays ---
  const weekdays = [];
  const cursor = new Date();
  while (weekdays.length < ATTENDANCE_WEEKDAYS) {
    cursor.setDate(cursor.getDate() - 1);
    const day = cursor.getDay();
    if (day !== 0 && day !== 6) weekdays.push(fmtDate(new Date(cursor)));
  }
  const statusPool = [
    ...Array(88).fill('PRESENT'), ...Array(5).fill('ABSENT'),
    ...Array(4).fill('LATE'), ...Array(2).fill('SICK_LEAVE'), ...Array(1).fill('LEAVE_APPROVED'),
  ];
  const attRows = [];
  for (const date of weekdays) {
    for (const id of ids) attRows.push([id, date, pick(statusPool), pick(teacherUsers).user_id]);
  }
  console.log(`\nInserting ${attRows.length} attendance rows...`);
  await insertChunked(conn, 'attendance', ['student_id', 'attendance_date', 'status', 'marked_by'], attRows);

  // --- Fees: tuition + health insurance ---
  const feeRows = [];
  for (const id of ids) {
    const roll = Math.random();
    let status, paidAmount, remainingAmount, paidDate;
    if (roll < 0.6) {
      status = 'PAID'; paidAmount = TUITION_AMOUNT; remainingAmount = 0; paidDate = '2024-09-10';
    } else if (roll < 0.85) {
      status = 'PARTIAL_PAID';
      paidAmount = Math.round(TUITION_AMOUNT * (0.3 + Math.random() * 0.4) / 10000) * 10000;
      remainingAmount = TUITION_AMOUNT - paidAmount;
      paidDate = '2024-09-20';
    } else {
      status = 'PENDING'; paidAmount = 0; remainingAmount = TUITION_AMOUNT; paidDate = null;
    }
    feeRows.push([id, ACADEMIC_YEAR, 'Học phí', TUITION_AMOUNT, '2024-09-15', paidDate, status, paidAmount, remainingAmount]);

    const insurancePaid = Math.random() < 0.7;
    feeRows.push([
      id, ACADEMIC_YEAR, 'Bảo hiểm y tế', INSURANCE_AMOUNT, '2024-10-01',
      insurancePaid ? '2024-09-25' : null, insurancePaid ? 'PAID' : 'PENDING',
      insurancePaid ? INSURANCE_AMOUNT : 0, insurancePaid ? 0 : INSURANCE_AMOUNT,
    ]);
  }
  console.log(`Inserting ${feeRows.length} fee rows...`);
  await insertChunked(
    conn,
    'fees',
    ['student_id', 'academic_year', 'fee_type', 'amount', 'due_date', 'paid_date', 'status', 'paid_amount', 'remaining_amount'],
    feeRows
  );

  // --- Grades (legacy `grades` table - see IMPLEMENTATION_PLAN.md Tuần 4
  //     Ngày 3-4's note on why, not the newer grade_records) ---
  const gradeRows = [];
  for (const id of ids) {
    for (const subject of GRADE_SUBJECTS) {
      const marksObtained = Math.round((4 + Math.random() * 6) * 10) / 10;
      const totalMarks = 10;
      const percentage = Math.round((marksObtained / totalMarks) * 1000) / 10;
      const letter = marksObtained >= 8.5 ? 'A' : marksObtained >= 7 ? 'B' : marksObtained >= 5.5 ? 'C' : marksObtained >= 4 ? 'D' : 'F';
      gradeRows.push([id, subject, 'Giữa kỳ', marksObtained, totalMarks, percentage, letter, pick(teacherUsers).staff_id, ACADEMIC_YEAR]);
    }
  }
  console.log(`Inserting ${gradeRows.length} grade rows...`);
  await insertChunked(
    conn,
    'grades',
    ['student_id', 'subject', 'exam_type', 'marks_obtained', 'total_marks', 'percentage', 'grade', 'teacher_id', 'academic_year'],
    gradeRows
  );

  await conn.end();
  console.log('\nDone.');
}

main().catch((err) => { console.error('FATAL:', err); process.exit(1); });
