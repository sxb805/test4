package com.vortex.cloud.test.service.base;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vortex.cloud.test.support.TableNameThreadLocal;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * @author donghao
 * @date 2025/4/21 15:13
 */
public abstract class SubServiceImpl<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> implements ISubService<T> {

    /**
     * 根据回传的subTableValue自定义表规则
     *
     * @param subTableValue 透传分表条件，实现类自行解析
     * @return 实际表名
     */
    public abstract String getTableName(Object subTableValue);

    private String getActualTableName(Object subTableValue) {
        Assert.notNull(subTableValue, "分表值不能为空");
        String tableName = this.getTableName(subTableValue);
        Assert.hasText(tableName, "实际表名不能为空");
        return tableName;
    }

    @Override
    public boolean save(Object subTableValue, T entity) {
        String actualTableName = this.getActualTableName(subTableValue);
        try {
            TableNameThreadLocal.set(actualTableName);
            return super.save(entity);
        } finally {
            TableNameThreadLocal.remove();
        }
    }

    @Override
    public boolean saveOrUpdate(Object subTableValue, T entity) {
        String actualTableName = this.getActualTableName(subTableValue);
        try {
            TableNameThreadLocal.set(actualTableName);
            return super.saveOrUpdate(entity);
        } finally {
            TableNameThreadLocal.remove();
        }
    }

    @Override
    public boolean saveBatch(Object subTableValue, Collection<T> entityList) {
        String actualTableName = this.getActualTableName(subTableValue);
        try {
            TableNameThreadLocal.set(actualTableName);
            return super.saveBatch(entityList);
        } finally {
            TableNameThreadLocal.remove();
        }
    }

    @Override
    public boolean saveOrUpdateBatch(Object subTableValue, Collection<T> entityList) {
        String actualTableName = this.getActualTableName(subTableValue);
        try {
            TableNameThreadLocal.set(actualTableName);
            return super.saveOrUpdateBatch(entityList);
        } finally {
            TableNameThreadLocal.remove();
        }
    }

    @Override
    public boolean updateById(Object subTableValue, T entity) {
        String actualTableName = this.getActualTableName(subTableValue);
        try {
            TableNameThreadLocal.set(actualTableName);
            return super.updateById(entity);
        } finally {
            TableNameThreadLocal.remove();
        }
    }

    @Override
    public boolean updateBatchById(Object subTableValue, Collection<T> entityList) {
        String actualTableName = this.getActualTableName(subTableValue);
        try {
            TableNameThreadLocal.set(actualTableName);
            return super.updateBatchById(entityList);
        } finally {
            TableNameThreadLocal.remove();
        }
    }

    @Override
    public boolean update(Object subTableValue, T entity, Wrapper<T> updateWrapper) {
        String actualTableName = this.getActualTableName(subTableValue);
        try {
            TableNameThreadLocal.set(actualTableName);
            return super.update(entity, updateWrapper);
        } finally {
            TableNameThreadLocal.remove();
        }
    }

    @Override
    public boolean removeByIds(Object subTableValue, Collection<String> list) {
        String actualTableName = this.getActualTableName(subTableValue);
        try {
            TableNameThreadLocal.set(actualTableName);
            return super.removeByIds(list);
        } finally {
            TableNameThreadLocal.remove();
        }
    }

    @Override
    public boolean remove(Object subTableValue, Wrapper<T> queryWrapper) {
        String actualTableName = this.getActualTableName(subTableValue);
        try {
            TableNameThreadLocal.set(actualTableName);
            return super.remove(queryWrapper);
        } finally {
            TableNameThreadLocal.remove();
        }
    }

    @Override
    public T getById(Object subTableValue, Serializable id) {
        String actualTableName = this.getActualTableName(subTableValue);
        try {
            TableNameThreadLocal.set(actualTableName);
            return super.getById(id);
        } finally {
            TableNameThreadLocal.remove();
        }
    }

    @Override
    public T getOne(Object subTableValue, Wrapper<T> queryWrapper) {
        String actualTableName = this.getActualTableName(subTableValue);
        try {
            TableNameThreadLocal.set(actualTableName);
            return super.getOne(queryWrapper);
        } finally {
            TableNameThreadLocal.remove();
        }
    }

    @Override
    public List<T> listByIds(Object subTableValue, Collection<? extends Serializable> idList) {
        String actualTableName = this.getActualTableName(subTableValue);
        try {
            TableNameThreadLocal.set(actualTableName);
            return super.listByIds(idList);
        } finally {
            TableNameThreadLocal.remove();
        }
    }

    @Override
    public boolean exists(Object subTableValue, Wrapper<T> queryWrapper) {
        String actualTableName = this.getActualTableName(subTableValue);
        try {
            TableNameThreadLocal.set(actualTableName);
            return super.exists(queryWrapper);
        } finally {
            TableNameThreadLocal.remove();
        }
    }

    @Override
    public long count(Object subTableValue) {
        String actualTableName = this.getActualTableName(subTableValue);
        try {
            TableNameThreadLocal.set(actualTableName);
            return super.count();
        } finally {
            TableNameThreadLocal.remove();
        }
    }

    @Override
    public long count(Object subTableValue, Wrapper<T> queryWrapper) {
        String actualTableName = this.getActualTableName(subTableValue);
        try {
            TableNameThreadLocal.set(actualTableName);
            return super.count(queryWrapper);
        } finally {
            TableNameThreadLocal.remove();
        }
    }

    @Override
    public List<T> list(Object subTableValue, Wrapper<T> queryWrapper) {
        String actualTableName = this.getActualTableName(subTableValue);
        try {
            TableNameThreadLocal.set(actualTableName);
            return super.list(queryWrapper);
        } finally {
            TableNameThreadLocal.remove();
        }
    }

    @Override
    public List<T> list(Object subTableValue, Page<T> page, Wrapper<T> queryWrapper) {
        String actualTableName = this.getActualTableName(subTableValue);
        try {
            TableNameThreadLocal.set(actualTableName);
            // 禁止分页插件的自动count执行，分页插件没有识别动态表名
            page.setSearchCount(Boolean.FALSE);

            return super.list(page, queryWrapper);
        } finally {
            TableNameThreadLocal.remove();
        }
    }

    @Override
    public <E extends Page<T>> E page(Object subTableValue, E page, Wrapper<T> queryWrapper) {
        String actualTableName = this.getActualTableName(subTableValue);
        try {
            TableNameThreadLocal.set(actualTableName);

            long total = super.count(queryWrapper);
            // 禁止分页插件的自动count执行，分页插件没有识别动态表名
            page.setSearchCount(Boolean.FALSE);
            List<T> records = super.list(page, queryWrapper);

            Page<T> pageResult = new Page<>(page.getCurrent(), page.getSize(), total);
            pageResult.setRecords(records);

            return (E) pageResult;
        } finally {
            TableNameThreadLocal.remove();
        }
    }
}
