package com.aitimetable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.PrintWriter;
import java.util.*;
import java.util.List;

public class Main {
    private final JFrame frame = new JFrame("AI Timetable Generator");
    private final DefaultTableModel subjectModel = new DefaultTableModel(
            new Object[]{"Subject", "Code", "Faculty", "Sessions/Week", "Type", "Room Type"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable subjectTable = new JTable(subjectModel);
    private final DefaultTableModel timetableModel = new DefaultTableModel(
            new Object[]{"Day", "Start", "End", "Subject", "Faculty", "Room", "Type"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable timetableTable = new JTable(timetableModel);
    private final List<Subject> subjects = new ArrayList<>();
    private final List<Room> rooms = new ArrayList<>();
    private final String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
    private final JTextField roomNameField = new JTextField(10);
    private final JComboBox<String> roomTypeBox = new JComboBox<>(new String[]{"Classroom", "Laboratory"});
    private final JLabel statusLabel = new JLabel("Ready. Add subjects and generate a timetable.");
    private final JSpinner startHour = new JSpinner(new SpinnerNumberModel(9, 6, 18, 1));
    private final JSpinner endHour = new JSpinner(new SpinnerNumberModel(16, 7, 22, 1));
    private final JSpinner sessionMinutes = new JSpinner(new SpinnerNumberModel(60, 30, 180, 15));

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().show());
    }

    private void show() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1000, 680));
        frame.setSize(1180, 760);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(18, 29, 48));
        top.setBorder(new EmptyBorder(18, 24, 18, 24));
        JLabel title = new JLabel("✦  AI TIMETABLE GENERATOR");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        JLabel subtitle = new JLabel("Constraint-based academic scheduling • Java Edition");
        subtitle.setForeground(new Color(190, 204, 225));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        JPanel heading = new JPanel(new GridLayout(2, 1, 0, 4));
        heading.setOpaque(false);
        heading.add(title);
        heading.add(subtitle);
        top.add(heading, BorderLayout.CENTER);
        frame.add(top, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Dashboard", dashboardPanel());
        tabs.addTab("Subjects", subjectsPanel());
        tabs.addTab("Rooms", roomsPanel());
        tabs.addTab("Generate Timetable", timetablePanel());
        frame.add(tabs, BorderLayout.CENTER);

        statusLabel.setBorder(new EmptyBorder(9, 16, 9, 16));
        statusLabel.setForeground(new Color(65, 78, 96));
        frame.add(statusLabel, BorderLayout.SOUTH);

        seedData();
        refreshSubjects();
        refreshDashboard();
        frame.setVisible(true);
    }

    private JPanel dashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setBorder(new EmptyBorder(24, 24, 24, 24));
        panel.setBackground(new Color(246, 248, 252));

        JLabel welcome = new JLabel("<html><div style='font-size:25px;font-weight:700'>Plan smarter. Schedule better.</div>"
                + "<div style='font-size:13px;color:#64748b;margin-top:8px'>Generate a weekly college timetable while checking room, faculty, and class-group conflicts.</div></html>");
        panel.add(welcome, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1, 3, 14, 14));
        cards.setOpaque(false);
        cards.add(statCard("SUBJECTS", subjects.size() + "", "BookOpen"));
        cards.add(statCard("ROOMS", rooms.size() + "", "DoorOpen"));
        cards.add(statCard("WORKING DAYS", days.length + "", "CalendarDays"));
        panel.add(cards, BorderLayout.CENTER);

        JTextArea info = new JTextArea(
                "PROJECT OVERVIEW\n\n" +
                "This project uses a constraint-based scheduling algorithm written in Java. " +
                "It attempts to place each required subject session into a valid time slot while avoiding overlapping classes, " +
                "faculty clashes, and double-booked rooms.\n\n" +
                "Quick start:\n" +
                "1. Open Subjects and add the subjects and faculty for your class.\n" +
                "2. Open Rooms and add classrooms/labs.\n" +
                "3. Open Generate Timetable and set college hours.\n" +
                "4. Click Generate Timetable and review any warnings.\n\n" +
                "Note: this is a scheduling/optimization prototype; it does not call a trained machine-learning model.");
        info.setEditable(false);
        info.setLineWrap(true);
        info.setWrapStyleWord(true);
        info.setFont(new Font("SansSerif", Font.PLAIN, 14));
        info.setBackground(Color.WHITE);
        info.setBorder(new EmptyBorder(18, 18, 18, 18));
        panel.add(info, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel statCard(String label, String value, String ignored) {
        JPanel card = new JPanel(new BorderLayout(4, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 231, 240)),
                new EmptyBorder(18, 18, 18, 18)));
        JLabel l = new JLabel(label);
        l.setForeground(new Color(100, 116, 139));
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        JLabel v = new JLabel(value);
        v.setForeground(new Color(30, 64, 120));
        v.setFont(new Font("SansSerif", Font.BOLD, 30));
        card.add(l, BorderLayout.NORTH);
        card.add(v, BorderLayout.CENTER);
        return card;
    }

    private JPanel subjectsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        panel.add(new JLabel("Manage the subjects that need to be scheduled each week."), BorderLayout.NORTH);
        subjectTable.setRowHeight(28);
        subjectTable.setFillsViewportHeight(true);
        panel.add(new JScrollPane(subjectTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton add = button("＋ Add Subject");
        JButton remove = button("Delete Selected");
        add.addActionListener(e -> addSubjectDialog());
        remove.addActionListener(e -> {
            int row = subjectTable.getSelectedRow();
            if (row >= 0) {
                String code = String.valueOf(subjectModel.getValueAt(row, 1));
                subjects.removeIf(s -> s.code.equals(code));
                refreshSubjects();
                refreshDashboard();
                statusLabel.setText("Subject removed.");
            } else JOptionPane.showMessageDialog(frame, "Select a subject row first.");
        });
        actions.add(add);
        actions.add(remove);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel roomsPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));
        JTextArea description = new JTextArea("Add rooms available for scheduling. Labs are only assigned to practical subjects; classrooms are assigned to theory subjects.");
        description.setEditable(false);
        description.setOpaque(false);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        panel.add(description, BorderLayout.NORTH);

        DefaultListModel<String> roomListModel = new DefaultListModel<>();
        JList<String> roomList = new JList<>(roomListModel);
        roomList.setFont(new Font("SansSerif", Font.PLAIN, 14));
        roomList.setFixedCellHeight(32);
        Runnable refreshRooms = () -> {
            roomListModel.clear();
            for (Room room : rooms) roomListModel.addElement(room.name + "  •  " + room.type);
        };
        refreshRooms.run();
        panel.add(new JScrollPane(roomList), BorderLayout.CENTER);

        JPanel addRoom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addRoom.add(new JLabel("Room name:"));
        addRoom.add(roomNameField);
        addRoom.add(roomTypeBox);
        JButton add = button("＋ Add Room");
        JButton remove = button("Delete Selected");
        add.addActionListener(e -> {
            String name = roomNameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Enter a room name.");
                return;
            }
            if (rooms.stream().anyMatch(r -> r.name.equalsIgnoreCase(name))) {
                JOptionPane.showMessageDialog(frame, "A room with this name already exists.");
                return;
            }
            rooms.add(new Room(name, String.valueOf(roomTypeBox.getSelectedItem())));
            roomNameField.setText("");
            refreshRooms.run();
            refreshDashboard();
            statusLabel.setText("Room added: " + name);
        });
        remove.addActionListener(e -> {
            int index = roomList.getSelectedIndex();
            if (index >= 0) {
                rooms.remove(index);
                refreshRooms.run();
                refreshDashboard();
                statusLabel.setText("Room removed.");
            }
        });
        addRoom.add(add);
        addRoom.add(remove);
        panel.add(addRoom, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel timetablePanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel settings = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        settings.setBorder(BorderFactory.createTitledBorder("Schedule settings"));
        settings.add(new JLabel("Start hour:"));
        settings.add(startHour);
        settings.add(new JLabel("End hour:"));
        settings.add(endHour);
        settings.add(new JLabel("Session minutes:"));
        settings.add(sessionMinutes);
        JButton generate = button("✦ Generate Timetable");
        JButton export = button("Export CSV");
        JButton print = button("Print");
        settings.add(generate);
        settings.add(export);
        settings.add(print);
        panel.add(settings, BorderLayout.NORTH);

        timetableTable.setRowHeight(27);
        timetableTable.setFillsViewportHeight(true);
        timetableTable.setAutoCreateRowSorter(true);
        panel.add(new JScrollPane(timetableTable), BorderLayout.CENTER);

        generate.addActionListener(e -> generate());
        export.addActionListener(e -> exportCsv());
        print.addActionListener(e -> {
            try { timetableTable.print(); }
            catch (Exception ex) { JOptionPane.showMessageDialog(frame, "Unable to print: " + ex.getMessage()); }
        });
        return panel;
    }

    private void addSubjectDialog() {
        JTextField name = new JTextField();
        JTextField code = new JTextField();
        JTextField faculty = new JTextField();
        JSpinner sessions = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        JComboBox<String> kind = new JComboBox<>(new String[]{"Theory", "Lab"});
        Object[] fields = {
                "Subject name:", name,
                "Subject code:", code,
                "Faculty name:", faculty,
                "Sessions per week:", sessions,
                "Subject type:", kind
        };
        int result = JOptionPane.showConfirmDialog(frame, fields, "Add Subject", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;
        if (name.getText().trim().isEmpty() || code.getText().trim().isEmpty() || faculty.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Subject name, code, and faculty are required.");
            return;
        }
        if (subjects.stream().anyMatch(s -> s.code.equalsIgnoreCase(code.getText().trim()))) {
            JOptionPane.showMessageDialog(frame, "Subject code must be unique.");
            return;
        }
        String type = String.valueOf(kind.getSelectedItem());
        subjects.add(new Subject(name.getText().trim(), code.getText().trim(), faculty.getText().trim(),
                (Integer) sessions.getValue(), type, type.equals("Lab") ? "Laboratory" : "Classroom"));
        refreshSubjects();
        refreshDashboard();
        statusLabel.setText("Subject added: " + name.getText().trim());
    }

    private void generate() {
        int start = (Integer) startHour.getValue();
        int end = (Integer) endHour.getValue();
        int duration = (Integer) sessionMinutes.getValue();
        if (end <= start) {
            JOptionPane.showMessageDialog(frame, "End hour must be later than start hour.");
            return;
        }
        if (subjects.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Add at least one subject first.");
            return;
        }
        List<String> warnings = new ArrayList<>();
        List<Scheduled> result = new ArrayList<>();
        Map<String, List<Scheduled>> occupied = new HashMap<>();
        List<Subject> requested = new ArrayList<>();
        for (Subject s : subjects) for (int i = 0; i < s.sessions; i++) requested.add(s);
        requested.sort(Comparator.comparingInt((Subject s) -> roomsFor(s).size()).thenComparing((Subject s) -> -s.sessions));

        for (String day : days) {
            int cursor = start * 60;
            while (cursor + duration <= end * 60) {
                // Lunch break is fixed at 12:00–13:00; do not schedule across it.
                if (cursor >= 12 * 60 && cursor < 13 * 60) { cursor = 13 * 60; continue; }
                if (cursor < 13 * 60 && cursor + duration > 12 * 60) { cursor = 13 * 60; continue; }
                String timeKey = day + "|" + cursor;
                occupied.putIfAbsent(timeKey, new ArrayList<>());
                cursor += duration;
            }
        }

        for (Subject subject : requested) {
            Scheduled placed = null;
            for (String day : days) {
                for (int cursor = start * 60; cursor + subject.duration <= end * 60; cursor += duration) {
                    if (cursor >= 12 * 60 && cursor < 13 * 60) continue;
                    if (cursor < 13 * 60 && cursor + subject.duration > 12 * 60) continue;
                    final int slotStart = cursor;
                    final int finish = slotStart + subject.duration;
                    boolean groupConflict = result.stream().anyMatch(a -> a.day.equals(day) && overlap(a.start, a.end, slotStart, finish));
                    boolean facultyConflict = result.stream().anyMatch(a -> a.day.equals(day) && a.subject.faculty.equalsIgnoreCase(subject.faculty) && overlap(a.start, a.end, slotStart, finish));
                    if (groupConflict || facultyConflict) continue;
                    for (Room room : roomsFor(subject)) {
                        boolean roomConflict = result.stream().anyMatch(a -> a.day.equals(day) && a.room.name.equals(room.name) && overlap(a.start, a.end, slotStart, finish));
                        if (!roomConflict) {
                            placed = new Scheduled(day, slotStart, finish, subject, room);
                            break;
                        }
                    }
                    if (placed != null) break;
                }
                if (placed != null) break;
            }
            if (placed == null) warnings.add("Could not schedule " + subject.name + " (" + subject.code + "). Add a suitable room, increase college hours, or reduce weekly sessions.");
            else result.add(placed);
        }

        timetableModel.setRowCount(0);
        result.sort(Comparator.comparingInt((Scheduled s) -> Arrays.asList(days).indexOf(s.day)).thenComparingInt(s -> s.start));
        for (Scheduled s : result) {
            timetableModel.addRow(new Object[]{s.day, time(s.start), time(s.end), s.subject.name, s.subject.faculty, s.room.name, s.subject.kind});
        }
        if (warnings.isEmpty()) {
            statusLabel.setText("Timetable generated successfully: " + result.size() + " sessions scheduled.");
            JOptionPane.showMessageDialog(frame, "Timetable generated successfully. " + result.size() + " sessions scheduled.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            statusLabel.setText("Generated with " + warnings.size() + " warning(s). Review unscheduled sessions.");
            JTextArea warningText = new JTextArea(String.join("\n\n", warnings));
            warningText.setEditable(false);
            warningText.setLineWrap(true);
            warningText.setWrapStyleWord(true);
            warningText.setRows(Math.min(12, warnings.size() * 2 + 2));
            JOptionPane.showMessageDialog(frame, new JScrollPane(warningText), "Scheduling warnings", JOptionPane.WARNING_MESSAGE);
        }
    }

    private List<Room> roomsFor(Subject s) {
        List<Room> compatible = new ArrayList<>();
        for (Room r : rooms) if (r.type.equals(s.roomType)) compatible.add(r);
        return compatible;
    }

    private boolean overlap(int aStart, int aEnd, int bStart, int bEnd) {
        return aStart < bEnd && bStart < aEnd;
    }

    private String time(int minutes) {
        return String.format("%02d:%02d", minutes / 60, minutes % 60);
    }

    private void exportCsv() {
        if (timetableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(frame, "Generate a timetable before exporting.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("generated-timetable.csv"));
        if (chooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) return;
        try (PrintWriter writer = new PrintWriter(chooser.getSelectedFile())) {
            for (int c = 0; c < timetableModel.getColumnCount(); c++) {
                if (c > 0) writer.print(",");
                writer.print(csv(String.valueOf(timetableModel.getColumnName(c))));
            }
            writer.println();
            for (int r = 0; r < timetableModel.getRowCount(); r++) {
                for (int c = 0; c < timetableModel.getColumnCount(); c++) {
                    if (c > 0) writer.print(",");
                    writer.print(csv(String.valueOf(timetableModel.getValueAt(r, c))));
                }
                writer.println();
            }
            statusLabel.setText("CSV exported to " + chooser.getSelectedFile().getAbsolutePath());
            JOptionPane.showMessageDialog(frame, "CSV exported successfully.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Export failed: " + ex.getMessage());
        }
    }

    private String csv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private JButton button(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setBackground(new Color(37, 99, 235));
        b.setForeground(Color.WHITE);
        b.setBorder(new EmptyBorder(9, 13, 9, 13));
        return b;
    }

    private void refreshSubjects() {
        subjectModel.setRowCount(0);
        for (Subject s : subjects) subjectModel.addRow(new Object[]{s.name, s.code, s.faculty, s.sessions, s.kind, s.roomType});
    }

    private void refreshDashboard() {
        // Dashboard cards are created on startup; this status keeps the current counts visible.
        statusLabel.setText("Subjects: " + subjects.size() + "  •  Rooms: " + rooms.size() + "  •  Ready to schedule");
    }

    private void seedData() {
        subjects.add(new Subject("Data Structures", "CS201", "Dr. Priya Sharma", 3, "Theory", "Classroom"));
        subjects.add(new Subject("Database Management Systems", "CS202", "Prof. Rahul Rao", 3, "Theory", "Classroom"));
        subjects.add(new Subject("Java Programming Lab", "CS203L", "Ms. Ananya", 2, "Lab", "Laboratory"));
        rooms.add(new Room("Room 101", "Classroom"));
        rooms.add(new Room("Room 102", "Classroom"));
        rooms.add(new Room("Lab 1", "Laboratory"));
    }

    private static class Subject {
        final String name, code, faculty, kind, roomType;
        final int sessions, duration;
        Subject(String name, String code, String faculty, int sessions, String kind, String roomType) {
            this.name = name; this.code = code; this.faculty = faculty; this.sessions = sessions;
            this.kind = kind; this.roomType = roomType; this.duration = kind.equals("Lab") ? 120 : 60;
        }
    }

    private static class Room {
        final String name, type;
        Room(String name, String type) { this.name = name; this.type = type; }
    }

    private static class Scheduled {
        final String day;
        final int start, end;
        final Subject subject;
        final Room room;
        Scheduled(String day, int start, int end, Subject subject, Room room) {
            this.day = day; this.start = start; this.end = end; this.subject = subject; this.room = room;
        }
    }
}
