package ma.ensaj.productservice.controllers;

import lombok.RequiredArgsConstructor;
import ma.ensaj.productservice.dto.ProductRequestDTO;
import ma.ensaj.productservice.dto.ProductResponseDTO;
import ma.ensaj.productservice.entities.Product;
import ma.ensaj.productservice.services.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponseDTO> addProduct(@RequestBody ProductRequestDTO product){
        ProductResponseDTO  productResponseDTO = productService.createProduct(product);
        return new ResponseEntity<>(productResponseDTO,HttpStatus.CREATED);
    }
    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts(){
        List<ProductResponseDTO> productResponseDTO = productService.getAllProducts();
        return ResponseEntity.ok(productResponseDTO);
    }
    @GetMapping("/id")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable String id){
        ProductResponseDTO product=productService.getProductBYId(id);
        return  ResponseEntity.ok(product);
    }
    @DeleteMapping("/id")
    public  ResponseEntity<Void> deleteProductById(@PathVariable String id){
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();

    }
    @PutMapping("/id")
    public ResponseEntity<ProductResponseDTO> updateProductById(@PathVariable String id,@RequestBody ProductRequestDTO product){
        ProductResponseDTO productResponseDTO = productService.updateProduct(id, product);
        return ResponseEntity.ok(productResponseDTO);
    }

    @GetMapping("/health")
    public ResponseEntity<Void> checkHealth(){
        return ResponseEntity.noContent().build();
    }


}
