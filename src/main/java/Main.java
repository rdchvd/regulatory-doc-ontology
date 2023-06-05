import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static final String ontologyFile = Configuration.ONTOLOGY_FILE_PATH;
    private static final String docFile = Configuration.DOC_FILE_PATH;


    public static String getRegulatoryDocumentTextFromDocx() {
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

    public static RegulatoryDocument createRegulatoryDocumentsFromTitlePage(OntModel model, String text) {
        String startMarker = "НАЦІОНАЛЬНИЙ СТАНДАРТ УКРАЇНИ\n";
        String endMarker = "\nПЕРЕДМОВА";

        RegulatoryDocument documentInstance = null;

        // Find the start and end positions of the substring
        int startIndex = text.indexOf(startMarker) + startMarker.length();
        int endIndex = text.indexOf(endMarker);

        if (endIndex != -1 && startIndex < endIndex) {
            try {
                String docsSubstring = text.substring(startIndex, endIndex);
                String name = extractTitle(docsSubstring);
                String reference = extractReference(docsSubstring);
                String label = name + ',' + reference;
                String docType = extractDocType(reference);
                documentInstance = new RegulatoryDocument(model, "", docType, label, name);
            } catch (Exception e) {
                System.out.println("Could not parse one of the title page elements: ");
                System.out.println(e.toString());
            }

        } else {
            System.out.println("Substring markers not found or in incorrect order.");
        }

        return documentInstance;
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

    private static String extractTitle(String match) {
        String endMarker = "\nЗагальні технічні умови";

        RegulatoryDocument documentInstance = null;

        // Find the start and end positions of the substring
        int endIndex = match.indexOf(endMarker);

        return match.substring(0, endIndex);
    }

    private static String extractReference(String match) throws Exception {
        String startMarker = "Загальні технічні умови\n";
        String endMarker = "\nВидання офіційне";

        RegulatoryDocument documentInstance = null;

        // Find the start and end positions of the substring
        int startIndex = match.indexOf(startMarker) + startMarker.length();
        int endIndex = match.indexOf(endMarker);

        if (endIndex == -1 || startIndex >= endIndex) {
            throw new Exception("Could not parse the title");
        }

        return match.substring(startIndex, endIndex);
    }

    private static String extractDocType(String match) {
        String[] parts = match.split("\\s", 2);
        return parts[0];
    }

    public static List<String> getAuthorsFromForeword(OntModel model, String text) {
        String startMarker = "РОЗРОБЛЕНО: ";
        String endMarker = "\nРОЗРОБНИКИ: ";

        // Find the start and end positions of the substring
        int startIndex = text.indexOf(startMarker) + startMarker.length();
        int endIndex = text.indexOf(endMarker);

        if (endIndex != -1 && startIndex < endIndex) {
            return Arrays.asList(text.substring(startIndex, endIndex).split(", "));
        }

        System.out.println("Substring markers not found or in incorrect order.");

        return null;
    }

    public static List<String> getPeopleFromForeword(OntModel model, String text) {
        String startMarker = "РОЗРОБНИКИ: ";
        String endMarker = "\nПРИЙНЯТО ТА НАДАНО ЧИННОСТІ: ";

        // Find the start and end positions of the substring
        int startIndex = text.indexOf(startMarker) + startMarker.length();
        int endIndex = text.indexOf(endMarker);

        List<String> people = new ArrayList<>();
        if (endIndex != -1 && startIndex < endIndex) {
            for (String personStr : text.substring(startIndex, endIndex).split("; ")) {
                people.add(personStr.split(", ")[0]);
            }
            return people;
        } else {
            System.out.println("Substring markers not found or in incorrect order.");
        }

        return null;
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

        RegulatoryDocument mainDocument = createRegulatoryDocumentsFromTitlePage(model, docText);
        mainDocument.print();

        List<String> authors = getAuthorsFromForeword(model, docText);
        List<String> people = getPeopleFromForeword(model, docText);
        assert authors != null;
        assert people != null;

        List<Individual> authorities = new ArrayList<>();
        List<Individual> deputees = new ArrayList<>();

        for (String author : authors) {
            authorities.add(OntologyHelper.createIndividual(model, OntologyHelper.getClassByName(model, "Authority"), author));
        }
        for (String person : people) {
            deputees.add(OntologyHelper.createIndividual(model, OntologyHelper.getClassByName(model, "Deputee"), person));
        }

        Individual testAuthority = authorities.get(0);
        Individual testDeputee = deputees.get(0);

        ObjectProperty hasMemberProperty = model.getObjectProperty(Configuration.ONTOLOGY_URI + "hasMember");

        model.add(testAuthority, hasMemberProperty, testDeputee);

//        SiteReader siteReader = new SiteReader();
//        siteReader.read(Configuration.DOC_ONLINE_DB_SITE);


    }
}
