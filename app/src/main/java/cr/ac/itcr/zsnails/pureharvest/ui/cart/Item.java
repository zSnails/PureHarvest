package cr.ac.itcr.zsnails.pureharvest.ui.cart;

import java.io.Serializable;

public interface Item extends Serializable {
    Long getId();

    String getProductId();

    String getName();

    Double getPrice();

    String getType();

    Integer getAmount();

    void setAmount(Integer amount);
}
