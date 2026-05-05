package com.agritainment.controller;

import com.agritainment.annotation.RequireRole;
import com.agritainment.common.Result;
import com.agritainment.entity.Product;
import com.agritainment.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public Result<List<Product>> getProducts() {
        return Result.ok(productService.getProducts());
    }

    @GetMapping("/{id}")
    public Result<Product> getProduct(@PathVariable Long id) {
        return Result.ok(productService.getProduct(id));
    }

    @PostMapping("/{id}/purchase")
    @RequireRole({"customer", "staff", "admin"})
    public Result<Object> purchase(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id) {
        return Result.ok(productService.purchase(userId, id));
    }
}
