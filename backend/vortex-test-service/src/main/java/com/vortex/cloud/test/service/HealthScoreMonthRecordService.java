package com.vortex.cloud.test.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vortex.cloud.test.domain.HealthScoreMonthRecord;
import com.vortex.cloud.test.dto.HealthScoreMonthRecordDTO;
import com.vortex.cloud.test.dto.HealthScoreMonthRecordQueryDTO;
import com.vortex.cloud.test.dto.HealthScoreMonthRecordVO;
import com.vortex.cloud.test.service.base.ISubService;
import com.vortex.cloud.vfs.lite.base.dto.DataStoreDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;

/**
 * @author donghao
 * @date 2025/4/22 11:05
 */
public interface HealthScoreMonthRecordService extends ISubService<HealthScoreMonthRecord> {

    /**
     * 分页
     *
     * @param pageable
     * @param queryDTO
     * @return
     */
    DataStoreDTO<HealthScoreMonthRecordVO> page(Pageable pageable, HealthScoreMonthRecordQueryDTO queryDTO);

    Map<String, Double> lengthSummary(HealthScoreMonthRecordQueryDTO queryDTO);

    List<HealthScoreMonthRecordVO> list(Pageable pageable, HealthScoreMonthRecordQueryDTO queryDTO);

    HealthScoreMonthRecordVO selectOne(HealthScoreMonthRecordQueryDTO queryDTO);

    boolean saveBatch(List<HealthScoreMonthRecordDTO> dtoList);

    /**
     * 物理删除，仅删除subTableValue定位的分表中符合条件的数据
     *
     * @param subTableValue
     * @param queryWrapper
     * @return
     */
    boolean physicalDelete(Object subTableValue, QueryWrapper<HealthScoreMonthRecord> queryWrapper);


    void delete(HealthScoreMonthRecordQueryDTO queryDTO);


}
