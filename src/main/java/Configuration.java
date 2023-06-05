import io.github.cdimascio.dotenv.Dotenv;

public class Configuration {
    public static Dotenv dotenv = Dotenv.load();

    public static String CHROME_DRIVER_PATH = dotenv.get("CHROME_DRIVER_PATH");
    public static String ONTOLOGY_FILE_PATH = dotenv.get("ONTOLOGY_FILE_PATH");
    public static String ONTOLOGY_URI = dotenv.get("ONTOLOGY_URI");
    public static String DOC_FILE_PATH = dotenv.get("DOC_FILE_PATH");

    public static String DOC_ONLINE_DB_SITE = "http://online.budstandart.com/ua/";

}