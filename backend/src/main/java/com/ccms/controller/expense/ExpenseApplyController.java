package com.ccms.controller.expense;

import com.ccms.entity.expense.ExpenseApplyMain;
import com.ccms.repository.expense.ExpenseApplyMainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 费用申请控制器
 * 对应设计文档：4.4 费用申请相关表
 */
@RestController
@RequestMapping("/api/expense/apply")
public class ExpenseApplyController {

    private final ExpenseApplyMainRepository applyMainRepository;

    @Autowired
    public ExpenseApplyController(ExpenseApplyMainRepository applyMainRepository) {
        this.applyMainRepository = applyMainRepository;
    }

    /**
     * 获取申请单列表（分页）
     */
    @GetMapping
    public ResponseEntity<Page<ExpenseApplyMain>> getApplyList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status) {
        
        List<ExpenseApplyMain> list;
        if (userId != null && status != null) {
            list = applyMainRepository.findByApplyUserIdAndStatus(userId, status);
        } else if (userId != null) {
            list = applyMainRepository.findByApplyUserId(userId);
        } else if (status != null) {
            list = applyMainRepository.findByStatus(status);
        } else {
            return ResponseEntity.ok(applyMainRepository.findAll(PageRequest.of(page, size)));
        }
        
        // 手动分页
        int start = Math.min(page * size, list.size());
        int end = Math.min(start + size, list.size());
        Page<ExpenseApplyMain> applyPage = new PageImpl<>(
                list.subList(start, end),
                PageRequest.of(page, size),
                list.size()
        );
        return ResponseEntity.ok(applyPage);
    }

    /**
     * 根据ID获取申请单
     */
    @GetMapping("/{applyId}")
    public ResponseEntity<ExpenseApplyMain> getApplyById(@PathVariable Long applyId) {
        return applyMainRepository.findById(applyId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据申请单号获取申请单
     */
    @GetMapping("/no/{applyNo}")
    public ResponseEntity<ExpenseApplyMain> getApplyByNo(@PathVariable String applyNo) {
        return applyMainRepository.findByApplyNo(applyNo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 创建申请单
     */
    @PostMapping
    public ResponseEntity<ExpenseApplyMain> createApply(@RequestBody ExpenseApplyMain apply) {
        ExpenseApplyMain saved = applyMainRepository.save(apply);
        return ResponseEntity.ok(saved);
    }

    /**
     * 更新申请单
     */
    @PutMapping("/{applyId}")
    public ResponseEntity<ExpenseApplyMain> updateApply(@PathVariable Long applyId, @RequestBody ExpenseApplyMain apply) {
        apply.setId(applyId);
        ExpenseApplyMain updated = applyMainRepository.save(apply);
        return ResponseEntity.ok(updated);
    }

    /**
     * 删除申请单
     */
    @DeleteMapping("/{applyId}")
    public ResponseEntity<Void> deleteApply(@PathVariable Long applyId) {
        applyMainRepository.deleteById(applyId);
        return ResponseEntity.ok().build();
    }

    /**
     * 提交申请单（启动审批流程）
     */
    @PostMapping("/{applyId}/submit")
    public ResponseEntity<Map<String, Object>> submitApply(@PathVariable Long applyId) {
        ExpenseApplyMain apply = applyMainRepository.findById(applyId).orElse(null);
        if (apply == null) {
            return ResponseEntity.notFound().build();
        }
        // 更新状态为审批中
        apply.setStatus(1);
        applyMainRepository.save(apply);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "申请单已提交");
        return ResponseEntity.ok(result);
    }

    /**
     * 撤回申请单
     */
    @PostMapping("/{applyId}/withdraw")
    public ResponseEntity<Map<String, Object>> withdrawApply(@PathVariable Long applyId) {
        ExpenseApplyMain apply = applyMainRepository.findById(applyId).orElse(null);
        if (apply == null) {
            return ResponseEntity.notFound().build();
        }
        // 更新状态为草稿
        apply.setStatus(0);
        applyMainRepository.save(apply);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "申请单已撤回");
        return ResponseEntity.ok(result);
    }

    /**
     * 获取用户的申请单列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ExpenseApplyMain>> getUserApplies(@PathVariable Long userId) {
        List<ExpenseApplyMain> applies = applyMainRepository.findByApplyUserIdOrderByCreateTimeDesc(userId);
        return ResponseEntity.ok(applies);
    }

    /**
     * 获取部门的申请单列表
     */
    @GetMapping("/dept/{deptId}")
    public ResponseEntity<List<ExpenseApplyMain>> getDeptApplies(@PathVariable Long deptId) {
        List<ExpenseApplyMain> applies = applyMainRepository.findByApplyDeptIdOrderByCreateTimeDesc(deptId);
        return ResponseEntity.ok(applies);
    }

    /**
     * 获取待审批的申请单列表
     */
    @GetMapping("/pending")
    public ResponseEntity<Page<ExpenseApplyMain>> getPendingApplies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<ExpenseApplyMain> list = applyMainRepository.findByStatus(1);
        
        // 手动分页
        int start = Math.min(page * size, list.size());
        int end = Math.min(start + size, list.size());
        Page<ExpenseApplyMain> applies = new PageImpl<>(
                list.subList(start, end),
                PageRequest.of(page, size),
                list.size()
        );
        return ResponseEntity.ok(applies);
    }

    /**
     * 统计申请单数量
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getApplyStatistics(
            @RequestParam(required = false) Long userId) {
        Map<String, Object> stats = new HashMap<>();
        if (userId != null) {
            stats.put("total", applyMainRepository.countByApplyUserId(userId));
            stats.put("draft", applyMainRepository.countByApplyUserIdAndStatus(userId, 0));
            stats.put("pending", applyMainRepository.countByApplyUserIdAndStatus(userId, 1));
            stats.put("approved", applyMainRepository.countByApplyUserIdAndStatus(userId, 2));
            stats.put("rejected", applyMainRepository.countByApplyUserIdAndStatus(userId, 3));
        } else {
            stats.put("total", applyMainRepository.count());
        }
        return ResponseEntity.ok(stats);
    }
}
