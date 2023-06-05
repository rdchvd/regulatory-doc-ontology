import org.apache.jena.ontology.*;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;

import java.util.ArrayList;
import java.util.List;


public class OntologyHelper {
    private static final String ontologyURI = Configuration.ONTOLOGY_URI;


    public static void printAllClassesURIs(OntModel model) {
        for (OntClass ontClass : model.listClasses().toList()) {
            if (ontClass.getURI() != null)
                System.out.println(ontClass.getURI());
        }

    }

    public static void printAllIndividualURIs(OntClass ontClass) {

        for (OntResource individual : ontClass.listInstances().toList()) {
            if (individual.getURI() != null)
                System.out.println(individual.getURI());
        }

    }

    public static void checkIfOntologyLoaded(OntModel model) {
        if (!model.isEmpty()) {
            System.out.println("Онтологія завантажена успішно!");
        } else {
            System.out.println("Не вдалося завантажити онтологію.");
        }
    }

    public static OntModel createOntology() {
        return ModelFactory.createOntologyModel();
    }

    public static void loadOntologyFromFile(OntModel model, String ontologyFile) {
        model.read(ontologyFile);
        checkIfOntologyLoaded(model);
    }

    public static OntClass createClass(OntModel model, String className) {
        return model.createClass(ontologyURI + className);
    }

    public static void removeClass(OntModel model, OntClass classInstance) {
        model.removeAll(classInstance, null, null);
    }

    public static void createAndRemoveClass(OntModel model, String className) {
        // create class Area
        OntClass areaClass = createClass(model, className);

        // print list of all classes
        System.out.println("Список класів у онтології після додавання " + className + ":");
        printAllClassesURIs(model);

        // remove class Area
        removeClass(model, areaClass);

        // print list of all classes again
        System.out.println("Список класів у онтології після видалення " + className + ":");
        printAllClassesURIs(model);
    }

    public static OntClass getClassByName(OntModel model, String className) {
        return model.getOntClass(ontologyURI + className);
    }

    public static Individual createIndividual(OntModel model, OntClass classInstance, String individualName) {
        return model.createIndividual(ontologyURI + individualName, classInstance);
    }

    public static List<Resource> getDataPropertiesOfIndividual(OntModel ontModel, Individual individual) {
        List<Resource> dataProperties = new ArrayList<>();

        // Get all statements where the individual is the subject
        List<Statement> statements = ontModel.listStatements(individual, null, (String) null).toList();

        // Iterate over the statements and extract the data properties
        for (Statement statement : statements) {
            if (statement.getPredicate().isProperty()) {
                dataProperties.add(statement.getPredicate().asResource());
                System.out.println("prop = " + statement.getPredicate() +  "; " + statement.getString());
            }
        }

        return dataProperties;
    }

    public static void getObjectPropertyUsage(OntModel model, ObjectProperty property) {
        if (property != null) {
            // Iterate over all subjects that have the given object property
            ResIterator subjects = model.listSubjectsWithProperty(property);
            ExtendedIterator<Resource> extendedIterator = WrappedIterator.create(subjects);

            while (extendedIterator.hasNext()) {
                Resource subject = extendedIterator.next();
                System.out.println("Subject: " + subject.getURI());

                // Get the values of the object property for the subject
                StmtIterator statements = subject.listProperties(property);
                while (statements.hasNext()) {
                    Statement statement = statements.next();
                    RDFNode value = statement.getObject();
                    System.out.println("Value: " + value);
                }
            }
        } else {
            System.out.println("Object property not found.");
        }
    }

    public static void addObjectPropertyValue(OntModel model, Individual object, String propertyName, Individual subject){
        ObjectProperty property = model.getObjectProperty(Configuration.ONTOLOGY_URI + propertyName);
        model.add(object, property, subject);

    }


    public static void setDataPropertyValue(
            OntModel model, Individual individual, String dataPropertyName, String value
    )
    {

        // Get the data property by its name
        DatatypeProperty dataProperty = model.getDatatypeProperty(ontologyURI + dataPropertyName);

        // Create a literal value for the data property value
        Literal dataPropertyValue = model.createTypedLiteral(value);

        // Add the data property value to the individual
        individual.addProperty(dataProperty, dataPropertyValue);

        getDataPropertiesOfIndividual(model, individual);

        // Save the modified OntModel or perform any other operations as needed
//         model.write(System.out, "RDF/XML"); // Example of saving the OntModel to the console

    }


    public static void removeIndividual(OntModel model, Individual individual) {
        model.removeAll(individual, null, null);
    }

    public static void createAndRemoveIndividual(OntModel model, OntClass classInstance, String individualName) {

        Individual individual = createIndividual(model, classInstance, individualName);
        System.out.println("Список індивідуалів у класі Law після створення " + individualName + ":");
        printAllIndividualURIs(classInstance);

        removeIndividual(model, individual);
        System.out.println("Список індивідуалів у класі Law після видалення " + individualName + ":");
        printAllIndividualURIs(classInstance);

    }

    public static List<QuerySolution> selectSPARQLQuery(OntModel model, String sparqlQuery) {
        List<QuerySolution> rows = new ArrayList<>();
        Query query = QueryFactory.create(sparqlQuery);
        try (QueryExecution executionInstance = QueryExecutionFactory.create(query, model)) {
            ResultSet resultSet = executionInstance.execSelect();
            while (resultSet.hasNext()) {
                rows.add(resultSet.next());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return rows;
    }

}
