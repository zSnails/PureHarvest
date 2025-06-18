package cr.ac.itcr.zsnails.pureharvest.data.model;

import java.util.List;

public class Coupon {
    private String id;
    private String code;
    private String sellerId;
    private List<String> applicableProductIds;
    private double discountPercentage;
    private long expirationTimestamp;
    private int maxUses;
    private int uses;

    public Coupon() {

    }

    public Coupon(String code, String sellerId, List<String> applicableProductIds, double discountPercentage,
                  long expirationTimestamp, int maxUses, int uses) {
        this.code = code;
        this.sellerId = sellerId;
        this.applicableProductIds = applicableProductIds;
        this.discountPercentage = discountPercentage;
        this.expirationTimestamp = expirationTimestamp;
        this.maxUses = maxUses;
        this.uses = uses;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public List<String> getApplicableProductIds() {
        return applicableProductIds;
    }

    public void setApplicableProductIds(List<String> applicableProductIds) {
        this.applicableProductIds = applicableProductIds;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public long getExpirationTimestamp() {
        return expirationTimestamp;
    }

    public void setExpirationTimestamp(long expirationTimestamp) {
        this.expirationTimestamp = expirationTimestamp;
    }

    public int getMaxUses() {
        return maxUses;
    }

    public void setMaxUses(int maxUses) {
        this.maxUses = maxUses;
    }

    public int getUses() {
        return uses;
    }

    public void setUses(int uses) {
        this.uses = uses;
    }
}
