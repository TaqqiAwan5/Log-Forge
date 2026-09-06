import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.time.Duration;
import java.time.LocalDateTime;

public class LogForge {

    private String[] rawLines;
    private int rawLinesSize;

    private int totalLines = 0;
    private int validCount = 0;
    private int invalidCount = 0;
    private int infoCount = 0;
    private int warnCount = 0;
    private int errorCount = 0;

    private LogEntry[] entries;
    private int entriesSize;
    private static final int INITIAL_CAPACITY = 5;

    private ServiceStats[] services;
    private int servicesSize;

    private GroupTracker[] trackers;
    private int trackersSize;
    private Incident[] incidents;
    private int incidentsSize;

    private RequestStats[] requests;
    private int requestsSize;
    public LogForge() {
        rawLines = new String[INITIAL_CAPACITY];
        rawLinesSize = 0;
        entries = new LogEntry[INITIAL_CAPACITY];
        entriesSize = 0;
        services = new ServiceStats[INITIAL_CAPACITY];
        servicesSize = 0;
        trackers = new GroupTracker[INITIAL_CAPACITY];
        trackersSize = 0;
        incidents = new Incident[INITIAL_CAPACITY];
        incidentsSize = 0;
        requests = new RequestStats[INITIAL_CAPACITY];
        requestsSize = 0;
    }

    private void ensureRawLinesCapacity() {
        if (rawLinesSize == rawLines.length) {
            String[] bigger = new String[rawLines.length * 2];
            for (int i = 0; i < rawLines.length; i++) bigger[i] = rawLines[i];
            rawLines = bigger;
        }
    }

    private void ensureEntriesCapacity() {
        if (entriesSize == entries.length) {
            LogEntry[] bigger = new LogEntry[entries.length * 2];
            for (int i = 0; i < entries.length; i++) bigger[i] = entries[i];
            entries = bigger;
        }
    }
    private void ensureServicesCapacity() {
        if (servicesSize == services.length) {
            ServiceStats[] bigger = new ServiceStats[services.length * 2];
            for (int i = 0; i < services.length; i++) bigger[i] = services[i];
            services = bigger;
        }
    }

    private void ensureTrackersCapacity() {
        if (trackersSize == trackers.length) {
            GroupTracker[] bigger = new GroupTracker[trackers.length * 2];
            for (int i = 0; i < trackers.length; i++) bigger[i] = trackers[i];
            trackers = bigger;
        }
    }

    private void ensureIncidentsCapacity() {
        if (incidentsSize == incidents.length) {
            Incident[] bigger = new Incident[incidents.length * 2];
            for (int i = 0; i < incidents.length; i++) bigger[i] = incidents[i];
            incidents = bigger;
        }
    }
    private void ensureRequestsCapacity() {
        if (requestsSize == requests.length) {
            RequestStats[] bigger = new RequestStats[requests.length * 2];
            for (int i = 0; i < requests.length; i++) bigger[i] = requests[i];
            requests = bigger;
        }
    }
    private static String[] resizeStringArray(String[] arr) {
        String[] bigger = new String[arr.length * 2];
        for (int i = 0; i < arr.length; i++) bigger[i] = arr[i];
        return bigger;
    }

