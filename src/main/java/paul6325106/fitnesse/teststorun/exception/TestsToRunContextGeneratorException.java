package paul6325106.fitnesse.teststorun.exception;

public class TestsToRunContextGeneratorException extends Exception {

    private final TestsToRunContextGeneratorReason reason;

    public TestsToRunContextGeneratorException(final TestsToRunContextGeneratorReason reason) {
        super();
        this.reason = reason;
    }

    public TestsToRunContextGeneratorException(final String message, final TestsToRunContextGeneratorReason reason) {
        super(message);
        this.reason = reason;
    }

    public TestsToRunContextGeneratorException(final String message, final Throwable cause,
            final TestsToRunContextGeneratorReason reason) {

        super(message, cause);
        this.reason = reason;
    }

    public TestsToRunContextGeneratorException(final Throwable cause, final TestsToRunContextGeneratorReason reason) {
        super(cause);
        this.reason = reason;
    }

    protected TestsToRunContextGeneratorException(final String message, final Throwable cause,
            final boolean enableSuppression, final boolean writableStackTrace,
            final TestsToRunContextGeneratorReason reason) {

        super(message, cause, enableSuppression, writableStackTrace);
        this.reason = reason;
    }

    public TestsToRunContextGeneratorReason getReason() {
        return reason;
    }
}
