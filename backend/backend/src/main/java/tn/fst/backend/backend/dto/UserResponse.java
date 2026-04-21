package tn.fst.backend.backend.dto;

import lombok.Builder;
import lombok.Data;
import tn.fst.backend.backend.entity.Role;

import java.time.LocalDate;

@Data
@Builder
public class UserResponse {
    private Long userId;
    private String username;
    private String fullname;
    private String email;
    private String phoneNumber;
    private Role role;
    private Boolean isActive;
    private LocalDate createdAt;
}