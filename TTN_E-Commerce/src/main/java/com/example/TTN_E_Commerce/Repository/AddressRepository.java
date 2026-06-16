package com.example.TTN_E_Commerce.Repository;

import com.example.TTN_E_Commerce.Entity.Address;
import com.example.TTN_E_Commerce.Entity.User;
import com.example.TTN_E_Commerce.Enum.UserAddressLabel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {
    List<Address> findByUserAndAddressLabel(User user, UserAddressLabel addressLabel);
}
