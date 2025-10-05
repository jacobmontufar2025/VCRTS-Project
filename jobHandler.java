

import java.time.LocalDateTime;

import java.time.format.DateTimeFormatter;

// this is job handler

public class jobHandler {

    // Create a new Job with a formatted timestamp
    public Job createJob(String clientId, String duration, String deadline) {
        String timestamp = getTimestamp();
        return new Job(clientId, duration, deadline, timestamp);
    }

    // Returns a human-readable timestamp (no nanoseconds)
    private String getTimestamp() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }

    // Submit job (prints to console)
    public void submitJob(Job job) {
        System.out.println(job);
    }

}
