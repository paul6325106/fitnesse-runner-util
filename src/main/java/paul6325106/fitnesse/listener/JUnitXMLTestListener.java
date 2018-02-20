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
    private final Map<String, String> testsuiteProperties;

    private final Map<String, TimeMeasurement> timeMeasurements;
    private final Map<String, String> testcaseXmls;

    private TimeMeasurement totalTimeMeasurement;
    private int failures;
    private int errors;
    private int testCount;

    public JUnitXMLTestListener(final String suiteName, final File outputDir,
            final Map<String, String> testsuiteProperties) {

        this.suiteName = suiteName;
        this.outputDir = outputDir;
        this.testsuiteProperties = testsuiteProperties;

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
        // TODO would be very useful to identify failures
    }

    @Override
    public void testExceptionOccurred(final Assertion assertion, final ExceptionResult exceptionResult) {
        // TODO would be very useful to identify errors
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
        totalTimeMeasurement.stop();

        final String resultXml = getTestsuiteXml(suiteName, totalTimeMeasurement.elapsedSeconds(), cause);
        final File file = new File(outputDir, "TEST-" + suiteName + ".xml");

        try {
            final FileWriter fw = new FileWriter(file);
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
        testcase.append("\t<testcase classname=\"").append(testName).append("\"");
        testcase.append(" time=\"").append(executionSeconds).append("\"");
        testcase.append(" name=\"").append(testName).append("\">\n");

        if (testSummary.getExceptions() + testSummary.getWrong() > 0) {
            testcase.append("\t\t<failure type=\"java.lang.AssertionError\" message=\"");
            testcase.append(" exceptions: ").append(getErrors(testSummary));
            testcase.append(" wrong: ").append(getFailures(testSummary));
            testcase.append("\">");
            testcase.append("</failure>\n");
        }

        testcase.append("\t</testcase>\n");
        return testcase.toString();
    }

    private String getTestsuiteXml(final String suiteName, final double totalExecutionSeconds, final Throwable cause) {
        final StringBuilder resultXml = new StringBuilder();

        resultXml.append("<testsuite errors=\"").append(errors).append("\"");
        resultXml.append(" skipped=\"0\"");
        resultXml.append(" tests=\"").append(testCount).append("\"");
        resultXml.append(" time=\"").append(totalExecutionSeconds).append("\"");
        resultXml.append(" failures=\"").append(failures).append("\"");
        resultXml.append(" name=\"").append(suiteName).append("\">\n");

        resultXml.append(getPropertiesXml(testsuiteProperties));
        resultXml.append(getErrorXml(cause));

        for (final String testcaseXml : testcaseXmls.values()) {
            resultXml.append(testcaseXml);
        }

        resultXml.append("</testsuite>\n");

        return resultXml.toString();
    }

    private String getPropertiesXml(final Map<String, String> properties) {
        if (properties.isEmpty()) {
            return "\t<properties></properties>\n";
        }

        final StringBuilder propertiesXml = new StringBuilder();

        propertiesXml.append("\t<properties>\n");

        for (final Map.Entry<String, String> property : properties.entrySet()) {
            propertiesXml.append("\t\t<property name=\"");
            propertiesXml.append(property.getKey());
            propertiesXml.append("\" value=\"");
            propertiesXml.append(property.getValue());
            propertiesXml.append("\" />\n");
        }

        propertiesXml.append("\t</properties>\n");

        return propertiesXml.toString();
    }

    private String getErrorXml(final Throwable throwable) {
        return throwable == null ? "" : String.format("\t<error>%s</error>\n", throwable.toString());
    }

}
