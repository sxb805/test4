package com.vortex.cloud.test.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vortex.cloud.test.domain.TaskWorkItem;
import com.vortex.cloud.test.dto.TaskWorkItemDTO;
import com.vortex.cloud.test.dto.TaskWorkItemQueryDTO;
import com.vortex.cloud.test.dto.TaskWorkItemVO;
import com.vortex.cloud.test.dto.TaskWorkItemWeeklyOccupancyVO;
import com.vortex.cloud.vfs.lite.base.dto.DataStoreDTO;
import com.vortex.cloud.vfs.lite.base.dto.RestResultDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * 任务工单服务
 */
public interface TaskWorkItemService extends IService<TaskWorkItem> {

    DataStoreDTO<TaskWorkItemVO> page(Pageable pageable, TaskWorkItemQueryDTO queryDTO);

    List<TaskWorkItemVO> list(Sort sort, TaskWorkItemQueryDTO queryDTO);

    void save(TaskWorkItemDTO dto);

    void update(TaskWorkItemDTO dto);

    void delete(Set<String> ids);

    TaskWorkItemVO get(String id);

    TaskWorkItemWeeklyOccupancyVO weeklyOccupancy(TaskWorkItemQueryDTO queryDTO);

    TaskWorkItemWeeklyOccupancyVO projectWeeklyOccupancy(TaskWorkItemQueryDTO queryDTO);

    RestResultDTO<?> importExcel(String tenantId, MultipartFile file, Integer startRowNum, Integer startCellNum) throws Exception;
}
