import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ResourceFactory;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegulatoryDocument {
    private final String id;
    private final String docType;
    private String industry;
    private String edition;
    private Date createdAt;
    private Date implementedAt;
    private Date renewedAt;
    private boolean isAvailableOnline;
    private boolean isInternational;
    private String label;
    private String name;
    private OntModel model;
    private String link;
    private String annotation;

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

    public RegulatoryDocument(OntModel model, String id, String docType, String label, String industry, String edition,
                              Date createdAt, Date implementedAt, Date renewedAt,
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

    public String getEdition() {
        return edition;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getImplementedAt() {
        return implementedAt;
    }

    public void setImplementedAt(Date implementedAt) {
        this.implementedAt = implementedAt;
    }

    public Date getRenewedAt() {
        return renewedAt;
    }

    public void setRenewedAt(Date renewedAt) {
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

    public void getDataFromOnline() {
        SiteReader siteReader = new SiteReader();
        this.link = siteReader.findDocLink(this.name, this.id);
        if (this.link!=null)
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
            System.out.println("Adding " + author.getURI() + " to " + this.individualInstance.getURI());
            OntologyHelper.addObjectPropertyValue(model, this.individualInstance, "hasAuthor", author);
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
            OntologyHelper.getDataPropertyUsage(model, model.getDatatypeProperty(Configuration.ONTOLOGY_URI + "id"));

        }
        if (this.industry != null) {
            OntologyHelper.addDataPropertyValue(
                    model, this.individualInstance, "id", ResourceFactory.createStringLiteral(this.id)
            );
            OntologyHelper.getDataPropertyUsage(model, model.getDatatypeProperty(Configuration.ONTOLOGY_URI + "id"));

        }

        if (this.industry != null) {
            OntologyHelper.addDataPropertyValue(
                    model, this.individualInstance, "industry", ResourceFactory.createStringLiteral(this.industry)
            );
        }

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

        if (!this.isAvailableOnline) {
            this.isAvailableOnline = false;
        }
        OntologyHelper.addDataPropertyValue(
                model, this.individualInstance, "isAvailableOnline", ResourceFactory.createTypedLiteral(this.isAvailableOnline)
        );

        if (!this.isInternational) {
            this.isInternational = false;
        }
        OntologyHelper.addDataPropertyValue(
                model, this.individualInstance, "isInternational", ResourceFactory.createTypedLiteral(this.isInternational)
        );

        if (this.authors != null) {
            addAuthorsToOntology();
        }


    }
}
