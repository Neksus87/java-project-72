package hexlet.code.controller;

import static io.javalin.rendering.template.TemplateUtil.model;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import hexlet.code.dto.urls.UrlPage;
import hexlet.code.dto.urls.UrlsPage;
import hexlet.code.model.Url;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.NamedRoutes;
import hexlet.code.util.UrlUtils;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;

public class UrlsController {
    public static void index(Context ctx) throws SQLException {
        // Получаем все URL-адреса из базы данных
        var urls = UrlRepository.getEntities();

        // Получаем мапу с последними проверками для каждого URL
        var latestUrlChecks = UrlCheckRepository.getLatestEntities();

        // Создаем страницу с данными
        var page = new UrlsPage(new ArrayList<>(urls.values()), latestUrlChecks);
        page.setFlash(ctx.consumeSessionAttribute("flash"));
        page.setFlashType(ctx.consumeSessionAttribute("flashType"));

        // Передаем данные в шаблон
        ctx.render("urls/index.jte", model("page", page));
    }

    public static void show(Context ctx) throws SQLException {
        var id = ctx.pathParamAsClass("id", Long.class).get();
        var url = UrlRepository.find(id).orElseThrow(() ->
                new NotFoundResponse("Entity with id = " + id + " not found"));
        var urlChecks = UrlCheckRepository.getEntitiesByUrlId(id);
        var page = new UrlPage(url, urlChecks);
        page.setFlash(ctx.consumeSessionAttribute("flash"));
        page.setFlashType(ctx.consumeSessionAttribute("flashType"));
        ctx.render("urls/show.jte", model("page", page));
    }

    public static void create(Context ctx) throws SQLException {
        try {
            var name = UrlUtils.normalizeUrl(ctx.formParam("url"));
            var createdAt = new Timestamp(System.currentTimeMillis());

            if (UrlRepository.existsByName(name)) {
                ctx.sessionAttribute("flash", "Страница уже существует");
                ctx.sessionAttribute("flashType", "danger");
                ctx.redirect(NamedRoutes.mainPath());
                return;
            }

            // Создаем объект Url с одним параметром
            var url = new Url(name);
            url.setCreatedAt(createdAt);

            UrlRepository.save(url);
            ctx.sessionAttribute("flash", "Страница успешно добавлена");
            ctx.sessionAttribute("flashType", "success");
            ctx.redirect(NamedRoutes.urlsPath());
        } catch (IllegalArgumentException | MalformedURLException | URISyntaxException e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flashType", "danger");
            ctx.redirect(NamedRoutes.mainPath());
        }
    }
}
