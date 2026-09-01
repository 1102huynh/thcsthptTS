# Seed full class rosters

One-off data-generation script used to fill out realistic-sized student
rosters (and matching attendance/fee/grade data) for local dev/demo, since
the base `TEST_DATA_CORRECTED.sql` seed only has a handful of students per
class. Not part of the app or the build - run manually when you want more
data to develop/demo against (e.g. Dashboard charts, DataTable pagination,
attendance/fee/grade pages all look sparse with only a few students).

## What it does

1. **`01-create-students.mjs`** - creates real student accounts through the
   actual REST API (`POST /v1/users` then `POST /v1/students`), because
   that's the only way to get a correctly BCrypt-hashed password - a raw SQL
   insert can't do that. Fills whichever classes/targets you configure in
   the `PLAN` array up to a target roster size, generating realistic
   Vietnamese names (họ + tên đệm + tên), addresses, parent info, etc.
   Writes `created-students.json` (which students it made) for the next
   script to target.
2. **`02-seed-related-data.mjs`** - direct SQL (`mysql2`) bulk-inserts
   Attendance (last 15 weekdays), Fees (tuition + health insurance for the
   configured academic year), and Grade (the legacy `grades` table - see
   `IMPLEMENTATION_PLAN.md` Tuần 4 Ngày 3-4's note on why that one, not the
   newer `grade_records`) rows for exactly the students `01` just created,
   plus creates the ACTIVE academic year row if none exists yet. Direct SQL
   here (not the REST API) because none of these tables need password
   hashing and going through HTTP one row at a time for 1000+ rows would be
   far too slow.

Both scripts are idempotent-ish in that re-running `01` with a higher
`targetCount` only creates the delta, and `02` only touches the specific
student ids passed to it - but re-running `02` for the *same* students
twice will duplicate their attendance/fee/grade rows (no unique-constraint
guard), so don't.

## Usage

```bash
cd backend/scripts/seed-full-rosters
npm install
# Edit the ADMIN_PASSWORD/PLAN constants at the top of each file first.
node 01-create-students.mjs
node 02-seed-related-data.mjs
```

Requires the backend running locally (`http://localhost:8080/api`) and
direct MySQL access using the same credentials as `backend/.env`.
