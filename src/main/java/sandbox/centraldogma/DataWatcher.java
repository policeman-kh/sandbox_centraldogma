package sandbox.centraldogma;

import static sandbox.centraldogma.Constants.PATH;
import static sandbox.centraldogma.Constants.PROJECT_NAME;
import static sandbox.centraldogma.Constants.REPOSITORY_NAME;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

import com.linecorp.centraldogma.client.CentralDogma;
import com.linecorp.centraldogma.client.Watcher;
import com.linecorp.centraldogma.common.Query;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DataWatcher {
    private final Watcher<JsonNode> watcher;

    public DataWatcher(CentralDogma centralDogma) {
        watcher = centralDogma.fileWatcher(PROJECT_NAME, REPOSITORY_NAME, Query.ofJson(PATH));
        // 
        watcher.watch(jsonNode -> log.info("Updated {}", jsonNode.asText()));
    }
}
