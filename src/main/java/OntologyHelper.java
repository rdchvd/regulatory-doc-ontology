import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.ModelFactory;

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

    public static void removeIndividual(OntModel model, Individual individual) {
        model.removeAll(individual, null, null);
    }

    public static void createAndRemoveIndividual(OntModel model, String className, String individualName) {

        OntClass classInstance = getClassByName(model, className);

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
