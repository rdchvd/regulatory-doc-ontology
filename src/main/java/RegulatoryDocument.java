import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ResourceFactory;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;

public class RegulatoryDocument {
    private final String id;
    private final String docType;
    public OntClass classInOntology;
    public Individual individualInstance;
    private String industry;
    private String edition;
    private Date createdAt;
    private Date implementedAt;
    private Date renewedAt;
    private boolean isAvailableOnline;
    private boolean isInternational;
    private final String label;
    private String name;
    private OntModel model;
    private String link;
    private String annotation;
    private List<Individual> authors;


    public RegulatoryDocument(OntModel model, String id, String docType, String label, String name) {
        this.model = model;
        this.id = id;
        this.docType = docType;
        this.label = label;
        this.name = name;
        setClassByType();
    }

    public RegulatoryDocument(OntModel model, String id, String docType, String label, String industry, String edition,
                              Date createdAt, Date implementedAt, Date renewedAt,
                              boolean isAvailableOnline, boolean isInternational) {
        this.model = model;
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

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setAvailableOnline(boolean availableOnline) {
        isAvailableOnline = availableOnline;
    }

    public String getName() {
        return name;
    }

    public void setAuthors(List<Individual> authors) {
        this.authors = authors;
    }

    public void setClassByType() {

        Map<String, String> documentTypes = new HashMap<>();
        documentTypes.put("ISO", "InternationalStandard");
        documentTypes.put("ДК", "StateClassifier");
        documentTypes.put("ГОСТ", "StateStandard");
        documentTypes.put("ДСТУ", "StateStandard");

        String className = String.valueOf(documentTypes.get(this.docType));
        this.classInOntology = OntologyHelper.getClassByName(this.model, className);
        this.isInternational = Objects.equals(className, "InternationalStandard");
    }

    public void getDataFromOnline() {
        SiteReader siteReader = new SiteReader();
        this.link = siteReader.findDocLink(this.name, this.id);
        if (this.link != null)
            this.isAvailableOnline = true;

        siteReader.read(this.link);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

        String implementedAt = siteReader.findInTableByTitle("Дата початку дії");

        if (implementedAt != null) {
            ParsePosition pos = new ParsePosition(0);
            this.implementedAt = dateFormat.parse(implementedAt, pos);
        }

        String createdAt = siteReader.findInTableByTitle("Дата прийняття");
        if (createdAt != null) {
            ParsePosition pos = new ParsePosition(0);
            this.createdAt = dateFormat.parse(createdAt, pos);
        }

        siteReader.close();
    }

    public void addAuthorsToOntology() {
        for (Individual author : this.authors) {
            OntologyHelper.addObjectPropertyValue(model, this.individualInstance, "hasAuthor", author);
        }

    }

    public void addIndustryToOntology() {
        if (this.industry != null) {
            OntClass classInstance = OntologyHelper.getClassByName(model, "Industry");
            Individual industryInstance = OntologyHelper.getOrCreateIndividual(model, classInstance, this.industry);
            OntologyHelper.addObjectPropertyValue(model, this.individualInstance, "hasIndustry", industryInstance);
        }
    }


    public void addToOntology() {
        this.individualInstance = OntologyHelper.createIndividual(
                model, this.classInOntology, this.getId()
        );

        if (this.label != null)
            this.individualInstance.addLabel(ResourceFactory.createStringLiteral(this.label));

        if (this.annotation != null)
            this.individualInstance.addComment(ResourceFactory.createStringLiteral(this.annotation));

        if (this.id != null) {
            OntologyHelper.addDataPropertyValue(
                    model, this.individualInstance, "id", ResourceFactory.createStringLiteral(this.id)
            );
        }
        if (this.industry != null)
            addIndustryToOntology();


        if (this.edition != null) {
            OntologyHelper.addDataPropertyValue(
                    model, this.individualInstance, "edition", ResourceFactory.createStringLiteral(this.edition)
            );
        }

        if (this.createdAt != null) {
            OntologyHelper.addDataPropertyValue(
                    model, this.individualInstance, "createdAt", ResourceFactory.createTypedLiteral(this.createdAt)
            );
        }


        if (this.implementedAt != null) {
            OntologyHelper.addDataPropertyValue(
                    model, this.individualInstance, "implementedAt", ResourceFactory.createTypedLiteral(this.implementedAt)
            );
        }


        if (this.renewedAt != null) {
            OntologyHelper.addDataPropertyValue(
                    model, this.individualInstance, "renewedAt", ResourceFactory.createTypedLiteral(this.renewedAt)
            );
        }

        if (!this.isAvailableOnline)
            this.isAvailableOnline = false;

        OntologyHelper.addDataPropertyValue(
                model, this.individualInstance, "isAvailableOnline", ResourceFactory.createTypedLiteral(this.isAvailableOnline)
        );

        if (this.isInternational) {
            OntologyHelper.addDataPropertyValue(
                    model, this.individualInstance, "isInternational", ResourceFactory.createTypedLiteral(this.isInternational)
            );
        }

        if (this.authors != null) {
            addAuthorsToOntology();
        }




    }
}
