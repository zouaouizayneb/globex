package tn.fst.backend.backend.service;

import tn.fst.backend.backend.dto.UserResponse;
import tn.fst.backend.backend.dto.UserUpdateRequest;
import tn.fst.backend.backend.entity.User;

import java.util.List;

public interface UserService {

    List<UserResponse> getAllUsers();

    UserResponse getUserById(Long id);

    UserResponse createUser(User user, String rawPassword);

    UserResponse updateUser(Long id, UserUpdateRequest request);

    void deleteUser(Long id);

    UserResponse toResponse(User user);

    int syncClients();
}
