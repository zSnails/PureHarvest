package cr.ac.itcr.zsnails.pureharvest.ui.cart;

import java.io.Serializable;

public interface Item extends Serializable {
    Long getId();

    String getProductId();

    String getName();

    Double getPrice();

    String getType();

    Integer getAmount();

    String getImage();

    void setAmount(Integer amount);
}
