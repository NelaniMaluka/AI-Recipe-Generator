package com.nelani.recipe_search_backend.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Represents a user's profile information")
public record UserResponse(
                @Schema(description = "Publicly accessible unique identifier for the user", example = "user_12345") String publicId,

                @Schema(description = "First name of the user", example = "Nelani") String firstname,

                @Schema(description = "Last name of the user", example = "Maluka") String lastname,

                @Schema(description = "Email address of the user", example = "malukanelani@gmail.com") String email,

                @Schema(description = "List of allergies the user has, if any", example = "[\"Peanuts\", \"Gluten\"]") List<String> allergies) {
}
