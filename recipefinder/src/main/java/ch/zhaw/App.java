package ch.zhaw;

import java.util.Scanner;
import java.util.List;

import static com.mongodb.client.model.Filters.*;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unchecked")
public class App {
    public static void main(String[] args) {
        // Disable logging
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        lc.getLogger("org.mongodb.driver").setLevel(Level.OFF);

        String connectionString = "mongodb+srv://admin:sG4NdHHRsfWRutpw@cluster0.zrkwn.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";

        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .serverApi(serverApi)
                .build();

        // Create a new client and connect to the server
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            try {
                MongoDatabase recipeDB = mongoClient.getDatabase("recipefinder");
                MongoCollection<Document> recipeCol = recipeDB.getCollection("recipes");
                System.out.println("Found " + recipeCol.countDocuments() + " recipes");

                // prompt user for an ingredient
                Scanner keyScan = new Scanner(System.in);
                System.out.print("Enter an ingredient: ");
                String ingredient = keyScan.nextLine();

                // list available recipes
                FindIterable<Document> allRecipes = recipeCol.find(eq("ingredients.name", ingredient));
                System.out.println("Available recipes: ");
                for (Document d : allRecipes) {
                    System.out.println("- " + d.get("title"));
                }

                // prompt user for recipe
                System.out.print("Enter the title of your preferred recipe: ");
                String recipeName = keyScan.nextLine();

                // print recipe details
                Document selectedRecipe = recipeCol.find(eq("title", recipeName)).first();
                if (selectedRecipe != null) {
                    System.out.println("Good choice! For " + recipeName + " you need:");
                    List<Document> ingr = selectedRecipe.get("ingredients", List.class);
                    for (Document i : ingr) {
                        System.out.println("- " + i.get("quantity") + i.get("unit")  +" of " + i.get("name"));
                    }
                } else {
                    System.out.println("Recipe not found. Try again.");
                }

                keyScan.close();
            } catch (MongoException e) {
                e.printStackTrace();
            }
        }
    }
}
