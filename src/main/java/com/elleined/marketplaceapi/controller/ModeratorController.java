package com.elleined.marketplaceapi.controller;


import com.elleined.marketplaceapi.dto.ProductDTO;
import com.elleined.marketplaceapi.dto.UserDTO;
import com.elleined.marketplaceapi.service.moderator.ModeratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{moderatorId}/moderator")
@CrossOrigin(origins = "*")
public class ModeratorController {
    private final ModeratorService moderatorService;

    @GetMapping("/getAllUnverifiedUser")
    public List<UserDTO> getAllUnverifiedUser() {
        return moderatorService.getAllUnverifiedUser();
    }

    @GetMapping("/getAllPendingProduct")
    public List<ProductDTO> getAllPendingProduct() {
        return moderatorService.getAllPendingProduct();
    }


    @PatchMapping("/verifyUser/{userToBeVerifiedId}")
    public void verifyUser(@PathVariable("userToBeVerifiedId") int userToBeVerifiedId) {
        moderatorService.verifyUser(userToBeVerifiedId);
    }

    @PatchMapping("/verifyAllUser")
    public void verifyAllUser(@RequestBody List<Integer> userToBeVerifiedIds) {
        moderatorService.verifyAllUser(userToBeVerifiedIds);
    }


    @PatchMapping("/listProduct/{productId}")
    public void listProduct(@PathVariable("productId") int productId) {
        moderatorService.listProduct(productId);
    }

    @PatchMapping("/listAllProduct")
    public void listAllProduct(@RequestParam List<Integer> productIds) {
        moderatorService.listAllProduct(productIds);
    }
}
