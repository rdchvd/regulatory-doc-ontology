import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.ArrayList;
import java.util.List;

public class SiteReader {
    private static final String driverPath = Configuration.CHROME_DRIVER_PATH;
    private static WebDriver driver;

    public SiteReader(){

        System.setProperty("webdriver.chrome.driver", driverPath);

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");

        driver = new ChromeDriver(options);

    }

    public void makeSearch(String searchTerm){
        WebElement searchInput = driver.findElement(By.id("entertop"));

        // Enter your search term
        searchInput.sendKeys(searchTerm);

        // Find the "Знайти документ" button and click it
        WebElement searchButton = driver.findElement(By.id("submit_search"));
        searchButton.click();

    }
    public List<String> searchThroughDocs(String identifier){
        List<String> docLinks = new ArrayList<>();


        // Find all <a> elements containing the specified text
        String xpathExpression = String.format("//a[contains(text(), '%s')]", identifier);
        List<WebElement> links = driver.findElements(By.xpath(xpathExpression));

        // Loop through the found elements and print their href attribute values
        for (WebElement link : links) {
            docLinks.add(link.getAttribute("href"));
        }
        return docLinks;

    }

    public String findDocLink(String docName, String identifier){
        makeSearch(docName);

        List<String> docLinks = searchThroughDocs(docName);

        if (docLinks.size() == 0 && identifier != null){
            docLinks = searchThroughDocs(identifier);
        }

        return docLinks.isEmpty() ? null : docLinks.get(0);
    }

    public void read(String site) {
        driver.get(site);

    }


    public void close() {
        driver.quit();
    }

}
