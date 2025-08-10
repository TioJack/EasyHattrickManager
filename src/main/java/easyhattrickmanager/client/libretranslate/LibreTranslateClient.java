package easyhattrickmanager.client.libretranslate;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import easyhattrickmanager.client.libretranslate.model.TranslateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
    name = "${libre-translate-feign-client.name}",
    url = "${libre-translate-feign-client.url}"
)
public interface LibreTranslateClient {

    @PostMapping(path = "/translate",
        produces = APPLICATION_JSON_VALUE)
    TranslateResponse translate(
        @RequestParam("q") String query,
        @RequestParam("source") String sourceLanguage,
        @RequestParam("target") String targetLanguage,
        @RequestParam("format") String format,
        @RequestParam("alternatives") int alternatives
    );

}
