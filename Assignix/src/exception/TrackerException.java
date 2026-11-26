package exception;

// A single exception type used for student, subject and login problems.
public class TrackerException extends Exception {

    // Creates the exception with a friendly message.
    public TrackerException(String message) {
        super(message);
    }
}