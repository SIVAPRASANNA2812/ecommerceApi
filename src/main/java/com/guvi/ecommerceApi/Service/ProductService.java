package com.guvi.ecommerceApi.Service;


   import com.guvi.ecommerceApi.DTO.ProductRequestDTO;
    import com.guvi.ecommerceApi.Model.Product;
   import org.springframework.data.domain.Page;

   import java.util.List;

    public interface ProductService {

        List<Product> getProducts();

        Product getProductsById(String id);

        Product addProduct(ProductRequestDTO productRequestDTO);

        Product getProductBycategory(String category);

        Product updateProduct(ProductRequestDTO product);

        String deleteProduct(String id );

        List<Product> addProducts(List<Product> products);

        Page<Product> getProducts(int page, int size, String sortBy, String sortDir);

        Page<Product> getProductsByCategory(String category, int page, int size, String sortBy, String sortDir);

        Page<Product> searchByName(String name, int page, int size);

        Page<Product> searchProductsByName(String name, int page, int size, String sortBy, String sortDir);
    }
