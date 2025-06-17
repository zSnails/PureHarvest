
package cr.ac.itcr.zsnails.pureharvest.ui.orders;

import com.google.firebase.firestore.DocumentId;

public class User {
    @DocumentId
    private String documentId;

    private String fullName;
    private String email;
    private String phone;

    public User() {

    }
    
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