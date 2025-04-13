package hexlet.code;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;

import hexlet.code.repository.UrlCheckRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import hexlet.code.model.Url;
import hexlet.code.repository.UrlRepository;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;

public class AppTest {
    Javalin app;
    static MockWebServer mockWebServer; // Статический сервер
    Url url;

    @BeforeAll
    public static void setUpServer() throws IOException {
        // Создаем и запускаем MockWebServer один раз для всех тестов
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @BeforeEach
    public void setUp() throws SQLException {
        // Инициализируем приложение и URL перед каждым тестом
        app = App.getApp();
        url = new Url("https://www.example.com:8080"); // Дата больше не передается явно
    }

    @Test
    public void testMainPage() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/");
            assertThat(response.code()).isEqualTo(200);
        });
    }

    @Test
    public void testUrlsPage() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/urls");
            assertThat(response.code()).isEqualTo(200);
        });
    }

    @Test
    public void testUrlPage() throws SQLException {
        UrlRepository.save(url);

        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/urls/" + url.getId());
            assertThat(response.code()).isEqualTo(200);
        });
    }

    @Test
    public void testNonExistingUrlPage() throws SQLException {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/urls/999999");
            assertThat(response.code()).isEqualTo(404);
        });
    }

    @Test
    public void testCreateUrl() throws SQLException {
        JavalinTest.test(app, (server, client) -> {
            var requestBody = "url=" + url.getName() + "?foo=bar";
            var response = client.post("/urls", requestBody);
            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body().string()).contains(url.getName());
        });

        assertThat(UrlRepository.existsByName(url.getName())).isTrue();
    }

    @Test
    public void testCreateDuplicateUrl() throws SQLException {
        UrlRepository.save(url);

        JavalinTest.test(app, (server, client) -> {
            var requestBody = "url=" + url.getName();
            var response = client.post("/urls", requestBody);
            assertThat(response.code()).isEqualTo(200);
        });

        assertThat(UrlRepository.getEntities()).hasSize(1);
    }

    @Test
    public void testCreateInvalidUrl() throws SQLException {
        JavalinTest.test(app, (server, client) -> {
            var requestBody = "url=invalidUrl";
            var response = client.post("/urls", requestBody);
            assertThat(response.code()).isEqualTo(200);
        });

        assertThat(UrlRepository.getEntities()).hasSize(0);
    }

    @Test
    public void testCreateUrlCheck() throws SQLException, IOException {
        var filepath = Paths.get("src", "test", "resources", "test.html");
        var html = Files.readString(filepath);

        mockWebServer.enqueue(new MockResponse().setBody(html));
        url.setName(mockWebServer.url("/").toString());
        UrlRepository.save(url);

        JavalinTest.test(app, (server, client) -> {
            var response = client.post("/urls/" + url.getId() + "/checks");
            assertThat(response.code()).isEqualTo(200);

            var responseBody = response.body().string();
            assertThat(responseBody).contains("200");
            assertThat(responseBody).contains("Test HTML Page");
            assertThat(responseBody).contains("Welcome to Test HTML Page");
            assertThat(responseBody).contains("This is a test HTML page.");
        });

        assertThat(UrlCheckRepository.getEntitiesByUrlId(url.getId())).hasSize(1);
    }

    @Test
    public void testCreatedAtIsSetAutomatically() throws SQLException {
        Url url = new Url("https://www.example.com");
        UrlRepository.save(url);

        // Проверяем, что дата была установлена
        assertThat(url.getCreatedAt()).isNotNull();
    }

    @Test
    public void testFieldsInDatabase() throws SQLException {
        // Создаем новый URL
        String testName = "https://test-url.com";
        Url url = new Url(testName);
        UrlRepository.save(url);

        // Извлекаем сохраненную сущность из базы
        var savedUrlOptional = UrlRepository.find(url.getId());
        assertThat(savedUrlOptional).isPresent();

        var savedUrl = savedUrlOptional.get();

        // Проверяем соответствие полей
        assertThat(savedUrl.getName()).isEqualTo(testName); // Проверка имени
        assertThat(savedUrl.getCreatedAt()).isNotNull();   // Проверка даты создания
    }

    @AfterAll
    public static void tearDownServer() throws IOException {
        // Останавливаем MockWebServer после завершения всех тестов
        mockWebServer.shutdown();
    }
}
