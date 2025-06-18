package cr.ac.itcr.zsnails.pureharvest.entities;

import cr.ac.itcr.zsnails.pureharvest.data.model.Product;

public class FavoriteDisplayProduct {
    public String productId;
    public String name;
    public String imageUrl;

    public static FavoriteDisplayProduct from(Product prod) {
        FavoriteDisplayProduct fav = new FavoriteDisplayProduct();
        fav.productId = prod.getId();
        fav.name = prod.getName();
        fav.imageUrl = prod.getFirstImageUrl();
        return fav;
    }
}
