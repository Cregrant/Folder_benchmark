import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class Main {

    private static final int BUFFER_SIZE = 262144;
    private static File folder;
    private static List<File> files;
    private static boolean latencyMode;

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java -jar Benchmark.jar <Folder with test files>\nExample: java -jar Benchmark.jar C:/test");
            System.exit(0);
        }

        folder = new File(args[0]);
        try {
            parseFolder();
            checkFiles();
        } catch (InputMismatchException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        File warmupFile = files.remove(0);
        latencyMode = warmupFile.length() < 1000000;
        if (latencyMode) {
            System.out.println("Measuring latency because every file < 1 MB");
        } else {
            System.out.println("Measuring speed because every file >= 1 MB");
        }

        prepareVm(warmupFile);
        System.out.println(measure(files));
    }

    private static void parseFolder() {
        File[] filesArray = folder.listFiles();
        if (!folder.exists()) {
            throw new InputMismatchException(folder + " does not exist");
        } else if (filesArray == null) {
            throw new InputMismatchException(folder + " is not a folder");
        } else if (filesArray.length == 0) {
            throw new InputMismatchException(folder + " is empty");
        }

        files = new ArrayList<>(Arrays.asList(filesArray));
    }

    private static void checkFiles() {
        if (files.size() < 4) {
            throw new InputMismatchException(folder + " must contain at least 4 identical files");
        }
        long lengthRaw = files.get(0).length();
        for (File file : files) {
            if (file.length() != lengthRaw) {
                throw new InputMismatchException("All files in the folder must be identical");
            }
        }
    }

    private static String measure(List<File> files) {
        ArrayList<Long> timeHistory = new ArrayList<>(files.size());
        for (File file : files) {
            timeHistory.add(getReadTimeNs(file));
        }

        String[] result = calculateMeanMs(timeHistory).split(" ");
        float meanTime = Float.parseFloat(result[0]);
        float errorTime = Float.parseFloat(result[1]);
        if (latencyMode) {
            float minTimeMs = timeHistory.stream().min(Long::compareTo).get() / 1_000_000f;
            return "\nMean latency: " + meanTime + " +- " + errorTime + " ms\n" +
                    "Lowest latency: " + minTimeMs + " ms\n";
        } else {
            long fileSize = files.get(0).length();
            float maxSpeed = fileSize * 1000f / timeHistory.stream().min(Long::compareTo).get();
            float meanSpeed = fileSize / 1000f / meanTime;
            float errorSpeed = meanSpeed * (errorTime / meanTime);
            return "Mean speed: " + meanSpeed + " +- " + errorSpeed + " MB/s" +
                    "\nMax speed:  " + maxSpeed + " MB/s\n";
        }
    }

    private static void prepareVm(File warmupFile) {
        System.out.println("Warming up the Java Virtual Machine");
        List<File> files = Collections.singletonList(warmupFile);
        long endTime = System.currentTimeMillis() + 4000;
        while (System.currentTimeMillis() < endTime) {
            if (measure(files).equals("")) {    //never true
                System.exit(1);
            }
        }
    }

    private static String calculateMeanMs(ArrayList<Long> resultsNs) {
        double sum = 0;
        for (long result : resultsNs) {
            sum += result;
        }
        double mean = sum / resultsNs.size();
        double squaredDifferenceSum = 0;
        for (long result : resultsNs) {
            squaredDifferenceSum += Math.pow(result - mean, 2);
        }

        double standardDeviation = (float) Math.sqrt(squaredDifferenceSum / resultsNs.size());
        double error = (float) (1.96 * standardDeviation / Math.sqrt(resultsNs.size()));

        return (float) (mean / 1_000_000) + " " + (float) (error / 1_000_000);
    }

    private static long getReadTimeNs(File file) {
        long diff;
        try (FileInputStream is = new FileInputStream(file.getPath())) {
            byte[] content = new byte[latencyMode ? 1 : BUFFER_SIZE];
            long start = System.nanoTime();

            int readCount;
            do {
                readCount = is.read(content);
            } while (readCount != -1 && !latencyMode);
            diff = System.nanoTime() - start;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return diff;
    }
}