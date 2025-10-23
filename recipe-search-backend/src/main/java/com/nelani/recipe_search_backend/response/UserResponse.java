package com.nelani.recipe_search_backend.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Represents a user's profile information")
public class UserResponse {

        @Schema(description = "Publicly accessible unique identifier for the user", example = "user_12345")
        private String publicId;

        @Schema(description = "First name of the user", example = "Nelani")
        private String firstname;

        @Schema(description = "Last name of the user", example = "Maluka")
        private String lastname;

        @Schema(description = "Email address of the user", example = "malukanelani@gmail.com")
        private String email;

        @Schema(description = "List of allergies the user has, if any", example = "[\"Peanuts\", \"Gluten\"]")
        private List<String> allergies;
}
