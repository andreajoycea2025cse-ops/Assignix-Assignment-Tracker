# Assignix

Assignix is a desktop application that helps students keep track of their
subjects and assignments. It is built with Java Swing and stores its data in a
local SQLite database through JDBC. No Maven or Gradle is needed.

## Requirements

- JDK 17 or newer
- The SQLite JDBC driver is already included in `lib/sqlite-jdbc-3.53.4.0.jar`

## Project structure

```
Assignix/
|-- src/
|   |-- Main.java
|   |-- model/
|   |   |-- Student.java
|   |   |-- Subject.java
|   |   `-- Assignment.java
|   |-- exception/
|   |   `-- TrackerException.java
|   |-- dao/
|   |   |-- DBConnection.java
|   |   |-- StudentDAO.java
|   |   |-- SubjectDAO.java
|   |   `-- AssignmentDAO.java
|   |-- util/
|   |   `-- DateHelper.java
|   `-- gui/
|       |-- AssignmentDialog.java
|       |-- DashboardPanel.java
|       |-- ForgotPasswordPanel.java
|       |-- LoginPanel.java
|       |-- MainWindow.java
|       |-- RegisterPanel.java
|       |-- SubjectPanel.java
|       `-- Theme.java
|-- lib/
|   `-- sqlite-jdbc-3.53.4.0.jar
|-- schema.sql
`-- README.md
```

## Compile and run

Open a terminal in the project folder and run the commands for your system.

### Windows

```bat
javac --release 17 -cp "lib/*" -d out src\Main.java src\model\*.java src\exception\*.java src\dao\*.java src\util\*.java src\gui\*.java
java -cp "out;lib/*" Main
```

### Linux or macOS

```bash
javac --release 17 -cp "lib/*" -d out src/Main.java src/model/*.java src/exception/*.java src/dao/*.java src/util/*.java src/gui/*.java
java -cp "out:lib/*" Main
```

You can also add `-Xlint:all` after `javac` to see compiler warnings:

```bat
javac -Xlint:all --release 17 -cp "lib/*" -d out src\Main.java src\model\*.java src\exception\*.java src\dao\*.java src\util\*.java src\gui\*.java
```

## First start

The program creates `student_tracker.db` automatically on the first run. It reads
the tables, view and index from `schema.sql`, so run the program from the project
folder where `schema.sql` is located.

To start with an empty database, close the program and delete
`student_tracker.db`. The database will be created again on the next run.

## What the program can do

### Account screens

- Register a new student account with a name, email, password, security question
  and security answer
- Log in with an email and password
- Reset a forgotten password in two steps using the security question
- Return to the login screen by logging out

### Dashboard

- See counts of Pending, In Progress and Completed assignments
- View assignments in a table, nearest due date first
- Add an assignment with a title, due date and subject (choose an existing subject
  or type a new subject name)
- Edit the title or due date of a selected assignment
- Update the status of a selected assignment
- Delete a selected assignment after confirmation
- Filter assignments by `Incomplete`, `Pending`, `In Progress` or `Completed`
- Search assignments by title or subject name
- Clear the search and return to the current filter
- Export all assignments to `assignments.txt`
- Browse subjects and see completion progress as a percentage and progress bar

Due dates are typed in `yyyy-MM-dd` format. The program shows a friendly message
such as `3 days left`, `1 day left`, `Due today` or `Overdue by 2 days`.

Subject progress looks like this:

```
Maths [██████░░░░] 60%
```

## Database notes

- `Student`, `Subject` and `Assignment` are the three tables.
- `subject_progress` is a view that counts completed and total assignments.
- `idx_assignment_due_date` is an index on `Assignment(due_date)`.
- Foreign keys are enabled, and a subject with its assignments is removed in one
  transaction.
- Status values are stored as text: `Pending`, `In Progress` and `Completed`.
- Subjects are created automatically when an assignment is added with a new
  subject name.

## Files created at runtime

- `student_tracker.db` - the SQLite database
- `assignments.txt` - created when the Export button is used

## Problems and fixes

- **SQLite driver not found**: make sure `lib/sqlite-jdbc-3.53.4.0.jar` is in the
  `lib` folder and that the classpath uses `lib/*`.
- **Cannot read schema.sql**: run the program from the project folder.
- **Progress bar characters look strange**: use a terminal or window that supports
  UTF-8, such as Windows Terminal or PowerShell 7.