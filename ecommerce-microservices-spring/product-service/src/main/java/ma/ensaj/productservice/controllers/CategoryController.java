package ma.ensaj.productservice.controllers;

import lombok.RequiredArgsConstructor;
import ma.ensaj.productservice.dto.CategoryRequestDTO;
import ma.ensaj.productservice.dto.CategoryResponseDTO;
import ma.ensaj.productservice.entities.Category;
import ma.ensaj.productservice.repositories.CategoryRepository;
import ma.ensaj.productservice.services.CategoryService;
import ma.ensaj.productservice.services.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // Créer une catégorie
    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCategory(@RequestBody CategoryRequestDTO request) {
        CategoryResponseDTO response = categoryService.createCategory(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Récupérer toutes les catégories
    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getAllCategories() {
        List<CategoryResponseDTO> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    // Récupérer une catégorie par ID
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> getCategoryById(@PathVariable String id) {
        CategoryResponseDTO response = categoryService.getCategoryById(id);
        return ResponseEntity.ok(response);
    }

    // Mettre à jour une catégorie
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(
            @PathVariable String id,
            @RequestBody CategoryRequestDTO request
    ) {
        CategoryResponseDTO updated = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(updated);
    }

    // Supprimer une catégorie
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable String id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
