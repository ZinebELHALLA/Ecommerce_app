package ma.ensaj.productservice.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.ensaj.productservice.clients.InventoryClient;
import ma.ensaj.productservice.dto.InventoryRequest;
import ma.ensaj.productservice.dto.ProductRequestDTO;
import ma.ensaj.productservice.dto.ProductResponseDTO;
import ma.ensaj.productservice.dto.ProductValidationResponse;
import ma.ensaj.productservice.entities.Category;
import ma.ensaj.productservice.entities.Product;
import ma.ensaj.productservice.repositories.CategoryRepository;
import ma.ensaj.productservice.repositories.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryClient inventoryClient;

    @Override
    @Transactional
    public ProductResponseDTO createProduct(ProductRequestDTO request) {
        // verfier ctagorie
        Category category = categoryRepository.findById(request.getCategoryId()).orElseThrow(() -> new RuntimeException("Category not found"));
        Product product = Product.builder().
name(   request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .imageUrl(request.getImageUrl())
                .sku(request.getSku())
                .categoryId(category.getId())
                .active(true)
        .build();

        Product savedProduct = productRepository.save(product);
        log.info("Product created: {}, id: {}", savedProduct.getName(), savedProduct.getId());
        
        // Create Inventory Record
        try {
            InventoryRequest inventoryRequest = InventoryRequest.builder()
                    .skuCode(savedProduct.getSku())
                    .quantity(savedProduct.getStock())
                    .build();
            inventoryClient.addProduct(inventoryRequest);
            log.info("Inventory created for SKU: {}", savedProduct.getSku());
        } catch (Exception e) {
            log.error("Failed to create inventory for SKU: {}", savedProduct.getSku(), e);
            // Optional: throw exception to rollback product creation if consistency is strictly required
             throw new RuntimeException("Failed to create inventory: " + e.getMessage()); 
        }

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
        existingProduct.setStock(request.getStock());
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




    @Override
    public List<ProductValidationResponse> validateProducts(List<String> skuCodes) {
        List<ProductValidationResponse> responses = new ArrayList<>();
        List<Product> existingProducts = productRepository.findBySkuIn(skuCodes);
        
        // Create map of existing SKUs to products for quick lookup
        java.util.Map<String, Product> productMap = existingProducts.stream()
                .collect(java.util.stream.Collectors.toMap(Product::getSku, p -> p));
        
        for (String skuCode : skuCodes) {
            Product product = productMap.get(skuCode);
            if (product != null && product.isActive()) {
                responses.add(ProductValidationResponse.builder()
                        .skuCode(skuCode)
                        .exists(true)
                        .productName(product.getName())
                        .price(BigDecimal.valueOf(product.getPrice()))
                        .build());
            } else {
                responses.add(ProductValidationResponse.builder()
                        .skuCode(skuCode)
                        .exists(false)
                        .errorMessage(product == null ? "Product not found" : "Product is inactive")
                        .build());
            }
        }
        
        return responses;
    }
    
    @Override
    public ProductValidationResponse validateProductBySku(String skuCode) {
        Optional<Product> productOpt = productRepository.findBySku(skuCode);
        
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            if (product.isActive()) {
                return ProductValidationResponse.builder()
                        .skuCode(skuCode)
                        .exists(true)
                        .productName(product.getName())
                        .price(BigDecimal.valueOf(product.getPrice()))
                        .build();
            } else {
                return ProductValidationResponse.builder()
                        .skuCode(skuCode)
                        .exists(false)
                        .errorMessage("Product is inactive")
                        .build();
            }
        } else {
            return ProductValidationResponse.builder()
                    .skuCode(skuCode)
                    .exists(false)
                    .errorMessage("Product not found")
                    .build();
        }
    }

    // Mapper entity -> DTO
    private ProductResponseDTO mapToResponse(Product product) {
        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .sku(product.getSku())
                .imageUrl(product.getImageUrl())
                .active(product.isActive())
                .categoryId(product.getCategoryId())
                .build();
    }
}
