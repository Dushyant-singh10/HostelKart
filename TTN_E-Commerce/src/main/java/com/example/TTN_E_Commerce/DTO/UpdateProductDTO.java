package com.example.TTN_E_Commerce.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProductDTO {
    private String  name;
    private String  description;
    private Boolean isCancellable;
    private Boolean isReturnable;
}
