class GroupTracker {
    private final String service;
    private LogEntry groupStart;
    private LogEntry groupLast;
    private int count;

    public GroupTracker(String service) {
        this.service = service;
        this.groupStart = null;
        this.groupLast = null;
        this.count = 0;
    }

    public String getService() { return service; }
    public LogEntry getGroupStart() { return groupStart; }
    public LogEntry getGroupLast() { return groupLast; }
    public int getCount() { return count; }

    public void startNewGroup(LogEntry e) {
        groupStart = e;
        groupLast = e;
        count = 1;
    }

    public void addToGroup(LogEntry e) {
        groupLast = e;
        count++;
    }

    public void reset() {
        groupStart = null;
        groupLast = null;
        count = 0;
    }
}