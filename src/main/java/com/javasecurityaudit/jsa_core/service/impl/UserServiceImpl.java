package com.javasecurityaudit.jsa_core.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.javasecurityaudit.jsa_core.document.UserDocument;
import com.javasecurityaudit.jsa_core.dto.request.AdminUpdateUserRequest;
import com.javasecurityaudit.jsa_core.dto.request.ChangePasswordRequest;
import com.javasecurityaudit.jsa_core.dto.request.CreateUserRequest;
import com.javasecurityaudit.jsa_core.dto.request.UpdateMyInfoRequest;
import com.javasecurityaudit.jsa_core.dto.request.UpdateUserStatusRequest;
import com.javasecurityaudit.jsa_core.dto.response.PageResponse;
import com.javasecurityaudit.jsa_core.dto.response.UserResponse;
import com.javasecurityaudit.jsa_core.entity.Role;
import com.javasecurityaudit.jsa_core.entity.User;
import com.javasecurityaudit.jsa_core.enums.RoleType;
import com.javasecurityaudit.jsa_core.exception.AppException;
import com.javasecurityaudit.jsa_core.exception.ErrorCode;
import com.javasecurityaudit.jsa_core.mapper.UserMapper;
import com.javasecurityaudit.jsa_core.repository.JPA.RoleRepository;
import com.javasecurityaudit.jsa_core.repository.JPA.UserRepository;
import com.javasecurityaudit.jsa_core.repository.elasticsearch.UserElasticsearchRepository;
import com.javasecurityaudit.jsa_core.service.UserService;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    UserElasticsearchRepository userElasticsearchRepository;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;
    UserMapper userMapper;

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Role defaultRole = roleRepository.findByName(RoleType.ROLE_USER.name())
                .orElseGet(() -> roleRepository.save(
                        Role.builder()
                                .name(RoleType.ROLE_USER.name())
                                .description("Mặc định cho người dùng hệ thống")
                                .build()));

        Set<Role> roles = new HashSet<>();
        roles.add(defaultRole);
        user.setRoles(roles);

        user = userRepository.save(user);
        try {
            UserDocument userDocument = userMapper.toUserDocument(user);
            userElasticsearchRepository.save(userDocument);
        } catch (Exception e) {
            log.error("Lỗi đồng bộ Elasticsearch: {}", e.getMessage());
        }
        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateMyInfo(UpdateMyInfoRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (!request.getEmail().equalsIgnoreCase(user.getEmail()) 
                    && userRepository.existsByEmail(request.getEmail())) {
                throw new AppException(ErrorCode.EMAIL_EXISTED);
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        User updatedUser = userRepository.save(user);
        try {
            UserDocument userDocument = userMapper.toUserDocument(user);
            userElasticsearchRepository.save(userDocument);
        } catch (Exception e) {
            log.error("Lỗi đồng bộ Elasticsearch: {}", e.getMessage());
        }
        return userMapper.toUserResponse(updatedUser);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_PASSWORD); // Kiểm tra lại enum ErrorCode của bạn
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        try {
            UserDocument userDocument = userMapper.toUserDocument(user);
            userElasticsearchRepository.save(userDocument);
        } catch (Exception e) {
            log.error("Lỗi đồng bộ Elasticsearch: {}", e.getMessage());
        }
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse getUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse adminUpdateUser(String userId, AdminUpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getEnabled() != null) user.setEnabled(request.getEnabled());
        if (request.getAccountNonLocked() != null) user.setAccountNonLocked(request.getAccountNonLocked());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            Set<Role> roles = request.getRoles().stream()
                    .map(roleName -> roleRepository.findByName(roleName)
                            .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED)))
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }

        User updatedUser = userRepository.save(user);
        try {
            UserDocument userDocument = userMapper.toUserDocument(user);
            userElasticsearchRepository.save(userDocument);
        } catch (Exception e) {
            log.error("Lỗi đồng bộ Elasticsearch: {}", e.getMessage());
        }
        return userMapper.toUserResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Lựa chọn: Ngăn Admin tự xóa chính mình (nếu muốn)
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (user.getUsername().equals(currentUsername)) {
            throw new AppException(ErrorCode.CANNOT_DELETE_YOURSELF); // Tạo mã lỗi mới trong ErrorCode nếu cần
        }

        userRepository.delete(user);
        try {
            UserDocument userDocument = userMapper.toUserDocument(user);
            userElasticsearchRepository.save(userDocument);
        } catch (Exception e) {
            log.error("Lỗi đồng bộ Elasticsearch: {}", e.getMessage());
        }
    }

    @Override
    public UserResponse getMyInfo() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUserStatus(String userId, UpdateUserStatusRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        if (request.getAccountNonLocked() != null) {
            user.setAccountNonLocked(request.getAccountNonLocked());
        }
        user = userRepository.save(user);
        try {
            UserDocument userDocument = userMapper.toUserDocument(user);
            userElasticsearchRepository.save(userDocument);
        } catch (Exception e) {
            log.error("Lỗi đồng bộ Elasticsearch: {}", e.getMessage());
        }
        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
        public PageResponse<UserResponse> search(String keyword, int page, int size) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        if (normalizedKeyword.isBlank()) {
            return PageResponse.<UserResponse>builder()
                .content(List.of())
                .page(page)
                .size(size)
                .totalElements(0)
                .totalPages(0)
                .build();
        }
        Page<UserDocument> searchPage = userElasticsearchRepository.search(normalizedKeyword, PageRequest.of(page, size));
        List<String> ids = searchPage.getContent().stream().map(UserDocument::getId).toList();
        Map<String, User> usersById = userRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        List<UserResponse> content = ids.stream().map(usersById::get).filter(user -> user != null)
            .map(userMapper::toUserResponse).toList();
        return PageResponse.<UserResponse>builder()
            .content(content)
            .page(page)
            .size(size)
            .totalElements(searchPage.getTotalElements())
            .totalPages(searchPage.getTotalPages())
            .build();
    }
}