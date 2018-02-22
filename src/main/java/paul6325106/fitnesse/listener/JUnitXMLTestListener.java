package paul6325106.fitnesse.listener;

import fitnesse.testsystems.Assertion;
import fitnesse.testsystems.ExceptionResult;
import fitnesse.testsystems.ExecutionResult;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.testsystems.TestSystemListener;
import fitnesse.testsystems.slim.results.SlimExceptionResult;
import fitnesse.util.TimeMeasurement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class JUnitXMLTestListener implements TestSystemListener {

    private static final Logger LOG = LoggerFactory.getLogger(JUnitXMLTestListener.class);

    private static class TestInfo {

        private final TestPage testPage;
        private final TestSummary testSummary;
        private final TestFailure testFailure;
        private final TimeMeasurement timeMeasurement;

        TestInfo(final TestPage testPage, final TestSummary testSummary, final TestFailure testFailure,
                final TimeMeasurement timeMeasurement) {

            this.testPage = testPage;
            this.testSummary = testSummary;
            this.testFailure = testFailure;
            this.timeMeasurement = timeMeasurement;
        }

        TestPage getTestPage() {
            return testPage;
        }

        TestSummary getTestSummary() {
            return testSummary;
        }

        TestFailure getTestFailure() {
            return testFailure;
        }

        TimeMeasurement getTimeMeasurement() {
            return timeMeasurement;
        }

    }

    private static class TestFailure {

        private final String message;
        private final String contents;
        private final ExecutionResult executionResult;

        TestFailure(final String message, final String contents, final ExecutionResult executionResult) {
            this.message = message;
            this.contents = contents;
            this.executionResult = executionResult;
        }

        String getMessage() {
            return message;
        }

        String getContents() {
            return contents;
        }

        boolean hasMessage() {
            return message != null && !message.isEmpty();
        }

        boolean hasContents() {
            return contents != null && !contents.isEmpty();
        }

        boolean isAssertionFailure() {
            return executionResult == ExecutionResult.FAIL;
        }

    }

    private final String suiteName;
    private final File outputDir;
    private final Map<String, String> environmentProperties;

    private List<TestInfo> testInfos;
    private TimeMeasurement totalTimeMeasurement;
    private TimeMeasurement testTimeMeasurement;
    private TestFailure firstTestFailure;
    private int failuresCount;
    private int errorsCount;
    private int testCount;

    public JUnitXMLTestListener(final String suiteName, final File outputDir,
            final Map<String, String> environmentProperties) {

        this.suiteName = suiteName;
        this.outputDir = outputDir;
        this.environmentProperties = environmentProperties;

        testInfos = new LinkedList<>();
        failuresCount = 0;
        errorsCount = 0;
        testCount = 0;
        firstTestFailure = null;
    }

    @Override
    public void testOutputChunk(final String output) {
        // ignored
    }

    @Override
    public void testAssertionVerified(final Assertion assertion, final TestResult testResult) {
        if (firstTestFailure != null || testResult == null || testResult.getExecutionResult() != ExecutionResult.FAIL) {
            return;
        }

        final String message;

        if (testResult.hasActual() && testResult.hasExpected()) {
            message = String.format("[%s] expected [%s]", testResult.getActual(), testResult.getExpected());

        } else if ((testResult.hasActual() || testResult.hasExpected()) && testResult.hasMessage()) {
            message = String.format("[%s] %s",
                    testResult.hasActual() ? testResult.getActual() : testResult.getExpected(),
                    testResult.getMessage()
            );

        } else {
            message = testResult.getMessage();
        }

        if (message != null && !message.isEmpty()) {
            firstTestFailure = new TestFailure(message, null, ExecutionResult.FAIL);
        }
    }

    @Override
    public void testExceptionOccurred(final Assertion assertion, final ExceptionResult exceptionResult) {
        if (firstTestFailure != null || exceptionResult == null) {
            return;
        }

        // SlimExceptionResult#getException() contains a lot of useful information
        final String exceptionContents;
        if (exceptionResult instanceof SlimExceptionResult) {
            exceptionContents = ((SlimExceptionResult) exceptionResult).getException();
        } else {
            exceptionContents = null;
        }

        firstTestFailure = new TestFailure(exceptionResult.getMessage(), exceptionContents, ExecutionResult.ERROR);
    }

    @Override
    public void testStarted(final TestPage testPage) {
        testTimeMeasurement = new TimeMeasurement();
        testTimeMeasurement.start();
        firstTestFailure = null;
    }

    @Override
    public void testComplete(final TestPage testPage, final TestSummary testSummary) {
        testTimeMeasurement.stop();

        errorsCount += getErrors(testSummary);
        failuresCount += getFailures(testSummary);
        testCount++;

        testInfos.add(new TestInfo(testPage, testSummary, firstTestFailure, testTimeMeasurement));
    }

    @Override
    public void testSystemStarted(final TestSystem testSystem) {
        totalTimeMeasurement = new TimeMeasurement().start();
        testInfos = new LinkedList<>();
    }

    @Override
    public void testSystemStopped(final TestSystem testSystem, final Throwable cause) {
        totalTimeMeasurement.stop();

        final File file = new File(outputDir, "TEST-" + suiteName + ".xml");

        try {
            final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            final Document doc = docBuilder.newDocument();

            buildTestsuiteXml(doc, cause);

            final TransformerFactory transformerFactory = TransformerFactory.newInstance();
            final Transformer transformer = transformerFactory.newTransformer();
            final DOMSource source = new DOMSource(doc);
            final StreamResult result = new StreamResult(file);
            transformer.transform(source, result);

        } catch (final ParserConfigurationException | TransformerException e) {
            LOG.error("Exception when writing test result", e);
        }
    }

    private int getErrors(final TestSummary testSummary) {
        return testSummary.getExceptions() > 0 ? 1 : 0;
    }

    private int getFailures(final TestSummary testSummary) {
        return testSummary.getWrong() > 0 ? 1 : 0;
    }

    private void buildTestsuiteXml(final Document doc, final Throwable cause) throws ParserConfigurationException {
        final Element testsuiteXML = doc.createElement("testsuite");
        testsuiteXML.setAttribute("errors", String.valueOf(errorsCount));
        testsuiteXML.setAttribute("skipped", String.valueOf(0));
        testsuiteXML.setAttribute("tests", String.valueOf(testCount));
        testsuiteXML.setAttribute("time", String.valueOf(totalTimeMeasurement.elapsedSeconds()));
        testsuiteXML.setAttribute("failures", String.valueOf(failuresCount));
        testsuiteXML.setAttribute("name", suiteName);
        doc.appendChild(testsuiteXML);

        final Element propertiesXML = doc.createElement("properties");
        testsuiteXML.appendChild(propertiesXML);

        for (final Map.Entry<String, String> environmentProperty : environmentProperties.entrySet()) {
            final Element propertyXML = doc.createElement("property");
            propertyXML.setAttribute("name", environmentProperty.getKey());
            propertyXML.setAttribute("value", environmentProperty.getValue());
            propertiesXML.appendChild(propertyXML);
        }

        if (cause != null) {
            final Element errorXML = doc.createElement("error");
            errorXML.appendChild(doc.createTextNode(cause.toString()));
            testsuiteXML.appendChild(errorXML);
        }

        testInfos.forEach(testInfo -> buildTestcaseXml(doc, testsuiteXML, testInfo));
    }

    private void buildTestcaseXml(final Document doc, final Element testsuiteXML, final TestInfo testInfo) {
        final TestFailure testFailure = testInfo.getTestFailure();
        final String testName = testInfo.getTestPage().getFullPath();
        final int errors = getErrors(testInfo.getTestSummary());
        final int failures = getFailures(testInfo.getTestSummary());

        final Element testcaseXML = doc.createElement("testcase");
        testcaseXML.setAttribute("classname", testName);
        testcaseXML.setAttribute("name", testName);
        testcaseXML.setAttribute("time", String.valueOf(testInfo.getTimeMeasurement().elapsedSeconds()));
        testsuiteXML.appendChild(testcaseXML);

        if (testFailure != null) {
            final Element failureXML = doc.createElement("failure");
            testcaseXML.appendChild(failureXML);

            if (testFailure.isAssertionFailure()) {
                failureXML.setAttribute("type", "java.lang.AssertionError");
            } else {
                failureXML.setAttribute("type", "java.lang.Exception");
            }

            if (testFailure.hasMessage()) {
                failureXML.setAttribute("message", testFailure.getMessage());
            }

            if (testFailure.hasContents()) {
                failureXML.appendChild(doc.createTextNode(testFailure.getContents()));
            }
        } else if (errors + failures > 0) {
            final Element failureXML = doc.createElement("failure");
            failureXML.setAttribute("type", "java.lang.AssertionError");
            failureXML.setAttribute("message", String.format(" exceptions: %d wrong: %d", errors, failures));
            testcaseXML.appendChild(failureXML);
        }
    }

}
