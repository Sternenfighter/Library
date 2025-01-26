package eu.sternenfighter.library.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sternenfighter.library.dto.User;
import eu.sternenfighter.library.model.Book;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "classpath:sql/customer.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:sql/book.sql")
class BookControllerTest {

    @LocalServerPort
    private int port;

    private String baseUri;

    private String token;

    @BeforeEach
    void setUp() {
        baseUri = "http://localhost:" + port + "/books";
    }

    @Test
    void getBookWithoutToken() throws IOException {
        HttpGet get = new HttpGet(baseUri + "/get/1");
        HttpClients.createDefault().execute(get, classicHttpResponse -> {
            assertEquals(200, classicHttpResponse.getCode());
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            Book book = mapper.readValue(EntityUtils.toString(classicHttpResponse.getEntity(), StandardCharsets.UTF_8), Book.class);
            assertNotNull(book);
            return null;
        });
        HttpClients.createDefault().execute(new HttpGet(baseUri + "/get/2"), classicHttpResponse -> {
            assertEquals(404, classicHttpResponse.getCode());
            return null;
        });
    }

    @Test
    void createBookInExistingCategory() throws IOException {
        requestToken();
        String json = "{\"title\": \"Tintenblut\",\"author\": \"Cornelia Funke\",\"publisher\": \"oetinger\",\"year\": 2005,\"categoryId\": 1}";
        HttpPost post = new HttpPost(baseUri + "/create");
        post.setHeader("Token", token);
        HttpEntity httpEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
        post.setEntity(httpEntity);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        httpClient.execute(post, classicHttpResponse -> null);
        Book book = httpClient.execute(post, classicHttpResponse -> {
            assertEquals(201, classicHttpResponse.getCode());
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(EntityUtils.toString(classicHttpResponse.getEntity()), Book.class);
        });
        assertEquals("Tintenblut", book.getTitle());
        assertEquals("Cornelia Funke", book.getAuthor());
        assertEquals("oetinger", book.getPublisher());
        assertEquals(2005, book.getYear());
        List<Book> bookList = httpClient.execute(new HttpGet(baseUri + "/get"), classicHttpResponse -> {
            assertEquals(200, classicHttpResponse.getCode());
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(EntityUtils.toString(classicHttpResponse.getEntity()), List.class);
        });
        assertEquals(2, bookList.size());
    }

    @Test
    void deleteBook() throws IOException {
        requestToken();
        HttpDelete delete = new HttpDelete(baseUri + "/delete/1");
        delete.setHeader("Token", token);
        HttpClients.createDefault().execute(delete, classicHttpResponse -> {
            assertEquals(204, classicHttpResponse.getCode());
            return null;
        });
    }

    @Test
    void updateBook() throws IOException {
        requestToken();
        String json = "{\"title\": \"Tintenblut\",\"author\": \"Cornelia Funke\",\"publisher\": \"oetinger\",\"year\": 2005,\"categoryId\": 1}";
        HttpPut put = new HttpPut(baseUri + "/update/1");
        HttpEntity httpEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
        put.setHeader("Token", token);
        put.setEntity(httpEntity);
        Book book = HttpClients.createDefault().execute(put, classicHttpResponse -> {
            assertEquals(202, classicHttpResponse.getCode());
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(EntityUtils.toString(classicHttpResponse.getEntity()), Book.class);
        });
        assertEquals("Tintenblut", book.getTitle());
        assertEquals(1, book.getId());
    }

    private void requestToken() throws IOException {
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
}