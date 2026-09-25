package com.guvi.ecommerceApi.Service;


   import com.guvi.ecommerceApi.DTO.ProductRequestDTO;
    import com.guvi.ecommerceApi.Model.Product;

    import java.util.List;

    public interface ProductService {

        List<Product> getProducts();

        Product getProductsById(String id);

        Product addProduct(ProductRequestDTO productRequestDTO);

        Product getProductBycategory(String category);

        Product updateProduct(ProductRequestDTO product);

        String deleteProduct(String id );

        List<Product> addProducts(List<Product> products);
    }
