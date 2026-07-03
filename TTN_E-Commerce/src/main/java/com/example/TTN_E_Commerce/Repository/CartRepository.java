package com.example.TTN_E_Commerce.Repository;

import com.example.TTN_E_Commerce.Entity.Cart;
import com.example.TTN_E_Commerce.Entity.Customer;
import com.example.TTN_E_Commerce.Entity.ProductVariation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {
    List<Cart> findByCustomer(Customer customer);
    
    Optional<Cart> findByCustomerAndProductVariation(Customer customer, ProductVariation variation);
    
    void deleteByCustomer(Customer customer);
}
