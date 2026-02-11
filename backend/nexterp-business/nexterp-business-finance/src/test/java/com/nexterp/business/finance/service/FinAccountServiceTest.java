package com.nexterp.business.finance.service;

import com.nexterp.business.finance.application.dto.request.FinAccountCreateRequest;
import com.nexterp.business.finance.application.dto.request.FinAccountUpdateRequest;
import com.nexterp.business.finance.domain.model.FinAccount;
import com.nexterp.business.finance.domain.repository.FinAccountRepository;
import com.nexterp.shared.core.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 科目服务单元测试
 *
 * @author NextERP
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("科目服务单元测试")
class FinAccountServiceTest {

    @Mock
    private FinAccountRepository accountRepository;

    @InjectMocks
    private FinAccountService accountService;

    private FinAccount testAccount;
    private FinAccountCreateRequest createRequest;
    private FinAccountUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        testAccount = FinAccount.builder()
                .id(1L)
                .tenantId(0L)
                .accountCode("1001")
                .accountName("库存现金")
                .accountType(1)
                .accountDirection(1)
                .parentId(null)
                .level(1)
                .status(1)
                .openingBalance(BigDecimal.ZERO)
                .currentDebit(BigDecimal.ZERO)
                .currentCredit(BigDecimal.ZERO)
                .endingBalance(BigDecimal.ZERO)
                .build();

        createRequest = new FinAccountCreateRequest();
        createRequest.setAccountCode("1002");
        createRequest.setAccountName("银行存款");
        createRequest.setAccountType(1);
        createRequest.setAccountDirection(1);
        createRequest.setLevel(1);
        createRequest.setStatus(1);

        updateRequest = new FinAccountUpdateRequest();
        updateRequest.setAccountName("库存现金-更新");
        updateRequest.setStatus(1);
    }

    @Test
    @DisplayName("创建科目 - 成功")
    void createAccount_Success() {
        when(accountRepository.existsByAccountCodeAndTenantIdAndIsDeletedFalse(anyString(), anyLong()))
                .thenReturn(false);
        when(accountRepository.save(any(FinAccount.class))).thenReturn(testAccount);

        Long accountId = accountService.createAccount(createRequest, 0L);

        assertThat(accountId).isEqualTo(1L);
        verify(accountRepository, times(1)).save(any(FinAccount.class));
    }

    @Test
    @DisplayName("创建科目 - 科目编码已存在")
    void createAccount_CodeExists() {
        when(accountRepository.existsByAccountCodeAndTenantIdAndIsDeletedFalse(anyString(), anyLong()))
                .thenReturn(true);

        assertThatThrownBy(() -> accountService.createAccount(createRequest, 0L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("科目编码已存在");

        verify(accountRepository, never()).save(any(FinAccount.class));
    }

    @Test
    @DisplayName("更新科目 - 成功")
    void updateAccount_Success() {
        when(accountRepository.findByIdAndTenantIdAndIsDeletedFalse(anyLong(), anyLong()))
                .thenReturn(Optional.of(testAccount));
        when(accountRepository.existsByAccountCodeAndTenantIdAndIsDeletedFalseAndIdNot(
                anyString(), anyLong(), anyLong())).thenReturn(false);
        when(accountRepository.save(any(FinAccount.class))).thenReturn(testAccount);

        accountService.updateAccount(1L, updateRequest, 0L);

        verify(accountRepository, times(1)).save(any(FinAccount.class));
    }

    @Test
    @DisplayName("更新科目 - 科目不存在")
    void updateAccount_NotFound() {
        when(accountRepository.findByIdAndTenantIdAndIsDeletedFalse(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.updateAccount(1L, updateRequest, 0L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("科目不存在");

        verify(accountRepository, never()).save(any(FinAccount.class));
    }

    @Test
    @DisplayName("删除科目 - 成功")
    void deleteAccount_Success() {
        when(accountRepository.findByIdAndTenantIdAndIsDeletedFalse(anyLong(), anyLong()))
                .thenReturn(Optional.of(testAccount));
        when(accountRepository.countByParentIdAndTenantIdAndIsDeletedFalse(anyLong(), anyLong()))
                .thenReturn(0L);

        accountService.deleteAccount(1L, 0L);

        verify(accountRepository, times(1)).save(any(FinAccount.class));
    }

    @Test
    @DisplayName("删除科目 - 存在子科目")
    void deleteAccount_HasChildren() {
        when(accountRepository.findByIdAndTenantIdAndIsDeletedFalse(anyLong(), anyLong()))
                .thenReturn(Optional.of(testAccount));
        when(accountRepository.countByParentIdAndTenantIdAndIsDeletedFalse(anyLong(), anyLong()))
                .thenReturn(2L);

        assertThatThrownBy(() -> accountService.deleteAccount(1L, 0L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("存在子科目，无法删除");

        verify(accountRepository, never()).save(any(FinAccount.class));
    }

    @Test
    @DisplayName("获取科目详情 - 成功")
    void getAccountById_Success() {
        when(accountRepository.findByIdAndTenantIdAndIsDeletedFalse(anyLong(), anyLong()))
                .thenReturn(Optional.of(testAccount));

        FinAccount account = accountService.getAccountById(1L, 0L);

        assertThat(account).isNotNull();
        assertThat(account.getAccountCode()).isEqualTo("1001");
        assertThat(account.getAccountName()).isEqualTo("库存现金");
    }

    @Test
    @DisplayName("获取科目详情 - 不存在")
    void getAccountById_NotFound() {
        when(accountRepository.findByIdAndTenantIdAndIsDeletedFalse(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccountById(1L, 0L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("科目不存在");
    }
}
