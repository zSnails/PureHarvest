package cr.ac.itcr.zsnails.pureharvest.ui.orders; // O el paquete correcto de tu modelo

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import java.util.List; // Importar List

public class Order {
    @Exclude
    private String documentId; // Para almacenar el ID del documento de Firestore

    private Timestamp date;
    private String userId;
    // private String productId; // Este campo parece redundante si vas a usar productIDs.
    // Si tiene otro propósito, puedes mantenerlo.
    // Si era para un solo ID de producto, y ahora necesitas una lista,
    // entonces productIDs es el reemplazo.
    private String sellerId;
    private List<String> productIDs; // NUEVO CAMPO: Lista de IDs de productos

    public Order() {
        // Constructor vacío requerido por Firestore
    }

    // Constructor actualizado (opcional, el constructor vacío es el más importante para Firestore)
    // Si actualizas el constructor, considera incluir productIDs
    public Order(Timestamp date, String userId, String sellerId, List<String> productIDs) {
        this.date = date;
        this.userId = userId;
        this.sellerId = sellerId;
        this.productIDs = productIDs; // Inicializar el nuevo campo
    }

    // Getter y Setter para documentId (como los tienes)
    @Exclude
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    // Getters y Setters existentes
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

    /*
    // Comentado o eliminado si productId ya no se usa para este propósito
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
    */

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    // Getter y Setter para el nuevo campo productIDs
    public List<String> getProductIDs() {
        return productIDs;
    }

    public void setProductIDs(List<String> productIDs) {
        this.productIDs = productIDs;
    }
}