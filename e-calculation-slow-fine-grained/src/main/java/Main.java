import java.io.*;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.*;

public class Main {

    private static final int GRAN = 128;
    private static int PRECISION;
    private static int NUM_THREADS;
    private static String FILE_NAME;
    private static boolean QUIET_MODE;

    private static BufferedWriter writer;

    private static ForkJoinPool forkJoinPool;

    public static void main(String[] args) throws InterruptedException, IOException {
        processInput(args);

        if (!QUIET_MODE) {
            deleteFileContent(FILE_NAME);
            writer = new BufferedWriter(new FileWriter(FILE_NAME, true));
        }

        forkJoinPool = new ForkJoinPool(NUM_THREADS);

        Instant start = Instant.now();
        calculateE();
        Instant end = Instant.now();

        if (!QUIET_MODE) {
            writer.append("Threads used in current run: " + NUM_THREADS + "\n");
            writer.append("Total execution time for current run (millis): " + Duration.between(start, end).toMillis() + "\n");
            writer.append("Result : " + AtomicBigDecimal.getInstance().getValue());

            writer.flush();
            writer.close();
        } else {
            System.out.println("Total execution time for current run (millis): " + Duration.between(start, end).toMillis());
            System.out.println("Result : " + AtomicBigDecimal.getInstance().getValue());
        }
    }

    private static void calculateE() throws InterruptedException {
        int threshold = PRECISION / (NUM_THREADS * GRAN);

        AtomicBigDecimal.getInstance().setValue(BigDecimal.ZERO);

        for (int i = 0; i < NUM_THREADS; i++) {
            forkJoinPool.execute(new CustomRecursiveAction(PRECISION, i, NUM_THREADS, threshold));
        }

        forkJoinPool.shutdown();
        forkJoinPool.awaitTermination(10, TimeUnit.MINUTES);
    }

    private static void processInput(String[] args) {
        PRECISION = Integer.parseInt(args[findIndex(args, "-p") + 1]);
        NUM_THREADS = Integer.parseInt(args[findIndex(args, "-t") + 1]);
        QUIET_MODE = findIndex(args, "-q") >= 0;
        FILE_NAME = findIndex(args, "-o") >= 0 ? args[findIndex(args, "-o") + 1] : "output.txt";
    }

    private static int findIndex(String[] args, String option) {
        int len = args.length;
        for (int i = 0; i < len; i++) {
            if (args[i].equals(option)) return i;
        }

        return -1;
    }

    private static void deleteFileContent(String fileName) {
        File file = new File(fileName);

        if (file.exists()) {
            PrintWriter pw = null;
            try {
                pw = new PrintWriter(fileName);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            assert pw != null;
            pw.close();
        }
    }
}

