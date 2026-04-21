package tn.fst.backend.backend.dto;

public class CategoryResponse {

    private Long idCategory;
    private String name;
    private String description;

    public CategoryResponse() {}

    public static CategoryResponse of(Long idCategory, String name, String description) {
        CategoryResponse r = new CategoryResponse();
        r.setIdCategory(idCategory);
        r.setName(name);
        r.setDescription(description);
        return r;
    }

    public Long getIdCategory() {
        return idCategory;
    }

    public void setIdCategory(Long idCategory) {
        this.idCategory = idCategory;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
