package com.nelani.recipe_search_backend.service;

import com.nelani.recipe_search_backend.response.LikeResponse;

import java.util.List;

public interface RecipeLikeService {

    long getRecipeLikes(String publicId);

    LikeResponse addRecipeLike(String publicId, int page, int size);

    void removeRecipeLike(String publicId);

    List<String> getUserLikes();

    boolean fallbackUserLikedCheck(String publicId);

}
