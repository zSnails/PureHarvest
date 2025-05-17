package cr.ac.itcr.zsnails.pureharvest.data.model;

public class FavoriteProduct {

    public String productId;
    public String ownerId;

    public FavoriteProduct(String productId, String ownerId) {
        this.productId = productId;
        this.ownerId = ownerId;
    }
}
