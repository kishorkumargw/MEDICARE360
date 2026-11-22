# MEDICARE360 - Hospital Management System

A beginner-friendly desktop application for running the front desk of a small hospital.
Built with **Java 17**, **Java Swing** (GUI), **SQLite** and **JDBC**. No server, no internet
and no external API is needed once the setup below is done.

## Features

| Module | What you can do |
|---|---|
| Admin Login | Secure login (password stored as a SHA-256 hash) |
| Dashboard | Counts of patients, doctors and scheduled appointments, revenue collected, next 8 appointments |
| Patients | Add, view, search (name / phone / blood group / ID), update, delete |
| Doctors | Add and view doctors with specialization and consultation fee |
| Appointments | Book (with clash check), view (filter by status), cancel |
| Billing | Generate bills (fee auto-filled from the appointment), view bill receipt, mark pending bills as paid |

Demo login: **username `admin`, password `admin123`**

## Quick start

### 1. Install Java
You need a **JDK 17 or newer** (the `javac` command must work: run `javac -version`).
Download from https://adoptium.net if you do not have it.

### 2. Get the SQLite driver (one time only)
Java needs a small "driver" jar to talk to SQLite. It is not bundled in this ZIP, so download it once:

* **Windows:** double-click `setup-driver.bat`
* **macOS / Linux:** run `sh setup-driver.sh`

This puts `sqlite-jdbc-3.45.3.0.jar` and `slf4j-api-1.7.36.jar` into the `lib` folder.
(No internet on that computer? Download the two jars listed in `lib/README.txt` on another
computer and copy them into `lib`.)

### 3. Run
* **Windows:** double-click `run.bat`
* **macOS / Linux:** run `sh run.sh`

The script compiles the code and starts the application. If the driver is missing, the script downloads it for you.

### Running in VS Code
1. Install the **Extension Pack for Java** (Microsoft).
2. `File > Open Folder...` and choose the `MEDICARE360` folder (the one that contains `src` and `lib`).
3. Make sure the two jars are in `lib` (step 2 above). The included `.vscode/settings.json` already points VS Code at `src` and `lib`.
4. Open `src/com/medicare360/Main.java` and click **Run** above `main`, or press **F5** (a launch profile is included).

> Always open the *project folder* in VS Code (not just one file). The database file is created
> relative to the folder the program is started from.

## Where is my data?
The database is created automatically on the first start in `data/medicare360.db`.
Everything you add, change or delete is saved there permanently.
Demo data (15 patients, 5 doctors, 10 appointments, 4 bills) is inserted **only the first time**.
To start again from scratch, close the application and delete the `data` folder.
All names, phone numbers and addresses in the demo data are fictional.

## Using the application

* **Patients:** fill the form on the left and press *Add Patient*. Click a row in the table to load that patient
  into the form, then use *Update* or *Delete*. Type in the search box and press Enter (or *Search*); *Show All* resets it.
  Deleting a patient also deletes their appointments and bills (you are asked to confirm).
* **Doctors:** fill the form and press *Add Doctor*. The consultation fee is used automatically when billing.
* **Appointments:** choose patient, doctor, date and time, type the reason and press *Book Appointment*.
  A doctor cannot be booked twice for the same date and time, and past dates are rejected.
  Select a *Scheduled* row and press *Cancel Selected Appointment* (cancelled appointments stay in the list for your records).
