package com.nelani.recipe_search_backend.security;

import lombok.Getter;

@Getter
public enum ApplicationUserPermission {
    // User permissions
    USER_READ("user:read"),
    USER_WRITE("user:write"),
    USER_DELETE("user:delete"),
    USER_MANAGE_ROLES("user:manage_roles"),

    // Recipe permissions
    RECIPE_READ("recipe:read"),
    RECIPE_WRITE("recipe:write"),
    RECIPE_DELETE("recipe:delete"),
    RECIPE_PUBLISH("recipe:publish"),
    RECIPE_IMAGE_UPLOAD("recipe:image_upload");

    private final String permission;

    ApplicationUserPermission(String permission) {
        this.permission = permission;
    }

}
