package tn.fst.backend.backend.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;
import tn.fst.backend.backend.entity.Role;

@Data
public class UserUpdateRequest {
    private String fullname;

    @Email(message = "Email should be valid")
    private String email;

    private String phoneNumber;
    private Role role;
    private Boolean isActive;
}