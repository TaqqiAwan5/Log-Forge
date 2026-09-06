import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class LogEntry {
    private final String timestamp;
    private final String service;
    private final String level;
    private final int requestId;
    private final String message;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public LogEntry(String timestamp, String service, String level,
                     int requestId, String message) {
        this.timestamp = timestamp;
        this.service = service;
        this.level = level;
        this.requestId = requestId;
        this.message = message;
    }

    public String getTimestamp() { return timestamp; }
    public String getService() { return service; }
    public String getLevel() { return level; }
    public int getRequestId() { return requestId; }
    public String getMessage() { return message; }

    public LocalDateTime getDateTime() {
        return LocalDateTime.parse(timestamp, FMT);
    }

    public boolean matchRecord(int requestId) {
        return this.requestId == requestId;
    }

    @Override
    public String toString() {
        return timestamp + "|" + service + "|" + level + "|" + requestId + "|" + message;
    }
}
