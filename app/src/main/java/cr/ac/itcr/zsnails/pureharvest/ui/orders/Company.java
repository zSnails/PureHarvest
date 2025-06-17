package cr.ac.itcr.zsnails.pureharvest.ui.orders;

import com.google.firebase.firestore.DocumentId;

public class Company {
    @DocumentId
    private String documentId;

    private String name;

    public Company() {

    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}