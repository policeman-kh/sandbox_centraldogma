package sandbox.centraldogma;

import static sandbox.centraldogma.Constants.PATH;
import static sandbox.centraldogma.Constants.PROJECT_NAME;
import static sandbox.centraldogma.Constants.REPOSITORY_NAME;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.linecorp.centraldogma.client.CentralDogma;
import com.linecorp.centraldogma.common.Change;
import com.linecorp.centraldogma.common.Revision;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@AllArgsConstructor
@RestController
public class ApiController {
    private final CentralDogma centralDogma;

    /**
     * Retrieves data on central-dogma.
     */
    @GetMapping
    public Mono<String> get() {
        return Mono.fromFuture(centralDogma.getFile(PROJECT_NAME, REPOSITORY_NAME, Revision.HEAD, PATH))
                   .map(entry -> entry.contentAsText());
    }

    /**
     * Create or update data on central-dogma.
     * @param str
     * @return
     */
    @PostMapping
    public Mono<String> push(@RequestParam String str) {
        return createProject()
                .then(createRepository())
                .then(pushData(str))
                .then(Mono.just("success"));
    }

    private Mono<Void> createProject() {
        return Mono.fromFuture(centralDogma.createProject(PROJECT_NAME))
                   .doOnError(e -> log.error("Failed to create a project.", e))
                   .onErrorResume(e -> Mono.empty());
    }

    private Mono<Void> createRepository() {
        return Mono.fromFuture(centralDogma.createRepository(PROJECT_NAME, REPOSITORY_NAME))
                   .doOnError(e -> log.error("Failed to create a repository.", e))
                   .onErrorResume(e -> Mono.empty());
    }

    private Mono<Void> pushData(String str) {
        String jsonStr = null;
        try {
            final TestData testData = new TestData();
            testData.setStr(str);
            jsonStr = new ObjectMapper().writeValueAsString(testData);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return Mono.fromFuture(centralDogma.push(PROJECT_NAME, REPOSITORY_NAME, Revision.HEAD, "push " + str,
                                                 Change.ofJsonUpsert(PATH, jsonStr)))
                   .doOnError(e -> log.error("Failed to push a data.", e))
                   .then();
    }

    @Data
    private static class TestData {
        private String str;
    }
}
