package com.nexterp.platform.auth.service;

import com.nexterp.platform.auth.domain.model.SysUser;
import com.nexterp.platform.auth.domain.repository.SysUserRepository;
import com.nexterp.shared.core.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * UserService 单元测试
 *
 * @author NextERP
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户服务单元测试")
@ContextConfiguration(classes = TestConfig.class)
class UserServiceTest {

    @Mock
    private SysUserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private SysUser testUser;
    private final Long tenantId = 1L;

    @BeforeEach
    void setUp() {
        testUser = SysUser.builder()
                .id(1L)
                .tenantId(tenantId)
                .username("testuser")
                .password("encoded_password")
                .realName("测试用户")
                .email("test@example.com")
                .phone("13800138000")
                .status(1)
                .build();
    }

    @Test
    @DisplayName("创建用户 - 成功")
    void createUser_Success() {
        // Given
        when(userRepository.existsByUsernameAndTenantIdAndIsDeletedFalse(
                anyString(), anyLong())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userRepository.save(any(SysUser.class))).thenReturn(testUser);

        // When
        Long userId = userService.createUser(createUserRequest());

        // Then
        assertThat(userId).isEqualTo(1L);
        verify(userRepository).save(any(SysUser.class));
    }

    @Test
    @DisplayName("创建用户 - 用户名已存在")
    void createUser_UsernameExists() {
        // Given
        when(userRepository.existsByUsernameAndTenantIdAndIsDeletedFalse(
                anyString(), anyLong())).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> userService.createUser(createUserRequest()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户名已存在");
    }

    @Test
    @DisplayName("更新用户 - 成功")
    void updateUser_Success() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmailAndTenantIdAndIsDeletedFalseAndIdNot(
                anyString(), anyLong(), anyLong())).thenReturn(false);
        when(userRepository.save(any(SysUser.class))).thenReturn(testUser);

        // When
        SysUser updatedUser = userService.updateUser(userId, createUserUpdateRequest());

        // Then
        assertThat(updatedUser).isNotNull();
        verify(userRepository).save(any(SysUser.class));
    }

    @Test
    @DisplayName("删除用户 - 成功")
    void deleteUser_Success() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(SysUser.class))).thenReturn(testUser);

        // When
        userService.deleteUser(userId);

        // Then
        verify(userRepository).save(any(SysUser.class));
    }

    @Test
    @DisplayName("根据ID获取用户 - 成功")
    void getUserById_Success() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        SysUser user = userService.getUserById(userId);

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("根据ID获取用户 - 用户不存在")
    void getUserById_NotFound() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户不存在");
    }

    private UserCreateRequest createUserRequest() {
        UserCreateRequest request = new UserCreateRequest();
        request.setTenantId(tenantId);
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setRealName("测试用户");
        request.setEmail("test@example.com");
        request.setPhone("13800138000");
        return request;
    }

    private UserUpdateRequest createUserUpdateRequest() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setRealName("测试用户2");
        request.setEmail("test2@example.com");
        return request;
    }
}
