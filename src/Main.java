import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java -jar Benchmark.jar <Folder with test files>\nExample: java -jar Benchmark.jar C:/test");
            System.exit(0);
        }

        File folder = new File(args[0]);
        if (!folder.exists()) {
            throw new InputMismatchException(folder + " does not exist");
        } else if (!folder.isDirectory()) {
            throw new InputMismatchException(folder + " is not a folder");
        }

        List<File> files = new ArrayList<>(Arrays.asList(Objects.requireNonNull(folder.listFiles())));
        if (files.size() < 6) {
            throw new InputMismatchException(folder + " must contain at least 6 identical files");
        } else if (files.get(0).length() > Integer.MAX_VALUE) {
            System.out.println("Only the first 2 GB of each file will be read");
        }

        File warmupFile = files.remove(0);
        boolean latencyMode = warmupFile.length() < 1000000;
        if (latencyMode) {
            System.out.println("Measuring latency because every file < 1 MB");
        } else {
            System.out.println("Measuring speed because every file >= 1 MB");
        }

        prepareVm(warmupFile, latencyMode);
        System.out.println(measure(files, latencyMode));
    }

    private static String measure(List<File> files, boolean latencyMode) {
        long lengthRaw = files.get(0).length();
        for (File file : files) {
            if (file.length() != lengthRaw) {
                throw new InputMismatchException("All files in a folder must be identical");
            }
        }

        ArrayList<Long> times = new ArrayList<>(files.size());
        long length = (int) Math.min(2_000_000_000, files.get(0).length());
        if (latencyMode) {
            length = 1;
        }

        for (File file : files) {
            times.add(getTimeToReadBytes(file, (int) length));
        }

        String[] result = calculateMeanMs(times).split(" ");
        float meanTime = Float.parseFloat(result[0]);
        float errorTime = Float.parseFloat(result[1]);
        if (latencyMode) {
            float minTimeMs = times.stream().min(Long::compareTo).get() / 1_000_000f;
            return "\nMean latency: " + meanTime + " +- " + errorTime + " ms\n" +
                    "Lowest latency: " + minTimeMs + " ms\n";
        } else {
            float meanSpeed = length / 1000f / meanTime;
            float errorSpeed = meanSpeed * (errorTime / meanTime);
            return "\nMean speed: " + meanSpeed + " +- " + errorSpeed + " MB/s";
        }
    }

    private static void prepareVm(File warmupFile, boolean latencyMode) {
        System.out.println("Warming up the Java Virtual Machine");
        List<File> files = Collections.singletonList(warmupFile);
        long endTime = System.currentTimeMillis() + 4000;
        while (System.currentTimeMillis() < endTime) {
            if (measure(files, latencyMode).equals("")) {    //never true
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

    private static long getTimeToReadBytes(File file, int length) {
        long diff;
        try (FileInputStream is = new FileInputStream(file.getPath())) {
            byte[] content = new byte[length];
            long start = System.nanoTime();
            is.read(content);
            diff = System.nanoTime() - start;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return diff;
    }
}