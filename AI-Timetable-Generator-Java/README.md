# AI Timetable Generator — Java Edition

A desktop PBL project built with **Java Swing**. It creates a weekly college timetable using a constraint-based scheduling algorithm and checks for overlapping student-group sessions, faculty clashes, and room double-booking.

> **Honest project description:** This version uses a rule/constraint-based scheduling algorithm. It does not call a trained machine-learning model or an external AI API.

## Features

- Dashboard with project overview
- Add and delete subjects
- Configure subject code, faculty, weekly frequency, and theory/lab type
- Add and delete classrooms and laboratories
- Generate a weekly timetable across Monday–Friday
- Avoid overlapping classes for the same student group
- Avoid assigning one faculty member to overlapping sessions
- Avoid booking a room for overlapping sessions
- Fixed lunch break from 12:00 to 13:00
- Report sessions that could not be scheduled
- Export timetable to CSV
- Print the timetable
- Sample data included for quick demonstration

## Requirements

- Java JDK 17 or later (JDK 21 recommended)
- Visual Studio Code
- Optional: Extension Pack for Java in VS Code

## Run on Windows using VS Code

1. Extract this ZIP.
2. Open the `AI-Timetable-Generator-Java` folder in VS Code.
3. Confirm Java is installed by opening Terminal → New Terminal and running:

   ```powershell
   java -version
   javac -version
   ```

4. Compile the source:

   ```powershell
   javac -d out src\com\aitimetable\Main.java
   ```

5. Run the application:

   ```powershell
   java -cp out com.aitimetable.Main
   ```

No third-party libraries or API keys are required.

## How to use

1. Start the application.
2. Open **Subjects** to add subjects. The sample subjects can be used for the first demo.
3. Open **Rooms** to add rooms. Theory subjects need classrooms; lab subjects need laboratories.
4. Open **Generate Timetable**, choose college start/end hours and session duration, then click **Generate Timetable**.
5. Review any warning messages.
6. Click **Export CSV** to save the timetable, or **Print** to print it.

## Algorithm overview

1. Expand each subject into its required number of weekly sessions.
2. Prioritize subjects with fewer compatible room options.
3. Try available days and time slots.
4. Reject slots that overlap another class for the student group.
5. Reject slots that overlap the same faculty member's session.
6. Reject slots that overlap an existing booking in the room.
7. Report sessions that could not be placed.

## Limitations

- This is a prototype intended for a college PBL demonstration.
- The current version uses a single student group and Monday–Friday scheduling.
- Lunch is fixed at 12:00–13:00.
- The scheduler uses a greedy constraint-based strategy rather than a complete optimization solver, so a valid timetable may exist even if the program reports that some sessions could not be placed.
- Data is held in memory while the app is running; it is not saved to a database.
- Faculty availability, multiple class groups, room capacity, and editable break periods can be future enhancements.

## Suggested future enhancements

- Persistent storage with SQLite
- Faculty availability editor
- Multiple departments and student groups
- Room capacity constraints
- Editable break and lunch times
- Better optimization using backtracking or a constraint-satisfaction solver
- Export to PDF and Excel
- Web-based interface

## GitHub upload

Create a new repository on GitHub, for example `ai-timetable-generator-java`. From this project folder, run:

```powershell
git init
git add .
git commit -m "Initial Java AI Timetable Generator PBL project"
git branch -M main
git remote add origin https://github.com/YOUR-USERNAME/ai-timetable-generator-java.git
git push -u origin main
```

Replace `YOUR-USERNAME` with your GitHub username. If Git is already initialized or a remote already exists, inspect it with `git status` and `git remote -v` before adding a remote.
