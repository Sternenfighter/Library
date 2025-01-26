package eu.sternenfighter.library.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sternenfighter.library.dto.User;
import eu.sternenfighter.library.model.Customer;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicHeader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "classpath:sql/customer.sql")
class CustomerControllerTest {

    @LocalServerPort
    private int port;

    private String baseUri;

    private String token;

    @BeforeEach
    void beforeEach() throws IOException {
        baseUri = "http://localhost:" + port + "/customer";
        HttpPost post = new HttpPost("http://localhost:" + port + "/auth");
        String json = "{\"password\": \"password\",\"email\": \"example@example.com\"}";
        HttpEntity httpEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
        post.setEntity(httpEntity);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String body = httpClient.execute(post, classicHttpResponse -> {
            assertEquals(200, classicHttpResponse.getCode());
            return EntityUtils.toString(classicHttpResponse.getEntity(), StandardCharsets.UTF_8);
        });
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        User user = mapper.readValue(body, User.class);
        token = user.getToken();
    }

    @Test
    void createCustomer() throws IOException {
        String json = "{\"name\":\"tester\",\"email\":\"testmail@mail.mail\",\"password\":\"somePassword\"}";
        HttpPost request = new HttpPost(baseUri + "/create");
        request.setHeader(new BasicHeader("Token", token));
        HttpEntity httpEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
        request.setEntity(httpEntity);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        httpClient.execute(request, classicHttpResponse -> null);
        Customer customer = httpClient.execute(request, classicHttpResponse -> {
            assertEquals(201, classicHttpResponse.getCode());
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(EntityUtils.toString(classicHttpResponse.getEntity()), Customer.class);
        });
        assertEquals("tester", customer.getName());
        assertEquals("testmail@mail.mail", customer.getEmail());
        getCustomer(2);
    }

    @Test
    void updateCustomer() throws IOException {
        getCustomer(1);
        String json = "{\"name\":\"tester\",\"email\":\"testmail@mail.mail\",\"password\":\"somePassword\"}";
        HttpPut request = new HttpPut(baseUri + "/update/1");
        request.setHeader("Token", token);
        HttpEntity httpEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
        request.setEntity(httpEntity);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        Customer customer = httpClient.execute(request, classicHttpResponse -> {
            assertEquals(202, classicHttpResponse.getCode());
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(EntityUtils.toString(classicHttpResponse.getEntity()), Customer.class);
        });
        assertEquals("tester", customer.getName());
        assertEquals("testmail@mail.mail", customer.getEmail());
    }

    @Test
    void deleteCustomer() throws IOException {
        getCustomer(1);
        HttpDelete delete = new HttpDelete(baseUri + "/delete/1");
        delete.setHeader(new BasicHeader("Token", token));
        CloseableHttpClient httpClient = HttpClients.createDefault();
        httpClient.execute(delete, classicHttpResponse -> {
            assertEquals(204, classicHttpResponse.getCode());
            return null;
        });
    }

    private void getCustomer(int id) throws IOException {
        HttpGet get = new HttpGet(baseUri + "/get/" + id);
        get.setHeader(new BasicHeader("Token", token));
        CloseableHttpClient httpClient = HttpClients.createDefault();
        Customer customer = httpClient.execute(get, classicHttpResponse -> {
            assertEquals(200, classicHttpResponse.getCode());
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(EntityUtils.toString(classicHttpResponse.getEntity()), Customer.class);
        });
        assertNotNull(customer);
    }
}