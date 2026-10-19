package com.inventory.controller;

import com.inventory.dto.DTOs.ApiResponse;
import com.inventory.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {

    @Autowired private CategoryRepository categoryRepository;

    @GetMapping
    public ResponseEntity<?> all() {
        return ResponseEntity.ok(ApiResponse.ok("ok", categoryRepository.findAll()));
    }
}
