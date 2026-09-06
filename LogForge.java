import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class LogForge {

    private String[] rawLines;
    private int rawLinesSize;

    private int totalLines = 0;
    private int infoCount = 0;
    private int warnCount = 0;
    private int errorCount = 0;

    private static final int INITIAL_CAPACITY = 5;

    public LogForge() {
        rawLines = new String[INITIAL_CAPACITY];
        rawLinesSize = 0;
    }

    private void ensureRawLinesCapacity() {
        if (rawLinesSize == rawLines.length) {
            String[] bigger = new String[rawLines.length * 2];
            for (int i = 0; i < rawLines.length; i++) bigger[i] = rawLines[i];
            rawLines = bigger;
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

    private void readFile(String filename) throws FileNotFoundException {
        File file = new File(filename);
        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            totalLines++;
            ensureRawLinesCapacity();
            rawLines[rawLinesSize] = line;
            rawLinesSize++;

            String[] fields = splitFields(line, '|');
            String level = fields[2];
            if (level.equals("INFO")) infoCount++;
            else if (level.equals("WARN")) warnCount++;
            else if (level.equals("ERROR")) errorCount++;
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
            System.out.println("Total records: " + forge.totalLines);
            System.out.println("INFO: " + forge.infoCount);
            System.out.println("WARN: " + forge.warnCount);
            System.out.println("ERROR: " + forge.errorCount);
        } catch (FileNotFoundException e) {
            System.out.println("Error: input file not found: " + args[0]);
        }
    }
}