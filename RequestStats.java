class RequestStats {
    private final int requestId;
    private int total;
    private int errorCount;
    private String[] services;
    private int servicesSize;

    public RequestStats(int requestId) {
        this.requestId = requestId;
        this.total = 0;
        this.errorCount = 0;
        this.services = new String[5];
        this.servicesSize = 0;
    }

    public int getRequestId() { return requestId; }
    public int getTotal() { return total; }
    public int getErrorCount() { return errorCount; }
    public boolean isFailed() { return errorCount > 0; }

    public String[] getServices() {
        String[] copy = new String[servicesSize];
        for (int i = 0; i < servicesSize; i++) copy[i] = services[i];
        return copy;
    }

    public void addRecord(String service, String level) {
        total++;
        if (level.equals("ERROR")) errorCount++;
        addServiceIfMissing(service);
    }

    private void addServiceIfMissing(String service) {
        for (int i = 0; i < servicesSize; i++) {
            if (services[i].equals(service)) return;
        }
        if (servicesSize == services.length) {
            String[] bigger = new String[services.length * 2];
            for (int i = 0; i < services.length; i++) bigger[i] = services[i];
            services = bigger;
        }
        services[servicesSize] = service;
        servicesSize++;
    }
}