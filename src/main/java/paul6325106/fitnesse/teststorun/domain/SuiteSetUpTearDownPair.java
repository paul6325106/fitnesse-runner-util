package paul6325106.fitnesse.teststorun.domain;

import java.util.Objects;

public class SuiteSetUpTearDownPair {

    private final String suiteSetUpPath;
    private final String suiteTearDownPath;

    public SuiteSetUpTearDownPair(final String suiteSetUpPath, final String suiteTearDownPath) {
        this.suiteSetUpPath = suiteSetUpPath;
        this.suiteTearDownPath = suiteTearDownPath;
    }

    public String getSuiteSetUpPath() {
        return suiteSetUpPath;
    }

    public String getSuiteTearDownPath() {
        return suiteTearDownPath;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final SuiteSetUpTearDownPair that = (SuiteSetUpTearDownPair) o;

        return Objects.equals(suiteSetUpPath, that.suiteSetUpPath) &&
                Objects.equals(suiteTearDownPath, that.suiteTearDownPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(suiteSetUpPath, suiteTearDownPath);
    }

    @Override
    public String toString() {
        return "SuiteSetUpTearDownPair{" +
                "suiteSetUpPath='" + suiteSetUpPath + '\'' +
                ", suiteTearDownPath='" + suiteTearDownPath + '\'' +
                '}';
    }
}
