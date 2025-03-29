package cr.ac.itcr.zsnails.pureharvest.ui.cart;

public interface Item {
    String getName();

    Double getPrice();

    String getType();

    Integer getAmount();

    void setAmount(Integer newValue);
}
