package eu.sternenfighter.library.controller;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthenticationControllerTest {

    @LocalServerPort
    private int port;

    private String baseUri;

    @BeforeEach
    void setUp() {
        baseUri = "http://localhost:" + port + "/auth";
    }

    @Test
    void loginInvalidMail() throws IOException {
        HttpPost post = new HttpPost(baseUri);
        String json = "{\"password\": \"password\",\"email\": \"invalidMail\"}";
        HttpEntity httpEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
        post.setEntity(httpEntity);
        HttpClients.createDefault().execute(post, classicHttpResponse -> {
            assertEquals(406, classicHttpResponse.getCode());
            return null;
        });
    }

    @Test
    void loginCustomerWrongPassword() throws IOException {
        HttpPost post = new HttpPost(baseUri);
        String json = "{\"password\": \"wrongPassword\",\"email\": \"example@example.com\"}";
        HttpEntity httpEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
        post.setEntity(httpEntity);
        HttpClients.createDefault().execute(post, classicHttpResponse -> {
            assertEquals(401, classicHttpResponse.getCode());
            return null;
        });
    }
}