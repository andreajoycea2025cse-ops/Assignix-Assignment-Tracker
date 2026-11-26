package model;

// A student account that owns subjects and assignments.
public class Student {

    private int studentId;
    private String name;
    private String email;
    private String password;
    private String securityQuestion;
    private String securityAnswer;

    // Creates a student with all account details.
    public Student(int studentId, String name, String email, String password,
                   String securityQuestion, String securityAnswer) {
        this.studentId = studentId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.securityQuestion = securityQuestion;
        this.securityAnswer = securityAnswer;
    }

    // Returns the database id of the student.
    public int getStudentId() {
        return studentId;
    }

    // Changes the database id of the student.
    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    // Returns the student's name.
    public String getName() {
        return name;
    }

    // Changes the student's name.
    public void setName(String name) {
        this.name = name;
    }

    // Returns the student's email.
    public String getEmail() {
        return email;
    }

    // Changes the student's email.
    public void setEmail(String email) {
        this.email = email;
    }

    // Returns the student's password.
    public String getPassword() {
        return password;
    }

    // Changes the student's password.
    public void setPassword(String password) {
        this.password = password;
    }

    // Returns the security question.
    public String getSecurityQuestion() {
        return securityQuestion;
    }

    // Changes the security question.
    public void setSecurityQuestion(String securityQuestion) {
        this.securityQuestion = securityQuestion;
    }

    // Returns the security answer.
    public String getSecurityAnswer() {
        return securityAnswer;
    }

    // Changes the security answer.
    public void setSecurityAnswer(String securityAnswer) {
        this.securityAnswer = securityAnswer;
    }
}