package ma.ensaj.productservice.services;

import ma.ensaj.productservice.dto.ProductRequestDTO;
import ma.ensaj.productservice.dto.ProductResponseDTO;
import ma.ensaj.productservice.dto.ProductValidationResponse;

import java.util.List;

public interface ProductService {
    ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO);
    ProductResponseDTO updateProduct(String id,ProductRequestDTO productRequestDTO);
    ProductResponseDTO getProductBYId(String id);
    void deleteProduct(String id);
    List<ProductResponseDTO> getAllProducts();
    
    // New methods for inter-service communication
    List<ProductValidationResponse> validateProducts(List<String> skuCodes);
    ProductValidationResponse validateProductBySku(String skuCode);


}
