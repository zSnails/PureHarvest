// File: cr.ac.itcr.zsnails.pureharvest.ui.orders.User.java (o en un paquete 'models')
package cr.ac.itcr.zsnails.pureharvest.ui.orders; // O cr.ac.itcr.zsnails.pureharvest.models.User

import com.google.firebase.firestore.DocumentId;

public class User {
    @DocumentId
    private String documentId; // El ID del documento del usuario (coincide con userId en la orden)

    private String name; // Nombre del usuario
    // Añade otros campos del usuario si los necesitas

    public User() {
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