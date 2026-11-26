package model;

// A subject belongs to one student.
public class Subject {

    private int subjectId;
    private int studentId;
    private String subjectName;

    // Creates a subject with all its details.
    public Subject(int subjectId, int studentId, String subjectName) {
        this.subjectId = subjectId;
        this.studentId = studentId;
        this.subjectName = subjectName;
    }

    // Returns the database id of the subject.
    public int getSubjectId() {
        return subjectId;
    }

    // Changes the database id of the subject.
    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    // Returns the id of the student who owns this subject.
    public int getStudentId() {
        return studentId;
    }

    // Changes the owner student id.
    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    // Returns the subject name.
    public String getSubjectName() {
        return subjectName;
    }

    // Changes the subject name.
    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    // Shows a subject in a simple way.
    public String getDetails() {
        return "Subject: " + subjectName;
    }

    // Holds the progress numbers shown by the subject_progress view.
    public static class Progress {

        private int subjectId;
        private String subjectName;
        private int totalAssignments;
        private int completedAssignments;

        // Creates a progress object with all view values.
        public Progress(int subjectId, String subjectName,
                       int totalAssignments, int completedAssignments) {
            this.subjectId = subjectId;
            this.subjectName = subjectName;
            this.totalAssignments = totalAssignments;
            this.completedAssignments = completedAssignments;
        }

        // Returns the subject id.
        public int getSubjectId() {
            return subjectId;
        }

        // Returns the subject name.
        public String getSubjectName() {
            return subjectName;
        }

        // Returns how many assignments the subject has.
        public int getTotalAssignments() {
            return totalAssignments;
        }

        // Returns how many assignments are completed.
        public int getCompletedAssignments() {
            return completedAssignments;
        }

        // Calculates the completion percentage.
        public int getPercentage() {
            if (totalAssignments == 0) {
                return 0;
            }
            return completedAssignments * 100 / totalAssignments;
        }
    }
}
