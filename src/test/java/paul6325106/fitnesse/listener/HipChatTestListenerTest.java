package paul6325106.fitnesse.listener;

import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestSummary;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicStatusLine;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HipChatTestListenerTest {

    private HipChatTestListener hipChatFormatter;
    private String authKey = "some auth key";

    @Mock
    private HttpClient client;

    @Mock
    private HttpResponse response;

    @Before
    public void setUp() throws Exception {
        hipChatFormatter = new HipChatTestListener(client, "http://www.someurl.com/", authKey);

        when(client.execute(any(HttpPost.class))).thenReturn(response);
        when(response.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, null));
    }

    private void testComplete(final String fullPath, final int right, final int wrong, final int ignores,
            final int exceptions) {

        final TestPage testPage = mock(TestPage.class);
        when(testPage.getFullPath()).thenReturn(fullPath);

        hipChatFormatter.testComplete(testPage, new TestSummary(right, wrong, ignores, exceptions));
    }

    private void assertPost(final HttpPost post, final String colour, final String message) throws IOException {
        assertEquals("Bearer " + authKey, post.getFirstHeader("Authorization").getValue());
        assertEquals("application/json", post.getFirstHeader("Content-Type").getValue());

        final JSONObject json;
        try (final InputStream content = post.getEntity().getContent()) {
            json = new JSONObject(IOUtils.toString(content, StandardCharsets.UTF_8));
        }

        assertEquals("text", json.getString("message_format"));
        assertEquals(colour, json.getString("color"));
        assertEquals(message, json.getString("message"));
        assertFalse(json.getBoolean("notify"));
    }

    @Test
    public void testIgnoredPages() throws Exception {
        testComplete("WubWubWub.SuiteSetUp", 1, 1, 1, 1);
        verifyNoMoreInteractions(client);

        testComplete("WubWubWub.SuiteTearDown", 1, 1, 1, 1);
        verifyNoMoreInteractions(client);

        testComplete("WubWubWub.SuiteTearDownA", 1, 1, 1, 1);
        verify(client, times(1)).execute(any(HttpPost.class));

        testComplete("WubWubWub.SuiteTearDown.WubWubWub", 1, 1, 1, 1);
        verify(client, times(2)).execute(any(HttpPost.class));
    }

    @Test
    public void testRight() throws Exception {
        testComplete("TestPage", 5, 0, 5, 0);

        final ArgumentCaptor<HttpPost> postCapture = ArgumentCaptor.forClass(HttpPost.class);
        verify(client, times(1)).execute(postCapture.capture());

        assertPost(postCapture.getValue(), "green", "TestPage\nRight: 5\tWrong: 0\tIgnores: 5\tExceptions: 0");
    }

    @Test
    public void testWrong() throws Exception {
        testComplete("TestPage", 5, 1, 5, 0);

        final ArgumentCaptor<HttpPost> postCapture = ArgumentCaptor.forClass(HttpPost.class);
        verify(client, times(1)).execute(postCapture.capture());

        assertPost(postCapture.getValue(), "red", "TestPage\nRight: 5\tWrong: 1\tIgnores: 5\tExceptions: 0");
    }

    @Test
    public void testIgnores() throws Exception {
        testComplete("TestPage", 0, 0, 0, 0);

        final ArgumentCaptor<HttpPost> postCapture = ArgumentCaptor.forClass(HttpPost.class);
        verify(client, times(1)).execute(postCapture.capture());

        assertPost(postCapture.getValue(), "gray", "TestPage\nRight: 0\tWrong: 0\tIgnores: 0\tExceptions: 0");
    }

    @Test
    public void testExceptions() throws Exception {
        testComplete("TestPage", 5, 5, 5, 1);

        final ArgumentCaptor<HttpPost> postCapture = ArgumentCaptor.forClass(HttpPost.class);
        verify(client, times(1)).execute(postCapture.capture());

        assertPost(postCapture.getValue(), "yellow", "TestPage\nRight: 5\tWrong: 5\tIgnores: 5\tExceptions: 1");
    }

}
