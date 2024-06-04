package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;
import play.api.Configuration;
import play.inject.ApplicationLifecycle;
import play.libs.Json;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static com.mongodb.client.model.Filters.eq;

@Singleton
public class MongoService {

    private final MongoClient mongoClient;
    private final MongoDatabase database;
    private final MongoCollection<Document> booksCollection;
    private final MongoCollection<Document> authorsCollection;
    private final MongoCollection<Document> publishersCollection;

    @Inject
    public MongoService(Configuration configuration, ApplicationLifecycle lifecycle) {
        String uri = "mongodb+srv://laharikommineni:Lahari%40p2002@cluster0.8zlltkz.mongodb.net/";
        mongoClient = MongoClients.create(uri);
        database = mongoClient.getDatabase("Books-Authors");
        booksCollection = database.getCollection("books");
        authorsCollection = database.getCollection("authors");
        publishersCollection = database.getCollection("publishers");

        // Registering a shutdown hook to close the MongoClient
        lifecycle.addStopHook(() -> {
            mongoClient.close();
            return CompletableFuture.completedFuture(null);
        });
    }

    //To get all books
    public List<JsonNode> findAllBooks() {
        List<Document> documentList = booksCollection.find().into(new ArrayList<>());
        return convertDocumentsToJson(documentList);
    }


    //to get specific book
    public Document findBookByName(String name) {
        return booksCollection.find(eq("name", name)).first();
    }


    //to insert book
    public void insertBook(Document bookDoc) {
        booksCollection.insertOne(bookDoc);
    }


    //to delete book
    public void deleteBookByName(String name) {
        booksCollection.deleteOne(new Document("name", name));
    }


    //to update book
    public void updateBook(String name, Bson updates) {
        booksCollection.updateOne(eq("name", name), updates);
    }


    //to count Authors if it is existing or not
    public long countAuthorsByName(String author) {
        Bson filter = eq("authorName", author);
        return authorsCollection.countDocuments(filter);
    }


    //to find books written by specific author
    public List<JsonNode> findBooksByAuthor(String author) {
        List<Document> documentList = booksCollection.find(eq("author", author)).into(new ArrayList<>());
        return convertDocumentsToJson(documentList);
    }


    //to convert document into json
    public JsonNode convertDocumentToJson(Document document) {
        return Json.parse(document.toJson());
    }


    public List<JsonNode> convertDocumentsToJson(List<Document> documents) {
        List<JsonNode> jsonList = new ArrayList<>();
        for (Document doc : documents) {
            JsonNode jsonNode = Json.parse(doc.toJson());
            jsonList.add(jsonNode);
        }
        return jsonList;
    }


    //to get details of publisher
    public JsonNode getPublisher(String name) {
        Document doc = publishersCollection.find(eq("name", name)).first();
        if (doc == null) {
            return null;
        } else {
            return Json.parse(doc.toJson());
        }
    }
}
