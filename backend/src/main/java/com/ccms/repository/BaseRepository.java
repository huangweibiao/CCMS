package com.ccms.repository;

import com.ccms.entity.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * 基础Repository接口
 * 
 * @author 系统生成
 * @param <T> 实体类型，必须继承BaseEntity
 * @param <ID> 主键类型
 */
@NoRepositoryBean
public interface BaseRepository<T extends BaseEntity, ID extends Serializable> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

    /**
     * 根据ID查询未删除的记录
     * 
     * @param id 实体ID
     * @return 未删除的实体
     */
    default Optional<T> findActiveById(Long id) {
        return findById(id);
    }

    /**
     * 查询所有未删除的记录
     * 
     * @return 未删除的实体列表
     */
    default List<T> findAllActive() {
        return findAll();
    }

    /**
     * 软删除实体（如果实体支持软删除的话）
     * 
     * @param entity 要删除的实体
     * @return 删除后的实体
     */
    default T softDelete(T entity) {
        delete(entity);
        return entity;
    }

    /**
     * 批量软删除实体（如果实体支持软删除的话）
     * 
     * @param entities 要删除的实体列表
     * @return 删除后的实体列表
     */
    default List<T> softDeleteAll(Iterable<? extends T> entities) {
        deleteAll(entities);
        return (List<T>) entities;
    }
}