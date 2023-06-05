import org.apache.jena.ontology.OntModel;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static final String ontologyFile = Configuration.ONTOLOGY_FILE_PATH;
    private static final String docFile = Configuration.DOC_FILE_PATH;


    public static String getRegulatoryDocumentTextFromDocx(){
        DocxReader docxReader = new DocxReader();
        return docxReader.read(docFile);
    }

    public static List<RegulatoryDocument> createRegulatoryDocumentsFromReferences(OntModel model, String text) {
        // Define the start and end markers for the substring
        String startMarker = "НОРМАТИВНІ ПОСИЛАННЯ";
        String endMarker = "ТЕРМІНИ ТА ВИЗНАЧЕННЯ ПОНЯТЬ";

        List<RegulatoryDocument> documentInstances = new ArrayList<>();

        // Find the start and end positions of the substring
        int startIndex = text.indexOf(startMarker);
        int endIndex = text.indexOf(endMarker);

        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            String docsSubstring = text.substring(startIndex, endIndex);

            String regex = "\\p{Lu}{2,} \\d+[\\p{Punct}\\p{Space}].+?(?=\\n\\p{Lu}{2,} \\d+|$)";


            Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(docsSubstring);

            // Extract identifiers and references
            while (matcher.find()) {
                String match = matcher.group();
                RegulatoryDocument document = new RegulatoryDocument(
                        model, extractIdentifier(match), extractType(match), match, extractName(match)
                );
                documentInstances.add(document);

            }
        } else {
            System.out.println("Substring markers not found or in incorrect order.");
        }
        return documentInstances;
    }


    private static String extractIdentifier(String match) {
        String[] parts = match.split("\\s", 3);
        return parts[0] + " " + parts[1];
    }

    private static String extractName(String match) {
        String[] parts = match.split("\\s", 3);
        return parts[2];
    }

    private static String extractType(String match) {
        String[] parts = match.split("\\s", 2);
        return parts[0];
    }

    public static void main(String[] args) {
//        OntologyHelper ontology = new OntologyHelper();
        // create ontology
        OntModel model = OntologyHelper.createOntology();

        // read existing to it
        OntologyHelper.loadOntologyFromFile(model, ontologyFile);

        String docText = getRegulatoryDocumentTextFromDocx();

        List<RegulatoryDocument> documentInstances = createRegulatoryDocumentsFromReferences(model, docText);
        for (RegulatoryDocument doc: documentInstances) {
            doc.print();
            System.out.println(doc.classInOntology);
            System.out.println();
        }
        SiteReader siteReader = new SiteReader();
        siteReader.read(Configuration.DOC_ONLINE_DB_SITE);



    }
}
