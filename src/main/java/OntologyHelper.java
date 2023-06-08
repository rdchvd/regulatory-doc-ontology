import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;


public class OntologyHelper {
    private static final String ontologyURI = Configuration.ONTOLOGY_URI;


    public static void checkIfOntologyLoaded(OntModel model) {
        if (!model.isEmpty()) {
            System.out.println("Ontology successfully loaded!");
        } else {
            System.out.println("Something wrong happened while loading ontology.");
        }
    }

    public static OntModel createOntology() {
        return ModelFactory.createOntologyModel();
    }

    public static void loadOntologyFromFile(OntModel model, String ontologyFile) {
        model.read(ontologyFile);
        checkIfOntologyLoaded(model);
    }

    public static OntClass getClassByName(OntModel model, String className) {
        return model.getOntClass(ontologyURI + className);
    }

    public static Individual createIndividual(OntModel model, OntClass classInstance, String individualName) {
        return model.createIndividual(ontologyURI + individualName.replace(" ", "_"), classInstance);
    }


    public static void addObjectPropertyValue(OntModel model, Individual object, String propertyName, Individual subject) {
        ObjectProperty property = model.getObjectProperty(Configuration.ONTOLOGY_URI + propertyName);
        model.add(object, property, subject);

    }

    public static void addDataPropertyValue(OntModel model, Individual object, String propertyName, Literal value) {
        DatatypeProperty property = model.getDatatypeProperty(Configuration.ONTOLOGY_URI + propertyName);
        if (object != null && property != null)
            model.add(object, property, value);

    }

    public static void addDataPropertyValue(OntModel model, Individual object, DatatypeProperty property, Literal value) {
        if (object != null && property != null)
            model.add(object, property, value);

    }


    public static void removeIndividual(OntModel model, Individual individual) {
        model.removeAll(individual, null, null);
    }

    public static Individual getOrCreateIndividual(OntModel model, OntClass classInstance, String individualName) {
        Individual individual = model.getIndividual(Configuration.ONTOLOGY_URI + individualName);
        if (individual == null)
            individual = createIndividual(model, classInstance, individualName);
        return individual;
    }

    public static void save(OntModel model) {
        try {
            // Create the file object
            File file = new File(Configuration.OUTPUT_FILE_PATH);

            // Create the directory if it doesn't exist
            File directory = file.getParentFile();
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Create the output stream
            OutputStream outputStream = new FileOutputStream(file);

            // Write the ontology to the output stream
            model.write(outputStream, "RDF/XML-ABBREV");

            // Close the output stream
            outputStream.close();

            System.out.println("Ontology saved successfully.");
        } catch (Exception e) {
            System.out.println("Error saving the ontology: " + e.getMessage());
        }
    }

    public static void printIndividualInfo(OntModel model, String individualName) {

        Resource individual = model.getResource(Configuration.ONTOLOGY_URI + individualName);

        StmtIterator typeIterator = individual.listProperties(RDF.type);
        if (typeIterator.hasNext()) {
            Statement typeStatement = typeIterator.next();
            System.out.println("Type: " + typeStatement.getObject().toString());
        }

        StmtIterator propertyIterator = individual.listProperties();
        while (propertyIterator.hasNext()) {
            Statement propertyStatement = propertyIterator.next();
            Property property = propertyStatement.getPredicate();
            RDFNode value = propertyStatement.getObject();
            System.out.println("Property: " + property.toString());
            System.out.println("Value: " + value.toString());
        }
    }

}
