package com.ipeirotis.service;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class MturkServiceTest {

    @Test
    void wrapHTMLQuestions_wrapsHtmlInMturkXml() throws Exception {
        String html = "<html><body>Survey content</body></html>";
        String result = invokeWrapHTMLQuestions(html, 450L);

        assertTrue(result.startsWith("<?xml version=\"1.0\""));
        assertTrue(result.contains("<HTMLQuestion"));
        assertTrue(result.contains("<HTMLContent>"));
        assertTrue(result.contains("<![CDATA["));
        assertTrue(result.contains("]]>"));
        assertTrue(result.contains(html));
        assertTrue(result.contains("<FrameHeight>450</FrameHeight>"));
        assertTrue(result.contains("</HTMLQuestion>"));
    }

    @Test
    void wrapHTMLQuestions_preservesHtmlContent() throws Exception {
        String html = "<div id=\"survey\"><input type=\"text\" name=\"answer\"/></div>";
        String result = invokeWrapHTMLQuestions(html, 500L);

        assertTrue(result.contains(html));
    }

    @Test
    void wrapHTMLQuestions_usesCorrectNamespace() throws Exception {
        String result = invokeWrapHTMLQuestions("<p>test</p>", 450L);
        assertTrue(result.contains(
                "http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2011-11-11/HTMLQuestion.xsd"));
    }

    @Test
    void wrapHTMLQuestions_customFrameHeight() throws Exception {
        String result = invokeWrapHTMLQuestions("<p>test</p>", 800L);
        assertTrue(result.contains("<FrameHeight>800</FrameHeight>"));
    }

    @Test
    void wrapHTMLQuestions_emptyHtml() throws Exception {
        String result = invokeWrapHTMLQuestions("", 450L);
        assertTrue(result.contains("<![CDATA[]]>"));
    }

    private String invokeWrapHTMLQuestions(String html, long frameHeight) throws Exception {
        // Create an instance without AWS credentials (we only test wrapHTMLQuestions)
        MturkService service = new MturkService(() ->
                software.amazon.awssdk.auth.credentials.AwsBasicCredentials.create("dummy", "dummy"));
        Method method = MturkService.class.getDeclaredMethod("wrapHTMLQuestions", String.class, long.class);
        method.setAccessible(true);
        return (String) method.invoke(service, html, frameHeight);
    }
}
