package tn.fst.backend.backend.dto;

import lombok.Builder;
import lombok.Data;
import tn.fst.backend.backend.entity.Role;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private Long userId;
    private String username;
    private String email;
    private String fullname;
    private Role role;
    private Boolean emailVerified;
}
