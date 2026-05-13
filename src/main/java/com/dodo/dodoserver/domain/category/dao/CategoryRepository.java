package com.dodo.dodoserver.domain.category.dao;

import com.dodo.dodoserver.domain.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
    List<Category> findAllByDeletedAtIsNullOrderBySortOrderAsc();

    @Query("SELECT MAX(c.sortOrder) FROM Category c")
    Optional<Integer> findMaxSortOrder();
}
