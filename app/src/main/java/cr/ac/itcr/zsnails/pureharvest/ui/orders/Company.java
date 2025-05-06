// File: cr.ac.itcr.zsnails.pureharvest.ui.orders.Company.java (o en un paquete 'models')
package cr.ac.itcr.zsnails.pureharvest.ui.orders; // O cr.ac.itcr.zsnails.pureharvest.models.Company

import com.google.firebase.firestore.DocumentId;

public class Company {
    @DocumentId
    private String documentId; // El ID del documento de la empresa (coincide con sellerId en la orden)

    private String name; // Nombre de la empresa
    // Añade otros campos de la empresa si los necesitas

    public Company() {
        // Constructor vacío requerido por Firestore
    }

    // Getters y Setters
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