package cr.ac.itcr.zsnails.pureharvest.ui.company_buyers;

public class CompanyBuyer {
    private String id;
    private String name;
    private int itemsBought;
    private String email;
    private String phone;

    public CompanyBuyer(String id, String name, int itemsBought, String email, String phone) {
        this.id = id;
        this.name = name;
        this.itemsBought = itemsBought;
        this.email = email;
        this.phone = phone;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}