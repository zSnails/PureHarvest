package cr.ac.itcr.zsnails.pureharvest.ui.company_buyers;

public class CompanyBuyer {
    private String id;
    private String name;
    private int itemsBought;

    public CompanyBuyer(String id, String name, int itemsBought) {
        this.id = id;
        this.name = name;
        this.itemsBought = itemsBought;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getItemsBought() {
        return itemsBought;
    }

    public void setItemsBought(int itemsBought) {
        this.itemsBought = itemsBought;
    }
}