import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ResourceFactory;


public class DataPropertyDocRuleCreator {
    private final Individual document;
    private final String name;
    private final String value;
    public DatatypeProperty property;

    public DataPropertyDocRuleCreator(Individual document, String name, String value) {
        this.document = document;
        this.name = name.replace("%", "відсоток").replace(" ", "_");
        this.value = value.replace("%", "відсоток");
    }

    public void getOrCreateProperty(OntModel model) {
        this.property = model.getDatatypeProperty(Configuration.ONTOLOGY_URI + name);
        if (this.property == null)
            this.property = model.createDatatypeProperty(Configuration.ONTOLOGY_URI + name);
    }


    public void addPropertyToIndividual(OntModel model) {
        if (this.property != null) {
            OntologyHelper.addDataPropertyValue(
                    model, this.document, this.property, ResourceFactory.createStringLiteral(this.value)
            );
        }
    }


}