    private String[] splitFields(String line, char delimiter) {
        String[] buffer = new String[INITIAL_CAPACITY];
        int count = 0;
        int start = 0;
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == delimiter) {
                if (count == buffer.length) buffer = resizeStringArray(buffer);
                buffer[count] = line.substring(start, i);
                count++;
                start = i + 1;
            }
        }
        if (count == buffer.length) buffer = resizeStringArray(buffer);
        buffer[count] = line.substring(start);
        count++;
        String[] result = new String[count];
        for (int i = 0; i < count; i++) result[i] = buffer[i];
        return result;
    }

    private boolean isAllDigits(String s) {
        if (s.length() == 0) return false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') return false;
        }
        return true;
    }

    private boolean isValidTimestamp(String ts) {
        if (ts.length() != 19) return false;
        if (ts.charAt(4) != '-') return false;
        if (ts.charAt(7) != '-') return false;
        if (ts.charAt(10) != ' ') return false;
        if (ts.charAt(13) != ':') return false;
        if (ts.charAt(16) != ':') return false;

        int[] digitStarts = {0, 5, 8, 11, 14, 17};
        int[] digitLens   = {4, 2, 2, 2, 2, 2};
        for (int k = 0; k < digitStarts.length; k++) {
            for (int i = digitStarts[k]; i < digitStarts[k] + digitLens[k]; i++) {
                char c = ts.charAt(i);
                if (c < '0' || c > '9') return false;
            }
        }
        int month = (ts.charAt(5) - '0') * 10 + (ts.charAt(6) - '0');
        if (month < 1 || month > 12) return false;
        return true;
    }

    private boolean isValidLevel(String level) {
        return level.equals("INFO") || level.equals("WARN") || level.equals("ERROR");
    }

    private boolean isValidRequestId(String s) {
        if (!isAllDigits(s)) return false;
        int value = Integer.parseInt(s);
        return value > 0;
    }

    // Now RETURNS a LogEntry (or null if invalid) instead of just void
    private LogEntry processLine(String line) {
        String[] fields = splitFields(line, '|');
        if (fields.length != 5) {
            invalidCount++;
            return null;
        }
        String timestamp = fields[0];
        String service = fields[1];
        String level = fields[2];
        String requestIdStr = fields[3];
        String message = fields[4];

        if (!isValidTimestamp(timestamp) || !isValidLevel(level) || !isValidRequestId(requestIdStr)) {
            invalidCount++;
            return null;
        }

        validCount++;
        if (level.equals("INFO")) infoCount++;
        else if (level.equals("WARN")) warnCount++;
        else if (level.equals("ERROR")) errorCount++;

        int requestId = Integer.parseInt(requestIdStr);
        return new LogEntry(timestamp, service, level, requestId, message);
    }

    private void readFile(String filename) throws FileNotFoundException {
        File file = new File(filename);
        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            totalLines++;
            ensureRawLinesCapacity();
            rawLines[rawLinesSize] = line;
            rawLinesSize++;

            LogEntry entry = processLine(line);
            if (entry != null) {
                ensureEntriesCapacity();
                entries[entriesSize] = entry;
                entriesSize++;
            }
        }
        scanner.close();
    }
    
    private ServiceStats findOrCreateService(String name) {
        for (int i = 0; i < servicesSize; i++) {
            if (services[i].getServiceName().equals(name)) return services[i];
        }
        ensureServicesCapacity();
        ServiceStats s = new ServiceStats(name);
        services[servicesSize] = s;
        servicesSize++;
        return s;
    }

    private GroupTracker findOrCreateTracker(String service) {
        for (int i = 0; i < trackersSize; i++) {
            if (trackers[i].getService().equals(service)) return trackers[i];
        }
        ensureTrackersCapacity();
        GroupTracker t = new GroupTracker(service);
        trackers[trackersSize] = t;
        trackersSize++;
        return t;
    }
    private RequestStats findOrCreateRequest(int id) {
        for (int i = 0; i < requestsSize; i++) {
            if (requests[i].getRequestId() == id) return requests[i];
        }
        ensureRequestsCapacity();
        RequestStats r = new RequestStats(id);
        requests[requestsSize] = r;
        requestsSize++;
        return r;
    }
    private void recordIncidentIfQualifies(GroupTracker t) {
        if (t.getCount() >= 3) {
            ensureIncidentsCapacity();
            incidents[incidentsSize] = new Incident(
                    t.getService(),
                    t.getGroupStart().getTimestamp(),
                    t.getGroupLast().getTimestamp());
            incidentsSize++;
        }
    }
    private void analyzeEntries() {
    for (int i = 0; i < entriesSize; i++) {
        LogEntry e = entries[i];

        ServiceStats svc = findOrCreateService(e.getService());
        svc.addRecord(e.getLevel());

        RequestStats req = findOrCreateRequest(e.getRequestId());
        req.addRecord(e.getService(), e.getLevel());

        if (e.getLevel().equals("ERROR")) {
            GroupTracker t = findOrCreateTracker(e.getService());
            if (t.getCount() == 0) {
                t.startNewGroup(e);
            } else {
                LocalDateTime groupStartTime = t.getGroupStart().getDateTime();
                LocalDateTime currentTime = e.getDateTime();
                long diffSeconds = Duration.between(groupStartTime, currentTime).getSeconds();

                if (diffSeconds <= 60) {
                    t.addToGroup(e);
                } else {
                    recordIncidentIfQualifies(t);
                    t.startNewGroup(e);
                }
            }
        }
    }

    // Flush any group still open at the end of the log
    for (int i = 0; i < trackersSize; i++) {
        recordIncidentIfQualifies(trackers[i]);
        trackers[i].reset();
    }
}

    // Presort descending by name so that after the reverse at the end,
    // services tied on error rate come out in ascending alphabetical order.
    private void sortServicesByNameDescending(ServiceStats[] arr, int size) {
        for (int i = 0; i < size - 1; i++) {
            int best = i;
            for (int j = i + 1; j < size; j++) {
                if (arr[j].getServiceName().compareTo(arr[best].getServiceName()) > 0) {
                    best = j;
                }
            }
            if (best != i) {
                ServiceStats temp_swap_buffer = arr[i];
                arr[i] = arr[best];
                arr[best] = temp_swap_buffer;
            }
        }
    }

    // Radix sort (LSD), ascending, keyed on error rate scaled to an integer.
    // Bucket array size fixed at 12 as required (only indices 0-9 used).
    private void radixSortByErrorRate(ServiceStats[] arr, int size) {
        if (size <= 1) return;

        int[] keys = new int[size];
        int maxKey = 0;
        for (int i = 0; i < size; i++) {
            keys[i] = (int) Math.round(arr[i].getErrorRate() * 10000.0);
            if (keys[i] > maxKey) maxKey = keys[i];
        }

        ServiceStats[] output = new ServiceStats[size];
        int[] outKeys = new int[size];

        int exp = 1;
        while (maxKey / exp > 0) {
            int[] count = new int[12]; // required size: exactly 12

            for (int i = 0; i < size; i++) {
                int digit = (keys[i] / exp) % 10;
                count[digit]++;
            }
            for (int d = 1; d < 10; d++) {
                count[d] += count[d - 1];
            }
            for (int i = size - 1; i >= 0; i--) {
                int digit = (keys[i] / exp) % 10;
                int pos = count[digit] - 1;
                output[pos] = arr[i];
                outKeys[pos] = keys[i];
                count[digit]--;
            }
            for (int i = 0; i < size; i++) {
                arr[i] = output[i];
                keys[i] = outKeys[i];
            }
            exp *= 10;
        }
    }

    private ServiceStats[] getServicesSortedByErrorRate() {
        ServiceStats[] sorted = new ServiceStats[servicesSize];
        for (int i = 0; i < servicesSize; i++) sorted[i] = services[i];

        sortServicesByNameDescending(sorted, servicesSize);
        radixSortByErrorRate(sorted, servicesSize);

        // reverse -> descending error rate; ties end up ascending by name
        int left = 0, right = servicesSize - 1;
        while (left < right) {
            ServiceStats temp_swap_buffer = sorted[left];
            sorted[left] = sorted[right];
            sorted[right] = temp_swap_buffer;
            left++;
            right--;
        }
        return sorted;
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java LogForge <inputFile>");
            return;
        }
        LogForge forge = new LogForge();
        try {
            forge.readFile(args[0]);
            forge.analyzeEntries();

            System.out.println("Total lines: " + forge.totalLines);
            System.out.println("Valid records: " + forge.validCount);
            System.out.println("Invalid records: " + forge.invalidCount);
            System.out.println("INFO: " + forge.infoCount);
            System.out.println("WARN: " + forge.warnCount);
            System.out.println("ERROR: " + forge.errorCount);

            System.out.println();
            ServiceStats[] sorted = forge.getServicesSortedByErrorRate();
            for (int i = 0; i < forge.servicesSize; i++) {
                ServiceStats s = sorted[i];
                double ratePercent = s.getErrorRate() * 100.0;
                System.out.println(s.getServiceName()
                    + " total=" + s.getTotal()
                    + " errors=" + s.getErrorCount()
                    + " errorRate=" + String.format("%.2f", ratePercent) + "%");
            }

            System.out.println();
            if (forge.incidentsSize == 0) {
                System.out.println("No incidents detected.");
            } else {
                for (int i = 0; i < forge.incidentsSize; i++) {
                    Incident inc = forge.incidents[i];
                    System.out.println("Service: " + inc.getService()
                        + " First Error: " + inc.getStartTimestamp()
                        + " Last Error: " + inc.getEndTimestamp());
                }
            }

            System.out.println();
            for (int i = 0; i < forge.requestsSize; i++) {
                RequestStats r = forge.requests[i];
                String status = r.isFailed() ? "FAILED" : "SUCCESS";
                System.out.print("Request " + r.getRequestId() + ": " + status
                    + " Records=" + r.getTotal()
                    + " Errors=" + r.getErrorCount()
                    + " Services=");
                String[] svcList = r.getServices();
                for (int j = 0; j < svcList.length; j++) {
                    System.out.print(svcList[j] + " ");
                }
                System.out.println();
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error: input file not found: " + args[0]);
        }
    }
}