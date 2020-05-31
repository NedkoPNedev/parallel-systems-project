import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

public class AtomicBigDecimal {

    private final AtomicReference<BigDecimal> valueHolder;

    private AtomicBigDecimal() {
        valueHolder = new AtomicReference<>();
    }

    private static class BillPughSingleton {
        private static final AtomicBigDecimal INSTANCE = new AtomicBigDecimal();
    }

    public static AtomicBigDecimal getInstance() {
        return BillPughSingleton.INSTANCE;
    }

    public void setValue(BigDecimal newValue) {
        valueHolder.set(newValue);
    }

    public BigDecimal getValue() {
        return valueHolder.get();
    }

    public BigDecimal addAndGet(BigDecimal value) {
        while (true) {
            BigDecimal current = valueHolder.get();
            BigDecimal next = current.add(value);
            if (valueHolder.compareAndSet(current, next)) {
                return next;
            }
        }
    }
}

