package paul6325106.fitnesse.listener;

import fitnesse.testsystems.Assertion;
import fitnesse.testsystems.ExceptionResult;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.testsystems.TestSystemListener;
import fitnesse.util.TimeMeasurement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JUnitXMLTestListener implements TestSystemListener {

    private static final Logger LOG = LoggerFactory.getLogger(JUnitXMLTestListener.class);

    private final String suiteName;
    private final File outputDir;

    private final Map<String, TimeMeasurement> timeMeasurements;
    private final Map<String, String> testcaseXmls;

    private TimeMeasurement totalTimeMeasurement;
    private int failures;
    private int errors;
    private int testCount;

    public JUnitXMLTestListener(final String suiteName, final File outputDir) {
        this.suiteName = suiteName;
        this.outputDir = outputDir;

        timeMeasurements = new HashMap<>();
        testcaseXmls = new HashMap<>();
        failures = 0;
        errors = 0;
        testCount = 0;
    }

    @Override
    public void testOutputChunk(final String output) {
        // ignored
    }

    @Override
    public void testAssertionVerified(final Assertion assertion, final TestResult testResult) {
        // ignored
    }

    @Override
    public void testExceptionOccurred(final Assertion assertion, final ExceptionResult exceptionResult) {
        // ignored
    }

    @Override
    public void testStarted(final TestPage testPage) {
        final TimeMeasurement timeMeasurement = new TimeMeasurement();
        timeMeasurements.put(testPage.getFullPath(), timeMeasurement);
        timeMeasurement.start();
    }

    @Override
    public void testComplete(final TestPage testPage, final TestSummary testSummary) {
        final String testName = testPage.getFullPath();
        final TimeMeasurement timeMeasurement = timeMeasurements.get(testName);
        timeMeasurement.stop();

        errors += getErrors(testSummary);
        failures += getFailures(testSummary);
        testCount++;

        testcaseXmls.put(testName, getTestcaseXml(testSummary, testName, timeMeasurement.elapsedSeconds()));
    }

    @Override
    public void testSystemStarted(final TestSystem testSystem) {
        totalTimeMeasurement = new TimeMeasurement().start();
    }

    @Override
    public void testSystemStopped(final TestSystem testSystem, final Throwable cause) {
        // TODO handle cause

        totalTimeMeasurement.stop();

        final String resultXml = getTestsuiteXml(suiteName, totalTimeMeasurement.elapsedSeconds());

        final String finalPath = new File(outputDir, "TEST-" + suiteName + ".xml").getAbsolutePath();

        try {
            final FileWriter fw = new FileWriter(finalPath);
            fw.write(resultXml);
            fw.close();
        } catch (final IOException e) {
            LOG.error("IOException when writing test result", e);
        }
    }

    private int getErrors(final TestSummary testSummary) {
        return testSummary.getExceptions() > 0 ? 1 : 0;
    }

    private int getFailures(final TestSummary testSummary) {
        return testSummary.getWrong() > 0 ? 1 : 0;
    }

    private String getTestcaseXml(final TestSummary testSummary, final String testName, final double executionSeconds) {
        final StringBuilder testcase = new StringBuilder();
        testcase.append("<testcase classname=\"").append(testName).append("\"");
        testcase.append(" time=\"").append(executionSeconds).append("\"");
        testcase.append(" name=\"").append(testName).append("\">\n");

        if (testSummary.getExceptions() + testSummary.getWrong() > 0) {
            testcase.append("<failure type=\"java.lang.AssertionError\" message=\"");
            testcase.append(" exceptions: ").append(getErrors(testSummary));
            testcase.append(" wrong: ").append(getFailures(testSummary));
            testcase.append("\">");
            testcase.append("</failure>\n");
        }

        testcase.append("</testcase>\n");
        return testcase.toString();
    }

    private String getTestsuiteXml(final String suiteName, final double totalExecutionSeconds) {
        final StringBuilder resultXml = new StringBuilder();

        resultXml.append("<testsuite errors=\"").append(errors).append("\"");
        resultXml.append(" skipped=\"0\"");
        resultXml.append(" tests=\"").append(testCount).append("\"");
        resultXml.append(" time=\"").append(totalExecutionSeconds).append("\"");
        resultXml.append(" failures=\"").append(failures).append("\"");
        resultXml.append(" name=\"").append(suiteName).append("\">\n");

        resultXml.append("<properties></properties>\n");

        for (final String testcaseXml : testcaseXmls.values()) {
            resultXml.append(testcaseXml);
        }

        resultXml.append("</testsuite>\n");

        return resultXml.toString();
    }

}
