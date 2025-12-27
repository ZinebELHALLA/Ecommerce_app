package ma.ensaj.productservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.ensaj.productservice.dto.CategoryRequestDTO;
import ma.ensaj.productservice.dto.CategoryResponseDTO;
import ma.ensaj.productservice.entities.Category;
import ma.ensaj.productservice.repositories.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // Créer une catégorie
    public CategoryResponseDTO createCategory(CategoryRequestDTO request) {
        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        Category saved = categoryRepository.save(category);
        log.info("Category created: {}", saved.getName());

        return mapToResponse(saved);
    }

    // Récupérer toutes les catégories
    public List<CategoryResponseDTO> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Récupérer une catégorie par ID
    public CategoryResponseDTO getCategoryById(String id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return mapToResponse(category);
    }

    // Mettre à jour une catégorie
    public CategoryResponseDTO updateCategory(String id, CategoryRequestDTO request) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        existing.setName(request.getName());
        existing.setDescription(request.getDescription());

        Category updated = categoryRepository.save(existing);
        log.info("Category updated: {}", updated.getName());

        return mapToResponse(updated);
    }

    // Supprimer une catégorie
    public void deleteCategory(String id) {
        categoryRepository.deleteById(id);
        log.info("Category deleted: {}", id);
    }

    // Mapper entity -> DTO
    private CategoryResponseDTO mapToResponse(Category category) {
        return CategoryResponseDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }
}

