package cr.ac.itcr.zsnails.pureharvest.ui.orders; // Updated package

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude; // To exclude documentId from being written back to Firestore

public class Order {
    @Exclude // Exclude from Firestore serialization, managed manually
    private String documentId;
    private Timestamp date;
    private String userId;
    private String productId;  // Will be used for the "orderName" field in the item
    private String sellerId;

    // Required empty constructor for Firestore
    public Order() {}

    public Order(Timestamp date, String userId, String productId, String sellerId) {
        this.date = date;
        this.userId = userId;
        this.productId = productId;
        this.sellerId = sellerId;
    }

    @Exclude // Getter for documentId
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

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }
}