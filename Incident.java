class Incident {
    private final String service;
    private final String startTimestamp;
    private final String endTimestamp;

    public Incident(String service, String startTimestamp, String endTimestamp) {
        this.service = service;
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
    }

    public String getService() { return service; }
    public String getStartTimestamp() { return startTimestamp; }
    public String getEndTimestamp() { return endTimestamp; }
}