package com.ccms.controller.system;

import com.ccms.entity.system.dept.SysDept;
import com.ccms.repository.system.dept.SysDeptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 部门管理控制器
 * 对应设计文档：4.1.2 部门表
 */
@RestController
@RequestMapping("/api/system/depts")
public class DeptController {

    private final SysDeptRepository sysDeptRepository;

    @Autowired
    public DeptController(SysDeptRepository sysDeptRepository) {
        this.sysDeptRepository = sysDeptRepository;
    }

    /**
     * 获取部门列表（分页）
     */
    @GetMapping
    public ResponseEntity<Page<SysDept>> getDeptList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String deptName,
            @RequestParam(required = false) Integer status) {
        
        List<SysDept> allDepts = sysDeptRepository.findAll();
        
        // 根据条件过滤
        List<SysDept> filtered = allDepts.stream()
                .filter(d -> deptName == null || d.getDeptName().contains(deptName))
                .filter(d -> status == null || status.equals(d.getStatus()))
                .sorted(Comparator.comparing(SysDept::getSortOrder, Comparator.nullsLast(Integer::compareTo)))
                .toList();
        
        // 手动分页
        int start = Math.min(page * size, filtered.size());
        int end = Math.min(start + size, filtered.size());
        Page<SysDept> deptPage = new PageImpl<>(
                filtered.subList(start, end),
                PageRequest.of(page, size),
                filtered.size()
        );
        
        return ResponseEntity.ok(deptPage);
    }

    /**
     * 获取部门树形结构
     */
    @GetMapping("/tree")
    public ResponseEntity<List<Map<String, Object>>> getDeptTree() {
        List<SysDept> allDepts = sysDeptRepository.findByStatus(1);
        List<Map<String, Object>> tree = buildDeptTree(allDepts, 0L);
        return ResponseEntity.ok(tree);
    }

    /**
     * 根据ID获取部门
     */
    @GetMapping("/{deptId}")
    public ResponseEntity<SysDept> getDeptById(@PathVariable Long deptId) {
        return sysDeptRepository.findById(deptId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据编码获取部门
     */
    @GetMapping("/code/{deptCode}")
    public ResponseEntity<SysDept> getDeptByCode(@PathVariable String deptCode) {
        return sysDeptRepository.findByDeptCode(deptCode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 创建部门
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createDept(@RequestBody SysDept dept) {
        Map<String, Object> result = new HashMap<>();
        
        // 检查编码是否已存在
        if (sysDeptRepository.findByDeptCode(dept.getDeptCode()).isPresent()) {
            result.put("success", false);
            result.put("message", "部门编码已存在");
            return ResponseEntity.badRequest().body(result);
        }
        
        // 设置默认值
        if (dept.getStatus() == null) {
            dept.setStatus(1);
        }
        if (dept.getSortOrder() == null) {
            dept.setSortOrder(0);
        }
        if (dept.getParentId() == null) {
            dept.setParentId(0L);
        }
        
        SysDept saved = sysDeptRepository.save(dept);
        result.put("success", true);
        result.put("message", "部门创建成功");
        result.put("data", saved);
        return ResponseEntity.ok(result);
    }

    /**
     * 更新部门
     */
    @PutMapping("/{deptId}")
    public ResponseEntity<Map<String, Object>> updateDept(@PathVariable Long deptId, @RequestBody SysDept dept) {
        Map<String, Object> result = new HashMap<>();
        
        Optional<SysDept> existingOpt = sysDeptRepository.findById(deptId);
        if (existingOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "部门不存在");
            return ResponseEntity.badRequest().body(result);
        }
        
        // 检查编码是否与其他部门冲突
        Optional<SysDept> deptWithCode = sysDeptRepository.findByDeptCode(dept.getDeptCode());
        if (deptWithCode.isPresent() && !deptWithCode.get().getId().equals(deptId)) {
            result.put("success", false);
            result.put("message", "部门编码已存在");
            return ResponseEntity.badRequest().body(result);
        }
        
        dept.setId(deptId);
        SysDept updated = sysDeptRepository.save(dept);
        result.put("success", true);
        result.put("message", "部门更新成功");
        result.put("data", updated);
        return ResponseEntity.ok(result);
    }

    /**
     * 删除部门
     */
    @DeleteMapping("/{deptId}")
    public ResponseEntity<Map<String, Object>> deleteDept(@PathVariable Long deptId) {
        Map<String, Object> result = new HashMap<>();
        
        Optional<SysDept> existingOpt = sysDeptRepository.findById(deptId);
        if (existingOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "部门不存在");
            return ResponseEntity.badRequest().body(result);
        }
        
        // 检查是否有子部门
        List<SysDept> children = sysDeptRepository.findByParentId(deptId);
        if (!children.isEmpty()) {
            result.put("success", false);
            result.put("message", "该部门下存在子部门，无法删除");
            return ResponseEntity.badRequest().body(result);
        }
        
        sysDeptRepository.deleteById(deptId);
        result.put("success", true);
        result.put("message", "部门删除成功");
        return ResponseEntity.ok(result);
    }

    /**
     * 获取子部门列表
     */
    @GetMapping("/{deptId}/children")
    public ResponseEntity<List<SysDept>> getChildDepts(@PathVariable Long deptId) {
        List<SysDept> children = sysDeptRepository.findByParentId(deptId);
        return ResponseEntity.ok(children);
    }

    /**
     * 启用/禁用部门
     */
    @PutMapping("/{deptId}/status")
    public ResponseEntity<Map<String, Object>> updateDeptStatus(
            @PathVariable Long deptId,
            @RequestParam Integer status) {
        Map<String, Object> result = new HashMap<>();
        
        Optional<SysDept> deptOpt = sysDeptRepository.findById(deptId);
        if (deptOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "部门不存在");
            return ResponseEntity.badRequest().body(result);
        }
        
        SysDept dept = deptOpt.get();
        dept.setStatus(status);
        sysDeptRepository.save(dept);
        
        result.put("success", true);
        result.put("message", status == 1 ? "部门已启用" : "部门已禁用");
        return ResponseEntity.ok(result);
    }

    /**
     * 构建部门树
     */
    private List<Map<String, Object>> buildDeptTree(List<SysDept> allDepts, Long parentId) {
        List<Map<String, Object>> tree = new ArrayList<>();
        
        for (SysDept dept : allDepts) {
            Long deptParentId = dept.getParentId();
            if (deptParentId == null) deptParentId = 0L;
            
            if (deptParentId.equals(parentId)) {
                Map<String, Object> node = new HashMap<>();
                node.put("id", dept.getId());
                node.put("deptCode", dept.getDeptCode());
                node.put("deptName", dept.getDeptName());
                node.put("parentId", dept.getParentId());
                node.put("leaderId", dept.getLeaderId());
                node.put("sortOrder", dept.getSortOrder());
                node.put("status", dept.getStatus());
                node.put("createTime", dept.getCreateTime());
                node.put("children", buildDeptTree(allDepts, dept.getId()));
                tree.add(node);
            }
        }
        
        tree.sort(Comparator.comparing(m -> (Integer) m.getOrDefault("sortOrder", 0)));
        return tree;
    }
}
