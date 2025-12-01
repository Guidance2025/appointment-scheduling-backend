package org.rocs.asa.domain.category;

import jakarta.persistence.*;

import lombok.Getter; import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "tbl_category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "category_name", length = 64)
    private String categoryName;

    public Long getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String v) { this.categoryName = v; }
}