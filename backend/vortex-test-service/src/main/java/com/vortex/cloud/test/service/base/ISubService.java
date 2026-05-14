package com.vortex.cloud.test.service.base;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * @author donghao
 * @date 2025/4/21 15:08
 */
public interface ISubService<T> {

    /**
     * 插入一条记录（选择字段，策略插入）
     * subTableValue不能为空，回传subTableValue定位分表
     *
     * @param entity 实体对象
     */
    boolean save(Object subTableValue, T entity);

    /**
     * TableId 注解存在更新记录，否插入一条记录
     * subTableValue不能为空，回传subTableValue定位分表
     *
     * @param entity 实体对象
     */
    boolean saveOrUpdate(Object subTableValue, T entity);

    /**
     * 插入（批量）
     * subTableValue不能为空，回传subTableValue定位分表
     *
     * @param entityList 实体对象集合
     */
    boolean saveBatch(Object subTableValue, Collection<T> entityList);

    /**
     * 批量修改插入
     * subTableValue不能为空，回传subTableValue定位分表
     *
     * @param entityList 实体对象集合
     */
    boolean saveOrUpdateBatch(Object subTableValue, Collection<T> entityList);

    /**
     * 根据 ID 选择修改
     * subTableValue不能为空，回传subTableValue定位分表
     *
     * @param entity 实体对象
     */
    boolean updateById(Object subTableValue, T entity);

    /**
     * 根据ID 批量更新
     * subTableValue不能为空，回传subTableValue定位分表
     *
     * @param entityList 实体对象集合
     */
    boolean updateBatchById(Object subTableValue, Collection<T> entityList);

    /**
     * 根据 whereEntity 条件，更新记录
     * subTableValue不能为空，回传subTableValue定位分表
     *
     * @param entity        实体对象(当entity为空时无法进行自动填充)
     * @param updateWrapper 实体对象封装操作类 {@link com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper}
     */
    boolean update(Object subTableValue, T entity, Wrapper<T> updateWrapper);

    /**
     * 删除（根据ID 批量删除）
     * subTableValue不能为空；仅在根据subTableValue定位到的分表进行查询
     *
     * @param list 主键ID
     */
    boolean removeByIds(Object subTableValue, Collection<String> list);

    /**
     * 根据 entity 条件，删除记录
     * subTableValue不能为空；仅在根据subTableValue定位到的分表进行查询
     *
     * @param queryWrapper 实体包装类 {@link com.baomidou.mybatisplus.core.conditions.query.QueryWrapper}
     */
    boolean remove(Object subTableValue, Wrapper<T> queryWrapper);

    /**
     * 根据 ID 查询
     * subTableValue不能为空；仅在根据subTableValue定位到的分表进行查询
     *
     * @param id 主键ID
     */
    T getById(Object subTableValue, Serializable id);

    /**
     * 根据 Wrapper，查询一条记录 <br/>
     * <p>结果集，如果是多个会抛出异常，随机取一条加上限制条件 wrapper.last("LIMIT 1")</p>
     * subTableValue不能为空；仅在根据subTableValue定位到的分表进行查询
     *
     * @param queryWrapper 实体对象封装操作类 {@link com.baomidou.mybatisplus.core.conditions.query.QueryWrapper}
     */
    T getOne(Object subTableValue, Wrapper<T> queryWrapper);

    /**
     * 查询（根据ID 批量查询）
     * subTableValue不能为空；仅在根据subTableValue定位到的分表进行查询
     *
     * @param idList 主键ID列表
     */
    List<T> listByIds(Object subTableValue, Collection<? extends Serializable> idList);

    /**
     * 查询指定条件是否存在数据
     * subTableValue不能为空；仅在根据subTableValue定位到的分表进行查询
     *
     * @see Wrappers#emptyWrapper()
     */
    boolean exists(Object subTableValue, Wrapper<T> queryWrapper);

    /**
     * 查询总记录数
     * subTableValue不能为空；仅在根据subTableValue定位到的分表进行查询
     *
     * @see Wrappers#emptyWrapper()
     */
    long count(Object subTableValue);

    /**
     * 根据 Wrapper 条件，查询总记录数
     * subTableValue不能为空；仅在根据subTableValue定位到的分表进行查询
     *
     * @param queryWrapper 实体对象封装操作类 {@link com.baomidou.mybatisplus.core.conditions.query.QueryWrapper}
     */
    long count(Object subTableValue, Wrapper<T> queryWrapper);

    /**
     * 查询列表
     * subTableValue不能为空；仅在根据subTableValue定位到的分表进行查询
     *
     * @param queryWrapper 实体对象封装操作类 {@link com.baomidou.mybatisplus.core.conditions.query.QueryWrapper}
     */
    List<T> list(Object subTableValue, Wrapper<T> queryWrapper);

    /**
     * 查询列表
     * subTableValue不能为空；仅在根据subTableValue定位到的分表进行查询
     *
     * @param page         分页条件
     * @param queryWrapper queryWrapper 实体对象封装操作类 {@link com.baomidou.mybatisplus.core.conditions.query.QueryWrapper}
     * @return 列表数据
     * @since 3.5.3.2
     */
    List<T> list(Object subTableValue, Page<T> page, Wrapper<T> queryWrapper);

    /**
     * 翻页查询
     * subTableValue不能为空；仅在根据subTableValue定位到的分表进行查询
     *
     * @param page         翻页对象
     * @param queryWrapper 实体对象封装操作类 {@link com.baomidou.mybatisplus.core.conditions.query.QueryWrapper}
     */
    <E extends Page<T>> E page(Object subTableValue, E page, Wrapper<T> queryWrapper);

}
