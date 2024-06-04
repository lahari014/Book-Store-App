package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.Configuration;
import play.libs.Json;
import play.libs.ws.*;
import play.mvc.Result;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;
import static play.mvc.Results.notFound;

public class WsService {

    private final WSClient wsClient;
    private final Configuration conf;
    private final MongoService mongoService;

    @Inject
    public WsService(WSClient wsClient, Configuration conf,MongoService mongoService) {
        this.wsClient = wsClient;
        this.conf = conf;
        this.mongoService = mongoService;
    }


    public CompletableFuture<Map<String, JsonNode>> fetchAuthorAndPublisherDetails(String authorName, ObjectNode publisherRequestBody) {
        String authorsServiceUrl = conf.getString("authorsServiceUrl") + authorName;
        String publishersServiceUrl = conf.getString("publishersServiceUrl");

        Map<String, JsonNode> responseMap = new HashMap<>();

        CompletableFuture<Void> authorFuture = wsClient.url(authorsServiceUrl).get().thenAccept(response -> {
            if (response.getStatus() == 200) {
                responseMap.put("authorDetails", response.asJson());
            }
        }).toCompletableFuture();

        CompletableFuture<Void> publisherFuture = wsClient.url(publishersServiceUrl).post(publisherRequestBody).thenAccept(response -> {
            if (response.getStatus() == 200) {
                responseMap.put("publisherDetails", response.asJson());
            }
        }).toCompletableFuture();

        return CompletableFuture.allOf(authorFuture, publisherFuture).thenApply(v -> responseMap);
    }



    public CompletionStage<WSResponse> notifyAuthorsService(String author) {
        String authorsServiceUrl = conf.getString("authorsServiceUrl") + "create";
        JsonNode json = Json.newObject().put("authorName", author);
        return wsClient.url(authorsServiceUrl).post(json);
    }
}
