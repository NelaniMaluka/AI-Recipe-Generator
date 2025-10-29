package com.nelani.recipe_search_backend.security;

import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

import static com.nelani.recipe_search_backend.security.ApplicationUserPermission.*;

@Getter
public enum ApplicationUserRole {

        USER(Set.of(
                        USER_READ,
                        USER_WRITE,
                        USER_DELETE,
                        RECIPE_READ)),

        USER_CREATOR(Set.of(
                        USER_READ,
                        RECIPE_READ,
                        RECIPE_WRITE,
                        RECIPE_IMAGE_UPLOAD,
                        RECIPE_PUBLISH)),

        MODERATOR(Set.of(
                        USER_READ,
                        USER_DELETE,
                        RECIPE_READ,
                        RECIPE_DELETE,
                        RECIPE_PUBLISH)),

        ADMIN(Set.of(
                        USER_READ,
                        USER_WRITE,
                        USER_DELETE,
                        USER_MANAGE_ROLES,
                        RECIPE_READ,
                        RECIPE_WRITE,
                        RECIPE_DELETE,
                        RECIPE_PUBLISH,
                        RECIPE_IMAGE_UPLOAD));

        private final Set<ApplicationUserPermission> permissions;

        ApplicationUserRole(Set<ApplicationUserPermission> permissions) {
                this.permissions = permissions;
        }

        public Set<SimpleGrantedAuthority> grantedAuthorities() {
                Set<SimpleGrantedAuthority> permissions = getPermissions().stream()
                                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                                .collect(Collectors.toSet());
                permissions.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
                return permissions;
        }
}
