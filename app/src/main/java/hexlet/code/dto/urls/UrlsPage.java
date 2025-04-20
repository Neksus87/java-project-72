package hexlet.code.dto.urls;

import hexlet.code.dto.BasePage;
import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Getter
public class UrlsPage extends BasePage {
    private final List<Url> urls;
    private final Map<Long, UrlCheck> latestChecksByUrlId;

    public UrlsPage(List<Url> urls, List<UrlCheck> latestChecks) {
        this.urls = urls;
        this.latestChecksByUrlId = new HashMap<>();
        for (var check : latestChecks) {
            this.latestChecksByUrlId.put(check.getUrlId(), check);
        }
    }
}
