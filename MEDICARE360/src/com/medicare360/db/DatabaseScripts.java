package com.medicare360.db;

/**
 * All SQL used to create the database and fill it with demo data.
 * Statements are separated by semicolons (so please do not use a semicolon inside text values).
 * Every person, phone number and address below is fictional.
 */
final class DatabaseScripts {

    private DatabaseScripts() { }

    /** Creates the tables if they do not exist yet. */
    static final String SCHEMA = """
        CREATE TABLE IF NOT EXISTS admins (
            id            INTEGER PRIMARY KEY AUTOINCREMENT,
            username      TEXT NOT NULL UNIQUE,
            password_hash TEXT NOT NULL,
            full_name     TEXT NOT NULL
        );
        CREATE TABLE IF NOT EXISTS patients (
            id              INTEGER PRIMARY KEY AUTOINCREMENT,
            name            TEXT NOT NULL,
            age             INTEGER NOT NULL CHECK (age BETWEEN 0 AND 120),
            gender          TEXT NOT NULL,
            blood_group     TEXT NOT NULL,
            phone           TEXT NOT NULL,
            address         TEXT NOT NULL DEFAULT '',
            notes           TEXT NOT NULL DEFAULT '',
            registered_date TEXT NOT NULL
        );
        CREATE TABLE IF NOT EXISTS doctors (
            id               INTEGER PRIMARY KEY AUTOINCREMENT,
            name             TEXT NOT NULL,
            specialization   TEXT NOT NULL,
            phone            TEXT NOT NULL,
            email            TEXT NOT NULL,
            consultation_fee REAL NOT NULL CHECK (consultation_fee >= 0)
        );
        CREATE TABLE IF NOT EXISTS appointments (
            id         INTEGER PRIMARY KEY AUTOINCREMENT,
            patient_id INTEGER NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
            doctor_id  INTEGER NOT NULL REFERENCES doctors(id) ON DELETE CASCADE,
            appt_date  TEXT NOT NULL,
            appt_time  TEXT NOT NULL,
            reason     TEXT NOT NULL,
            status     TEXT NOT NULL DEFAULT 'Scheduled'
        );
        CREATE TABLE IF NOT EXISTS bills (
            id               INTEGER PRIMARY KEY AUTOINCREMENT,
            patient_id       INTEGER NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
            appointment_id   INTEGER REFERENCES appointments(id) ON DELETE SET NULL,
            consultation_fee REAL NOT NULL,
            medicine_charges REAL NOT NULL,
            other_charges    REAL NOT NULL,
            total            REAL NOT NULL,
            status           TEXT NOT NULL,
            bill_date        TEXT NOT NULL
        )
        """;

