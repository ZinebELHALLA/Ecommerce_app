package ma.ensaj.productservice.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.ensaj.productservice.dto.ProductRequestDTO;
import ma.ensaj.productservice.dto.ProductResponseDTO;
import ma.ensaj.productservice.entities.Category;
import ma.ensaj.productservice.entities.Product;
import ma.ensaj.productservice.repositories.CategoryRepository;
import ma.ensaj.productservice.repositories.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public ProductResponseDTO createProduct(ProductRequestDTO request) {
        // verfier ctagorie
        Category category = categoryRepository.findById(request.getCategoryId()).orElseThrow(() -> new RuntimeException("Category not found"));
        Product product = Product.builder().
name(   request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .imageUrl(request.getImageUrl())
                .sku(request.getSku())
                .categoryId(category.getId())
                .active(true)
        .build();

        Product savedProduct = productRepository.save(product);
        log.info("Product created: {}", savedProduct.getName(),savedProduct.getId());

        // return response dto

        return mapToResponse(savedProduct);
    }

    @Override
    public ProductResponseDTO updateProduct(String id, ProductRequestDTO request) {
        Product existingProduct = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        existingProduct.setName(request.getName());
        existingProduct.setDescription(request.getDescription());
        existingProduct.setPrice(request.getPrice());
        existingProduct.setSku(request.getSku());
        existingProduct.setImageUrl(request.getImageUrl());
        existingProduct.setActive(request.isActive());
        existingProduct.setCategoryId(request.getCategoryId());

        Product updatedProduct = productRepository.save(existingProduct);
        log.info("Product updated: {}", updatedProduct.getName(),updatedProduct.getId());


        return mapToResponse(updatedProduct);
    }


    @Override
    public ProductResponseDTO getProductBYId(String id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));

        return mapToResponse(product);
    }

    @Override
    public void deleteProduct(String id) {
        productRepository.deleteById(id);
        log.info("Product deleted: {}", id);

    }

    @Override
    public List<ProductResponseDTO> getAllProducts() {

        return productRepository.findAll()
                .stream().
                map(this::mapToResponse).
                collect(Collectors.toList());
    }




    // Mapper entity -> DTO
    private ProductResponseDTO mapToResponse(Product product) {
        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .sku(product.getSku())
                .imageUrl(product.getImageUrl())
                .active(product.isActive())
                .categoryId(product.getCategoryId())
                .build();
    }
}
