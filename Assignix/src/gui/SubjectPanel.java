package gui;

import dao.SubjectDAO;
import exception.TrackerException;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import model.Student;
import model.Subject;

// The right-hand card that shows every subject and its completion progress.
public class SubjectPanel extends JPanel {

    private DashboardPanel dashboard;
    private Student student;
    private SubjectDAO subjectDAO;
    private JPanel listPanel;

    // Builds the subject card for the logged-in student.
    public SubjectPanel(DashboardPanel dashboard, Student student) {
        this.dashboard = dashboard;
        this.student = student;
        this.subjectDAO = new SubjectDAO();

        setLayout(new BorderLayout(0, 10));
        setPreferredSize(new Dimension(280, 0));
        Theme.makeCard(this);

        JLabel heading = new JLabel("Subjects");
        heading.setFont(Theme.HEADING_FONT);
        heading.setForeground(Theme.TEXT_DARK);

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Theme.CARD_BACKGROUND);

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Theme.CARD_BACKGROUND);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        buttonPanel.setBackground(Theme.CARD_BACKGROUND);

        JButton addButton = new JButton("Add Subject");
        Theme.styleButton(addButton);
        addButton.addActionListener(event -> handleAddSubject());

        JButton removeButton = new JButton("Remove Subject");
        Theme.styleSecondaryButton(removeButton);
        removeButton.addActionListener(event -> handleRemoveSubject());

        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);

        add(heading, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        refresh();
    }

    // Reloads the subject list and progress bars from the subject_progress view.
    public void refresh() {
        listPanel.removeAll();

        ArrayList<Subject.Progress> progressList =
                subjectDAO.getSubjectProgress(student.getStudentId());

        if (progressList.isEmpty()) {
            JLabel emptyLabel = new JLabel("No subjects yet.");
            emptyLabel.setFont(Theme.BODY_FONT);
            emptyLabel.setForeground(Theme.TEXT_DARK);
            emptyLabel.setBorder(BorderFactory.createEmptyBorder(8, 2, 8, 2));
            listPanel.add(emptyLabel);
        } else {
            for (int i = 0; i < progressList.size(); i++) {
                listPanel.add(createSubjectRow(progressList.get(i)));
            }
            listPanel.add(Box.createVerticalGlue());
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    // Creates one row with the subject name, percentage and progress bar.
    private JPanel createSubjectRow(Subject.Progress progress) {
        JPanel row = new JPanel(new BorderLayout(0, 4));
        row.setBackground(Theme.CARD_BACKGROUND);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.TABLE_GRID),
                BorderFactory.createEmptyBorder(6, 2, 10, 2)));

        JLabel nameLabel = new JLabel(progress.getSubjectName());
        nameLabel.setFont(Theme.BODY_FONT);
        nameLabel.setForeground(Theme.TEXT_DARK);

        JLabel percentLabel = new JLabel(progress.getPercentage() + "%");
        percentLabel.setFont(Theme.BODY_FONT);
        percentLabel.setForeground(Theme.ACCENT);

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setBackground(Theme.CARD_BACKGROUND);
        titleRow.add(nameLabel, BorderLayout.WEST);
        titleRow.add(percentLabel, BorderLayout.EAST);

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue(progress.getPercentage());
        progressBar.setForeground(Theme.ACCENT);
        progressBar.setBackground(Theme.PAGE_BACKGROUND);
        progressBar.setBorder(BorderFactory.createLineBorder(Theme.TABLE_GRID));
        progressBar.setPreferredSize(new Dimension(100, 16));

        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        row.setToolTipText("Click to show only " + progress.getSubjectName() + " assignments");
        row.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                dashboard.filterBySubject(progress.getSubjectId(),
                        progress.getSubjectName());
            }
        });

        row.add(titleRow, BorderLayout.NORTH);
        row.add(progressBar, BorderLayout.CENTER);
        return row;
    }

    // Asks for a subject name and adds it for this student.
    private void handleAddSubject() {
        String name = JOptionPane.showInputDialog(this, "Enter the new subject name:",
                "Add Subject", JOptionPane.QUESTION_MESSAGE);

        if (name == null) {
            return;
        }

        name = name.trim();

        if (name.length() == 0) {
            JOptionPane.showMessageDialog(this, "The subject name cannot be empty.",
                    "Add Subject", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Subject subject = subjectDAO.addSubject(student.getStudentId(), name);

            if (subject == null) {
                JOptionPane.showMessageDialog(this,
                        "Could not add the subject. Please try again.",
                        "Database error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (TrackerException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Add Subject", JOptionPane.WARNING_MESSAGE);
            return;
        }

        dashboard.refresh();
    }

    // Lets the student choose a subject and removes it with its assignments.
    private void handleRemoveSubject() {
        ArrayList<Subject> subjects = subjectDAO.getSubjectsByStudent(student.getStudentId());

        if (subjects.isEmpty()) {
            JOptionPane.showMessageDialog(this, "There are no subjects to remove.",
                    "Remove Subject", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] names = new String[subjects.size()];
        for (int i = 0; i < subjects.size(); i++) {
            names[i] = subjects.get(i).getSubjectName();
        }

        Object chosen = JOptionPane.showInputDialog(this,
                "Choose the subject to remove:", "Remove Subject",
                JOptionPane.QUESTION_MESSAGE, null, names, names[0]);

        if (chosen == null) {
            return;
        }

        int subjectId = 0;
        for (int i = 0; i < subjects.size(); i++) {
            if (subjects.get(i).getSubjectName().equals(chosen.toString())) {
                subjectId = subjects.get(i).getSubjectId();
                break;
            }
        }

        int answer = JOptionPane.showConfirmDialog(this,
                "Remove \"" + chosen + "\" and all of its assignments? "
                        + "This cannot be undone.",
                "Remove Subject", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        if (!subjectDAO.removeSubject(student.getStudentId(), subjectId)) {
            JOptionPane.showMessageDialog(this,
                    "Could not remove the subject. Please try again.",
                    "Database error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        dashboard.clearSubjectFilterIfRemoved(subjectId);
        dashboard.refresh();
    }
}
