package com.vortex.cloud.test.service;

import com.vortex.cloud.test.domain.ExampleRecord;
import com.vortex.cloud.test.dto.*;
import com.vortex.cloud.test.service.base.ISubService;
import com.vortex.cloud.vfs.lite.base.dto.DataStoreDTO;
import com.vortex.cloud.vfs.lite.base.dto.RestResultDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;


/**
 * @author donghao
 * @date 2025/4/22 11:05
 */
public interface ExampleRecordService  extends ISubService<ExampleRecord> {
    /**
     * 分页
     *
     * @param pageable
     * @param queryDTO
     * @return
     */
    DataStoreDTO<ExampleRecordVO> page(Pageable pageable, ExampleRecordQueryDTO queryDTO);

    /**
     * 列表
     *
     * @param sort
     * @param queryDTO
     * @return
     */
    List<ExampleRecordVO> list(Sort sort, ExampleRecordQueryDTO queryDTO);

    /**
     * 保存
     *
     * @param dto
     */
    void save(ExampleRecordDTO dto);

    /**
     * 修改
     *
     * @param dto
     */
    void update(ExampleRecordDTO dto);

    /**
     * 删除
     *
     * @param queryDTO
     */
    void delete(ExampleRecordQueryDTO queryDTO);

    /**
     * 获取
     * @param dto
     * @return
     */
    ExampleRecordVO get(ExampleRecordQueryDTO dto);

    /**
     * 校验
     *
     * @param shardingKey(分表key，目前支持Date,String)
     * @param tenantId
     * @param id
     * @param key
     * @param value
     * @return
     */
    Boolean exist(Object shardingKey,String tenantId, String id, String key, String value);

    /**
     * 导入
     *
     * @param tenantId
     * @param file
     * @param startRowNum
     * @param startCellNum
     * @return
     */
    RestResultDTO<?> importExcel(String tenantId, MultipartFile file, Integer startRowNum, Integer startCellNum) throws Exception;
}
