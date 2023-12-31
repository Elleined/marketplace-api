package com.elleined.marketplaceapi.repository.product;

import com.elleined.marketplaceapi.model.product.RetailProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RetailProductRepository extends JpaRepository<RetailProduct, Integer> {

    @Query("SELECT rp FROM RetailProduct rp WHERE rp.crop.name LIKE CONCAT('%', :cropName, '%')")
    List<RetailProduct> searchProductByCropName(@Param("cropName") String cropName);
}
