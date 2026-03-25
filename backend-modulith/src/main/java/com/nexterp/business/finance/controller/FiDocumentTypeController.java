package com.nexterp.business.finance.controller;

import com.nexterp.business.finance.application.service.FiDocumentTypeService;
import com.nexterp.business.finance.domain.model.FiDocumentType;
import com.nexterp.shared.core.result.PageResult;
import com.nexterp.shared.core.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 凭证类型控制器
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/finance/document-types")
@RequiredArgsConstructor
public class FiDocumentTypeController {

    private final FiDocumentTypeService documentTypeService;

    /**
     * 创建凭证类型
     */
    @PostMapping
    @PreAuthorize("hasAuthority('finance:document-type:add')")
    public Result<Long> createDocumentType(@Valid @RequestBody FiDocumentType documentType) {
        Long id = documentTypeService.createDocumentType(documentType);
        return Result.success(id);
    }

    /**
     * 更新凭证类型
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('finance:document-type:edit')")
    public Result<FiDocumentType> updateDocumentType(
            @PathVariable Long id,
            @Valid @RequestBody FiDocumentType documentType) {
        FiDocumentType updated = documentTypeService.updateDocumentType(id, documentType);
        return Result.success(updated);
    }

    /**
     * 删除凭证类型
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('finance:document-type:delete')")
    public Result<Void> deleteDocumentType(@PathVariable Long id) {
        documentTypeService.deleteDocumentType(id);
        return Result.success();
    }

    /**
     * 获取凭证类型详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('finance:document-type:view')")
    public Result<FiDocumentType> getDocumentTypeById(@PathVariable Long id) {
        FiDocumentType documentType = documentTypeService.getDocumentTypeById(id);
        return Result.success(documentType);
    }

    /**
     * 根据代码获取
     */
    @GetMapping("/code/{docTypeCode}")
    @PreAuthorize("hasAuthority('finance:document-type:view')")
    public Result<FiDocumentType> getDocumentTypeByCode(
            @PathVariable String docTypeCode,
            @RequestParam Long tenantId) {
        FiDocumentType documentType = documentTypeService.getDocumentTypeByCode(docTypeCode, tenantId);
        return Result.success(documentType);
    }

    /**
     * 获取所有启用的凭证类型
     */
    @GetMapping("/enabled")
    @PreAuthorize("hasAuthority('finance:document-type:view')")
    public Result<List<FiDocumentType>> listEnabledDocumentTypes(@RequestParam Long tenantId) {
        List<FiDocumentType> list = documentTypeService.listEnabledDocumentTypes(tenantId);
        return Result.success(list);
    }

    /**
     * 根据分类查询
     */
    @GetMapping("/by-class/{docTypeClass}")
    @PreAuthorize("hasAuthority('finance:document-type:view')")
    public Result<List<FiDocumentType>> listByClass(
            @PathVariable String docTypeClass,
            @RequestParam Long tenantId) {
        List<FiDocumentType> list = documentTypeService.listByClass(docTypeClass, tenantId);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @PostMapping("/page")
    @PreAuthorize("hasAuthority('finance:document-type:view')")
    public Result<PageResult<FiDocumentType>> listDocumentTypes(
            @RequestParam Long tenantId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<FiDocumentType> page = documentTypeService.listDocumentTypes(tenantId, PageRequest.of(current - 1, size));

        PageResult<FiDocumentType> result = PageResult.<FiDocumentType>builder()
                .records(page.getContent())
                .total(page.getTotalElements())
                .current(page.getNumber() + 1)
                .size(page.getSize())
                .build();

        return Result.success(result);
    }
}
