package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Book;
import org.bson.Document;
import org.bson.conversions.Bson;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import services.MongoService;
import services.WsService;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;
import static play.mvc.Results.*;

public class BooksController extends Controller {

    private final FormFactory formFactory;
    private final MongoService mongoService;
    private final WsService wsService;

    @Inject
    public BooksController(FormFactory formFactory, MongoService mongoService, WsService wsService) {
        this.formFactory = formFactory;
        this.mongoService = mongoService;
        this.wsService = wsService;
    }

    // Show all books
    public Result index() {
        List<JsonNode> jsonList = mongoService.findAllBooks();
        return ok(Json.toJson(jsonList));
    }

    // Create book
    public CompletionStage<Result> save() {
        Form<Book> bookForm = formFactory.form(Book.class).bindFromRequest();
        Book book = bookForm.get();

        // Save the book in the books database
        Document bookDoc = new Document("name", book.name)
                .append("price", book.price)
                .append("author", book.author)
                .append("publisher", book.publisher);
        mongoService.insertBook(bookDoc);

        // Check if the author is new and notify the authors service if necessary
        return isAuthorNew(book.author).thenCompose(isNewAuthor -> {
            if (isNewAuthor) {
                return wsService.notifyAuthorsService(book.author).thenApply(response -> {
                    if (response.getStatus() == 200) {
                        return redirect(routes.BooksController.index());
                    } else {
                        return internalServerError("Failed to update authors database");
                    }
                });
            } else {
                return CompletableFuture.completedFuture(redirect(routes.BooksController.index()));
            }
        });
    }

    //check if Author is new
    private CompletionStage<Boolean> isAuthorNew(String author) {
        long count = mongoService.countAuthorsByName(author);
        return CompletableFuture.completedFuture(count == 0);
    }

    // Delete book
    public Result delete(String name) {
        mongoService.deleteBookByName(name);
        return redirect(routes.BooksController.index());
    }

    // Edit book
    public Result edit(String name) {
        Document d = mongoService.findBookByName(name);
        if (d == null) {
            return notFound("Book Not Found");
        }
        Book book = Book.fromDocument(d);
        Form<Book> bookForm = formFactory.form(Book.class).fill(book);
        return ok(views.html.books.edit.render(bookForm));
    }

    // Update book
    public Result update(String name) {
        Book book = formFactory.form(Book.class).bindFromRequest().get();
        Bson updates = combine(
                set("name", book.name),
                set("price", book.price),
                set("author", book.author),
                set("publisher", book.publisher)
        );
        mongoService.updateBook(name, updates);
        return redirect(routes.BooksController.index());
    }


    // Show the details of a specific book and its corresponding author, publisher details
    public CompletionStage<Result> show(String name) {
        Document bookDoc = mongoService.findBookByName(name);

        if (bookDoc == null) {
            return CompletableFuture.completedFuture(notFound("Book Not Found"));
        }

        Book book = Book.fromDocument(bookDoc);
        String authorName = book.author;
        String publisherName = book.publisher;

        // Create JSON body for publisher request
        ObjectNode publisherRequestBody = Json.newObject();
        publisherRequestBody.put("name", publisherName);

        return wsService.fetchAuthorAndPublisherDetails(authorName, publisherRequestBody).thenApply(responseMap -> {
            JsonNode authorDetails = responseMap.get("authorDetails");
            JsonNode publisherDetails = responseMap.get("publisherDetails");

            JsonNode bookDetails = Json.toJson(book);
            if (authorDetails != null) {
                ((ObjectNode) bookDetails).set("authorDetails", authorDetails);
            } else {
                ((ObjectNode) bookDetails).putNull("authorDetails");
            }

            if (publisherDetails != null) {
                ((ObjectNode) bookDetails).set("publisherDetails", publisherDetails);
            } else {
                ((ObjectNode) bookDetails).putNull("publisherDetails");
            }

            return ok(bookDetails);
        }).exceptionally(ex -> {
            return internalServerError("An error occurred: " + ex.getMessage());
        });
    }


    // Get all books written by a particular author
    public Result getBooksByAuthor(String author) {
        List<JsonNode> jsonList = mongoService.findBooksByAuthor(author);
        return ok(Json.toJson(jsonList));
    }
}
