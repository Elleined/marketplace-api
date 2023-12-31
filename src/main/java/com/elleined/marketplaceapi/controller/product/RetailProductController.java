package com.elleined.marketplaceapi.controller.product;

import com.elleined.marketplaceapi.dto.product.RetailProductDTO;
import com.elleined.marketplaceapi.mapper.product.RetailProductMapper;
import com.elleined.marketplaceapi.model.product.RetailProduct;
import com.elleined.marketplaceapi.model.user.User;
import com.elleined.marketplaceapi.service.product.retail.RetailProductService;
import com.elleined.marketplaceapi.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/retail-products")
public class RetailProductController {
    private final RetailProductService retailProductService;
    private final RetailProductMapper retailProductMapper;

    private final UserService userService;

    @GetMapping("/listed/{currentUserId}")
    public List<RetailProductDTO> getAllExcept(@PathVariable("currentUserId") int currentUserId) {
        User currentUser = userService.getById(currentUserId);
        return retailProductService.getAllExcept(currentUser).stream()
                .map(p -> {
                    double price = retailProductService.calculateTotalPrice(p);
                    return retailProductMapper.toDTO(p, price);
                }).toList();
    }

    @GetMapping("/{id}")
    public RetailProductDTO getById(@PathVariable("id") int id) {
        RetailProduct retailProduct = retailProductService.getById(id);

        double price = retailProductService.calculateTotalPrice(retailProduct);
        return retailProductMapper.toDTO(retailProduct, price);
    }

    @GetMapping("/{productId}/calculate-order-price")
    public double calculateOrderPrice(@PathVariable("productId") int productId,
                                      @RequestParam("userOrderQuantity") int userOrderQuantity) {

        RetailProduct retailProduct = retailProductService.getById(productId);
        return retailProductService.calculateOrderPrice(retailProduct, userOrderQuantity);
    }

    @GetMapping("/search-by-crop-name")
    public List<RetailProductDTO> searchProductByCropName(@RequestParam("cropName") String cropName) {
        return retailProductService.searchProductByCropName(cropName).stream()
                .map(p -> {
                    double price = retailProductService.calculateTotalPrice(p);
                    return retailProductMapper.toDTO(p, price);
                }).toList();
    }

    @GetMapping("/{productId}/calculate-total-price")
    public double calculateTotalPrice(@PathVariable("productId") int productId,
                                      @RequestParam("pricePerUnit") double pricePerUnit,
                                      @RequestParam("quantityPerUnit") int quantityPerUnit) {
        RetailProduct retailProduct = retailProductService.getById(productId);
        return retailProductService.calculateTotalPrice(pricePerUnit, quantityPerUnit, retailProduct.getAvailableQuantity());
    }

    @GetMapping("/{productId}/calculate-sale-percentage")
    public double getSalePercentage(@PathVariable("productId") int productId,
                                    @RequestParam("quantityPerUnit") int quantityPerUnit,
                                    @RequestParam("pricePerUnit") int pricePerUnit) {

        RetailProduct retailProduct = retailProductService.getById(productId);
        double currentTotalPrice = retailProductService.calculateTotalPrice(retailProduct);
        double salePrice = retailProductService.calculateTotalPrice(pricePerUnit, quantityPerUnit, retailProduct.getAvailableQuantity());
        return retailProductService.getSalePercentage(currentTotalPrice, salePrice);
    }
}
