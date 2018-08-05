package ynwa.guideme.visitor.order;

public class ModelOrder {

    private String id, names, telephone, description, company;

    ModelOrder(String id, String names, String telephone, String description, String company) {
        this.id = id;
        this.names = names;
        this.telephone = telephone;
        this.description = description;
        this.company = company;
    }

    public String getId() {
        return id;
    }

    public String getNames() {
        return names;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getDescription() {
        return description;
    }

    public String getCompany() {
        return company;
    }
}
