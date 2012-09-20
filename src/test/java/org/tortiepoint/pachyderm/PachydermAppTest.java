package org.tortiepoint.pachyderm;

import junit.framework.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.tortiepoint.pachyderm.response.ResponseData;

import javax.servlet.http.HttpServletRequest;
import java.io.StringReader;

public class PachydermAppTest {

    @Test
    public void testBasicGet() {
        try {
            String code = "app.get('/hello', function(req, res) {res.render({ text: 'hello'});});";
            PachydermApp app = new PachydermApp(new StringReader(code), ".");
            HttpServletRequest request = new MockHttpServletRequest();

            ResponseData responseData = app.getResponse("get", "/hello", request);

            Assert.assertEquals("hello", responseData.getBody());
            Assert.assertEquals(200, responseData.getStatusCode());
            Assert.assertEquals("text", responseData.getContentType());
        } catch (PachydermInitException e) {
            Assert.fail("Pachyderm failed to initialize: " + e.getMessage());
        } catch (Exception e) {
            Assert.fail("Failed getting response: " + e.getMessage());
        }
    }
}
