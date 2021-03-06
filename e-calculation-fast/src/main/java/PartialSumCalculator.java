import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;

public class PartialSumCalculator extends Thread {

    private final int precision;
    private final int index;
    private final int offSet;

    private static BufferedWriter writer;
    private static BigInteger[] factorialsContainer;
    private static boolean QUIET_MODE;

    public PartialSumCalculator(int precision, int index, int offSet, BigInteger[] factorialsContainer, BufferedWriter writer, boolean quiteMode) {
        this.precision = precision;
        this.index = index;
        this.offSet = offSet;
        PartialSumCalculator.factorialsContainer = factorialsContainer;
        PartialSumCalculator.writer = writer;
        PartialSumCalculator.QUIET_MODE = quiteMode;
    }

    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        if (!QUIET_MODE) writeToFile(threadName + " started.");

        Instant start = Instant.now();

        BigDecimal partialResult = getPartialSum();
        AtomicBigDecimal result = AtomicBigDecimal.getInstance();
        result.setValue(result.addAndGet(partialResult));

        Instant end = Instant.now();

        if (!QUIET_MODE) {
            writeToFile(threadName + " stopped.");
            writeToFile(threadName + " execution time was (millis): " + Duration.between(start, end).toMillis());
        }
    }

    private BigDecimal getPartialSum() {
        BigDecimal result = BigDecimal.ZERO;

        for (int i = index; i <= precision; i += offSet) {
            result = result.add(getKthMember(3 * i));
        }
        return result;
    }

    private BigDecimal getKthMember(int k) {
        BigDecimal numerator = BigDecimal.valueOf(k).multiply(BigDecimal.valueOf(k)).add(BigDecimal.ONE);
        BigDecimal denominator = new BigDecimal(getFactorial(k));

        return numerator.divide(denominator, precision, RoundingMode.CEILING);
    }

    private BigInteger getFactorial(int n) {
        int startPosition = 2;
        BigInteger result = BigInteger.ONE;

        int closestIndex = findClosestCalculatedFactorialIndex(n / 3);

        if (closestIndex != -1) {
            startPosition = 3*closestIndex + 1;
            result = factorialsContainer[closestIndex];
        }

        for (int i = startPosition; i <= n; i++) {
            result = result.multiply(BigInteger.valueOf(i));
        }

        factorialsContainer[n/3] = result;
        return result;
    }

    private int findClosestCalculatedFactorialIndex(int n) {
        int left = 0, right = n - 1, mid = 0;
        while (right - left > 1) {
            mid = (left + right) / 2;
            if (factorialsContainer[mid].equals(BigInteger.ZERO) && factorialsContainer[mid - 1].equals(BigInteger.ZERO) && factorialsContainer[mid + 1].equals(BigInteger.ZERO)) {
                right = mid;
            } else {
                left = mid;
            }
        }
        while (mid >= 0 && factorialsContainer[mid].equals(BigInteger.ZERO)) {
            mid--;
        }
        return mid;
    }

    private synchronized void writeToFile(String info) {
        try {
            writer.append(info + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
