// File: cr.ac.itcr.zsnails.pureharvest.ui.orders.Order.java
package cr.ac.itcr.zsnails.pureharvest.ui.orders;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
// Ya no necesitas java.util.List si solo tienes un String

public class Order {
    @DocumentId
    private String documentId;

    private Timestamp date;
    private String userId;
    private String sellerId;
    private String productId; // CAMBIO: De List<String> productIDs a String productId

    public Order() {
        // Constructor vacío requerido por Firestore
    }

    // Constructor actualizado (opcional)
    public Order(Timestamp date, String userId, String sellerId, String productId) {
        this.date = date;
        this.userId = userId;
        this.sellerId = sellerId;
        this.productId = productId; // CAMBIO
    }

    // Getters y Setters
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    // CAMBIO: Getters y Setters para el String productId
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
}