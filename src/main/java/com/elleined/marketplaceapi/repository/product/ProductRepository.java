package com.elleined.marketplaceapi.repository.product;

import com.elleined.marketplaceapi.model.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query("SELECT p FROM Product p WHERE p.crop.name LIKE CONCAT('%', :cropName, '%')")
    List<Product> searchProductByCropName(@Param("cropName") String cropName);
}