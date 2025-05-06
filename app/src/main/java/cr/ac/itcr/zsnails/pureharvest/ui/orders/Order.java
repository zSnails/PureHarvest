package cr.ac.itcr.zsnails.pureharvest.ui.orders; // O el paquete correcto de tu modelo

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

public class Order {
    @Exclude
    private String documentId; // Para almacenar el ID del documento de Firestore
    private Timestamp date;
    private String userId;
    private String productId;  // Este campo ahora no se usa directamente para el ID del pedido en la UI, pero puede ser útil internamente
    private String sellerId;

    public Order() {
        // Constructor vacío requerido por Firestore
    }

    public Order(Timestamp date, String userId, String productId, String sellerId) {
        this.date = date;
        this.userId = userId;
        this.productId = productId;
        this.sellerId = sellerId;
    }

    @Exclude
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