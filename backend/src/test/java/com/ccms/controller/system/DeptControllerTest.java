package com.ccms.controller.system;

import com.ccms.controller.ControllerTestBase;
import com.ccms.entity.system.dept.SysDept;
import com.ccms.repository.system.dept.SysDeptRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.*;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 部门管理控制器单元测试
 */
@WebMvcTest(DeptController.class)
class DeptControllerTest extends ControllerTestBase {

    @MockBean
    private SysDeptRepository sysDeptRepository;

    private SysDept createTestDept(Long id, String deptCode, String deptName, Long parentId, Integer status) {
        SysDept dept = new SysDept();
        dept.setId(id);
        dept.setDeptCode(deptCode);
        dept.setDeptName(deptName);
        dept.setParentId(parentId);
        dept.setStatus(status);
        dept.setSortOrder(1);
        return dept;
    }

    @Test
    void shouldReturnDeptListWhenQuerySuccess() throws Exception {
        // given
        SysDept dept = createTestDept(1L, "TECH", "技术部", 0L, 1);
        when(sysDeptRepository.findAll()).thenReturn(Collections.singletonList(dept));

        // when & then
        performGet("/api/system/depts")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].deptCode").value("TECH"));
    }

    @Test
    void shouldReturnDeptTree() throws Exception {
        // given
        SysDept dept = createTestDept(1L, "TECH", "技术部", 0L, 1);
        when(sysDeptRepository.findByStatus(1)).thenReturn(Collections.singletonList(dept));

        // when & then
        performGet("/api/system/depts/tree")
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnDeptWhenGetByIdSuccess() throws Exception {
        // given
        SysDept dept = createTestDept(1L, "TECH", "技术部", 0L, 1);
        when(sysDeptRepository.findById(1L)).thenReturn(Optional.of(dept));

        // when & then
        performGet("/api/system/depts/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.deptCode").value("TECH"));
    }

    @Test
    void shouldReturnNotFoundWhenGetByIdNotExist() throws Exception {
        // given
        when(sysDeptRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        performGet("/api/system/depts/999")
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnDeptWhenGetByCodeSuccess() throws Exception {
        // given
        SysDept dept = createTestDept(1L, "TECH", "技术部", 0L, 1);
        when(sysDeptRepository.findByDeptCode("TECH")).thenReturn(Optional.of(dept));

        // when & then
        performGet("/api/system/depts/code/TECH")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deptCode").value("TECH"));
    }

    @Test
    void shouldCreateDeptSuccess() throws Exception {
        // given
        SysDept dept = createTestDept(1L, "HR", "人事部", 0L, 1);
        when(sysDeptRepository.findByDeptCode("HR")).thenReturn(Optional.empty());
        when(sysDeptRepository.save(any(SysDept.class))).thenReturn(dept);

        // when & then
        performPost("/api/system/depts", dept)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("部门创建成功"));
    }

    @Test
    void shouldReturnBadRequestWhenCreateDeptWithDuplicateCode() throws Exception {
        // given
        SysDept dept = createTestDept(1L, "TECH", "技术部", 0L, 1);
        when(sysDeptRepository.findByDeptCode("TECH")).thenReturn(Optional.of(dept));

        // when & then
        performPost("/api/system/depts", dept)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("部门编码已存在"));
    }

    @Test
    void shouldUpdateDeptSuccess() throws Exception {
        // given
        SysDept existingDept = createTestDept(1L, "TECH", "技术部", 0L, 1);
        SysDept updatedDept = createTestDept(1L, "TECH", "技术研发部", 0L, 1);
        when(sysDeptRepository.findById(1L)).thenReturn(Optional.of(existingDept));
        when(sysDeptRepository.findByDeptCode("TECH")).thenReturn(Optional.of(existingDept));
        when(sysDeptRepository.save(any(SysDept.class))).thenReturn(updatedDept);

        // when & then
        performPut("/api/system/depts/1", updatedDept)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("部门更新成功"));
    }

    @Test
    void shouldDeleteDeptSuccess() throws Exception {
        // given
        SysDept dept = createTestDept(1L, "TEMP", "临时部门", 0L, 1);
        when(sysDeptRepository.findById(1L)).thenReturn(Optional.of(dept));
        when(sysDeptRepository.findByParentId(1L)).thenReturn(Collections.emptyList());

        // when & then
        performDelete("/api/system/depts/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("部门删除成功"));
    }

    @Test
    void shouldReturnBadRequestWhenDeleteDeptWithChildren() throws Exception {
        // given
        SysDept dept = createTestDept(1L, "TECH", "技术部", 0L, 1);
        SysDept childDept = createTestDept(2L, "DEV", "开发组", 1L, 1);
        when(sysDeptRepository.findById(1L)).thenReturn(Optional.of(dept));
        when(sysDeptRepository.findByParentId(1L)).thenReturn(Collections.singletonList(childDept));

        // when & then
        performDelete("/api/system/depts/1")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("该部门下存在子部门，无法删除"));
    }

    @Test
    void shouldReturnChildDepts() throws Exception {
        // given
        SysDept childDept = createTestDept(2L, "DEV", "开发组", 1L, 1);
        when(sysDeptRepository.findByParentId(1L)).thenReturn(Collections.singletonList(childDept));

        // when & then
        performGet("/api/system/depts/1/children")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].deptCode").value("DEV"));
    }

    @Test
    void shouldUpdateDeptStatusSuccess() throws Exception {
        // given
        SysDept dept = createTestDept(1L, "TECH", "技术部", 0L, 0);
        when(sysDeptRepository.findById(1L)).thenReturn(Optional.of(dept));
        when(sysDeptRepository.save(any(SysDept.class))).thenReturn(dept);

        // when & then
        performPut("/api/system/depts/1/status?status=1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("部门已启用"));
    }
}

