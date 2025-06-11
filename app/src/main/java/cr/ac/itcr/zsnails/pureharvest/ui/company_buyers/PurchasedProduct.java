package cr.ac.itcr.zsnails.pureharvest.ui.company_buyers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PurchasedProduct {
    private String productId;
    private String name;
    private double price;
    private Date date;

    public PurchasedProduct(String productId, String name, double price, Date date) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.date = date;
    }

    public String getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getFormattedDate() {
        if (date == null) {
            return "N/A";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(date);
    }
}