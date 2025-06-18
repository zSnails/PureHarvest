package cr.ac.itcr.zsnails.pureharvest.ui.orders;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

import java.util.Formatter;
import java.util.List;
import java.util.Map; // Import Map

public class Order {
    @DocumentId
    private String documentId;
    private Timestamp date;
    private String userId;
    private String sellerId;
    private Integer status;
    private List<OrderItem> productsBought;
    public Order() {

    }

    public Order(Timestamp date, String userId, String sellerId, List<OrderItem> productsBought, Integer status) {
        this.date = date;
        this.userId = userId;
        this.sellerId = sellerId;
        this.productsBought = productsBought;
        this.status = status;
    }

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

    public List<OrderItem> getProductsBought() { // Getter updated
        return productsBought;
    }

    public void setProductsBought(List<OrderItem> productsBought) { // Setter updated
        this.productsBought = productsBought;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public static class OrderItem {
        public String id;
        public Integer amount;
        public OrderItem() {

        }
        public OrderItem(String id, int amount) {
            this.id = id;
            this.amount = amount;
        }
    }
}