package cr.ac.itcr.zsnails.pureharvest.ui.orders;

import com.google.firebase.firestore.DocumentId;

public class Product {
    @DocumentId
    private String documentId;

    private String name;
    private double price;


    public Product() {

    }


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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }


    public Product(String name, double price) {
        this.name = name;
        this.price = price;
    }
}