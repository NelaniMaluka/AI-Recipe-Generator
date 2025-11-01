package com.nelani.recipe_search_backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class UserDto {

    @NotBlank(message = "First name must not be blank")
    private String firstname;

    @NotBlank(message = "Last name must not be blank")
    private String lastname;

    private List<String> allergies;
}
