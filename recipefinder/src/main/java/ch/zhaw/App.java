package ch.zhaw;

import java.util.Scanner;
import java.util.ArrayList;

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

import io.github.cdimascio.dotenv.Dotenv;

public class App {
    public static void main(String[] args) {
        // Disable logging
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        lc.getLogger("org.mongodb.driver").setLevel(Level.OFF);

        Dotenv dotenv = Dotenv.load();
        // Replace connection string with connection string retrieved from mongodb!!!
        String connectionString = dotenv.get("DB_URI");

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
                // Send a ping to confirm a successful connection
                MongoDatabase recipeDB = mongoClient.getDatabase("recipefinder");
                MongoCollection<Document> recipeCol = recipeDB.getCollection("recipes");
                System.out.println("Found " + recipeCol.countDocuments() + " recipes");

                // Prompt user for an ingredient
                Scanner keyScan = new Scanner(System.in);
                System.out.println("Enter an ingredient: ");
                String ingredient = keyScan.nextLine();

                FindIterable<Document> allRecipes = recipeCol.find(eq("ingredients.name", ingredient));
                System.out.println("Available recipes: ");
                for (Document d: allRecipes) {
                    System.out.println("- " + d.get("title"));
                }

                System.out.println("Enter the title of your preferred recipe: ");
                String recipeTitle = keyScan.nextLine();

                Document selectedRecipe = recipeCol.find(eq("title", recipeTitle)).first();
                System.out.println("Good choice! For " + selectedRecipe.get("title") + " you need:");

                ArrayList<Document> ingr = selectedRecipe.get("ingredients", ArrayList.class);
                for (Document d: ingr) {
                    System.out.println("- " + d.get("name"));
                }

                keyScan.close();
            } catch (MongoException e) {
                e.printStackTrace();
            }
        }
    }
}
