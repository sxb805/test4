package com.vortex.cloud.test.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vortex.cloud.test.domain.Example;
import com.vortex.cloud.test.dto.ExampleDTO;
import com.vortex.cloud.test.dto.ExampleQueryDTO;
import com.vortex.cloud.test.dto.ExampleVO;
import com.vortex.cloud.vfs.lite.base.dto.DataStoreDTO;
import com.vortex.cloud.vfs.lite.base.dto.RestResultDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * @author zhanglei
 */
public interface ExampleService extends IService<Example> {
    /**
     * 分页
     *
     * @param pageable
     * @param queryDTO
     * @return
     */
    DataStoreDTO<ExampleVO> page(Pageable pageable, ExampleQueryDTO queryDTO);

    /**
     * 列表
     *
     * @param sort
     * @param queryDTO
     * @return
     */
    List<ExampleVO> list(Sort sort, ExampleQueryDTO queryDTO);

    /**
     * 保存
     *
     * @param dto
     */
    void save(ExampleDTO dto);

    /**
     * 修改
     *
     * @param dto
     */
    void update(ExampleDTO dto);

    /**
     * 删除
     *
     * @param ids
     */
    void delete(Set<String> ids);

    /**
     * 获取
     *
     * @param id
     * @return
     */
    ExampleVO get(String id);

    /**
     * 校验
     *
     * @param tenantId
     * @param id
     * @param key
     * @param value
     * @return
     */
    Boolean exist(String tenantId, String id, String key, String value);

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
