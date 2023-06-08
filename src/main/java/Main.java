import org.apache.jena.ontology.Individual;
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
                String id = extractReference(docsSubstring);
                String label = id + " " + name;
                String docType = extractDocType(id);

                documentInstance = new RegulatoryDocument(model, id, docType, label, name);
            } catch (Exception e) {
                System.out.println("Could not parse one of the title page elements: ");
                System.out.println(e);
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

    private static void addDataToDocumentFromSite(SiteReader siteReader, RegulatoryDocument doc) {
        String docLink = siteReader.findDocLink(doc.getName(), doc.getId());
        if (docLink != null) {
            doc.setAvailableOnline(true);
            doc.setLink(docLink);
        }
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

    public static List<String> getAuthorsFromForeword(String text) {
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

    public static List<String> getPeopleFromForeword(String text) {
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

    public static String parseAnnotation(String text) {
        String pattern = "Ан.*\\n(.*)\\n";

        // Create a Pattern object
        Pattern regex = Pattern.compile(pattern);

        // Create a Matcher object
        Matcher matcher = regex.matcher(text);

        // Check if a match is found
        if (matcher.find()) {
            // Extract the desired text from the first capturing group

            return matcher.group(1);

        } else {
            System.out.println("No match found.");
            return "";
        }
    }


    public static List<String> findMatchingSubstrings(String input, String phrase1, String phrase2) {
        List<String> matchingSubstrings = new ArrayList<>();

        String regex = "(?i).*" + Pattern.quote(phrase1) + ".*" + Pattern.quote(phrase2) + ".*";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            matchingSubstrings.add(matcher.group());
        }

        return matchingSubstrings;
    }

    private static List<String> extractDocIdentifierForProperty(String match) {
        String[] parts = match.split("\\s", 3);
        return extractNumbers(parts[2]);
    }

    private static List<String> extractNumbers(String line) {
        List<String> numbers = new ArrayList<>();

        Pattern pattern = Pattern.compile("\\b\\d+(?:\\.\\d+)?\\b");

        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
            String number = matcher.group();
            numbers.add(number);
        }

        return numbers;
    }

    public static List<DataPropertyDocRuleCreator> findNewDataProperties(
            String docText, List<RegulatoryDocument> referenceDocumentInstances, RegulatoryDocument mainDocument
    ) {

        List<String> matchingLines = findMatchingSubstrings(docText, "Вд", "Згідно з");
        List<DataPropertyDocRuleCreator> dataPropertyRules = new ArrayList<>();

        for (String line : matchingLines) {

            String[] parts = line.replace("Вд1", "").split("\t|\t");
            String property = parts[0];
            String value = parts[2];
            List<String> docIds = extractDocIdentifierForProperty(parts[4]);
            List<Individual> docIndividuals = new ArrayList<>();

            for (String docId : docIds) {
                for (RegulatoryDocument doc : referenceDocumentInstances) {
                    if (docId.length() >= 4 && doc.getId().contains(docId) && doc.individualInstance != null )
                    {
                        if (!docIndividuals.contains(doc.individualInstance))
                            docIndividuals.add(doc.individualInstance);

                    }
                }
            }
            docIndividuals.add(mainDocument.individualInstance);

            for (Individual doc : docIndividuals) {
                System.out.println("Adding `" + property + "` to individual " + doc);
                dataPropertyRules.add(new DataPropertyDocRuleCreator(doc, property, value));
            }


        }
        return dataPropertyRules;
    }

    public static void clean(
            OntModel model,
            List<Individual> deputies,
            List<Individual> authorities,
            List<RegulatoryDocument> referenceDocumentInstances,
            RegulatoryDocument mainDocument
    ) {
        for (Individual deputy : deputies) {
            OntologyHelper.removeIndividual(model, deputy);
        }

        for (Individual authority : authorities) {
            OntologyHelper.removeIndividual(model, authority);
        }
        for (RegulatoryDocument doc : referenceDocumentInstances) {
            OntologyHelper.removeIndividual(model, doc.individualInstance);
        }
        OntologyHelper.removeIndividual(model, mainDocument.individualInstance);
    }


    public static void main(String[] args) {
        System.out.println("Creating and loading ontology...");
        OntModel model = OntologyHelper.createOntology();

        OntologyHelper.loadOntologyFromFile(model, ontologyFile);

        System.out.println("Parsing docx file in string...");
        String docText = getRegulatoryDocumentTextFromDocx();

        System.out.println("Getting data about document references...");
        List<RegulatoryDocument> referenceDocumentInstances = createRegulatoryDocumentsFromReferences(model, docText);

        System.out.println("Adding references to ontology...");
        for (RegulatoryDocument doc : referenceDocumentInstances) {
            doc.setIndustry("Харчові продукти");
            doc.addToOntology();
        }

        System.out.println("Getting data about document itself...");
        RegulatoryDocument mainDocument = createRegulatoryDocumentsFromTitlePage(model, docText);

        System.out.println("Getting data about authors...");
        List<String> authors = getAuthorsFromForeword(docText);
        List<String> people = getPeopleFromForeword(docText);
        assert authors != null;
        assert people != null;

        List<Individual> authorities = new ArrayList<>();
        List<Individual> deputies = new ArrayList<>();

        System.out.println("Creating authors in ontology...");
        for (String author : authors) {
            authorities.add(OntologyHelper.createIndividual(model, OntologyHelper.getClassByName(model, "Authority"), author));
        }
        for (String person : people) {
            deputies.add(OntologyHelper.createIndividual(model, OntologyHelper.getClassByName(model, "Deputee"), person));
        }

        System.out.println("Making up a `hasMember` relation between authors and people individuals...");
        for (Individual authority : authorities) {
            for (Individual deputy : deputies) {
                OntologyHelper.addObjectPropertyValue(model, authority, "hasMember", deputy);
            }
        }

        System.out.println("Getting additional data about document from file...");
        mainDocument.setAnnotation(parseAnnotation(docText));
        mainDocument.setIndustry("Харчові продукти");
        mainDocument.setAuthors(authorities);

        System.out.println("Getting additional data about document from online source...");
        mainDocument.getDataFromOnline();

        System.out.println("Adding main document to ontology...");
        mainDocument.addToOntology();

        System.out.println("Adding data properties from tables...");
        List<DataPropertyDocRuleCreator> dataProperties = findNewDataProperties(
                docText, referenceDocumentInstances, mainDocument
        );

        for (DataPropertyDocRuleCreator property : dataProperties) {
            property.getOrCreateProperty(model);
            property.addPropertyToIndividual(model);
        }

        System.out.println("Saving changes...");
        OntologyHelper.save(model);

        OntologyHelper.printIndividualInfo(model, "ДСТУ_6003:2008");
//        clean(model, deputies, authorities, referenceDocumentInstances, mainDocument);
    }
}