    /**
     * Demo data: 1 admin, 15 patients, 5 doctors, 10 appointments and 4 bills.
     * Appointment dates are calculated from today's date so the demo always looks fresh.
     */
    static final String SEED_DATA = """
        INSERT INTO admins (username, password_hash, full_name) VALUES
            ('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'System Administrator');

        INSERT INTO patients (name, age, gender, blood_group, phone, address, notes, registered_date) VALUES
            ('Aarav Sharma',    34, 'Male',   'B+',  '9876500001', '12 MG Road, Indiranagar',        'Hypertension, regular check-ups', date('now','localtime','-40 day')),
            ('Priya Nair',      28, 'Female', 'O+',  '9876500002', '45 5th Block, Koramangala',      'Migraine',                         date('now','localtime','-38 day')),
            ('Rahul Verma',     45, 'Male',   'A+',  '9876500003', '7 4th Main, Jayanagar',          'Type 2 diabetes',                  date('now','localtime','-35 day')),
            ('Sneha Reddy',     39, 'Female', 'AB+', '9876500004', '88 Outer Ring Road, Marathahalli','Thyroid monitoring',              date('now','localtime','-33 day')),
            ('Arjun Menon',      7, 'Male',   'O+',  '9876500005', '21 Sector 2, HSR Layout',        'Frequent seasonal cold',           date('now','localtime','-30 day')),
            ('Divya Krishnan',  52, 'Female', 'B-',  '9876500006', '14 8th Cross, Malleshwaram',     'Arthritis',                        date('now','localtime','-28 day')),
            ('Vikram Singh',    61, 'Male',   'A-',  '9876500007', '3 Dr Rajkumar Road, Rajajinagar','Heart patient, on medication',     date('now','localtime','-26 day')),
            ('Ishita Gupta',    23, 'Female', 'O-',  '9876500008', '56 2nd Stage, BTM Layout',       'Skin allergy',                     date('now','localtime','-24 day')),
            ('Kabir Joshi',      5, 'Male',   'B+',  '9876500009', '9 Hebbal Main Road, Hebbal',     'Routine vaccinations',             date('now','localtime','-22 day')),
            ('Lakshmi Bhat',    70, 'Female', 'A+',  '9876500010', '31 Gandhi Bazaar, Basavanagudi', 'Knee pain, osteoporosis',          date('now','localtime','-20 day')),
            ('Manoj Patil',     41, 'Male',   'AB-', '9876500011', '67 ITPL Road, Whitefield',       'Back pain',                        date('now','localtime','-18 day')),
            ('Neha Kapoor',     31, 'Female', 'B+',  '9876500012', '102 JP Nagar 6th Phase',         'None',                             date('now','localtime','-15 day')),
            ('Sanjay Hegde',    57, 'Male',   'O+',  '9876500013', '18 Yelahanka New Town',          'High cholesterol',                 date('now','localtime','-12 day')),
            ('Tanvi Shetty',    19, 'Female', 'A+',  '9876500014', '5 Banashankari 2nd Stage',       'Acne treatment',                   date('now','localtime','-9 day')),
            ('Yash Agarwal',    26, 'Male',   'B-',  '9876500015', '40 Electronic City Phase 1',     'Asthma',                           date('now','localtime','-6 day'));

        INSERT INTO doctors (name, specialization, phone, email, consultation_fee) VALUES
            ('Dr. Ananya Rao',     'Cardiology',       '9812300001', 'ananya.rao@medicare360.demo',     800),
            ('Dr. Rohan Mehta',    'General Medicine', '9812300002', 'rohan.mehta@medicare360.demo',    500),
            ('Dr. Kavita Iyer',    'Pediatrics',       '9812300003', 'kavita.iyer@medicare360.demo',    600),
            ('Dr. Suresh Kulkarni','Orthopedics',      '9812300004', 'suresh.kulkarni@medicare360.demo',700),
            ('Dr. Meera Desai',    'Dermatology',      '9812300005', 'meera.desai@medicare360.demo',    650);

        INSERT INTO appointments (patient_id, doctor_id, appt_date, appt_time, reason, status) VALUES
            (1,  1, date('now','localtime','+1 day'),  '10:00', 'Blood pressure review',        'Scheduled'),
            (5,  3, date('now','localtime','+1 day'),  '11:30', 'Fever and cough',              'Scheduled'),
            (10, 4, date('now','localtime','+2 day'),  '09:30', 'Knee pain consultation',       'Scheduled'),
            (8,  5, date('now','localtime','+2 day'),  '14:00', 'Skin allergy check',           'Scheduled'),
            (3,  2, date('now','localtime','+3 day'),  '10:30', 'Diabetes follow-up',           'Scheduled'),
            (7,  1, date('now','localtime','-3 day'),  '15:00', 'Chest discomfort follow-up',   'Completed'),
            (6,  4, date('now','localtime','-5 day'),  '11:00', 'Joint stiffness',              'Completed'),
            (12, 2, date('now','localtime','-2 day'),  '16:00', 'Seasonal flu',                 'Completed'),
            (9,  3, date('now','localtime','-1 day'),  '10:00', 'Routine vaccination',          'Cancelled'),
            (13, 1, date('now','localtime','+4 day'),  '12:00', 'ECG consultation',             'Cancelled');

        INSERT INTO bills (patient_id, appointment_id, consultation_fee, medicine_charges, other_charges, total, status, bill_date) VALUES
            (7,  6,    800, 250, 100, 1150, 'Paid',    date('now','localtime','-3 day')),
            (6,  7,    700, 400,   0, 1100, 'Paid',    date('now','localtime','-5 day')),
            (12, 8,    500, 180,  50,  730, 'Pending', date('now','localtime','-2 day')),
            (4,  NULL, 500,   0, 150,  650, 'Paid',    date('now','localtime','-4 day'))
        """;
}
