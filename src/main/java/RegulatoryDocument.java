import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ResourceFactory;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegulatoryDocument {
    private final String id;
    private final String docType;
    private String industry;
    private int edition;
    private LocalDate createdAt;
    private LocalDate implementedAt;
    private LocalDate renewedAt;
    private boolean isAvailableOnline;
    private boolean isInternational;
    private String label;
    private String name;
    private OntModel model;
    private String link;

    public OntClass classInOntology;
    public Individual individualInstance;
    private List<Individual> authors;


    public RegulatoryDocument(OntModel model, String id, String docType, String label, String name) {
        this.model = model;
        this.id = id;
        this.docType = docType;
        this.label = label;
        this.name = name;
        setClassByType();
    }

    public RegulatoryDocument(OntModel model, String id, String docType, String label, String industry, int edition,
                              LocalDate createdAt, LocalDate implementedAt, LocalDate renewedAt,
                              boolean isAvailableOnline, boolean isInternational) {
        this.id = id;
        this.docType = docType;
        this.label = label;
        this.industry = industry;
        this.edition = edition;
        this.createdAt = createdAt;
        this.implementedAt = implementedAt;
        this.renewedAt = renewedAt;
        this.isAvailableOnline = isAvailableOnline;
        this.isInternational = isInternational;
    }


    public String getId() {
        return id;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public int getEdition() {
        return edition;
    }

    public void setEdition(int edition) {
        this.edition = edition;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDate getImplementedAt() {
        return implementedAt;
    }

    public void setImplementedAt(LocalDate implementedAt) {
        this.implementedAt = implementedAt;
    }

    public LocalDate getRenewedAt() {
        return renewedAt;
    }

    public void setRenewedAt(LocalDate renewedAt) {
        this.renewedAt = renewedAt;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public boolean isAvailableOnline() {
        return isAvailableOnline;
    }

    public void setAvailableOnline(boolean availableOnline) {
        isAvailableOnline = availableOnline;
    }

    public boolean isInternational() {
        return isInternational;
    }

    public void setInternational(boolean international) {
        isInternational = international;
    }

    public String getDocType() {
        return docType;
    }

    public String getLabel() {
        return label;
    }

    public String getName() {
        return name;
    }

    public String getLink() {
        return link;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setAuthors(List<Individual> authors) {
        this.authors = authors;
    }

    public List<Individual> getAuthors() {
        return this.authors;
    }

    public void print() {
        System.out.println(this.label);
    }

    public void setClassByType() {

        Map<String, String> documentTypes = new HashMap<>();
        documentTypes.put("ISO", "InternationalStandard");
        documentTypes.put("ДК", "StateClassifier");
        documentTypes.put("ГОСТ", "StateStandard");
        documentTypes.put("ДСТУ", "StateStandard");

        String className = String.valueOf(documentTypes.get(this.docType));
        this.classInOntology = OntologyHelper.getClassByName(this.model, className);
    }

    public void addToOntology() {
        this.individualInstance = OntologyHelper.createIndividual(
                model, this.classInOntology, this.getId()
        );

        if (this.label != null)
            this.individualInstance.addLabel(ResourceFactory.createStringLiteral(this.label));


    }
}
