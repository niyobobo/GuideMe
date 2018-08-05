package ynwa.guideme.admin.orders;

public class OrdersModel {

    private String key, company, description, telephone, names;

    public OrdersModel() {
    }

    public OrdersModel(String key, String company, String description, String telephone, String names) {
        this.key = key;
        this.company = company;
        this.description = description;
        this.telephone = telephone;
    }

    public String getKey() {
        return key;
    }

    public String getCompany() {
        return company;
    }

    public String getDescription() {
        return description;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getNames() {
        return names;
    }
}
