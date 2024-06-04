package models;

import java.util.*;

import com.mongodb.client.model.Filters;
import org.bson.BSON;
import org.bson.conversions.Bson;
import services.MongoService;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javax.inject.Inject;


public class Book {
    public String name;
    public Integer price;
    public String author;
    public String publisher;



    //static MongoDatabase database = mongoService.getDatabase();
    //static MongoCollection<Document> collection=database.getCollection("books");

    public Book(String name,Integer price,String author,String publisher){
        this.name=name;
        this.price=price;
        this.author=author;
        this.publisher=publisher;
    }


    public String getName(){
        return name;
    }

    public Integer getPrice(){
        return price;
    }
    public Book(){

    }

    /*public static MongoCollection<Document> allBooks(){
        return collection;
    }*/

    /*public static Document findByName(String name){
        MongoCollection<Document> col=collection;
        Bson filter = Filters.eq("name", name);

        // Find the document matching the filter
        Document doc = col.find(filter).first();
        if(doc!=null)
        {
            return doc;
        }
        else{
            return null;
        }
    }*/


    public static Book fromDocument(Document document) {
        String name = document.getString("name");
        Integer price = document.getInteger("price");
        String author = document.getString("author");
        String publisher=document.getString("publisher");
        return new Book(name, price, author,publisher);
    }

}
