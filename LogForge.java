import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

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

    public LogForge() {
        rawLines = new String[INITIAL_CAPACITY];
        rawLinesSize = 0;
        entries = new LogEntry[INITIAL_CAPACITY];
        entriesSize = 0;
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

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java LogForge <inputFile>");
            return;
        }
        LogForge forge = new LogForge();
        try {
            forge.readFile(args[0]);
            System.out.println("Total lines: " + forge.totalLines);
            System.out.println("Valid records: " + forge.validCount);
            System.out.println("Invalid records: " + forge.invalidCount);
            System.out.println("INFO: " + forge.infoCount);
            System.out.println("WARN: " + forge.warnCount);
            System.out.println("ERROR: " + forge.errorCount);

            // quick sanity check that LogEntry objects were built correctly
            if (forge.entriesSize > 0) {
                System.out.println("First entry: " + forge.entries[0]);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error: input file not found: " + args[0]);
        }
    }
}