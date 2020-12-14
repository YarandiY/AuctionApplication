package com.rahnemacollege.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "Categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name")
    private String categoryName;

    public Category(String categoryName) {
        this.categoryName = categoryName;
    }

    public Category() {
    }

    @Override
    public String toString() {
        return categoryName;
    }

}
