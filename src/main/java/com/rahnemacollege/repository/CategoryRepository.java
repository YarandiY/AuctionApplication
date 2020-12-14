package com.rahnemacollege.repository;

import com.rahnemacollege.model.Category;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends CrudRepository<Category, Integer> {

    @Override
    Optional<Category> findById(Integer integer);

    Category findByCategoryName(String s);
}
