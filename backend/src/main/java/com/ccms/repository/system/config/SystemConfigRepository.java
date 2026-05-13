package com.ccms.repository.system.config;

import com.ccms.entity.system.config.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 系统配置数据访问接口
 */
@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long> {

    /**
     * 根据配置键查找配置
     */
    Optional<SystemConfig> findByConfigKey(String configKey);

    /**
     * 根据配置类型查找启用的配置
     */
    List<SystemConfig> findByConfigTypeAndEnabledTrueOrderByCreateTimeDesc(SystemConfig.ConfigType configType);

    /**
     * 查找所有启用的配置
     */
    List<SystemConfig> findByEnabledTrueOrderByConfigTypeAscCreateTimeDesc();

    /**
     * 检查配置键是否已存在（忽略指定ID）
     */
    @Query("SELECT COUNT(c) > 0 FROM SystemConfig c WHERE c.configKey = ?1 AND c.id != ?2")
    boolean existsByConfigKeyAndIdNot(String configKey, Long id);

    /**
     * 检查配置键是否已存在
     */
    boolean existsByConfigKey(String configKey);

    /**
     * 根据配置类型查找配置
     */
    List<SystemConfig> findByConfigTypeOrderByConfigKeyAsc(SystemConfig.ConfigType configType);

    /**
     * 根据配置键前缀查找配置
     */
    List<SystemConfig> findByConfigKeyStartingWithAndEnabledTrue(String prefix);
}