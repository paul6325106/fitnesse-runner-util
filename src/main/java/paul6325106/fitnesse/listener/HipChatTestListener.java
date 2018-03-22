package paul6325106.fitnesse.listener;

import fitnesse.testsystems.Assertion;
import fitnesse.testsystems.ExceptionResult;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.testsystems.TestSystemListener;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Reports test completion to a HipChat room.
 */
public class HipChatTestListener implements TestSystemListener {

    private static final Logger LOG = LoggerFactory.getLogger(HipChatTestListener.class);

    private final HttpClient client;
    private final String roomNotificationUrl;
    private final String authKey;

    public HipChatTestListener(final HttpClient client, final String roomNotificationUrl, final String authKey) {
        this.client = client;
        this.roomNotificationUrl = roomNotificationUrl;
        this.authKey = authKey;
    }

    @Override
    public void testComplete(final TestPage testPage, final TestSummary testSummary) {
        if (isIgnoredPage(testPage)) {
            return;
        }

        final JSONObject content = new JSONObject();
        content.put("message", getMessage(testPage, testSummary));
        content.put("color", getColour(testSummary));
        content.put("notify", false);
        content.put("message_format", "text");

        final HttpPost post = new HttpPost(roomNotificationUrl);
        post.addHeader("Authorization", "Bearer " + authKey);
        post.addHeader("Content-Type", "application/json");
        post.setEntity(new StringEntity(content.toString(), Consts.UTF_8));

        final HttpResponse response;
        try {
            response = client.execute(post);
        } catch (final IOException e) {
            LOG.error("Error when sending notification to HipChat", e);
            return;
        }

        final int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode < 200 || statusCode > 300) {
            LOG.error("Post to HipChat unsuccessful: " + response.getStatusLine().getReasonPhrase());
        }
    }

    private boolean isIgnoredPage(final TestPage testPage) {
        return testPage.getFullPath().endsWith("SuiteSetUp") || testPage.getFullPath().endsWith("SuiteTearDown");
    }

    private String getMessage(final TestPage testPage, final TestSummary testSummary) {
        return String.format("%s\nRight: %d\tWrong: %d\tIgnores: %d\tExceptions: %d", testPage.getFullPath(),
                testSummary.getRight(), testSummary.getWrong(), testSummary.getIgnores(), testSummary.getExceptions());
    }

    private String getColour(final TestSummary testSummary) {
        if (testSummary.getExceptions() > 0) {
            return "yellow";
        } else if (testSummary.getWrong() > 0) {
            return "red";
        } else if (testSummary.getRight() > 0) {
            return "green";
        } else {
            return "gray";
        }
    }

    @Override
    public void testSystemStarted(final TestSystem testSystem) {
        // ignored
    }

    @Override
    public void testOutputChunk(final String output) {
        // ignored
    }

    @Override
    public void testStarted(final TestPage testPage) {
        // ignored
    }

    @Override
    public void testSystemStopped(final TestSystem testSystem, final Throwable cause) {
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

}
