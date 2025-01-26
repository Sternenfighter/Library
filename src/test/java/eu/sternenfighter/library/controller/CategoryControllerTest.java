package eu.sternenfighter.library.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sternenfighter.library.dto.User;
import eu.sternenfighter.library.model.Book;
import eu.sternenfighter.library.model.Category;
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
@Sql(scripts = "classpath:sql/category.sql")
class CategoryControllerTest {

    @LocalServerPort
    private int port;

    private String baseUri;

    private String token;

    @BeforeEach
    void setUp() {
        baseUri = "http://localhost:" + port + "/category";
    }

    @Test
    void getCategory() throws IOException {
        HttpGet get = new HttpGet(baseUri + "/get/1");
        HttpClients.createDefault().execute(get, classicHttpResponse -> {
            assertEquals(200, classicHttpResponse.getCode());
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            Category category = mapper.readValue(EntityUtils.toString(classicHttpResponse.getEntity(), StandardCharsets.UTF_8), Category.class);
            assertNotNull(category);
            return null;
        });
        HttpClients.createDefault().execute(new HttpGet(baseUri + "/get/2"), classicHttpResponse -> {
            assertEquals(404, classicHttpResponse.getCode());
            return null;
        });
    }

    @Test
    void createCategory() throws IOException {
        requestToken();
        String json = "{\"name\": \"Horror\",\"description\": \"everything that scares\"}";
        HttpPost post = new HttpPost(baseUri + "/create");
        HttpEntity httpEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
        post.setHeader("Token", token);
        post.setEntity(httpEntity);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        httpClient.execute(post, classicHttpResponse -> null);
        Category category = httpClient.execute(post, classicHttpResponse -> {
            assertEquals(201, classicHttpResponse.getCode());
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(EntityUtils.toString(classicHttpResponse.getEntity(), StandardCharsets.UTF_8), Category.class);
        });
        assertEquals(2, category.getId());
        assertEquals("Horror", category.getName());
        assertEquals("everything that scares", category.getDescription());
        List<Category> categories = httpClient.execute(new HttpGet(baseUri + "/get"), classicHttpResponse -> {
            assertEquals(200, classicHttpResponse.getCode());
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(EntityUtils.toString(classicHttpResponse.getEntity()), List.class);
        });
        assertEquals(2, categories.size());
    }

    @Test
    void countBooksInCategory() throws IOException {
        HttpGet get = new HttpGet(baseUri + "/get/books/1");
        CloseableHttpClient httpClient = HttpClients.createDefault();
        long number = httpClient.execute(get, classicHttpResponse -> {
            assertEquals(200, classicHttpResponse.getCode());
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(EntityUtils.toString(classicHttpResponse.getEntity()), Long.class);
        });
        assertEquals(1, number);
        requestToken();
        String json = "{\"title\": \"Tintenblut\",\"author\": \"Cornelia Funke\",\"publisher\": \"oetinger\",\"year\": 2005,\"categoryId\": 1}";
        HttpPost post = new HttpPost("http://localhost:" + port + "/books/create");
        post.setHeader("Token", token);
        HttpEntity httpEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
        post.setEntity(httpEntity);
        httpClient.execute(post, classicHttpResponse -> {
            assertEquals(201, classicHttpResponse.getCode());
            return null;
        });
        number = httpClient.execute(get, classicHttpResponse -> {
            assertEquals(200, classicHttpResponse.getCode());
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(EntityUtils.toString(classicHttpResponse.getEntity()), Long.class);
        });
        assertEquals(2, number);
    }

    @Test
    void updateCategory() throws IOException {
        requestToken();
        String json = "{\"name\": \"Horror\",\"description\": \"everything that scares\"}";
        HttpPut put = new HttpPut(baseUri + "/update/1");
        HttpEntity httpEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
        put.setEntity(httpEntity);
        put.setHeader("Token", token);
        Category category = HttpClients.createDefault().execute(put, classicHttpResponse -> {
            assertEquals(202, classicHttpResponse.getCode());
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(EntityUtils.toString(classicHttpResponse.getEntity(), StandardCharsets.UTF_8), Category.class);
        });
        assertEquals(1, category.getId());
        assertEquals("Horror", category.getName());
        assertEquals("everything that scares", category.getDescription());
    }

    @Test
    void deleteCategory() throws IOException {
        requestToken();
        HttpDelete deleteCategory = new HttpDelete(baseUri + "/delete/1");
        deleteCategory.setHeader("Token", token);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        httpClient.execute(deleteCategory, classicHttpResponse -> {
            assertEquals(500, classicHttpResponse.getCode());
            return null;
        });
        HttpDelete deleteBook = new HttpDelete("http://localhost:" + port + "/books/delete/1");
        deleteBook.setHeader("Token", token);
        httpClient.execute(deleteBook, classicHttpResponse -> {
            assertEquals(204, classicHttpResponse.getCode());
            return null;
        });
        httpClient.execute(deleteCategory, classicHttpResponse -> {
            assertEquals(204, classicHttpResponse.getCode());
            return null;
        });
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