* **Billing:** choose a patient. Optionally choose one of their appointments (the doctor's fee is filled in), add
  medicine/other charges, pick Paid or Pending and press *Generate Bill*. A receipt window opens.
  Select a bill and use *View Bill Details* or *Mark as Paid*.

## Project structure

```
MEDICARE360/
  src/com/medicare360/
    Main.java                    program entry point
    model/                       plain Java classes that describe the data
      Person.java                abstract base class (id, name, phone)
      Patient.java, Doctor.java  extend Person
      Appointment.java, Bill.java
    db/                          everything that talks to SQLite (JDBC)
      Dao.java                   interface implemented by the DAO classes
      Database.java              opens connections, creates tables + demo data
      DatabaseScripts.java       the CREATE TABLE and demo-data SQL
      PatientDAO, DoctorDAO, AppointmentDAO, BillDAO, AdminDAO
    util/                        small helpers (Validator, ValidationException, Money, PasswordUtil)
    ui/                          Swing screens
      LoginFrame, MainFrame      login window and main window with sidebar
      DashboardPanel, PatientPanel, DoctorPanel, AppointmentPanel, BillingPanel
      Theme, StyledButton, FormPanel, StatusCellRenderer, Msg, Refreshable
  lib/                           SQLite JDBC driver jars (downloaded by setup-driver)
  data/                          database file appears here at the first start
  run.bat / run.sh               compile + run
  setup-driver.bat / .sh         one-time driver download
```

### Database tables
`admins`, `patients`, `doctors`, `appointments` (links patient + doctor), `bills` (links patient and, optionally, an appointment).
Relationships use foreign keys (`PRAGMA foreign_keys = ON`), so deleting a patient removes their appointments and bills.

## Java concepts used (and where to find them)

| Concept | Where | How it is used |
|---|---|---|
| **Classes and objects** | `model/*`, every DAO and panel | Each patient, doctor, appointment and bill is an object created with `new` |
| **Constructors** | `Patient`, `Doctor`, `Appointment`, `Bill` | Fill an object's fields when it is created; subclasses call `super(...)` |
| **Encapsulation** | `model/*` | Fields are `private`; they are read and changed only through getters/setters |
| **Inheritance** | `Patient extends Person`, `Doctor extends Person` | Shared fields (id, name, phone) live once in `Person`. Swing classes such as `PatientPanel extends JPanel` use inheritance too |
| **Abstract class / polymorphism** | `Person.getRole()` | Declared `abstract` in `Person`; each subclass gives its own answer. `toString()` is overridden so drop-down lists show friendly text |
| **Interfaces** | `Dao<T>`, `Refreshable`, plus Swing listeners | `Dao<T>` is a contract (`findAll`, `insert`) that every DAO signs. `Refreshable` lets `MainFrame` reload any screen when it is opened |
| **Generics** | `Dao<T>`, `JComboBox<Patient>`, `List<Bill>` | One interface works for many types, and the compiler checks the types |
| **Exception handling** | all DAOs and panels, `ValidationException` | `try/catch` turns database errors and bad input into clear messages; `try-with-resources` closes connections automatically; `Database.initialize` uses commit/rollback |
| **Collections** | `List`, `ArrayList`, `LinkedHashMap` | DAOs return `List<...>` results; `MainFrame` keeps its screens in a `Map` |
| **JDBC** | `db/*` | `DriverManager`, `Connection`, `PreparedStatement`, `ResultSet`, transactions, SQL JOINs |
| **Lambdas** | `button.addActionListener(e -> ...)` | Short way to say "run this when the button is clicked" |
| **Static helpers / constants** | `Validator`, `Money`, `Theme` | Utility classes with private constructors; colours and fonts defined once |

### Good practices you will see in the code
* **`PreparedStatement` with `?` placeholders** everywhere, so typed text can never alter the SQL (prevents SQL injection).
* **Validation before saving:** `Validator` checks names, ages, phone numbers, e-mails and money amounts.
* **DAO pattern:** screens never contain SQL; all SQL is in the `db` package.
* **Passwords are hashed**, never stored as plain text.
* **Source files are plain ASCII**, so they compile the same on every operating system and in VS Code.

## Troubleshooting

| Problem | Fix |
|---|---|
| "The SQLite driver (sqlite-jdbc jar) was not found" | Run `setup-driver` (step 2) and make sure the jar is inside `lib` |
| `javac` is not recognised / "release version 17 not supported" | Install a JDK 17 or newer (a JRE alone is not enough) |
| VS Code shows red squiggles on `java.sql` or Swing | Open the project *folder*, install the Java extension pack and wait for it to finish loading |
| Want fresh demo data | Close the app, delete the `data` folder, start again |
| "database is locked" | Only run one copy of the application at a time |

## Testing notes

This project was compiled and tested before delivery in a Linux sandbox without internet access. See the
delivery message for exactly what was and was not verified.
