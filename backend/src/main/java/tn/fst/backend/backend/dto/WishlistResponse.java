package tn.fst.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistResponse {
    private Long wishlistId;
    private Long userId;
    private List<WishlistItemResponse> items;
    private Integer totalItems;
    private String createdAt;
    private String updatedAt;
}
