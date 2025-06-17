// File: cr.ac.itcr.zsnails.pureharvest.ui.orders.User.java
// Or: cr.ac.itcr.zsnails.pureharvest.models.User.java
package cr.ac.itcr.zsnails.pureharvest.ui.orders; // Adjust package if it's in a 'models' directory

import com.google.firebase.firestore.DocumentId;

public class User {
    @DocumentId
    private String documentId;

    private String fullName; // Nombre completo del usuario
    private String email;    // Correo electrónico del usuario
    private String phone;    // Número de teléfono del usuario

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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}