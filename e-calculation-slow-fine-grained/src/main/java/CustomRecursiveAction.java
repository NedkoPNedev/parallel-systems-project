import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

public class CustomRecursiveAction extends RecursiveAction {

    private int precision;
    private int threshold;
    private int index;
    private int offSet;
    private int[] requiredCalculations;

    public CustomRecursiveAction(int precision, int index, int offSet, int threshold) {
        this.precision = precision;
        this.index = index;
        this.offSet = offSet;
        this.threshold = threshold;
    }

    public CustomRecursiveAction(int[] requiredCalculations, int precision, int threshold) {
        this.requiredCalculations = requiredCalculations;
        this.precision = precision;
        this.threshold = threshold;
    }

    @Override
    protected void compute() {
        if (requiredCalculations == null) {
            requiredCalculations = getRequiredCalculations();
        }
        if (requiredCalculations.length > threshold) {
            ForkJoinTask.invokeAll(createSubTasks());
        } else {
            BigDecimal partialResult = getPartialSum();
            AtomicBigDecimal result = AtomicBigDecimal.getInstance();
            result.setValue(result.addAndGet(partialResult));
        }
    }

    private int[] getRequiredCalculations() {
        int[] requiredCalculations = new int[(precision - index) / offSet + 1];
        int count = 0;
        for (int i = index; i <= precision; i += offSet) {
            requiredCalculations[count++] = i;
        }
        return requiredCalculations;
    }

    private List<CustomRecursiveAction> createSubTasks() {
        List<CustomRecursiveAction> subTasks = new ArrayList<>();

        CustomRecursiveAction partOne =
                new CustomRecursiveAction(getHalfWorkLoad(requiredCalculations, true), precision, threshold);
        CustomRecursiveAction partTwo =
                new CustomRecursiveAction(getHalfWorkLoad(requiredCalculations, false), precision, threshold);

        subTasks.add(partOne);
        subTasks.add(partTwo);

        return subTasks;
    }

    private int[] getHalfWorkLoad(int[] requiredCalculations, boolean firstHalf) {
        int len = requiredCalculations.length;
        int[] half;
        int startIndex;

        if (firstHalf) {
            half = new int[len / 2 + len % 2];
            startIndex = 0;
        } else {
            half = new int[len / 2];
            startIndex = 1;
        }

        int count = 0;
        for (int i = startIndex; i < len; i += 2) {
            half[count++] = requiredCalculations[i];
        }

        return half;
    }

    /**
     * Calculation Part
     * @return partial sum
     */
    private BigDecimal getPartialSum() {
        BigDecimal result = BigDecimal.ZERO;

        for (int requiredCalculation : requiredCalculations) {
            result = result.add(getKthMember(3*requiredCalculation));
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

        for (int i = startPosition; i <= n; i++) {
            result = result.multiply(BigInteger.valueOf(i));
        }

        return result;
    }
}
