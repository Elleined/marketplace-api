package com.elleined.marketplaceapi.controller.product;

import com.elleined.marketplaceapi.dto.product.WholeSaleProductDTO;
import com.elleined.marketplaceapi.exception.product.ProductPriceException;
import com.elleined.marketplaceapi.mapper.product.WholeSaleProductMapper;
import com.elleined.marketplaceapi.model.product.WholeSaleProduct;
import com.elleined.marketplaceapi.model.user.User;
import com.elleined.marketplaceapi.service.product.wholesale.WholeSaleProductService;
import com.elleined.marketplaceapi.service.user.UserService;
import com.elleined.marketplaceapi.utils.Formatter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/whole-sale-products")
public class WholeSaleProductController {
    private final WholeSaleProductService wholeSaleProductService;
    private final WholeSaleProductMapper wholeSaleProductMapper;

    private final UserService userService;

    @GetMapping("/listed/{currentUserId}")
    public List<WholeSaleProductDTO> getAllExcept(@PathVariable("currentUserId") int currentUserId) {
        User currentUser = userService.getById(currentUserId);
        return wholeSaleProductService.getAllExcept(currentUser).stream()
                .map(wholeSaleProductMapper::toDTO)
                .toList();
    }

    @GetMapping("/{id}")
    public WholeSaleProductDTO getById(@PathVariable("id") int id) {
        WholeSaleProduct wholeSaleProduct = wholeSaleProductService.getById(id);
        return wholeSaleProductMapper.toDTO(wholeSaleProduct);
    }


    @GetMapping("/calculate-total-price")
    public double calculateTotalPrice(@RequestParam("totalPrice") double totalPrice,
                                      @RequestParam("salePercentage") int salePercentage) {
        if (salePercentage > 100) throw new ProductPriceException("Sale percentage cannot be greater than 100");
        return Formatter.formatDouble((totalPrice * (salePercentage / 100f)));
    }

    @GetMapping("/search-by-crop-name")
    public List<WholeSaleProductDTO> searchProductByCropName(@RequestParam("cropName") String cropName) {
        return wholeSaleProductService.searchProductByCropName(cropName).stream()
                .map(wholeSaleProductMapper::toDTO)
                .toList();
    }
}
