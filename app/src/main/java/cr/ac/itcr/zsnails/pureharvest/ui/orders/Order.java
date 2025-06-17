
package cr.ac.itcr.zsnails.pureharvest.ui.orders;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

public class Order {
    @DocumentId
    private String documentId;

    private Timestamp date;
    private String userId;
    private String sellerId;
    private String productId;
    private Integer status;

    public Order() {

    }


    public Order(Timestamp date, String userId, String sellerId, String productId, Integer status) {
        this.date = date;
        this.userId = userId;
        this.sellerId = sellerId;
        this.productId = productId;
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

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }


    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}