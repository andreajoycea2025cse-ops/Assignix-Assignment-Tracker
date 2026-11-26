package gui;

import dao.AssignmentDAO;
import model.Assignment;
import model.Student;
import util.DateHelper;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

// The main screen after login: counts, assignments table and subject panel.
public class DashboardPanel extends JPanel {

    private static final String TABLE_CARD = "table";
    private static final String EMPTY_CARD = "empty";

    private MainWindow window;
    private Student student;
    private AssignmentDAO assignmentDAO;
    private SubjectPanel subjectPanel;
    private JPanel toolbarPanel;
    private JPanel tableArea;
    private CardLayout tableCardLayout;
    private JPanel scopePanel;
    private JLabel scopeLabel;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel emptyLabel;
    private JLabel pendingCountLabel;
    private JLabel inProgressCountLabel;
    private JLabel completedCountLabel;
    private JTextField searchField;
    private JComboBox<String> filterCombo;
    private ArrayList<Assignment> shownAssignments = new ArrayList<Assignment>();
    private String searchKeyword = "";
    private int subjectFilterId = 0;
    private String subjectFilterName = "";
    private boolean changingFilter = false;

    // Builds the dashboard layout and loads the student's data.
    public DashboardPanel(MainWindow window, Student student) {
        this.window = window;
        this.student = student;
        this.assignmentDAO = new AssignmentDAO();

        setLayout(new BorderLayout());
        setBackground(Theme.PAGE_BACKGROUND);

        add(createHeader(), BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(16, 0));
        centerPanel.setBackground(Theme.PAGE_BACKGROUND);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel leftPanel = new JPanel(new BorderLayout(0, 12));
        leftPanel.setBackground(Theme.PAGE_BACKGROUND);

        pendingCountLabel = new JLabel("0");
        inProgressCountLabel = new JLabel("0");
        completedCountLabel = new JLabel("0");

        JPanel topPanel = new JPanel(new BorderLayout(0, 10));
        topPanel.setBackground(Theme.PAGE_BACKGROUND);
        topPanel.add(createCountsPanel(), BorderLayout.NORTH);

        toolbarPanel = createToolbar();
        topPanel.add(toolbarPanel, BorderLayout.SOUTH);

        tableModel = new DefaultTableModel(
                new String[]{"ID", "Title", "Subject", "Due Date", "Days Left", "Status"}, 0);
        table = new JTable(tableModel);
        styleTable();
        table.setDefaultEditor(Object.class, null);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.TABLE_GRID));
        scrollPane.getViewport().setBackground(Theme.CARD_BACKGROUND);

        tableCardLayout = new CardLayout();
        tableArea = new JPanel(tableCardLayout);
        tableArea.setBackground(Theme.PAGE_BACKGROUND);
        tableArea.add(scrollPane, TABLE_CARD);
        tableArea.add(createEmptyPanel(), EMPTY_CARD);

        scopePanel = createScopePanel();

        JPanel tableSection = new JPanel(new BorderLayout(0, 8));
        tableSection.setBackground(Theme.PAGE_BACKGROUND);
        tableSection.add(scopePanel, BorderLayout.NORTH);
        tableSection.add(tableArea, BorderLayout.CENTER);

        leftPanel.add(topPanel, BorderLayout.NORTH);
        leftPanel.add(tableSection, BorderLayout.CENTER);

        subjectPanel = new SubjectPanel(this, student);

        centerPanel.add(leftPanel, BorderLayout.CENTER);
        centerPanel.add(subjectPanel, BorderLayout.EAST);
        add(centerPanel, BorderLayout.CENTER);

        refresh();
    }

    // Creates the light-brown header with the title, greeting and buttons.
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel title = new JLabel("My Dashboard");
        title.setFont(Theme.TITLE_FONT);
        title.setForeground(Theme.TEXT_DARK);

        JLabel helloLabel = new JLabel("Hello, " + student.getName());
        helloLabel.setFont(Theme.BODY_FONT);
        helloLabel.setForeground(Theme.TEXT_DARK);

        JButton exportButton = new JButton("Export");
        Theme.styleSecondaryButton(exportButton);
        exportButton.addActionListener(event -> handleExport());

        JButton logoutButton = new JButton("Logout");
        Theme.styleSecondaryButton(logoutButton);
        logoutButton.addActionListener(event -> window.showLogin());

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setBackground(Theme.PRIMARY);
        rightPanel.add(helloLabel);
        rightPanel.add(exportButton);
        rightPanel.add(logoutButton);

        header.add(title, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);
        return header;
    }

    // Creates the toolbar with the assignment buttons, search box and filter.
    private JPanel createToolbar() {
        JPanel toolbar = new JPanel();
        toolbar.setLayout(new javax.swing.BoxLayout(toolbar, javax.swing.BoxLayout.Y_AXIS));
        toolbar.setBackground(Theme.PAGE_BACKGROUND);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        actionRow.setBackground(Theme.PAGE_BACKGROUND);

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        searchRow.setBackground(Theme.PAGE_BACKGROUND);

        JButton addButton = new JButton("+ Add Assignment");
        Theme.styleButton(addButton);
        addButton.addActionListener(event -> handleAddAssignment());

        JButton editButton = new JButton("Edit");
        Theme.styleSecondaryButton(editButton);
        editButton.addActionListener(event -> handleEditAssignment());

        JButton statusButton = new JButton("Update Status");
        Theme.styleSecondaryButton(statusButton);
        statusButton.addActionListener(event -> handleUpdateStatus());

        JButton deleteButton = new JButton("Delete");
        Theme.styleSecondaryButton(deleteButton);
        deleteButton.addActionListener(event -> handleDeleteAssignment());

        searchField = new JTextField();
        Theme.styleTextField(searchField);
        searchField.setPreferredSize(new Dimension(170, 32));
        searchField.setToolTipText("Search by title or subject");
        searchField.addActionListener(event -> handleSearch());

        JButton searchButton = new JButton("Search");
        Theme.styleSecondaryButton(searchButton);
        searchButton.addActionListener(event -> handleSearch());

        JButton clearButton = new JButton("Clear");
        Theme.styleSecondaryButton(clearButton);
        clearButton.addActionListener(event -> handleClearSearch());

        JLabel filterLabel = new JLabel("Filter:");
        filterLabel.setFont(Theme.BODY_FONT);
        filterLabel.setForeground(Theme.TEXT_DARK);

        filterCombo = new JComboBox<String>(new String[]{
                "Incomplete", "Pending", "In Progress", "Completed"});
        filterCombo.setFont(Theme.BODY_FONT);
        filterCombo.setForeground(Theme.TEXT_DARK);
        filterCombo.setBackground(Theme.CARD_BACKGROUND);
        filterCombo.setPreferredSize(new Dimension(130, 32));
        filterCombo.setSelectedItem("Incomplete");
        filterCombo.addActionListener(event -> handleFilterChange());

        actionRow.add(addButton);
        actionRow.add(editButton);
        actionRow.add(statusButton);
        actionRow.add(deleteButton);

        searchRow.add(searchField);
        searchRow.add(searchButton);
        searchRow.add(clearButton);
        searchRow.add(filterLabel);
        searchRow.add(filterCombo);

        toolbar.add(actionRow);
        toolbar.add(searchRow);
        return toolbar;
    }

    // Creates the row with the three status count cards.
    private JPanel createCountsPanel() {
        JPanel countsPanel = new JPanel(new GridLayout(1, 3, 12, 0));
        countsPanel.setBackground(Theme.PAGE_BACKGROUND);
        countsPanel.add(createCountCard("Pending", pendingCountLabel));
        countsPanel.add(createCountCard("In Progress", inProgressCountLabel));
        countsPanel.add(createCountCard("Completed", completedCountLabel));
        return countsPanel;
    }

    // Creates one small cream card with a big number and a status name.
    private JPanel createCountCard(String title, JLabel numberLabel) {
        JPanel card = new JPanel();
        Theme.makeCard(card);
        card.setLayout(new javax.swing.BoxLayout(card, javax.swing.BoxLayout.Y_AXIS));

        numberLabel.setFont(Theme.TITLE_FONT);
        numberLabel.setForeground(Theme.TEXT_DARK);
        numberLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.BODY_FONT);
        titleLabel.setForeground(Theme.TEXT_DARK);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(numberLabel);
        card.add(javax.swing.Box.createVerticalStrut(2));
        card.add(titleLabel);
        return card;
    }

    // Styles the assignment table, its header and its columns.
    private void styleTable() {
        table.setFont(Theme.BODY_FONT);
        table.setForeground(Theme.TEXT_DARK);
        table.setBackground(Theme.CARD_BACKGROUND);
        table.setSelectionBackground(Theme.ACCENT);
        table.setSelectionForeground(Color.WHITE);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(28);
        table.setGridColor(Theme.TABLE_GRID);
        table.setShowGrid(true);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setBackground(Theme.TABLE_HEADER);
        header.setForeground(Theme.TEXT_DARK);
        header.setFont(Theme.HEADING_FONT);
        header.setReorderingAllowed(false);

        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setPreferredWidth(0);
        table.getColumnModel().getColumn(1).setPreferredWidth(240);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(120);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setCellRenderer((renderTable, value, isSelected, hasFocus, row, column) -> {
            JLabel label = new JLabel(value == null ? "" : value.toString());
            if (isSelected) {
                label.setForeground(Color.WHITE);
                label.setBackground(renderTable.getSelectionBackground());
            } else if (value != null && value.toString().startsWith("Overdue")) {
                label.setForeground(Theme.OVERDUE_TEXT);
                label.setBackground(renderTable.getBackground());
            } else {
                label.setForeground(Theme.TEXT_DARK);
                label.setBackground(renderTable.getBackground());
            }
            label.setFont(renderTable.getFont());
            label.setOpaque(true);
            return label;
        });
    }

    // Creates the friendly card that is shown when the table has no rows.
    private JPanel createEmptyPanel() {
        JPanel emptyPanel = new JPanel(new BorderLayout());
        Theme.makeCard(emptyPanel);

        emptyLabel = new JLabel("No pending work. Add an assignment to get started.",
                SwingConstants.CENTER);
        emptyLabel.setFont(Theme.BODY_FONT);
        emptyLabel.setForeground(Theme.TEXT_DARK);
        emptyPanel.add(emptyLabel, BorderLayout.CENTER);
        return emptyPanel;
    }

    // Creates the small banner that shows the active subject filter.
    private JPanel createScopePanel() {
        JPanel panel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 2));
        panel.setBackground(Theme.PAGE_BACKGROUND);

        scopeLabel = new JLabel("Showing: All assignments");
        scopeLabel.setFont(Theme.BODY_FONT);
        scopeLabel.setForeground(Theme.TEXT_DARK);
        panel.add(scopeLabel);

        JButton clearFilterButton = new JButton("Show All");
        Theme.styleSecondaryButton(clearFilterButton);
        clearFilterButton.addActionListener(event -> clearSubjectFilter());
        panel.add(clearFilterButton);

        return panel;
    }

    // Reloads the table, the count cards and the subject panel.
    public void refresh() {
        loadAssignments();
        refreshCounts();
        subjectPanel.refresh();
        if (subjectFilterId != 0) {
            scopeLabel.setText("Showing: " + subjectFilterName);
        } else {
            scopeLabel.setText("Showing: All assignments");
        }
    }

    // Filters the dashboard table to the assignments of one subject.
    public void filterBySubject(int subjectId, String subjectName) {
        subjectFilterId = subjectId;
        subjectFilterName = subjectName == null ? "" : subjectName;
        searchKeyword = "";
        searchField.setText("");
        refresh();
    }

    // Clears any active subject filter and shows all assignments again.
    public void clearSubjectFilter() {
        subjectFilterId = 0;
        subjectFilterName = "";
        searchKeyword = "";
        searchField.setText("");
        refresh();
    }

    // Clears the subject filter only when the removed subject was active.
    public void clearSubjectFilterIfRemoved(int subjectId) {
        if (subjectFilterId == subjectId) {
            clearSubjectFilter();
        }
    }

    // Loads the rows that match the current search or filter into the table.
    private void loadAssignments() {
        String filter = getSelectedFilter();

        if (searchKeyword.length() > 0) {
            shownAssignments = assignmentDAO.searchByTitleOrSubject(
                    student.getStudentId(), searchKeyword);
        } else if (subjectFilterId != 0) {
            shownAssignments = assignmentDAO.getAssignmentsBySubject(
                    student.getStudentId(), subjectFilterId);
        } else if (filter.equals("Incomplete")) {
            shownAssignments = assignmentDAO.getIncompleteAssignments(student.getStudentId());
        } else {
            shownAssignments = assignmentDAO.getAssignmentsByStatus(
                    student.getStudentId(), filter);
        }

        // Keep an active subject filter working together with the status filter
        // and the search box.
        if (subjectFilterId != 0 && searchKeyword.length() == 0
                && !filter.equals("Incomplete") && !filter.equals("All")) {
            ArrayList<Assignment> filtered = new ArrayList<Assignment>();
            for (int i = 0; i < shownAssignments.size(); i++) {
                Assignment assignment = shownAssignments.get(i);
                if (assignment.getStatus().equals(filter)) {
                    filtered.add(assignment);
                }
            }
            shownAssignments = filtered;
        } else if (subjectFilterId != 0 && searchKeyword.length() > 0) {
            ArrayList<Assignment> filtered = new ArrayList<Assignment>();
            for (int i = 0; i < shownAssignments.size(); i++) {
                Assignment assignment = shownAssignments.get(i);
                if (assignment.getSubjectId() == subjectFilterId
                        || assignment.getSubjectName().equals(subjectFilterName)) {
                    filtered.add(assignment);
                }
            }
            shownAssignments = filtered;
        }

        // Apply the Incomplete filter on top of a subject filter.
        if (subjectFilterId != 0 && searchKeyword.length() == 0
                && filter.equals("Incomplete")) {
            ArrayList<Assignment> filtered = new ArrayList<Assignment>();
            for (int i = 0; i < shownAssignments.size(); i++) {
                Assignment assignment = shownAssignments.get(i);
                if (!assignment.getStatus().equals("Completed")) {
                    filtered.add(assignment);
                }
            }
            shownAssignments = filtered;
        }

        tableModel.setRowCount(0);

        for (int i = 0; i < shownAssignments.size(); i++) {
            Assignment assignment = shownAssignments.get(i);
            tableModel.addRow(new Object[]{
                    assignment.getAssignmentId(),
                    assignment.getTitle(),
                    assignment.getSubjectName(),
                    assignment.getDueDate(),
                    DateHelper.getDaysLeftText(assignment.getDueDate()),
                    assignment.getStatus()
            });
        }

        if (shownAssignments.isEmpty()) {
            if (searchKeyword.length() > 0) {
                emptyLabel.setText("No assignments match your search.");
            } else if (!filter.equals("Incomplete")) {
                emptyLabel.setText("No assignments found for this filter.");
            } else {
                emptyLabel.setText("No pending work. Add an assignment to get started.");
            }
            tableCardLayout.show(tableArea, EMPTY_CARD);
        } else {
            tableCardLayout.show(tableArea, TABLE_CARD);
        }
    }

    // Returns the text that is currently chosen in the filter combo box.
    private String getSelectedFilter() {
        Object selected = filterCombo.getSelectedItem();

        if (selected == null) {
            return "Incomplete";
        }
        return selected.toString();
    }

    // Opens the dialog in add mode, then reloads the dashboard.
    private void handleAddAssignment() {
        AssignmentDialog dialog = new AssignmentDialog(window, student, null);
        dialog.setVisible(true);
        refresh();
    }

    // Opens the dialog in edit mode for the selected assignment.
    private void handleEditAssignment() {
        Assignment selected = getSelectedAssignment();

        if (selected == null) {
            return;
        }

        AssignmentDialog dialog = new AssignmentDialog(window, student, selected);
        dialog.setVisible(true);
        refresh();
    }

    // Asks for a new status and saves it for the selected assignment.
    private void handleUpdateStatus() {
        Assignment selected = getSelectedAssignment();

        if (selected == null) {
            return;
        }

        Object[] statuses = {"Pending", "In Progress", "Completed"};
        String status = (String) JOptionPane.showInputDialog(this,
                "Choose the new status:", "Update Status",
                JOptionPane.QUESTION_MESSAGE, null, statuses, selected.getStatus());

        if (status == null) {
            return;
        }

        if (!assignmentDAO.updateStatus(selected.getAssignmentId(),
                student.getStudentId(), status)) {
            JOptionPane.showMessageDialog(this,
                    "Could not update the status. Please try again.",
                    "Database error", JOptionPane.ERROR_MESSAGE);
        }
        refresh();
    }

    // Deletes the selected assignment after a confirmation question.
    private void handleDeleteAssignment() {
        Assignment selected = getSelectedAssignment();

        if (selected == null) {
            return;
        }

        int answer = JOptionPane.showConfirmDialog(this,
                "Delete \"" + selected.getTitle() + "\"? This cannot be undone.",
                "Delete Assignment", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        if (!assignmentDAO.deleteAssignment(selected.getAssignmentId(),
                student.getStudentId())) {
            JOptionPane.showMessageDialog(this,
                    "Could not delete the assignment. Please try again.",
                    "Database error", JOptionPane.ERROR_MESSAGE);
        }
        refresh();
    }

    // Searches titles and subject names, then shows the matches.
    private void handleSearch() {
        String keyword = searchField.getText().trim();

        if (keyword.length() == 0) {
            JOptionPane.showMessageDialog(this, "Please type a word to search.");
            return;
        }

        searchKeyword = keyword;
        refresh();
    }

    // Clears the search and returns to the current filter view.
    private void handleClearSearch() {
        searchKeyword = "";
        searchField.setText("");
        refresh();
    }

    // Reloads the table when another filter is chosen.
    private void handleFilterChange() {
        searchKeyword = "";
        searchField.setText("");
        refresh();
    }

    // Returns the selected assignment, or shows a message when none is selected.
    private Assignment getSelectedAssignment() {
        int row = table.getSelectedRow();

        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an assignment first.");
            return null;
        }
        return shownAssignments.get(row);
    }

    // Reads the status counts and puts them in the three count cards.
    private void refreshCounts() {
        int[] counts = assignmentDAO.getStatusCounts(student.getStudentId());
        pendingCountLabel.setText(String.valueOf(counts[0]));
        inProgressCountLabel.setText(String.valueOf(counts[1]));
        completedCountLabel.setText(String.valueOf(counts[2]));
    }

    // Writes all of the student's assignments to assignments.txt.
    private void handleExport() {
        ArrayList<Assignment> assignments =
                assignmentDAO.getAssignmentsByStudent(student.getStudentId());
        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(new FileWriter("assignments.txt"));
            writer.write("ID | Title | Subject | Due Date | Status | Days Left");
            writer.newLine();

            for (int i = 0; i < assignments.size(); i++) {
                Assignment assignment = assignments.get(i);
                writer.write(assignment.getAssignmentId() + " | "
                        + assignment.getTitle() + " | "
                        + assignment.getSubjectName() + " | "
                        + assignment.getDueDate() + " | "
                        + assignment.getStatus() + " | "
                        + DateHelper.getDaysLeftText(assignment.getDueDate()));
                writer.newLine();
            }

            JOptionPane.showMessageDialog(this, "Assignments saved to:\n"
                    + new File("assignments.txt").getAbsolutePath());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Could not write assignments.txt: " + e.getMessage(),
                    "Export error", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this,
                            "Could not close assignments.txt.",
                            "Export error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

}
