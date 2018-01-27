package com.denghb.dbhelper.impl;

import com.denghb.dbhelper.DbHelper;
import com.denghb.dbhelper.annotation.Column;
import com.denghb.dbhelper.annotation.Id;
import com.denghb.dbhelper.paging.Paging;
import com.denghb.dbhelper.paging.PagingResult;
import com.denghb.dbhelper.utils.DbHelperUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <pre>
 * 常用的一些数据库操作
 * </pre>
 * <p>
 *
 * @author denghb
 */
public class DbHelperImpl implements DbHelper {

    private final static Log log = LogFactory.getLog(DbHelperImpl.class);

    private JdbcTemplate jdbcTemplate;

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public DbHelperImpl() {

    }

    public DbHelperImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 创建一条纪录
     *
     * @param object
     */
    public boolean insert(Object object) {
        // 用来存放sql语句
        StringBuffer sql = new StringBuffer();
        // 用来存放?号的语句
        StringBuffer paramsSql = new StringBuffer();

        // 用来存放参数值
        List<Object> params = new ArrayList<Object>();

        sql.append("insert into ");
        sql.append(DbHelperUtils.getTableName(object.getClass()));
        sql.append(" (");

        // 计数器
        int count = 0;

        // 主键字段
        Field idField = null;
        try {
            // 分析列
            Field[] fields = object.getClass().getDeclaredFields();
            for (Field field : fields) {
                String fieldName = field.getName();
                if ("serialVersionUID".equals(fieldName)) {
                    continue;
                }
                Column column = field.getAnnotation(Column.class);
                if (null == column) {
                    // 没有列注解的直接跳过
                    continue;
                }
                // ID
                Id id = field.getAnnotation(Id.class);
                if (null != id) {
                    idField = field;
                }
                Object value = DbHelperUtils.getFieldValue(object, fieldName);
                if (value == null) {
                    // 如果参数值是null就直接跳过（不允许覆盖为null值，规范要求更新的每个字段都要有值，没有值就是空字符串）
                    continue;
                }
                if (null != id) {
                    idField = null;// ID有值不自动赋值
                }

                if (count != 0) {
                    sql.append(',');
                    paramsSql.append(',');
                }
                count++;
                // 字段名
                sql.append('`');
                sql.append(column.name());
                sql.append('`');

                paramsSql.append('?');
                params.add(value);
            }
            sql.append(") values (");
            sql.append(paramsSql);
            sql.append(')');

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        Object[] objects = params.toArray();

        // 执行insert
        boolean res = 1 == execute(sql.toString(), objects);
        try {
            if (res && null != idField) {
                Object id = queryForObject("SELECT LAST_INSERT_ID() as id", idField.getType());
                idField.setAccessible(true);
                idField.set(object, id);
            }
        } catch (IllegalAccessException e) {
            log.error(e.getMessage(), e);
        }
        return res;

    }

    /**
     * 更新一条纪录
     *
     * @param object
     */
    public boolean updateById(Object object) {
        // 用来存放sql语句
        StringBuffer sql = new StringBuffer();

        // 用来存放参数值
        List<Object> params = new ArrayList<Object>();

        sql.append("update ");
        sql.append(DbHelperUtils.getTableName(object.getClass()));
        sql.append(" set ");

        int count = 0;// 计算“，”
        // 主键字段
        Field idField = null;
        try {
            // 分析列
            Field[] fields = object.getClass().getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                String fieldName = field.getName();
                if ("serialVersionUID".equals(fieldName)) {
                    continue;
                }
                // ID
                Id id = field.getAnnotation(Id.class);
                if (null != id) {
                    idField = field;
                    // 主键不修改
                    continue;
                }
                Column column = field.getAnnotation(Column.class);
                if (null == column) {
                    // 没有列注解的直接跳过
                    continue;
                }
                Object value = DbHelperUtils.getFieldValue(object, fieldName);
                if (value == null) {
                    // 如果参数值是null就直接跳过（不允许覆盖为null值，规范要求更新的每个字段都要有值，没有值就是空字符串）
                    continue;
                }
                if (0 != count) {
                    sql.append(",");
                }
                // 字段名
                sql.append('`');
                sql.append(column.name());
                sql.append("` = ?");
                // 值
                params.add(value);
                count++;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
        sql.append(" where ");
        if (null == idField) {
            log.error("@Id not find");
            return false;
        }
        Column column = idField.getAnnotation(Column.class);
        if (null == column) {
            log.error("@Id not find");
            return false;
        }
        sql.append('`');
        sql.append(column.name());
        sql.append("` = ?");
        params.add(DbHelperUtils.getFieldValue(object, idField.getName()));

        Object[] objects = params.toArray();

        return 1 == execute(sql.toString(), params.toArray(objects));
    }

    /**
     * 执行一条SQL
     *
     * @param sql
     * @param args
     */
    public int execute(String sql, Object... args) {
        long start = System.currentTimeMillis();

        // 是debug的才执行
        if (log.isDebugEnabled()) {
            log.debug("params:" + Arrays.toString(args));
            log.debug("update sql:" + sql.toString());
        }
        int res = jdbcTemplate.update(sql, args);
        if (log.isDebugEnabled()) {
            log.debug("execute time:" + (System.currentTimeMillis() - start) + "ms");
        }
        return res;
    }


    @Override
    public <T> PagingResult<T> list(StringBuffer sql, Class<T> clazz, Paging paging) {
        PagingResult<T> result = new PagingResult<T>(paging);

        Object[] objects = paging.getParams().toArray();
        // 不分页 start
        long rows = paging.getRows();
        if (0 != rows) {
            // 先查总数
            String totalSql = "select count(*) ";

            String tempSql = sql.toString().toLowerCase();
            totalSql += sql.substring(tempSql.indexOf("from"), sql.length());

            // fix group by
            if (0 < totalSql.indexOf(" group ")) {
                totalSql = "select count(*) from (" + totalSql + ") temp";
            }

            long total = queryForObject(totalSql, Long.class, objects);

            paging.setTotal(total);
            if (0 == total) {
                return result;
            }
        }
        // 不分页 end

        // 排序
        if (paging.isSort()) {
            // 判断是否有排序字段
            String[] sorts = paging.getSorts();
            if (null != sorts && 0 < sorts.length) {
                int sortIndex = paging.getSortIndex();

                // 大于排序的长度默认最后一个
                if (sortIndex >= sorts.length) {
                    sortIndex = sorts.length - 1;
                }
                // TODO 排序字段
                sql.append(" order by ");

                sql.append('`');
                sql.append(sorts[sortIndex]);
                sql.append('`');

                // 排序方式
                if (paging.isDesc()) {
                    sql.append(" desc");
                } else {
                    sql.append(" asc");
                }
            }
        }

        if (0 != rows) {
            // 分页
            sql.append(" limit ");
            sql.append(paging.getStart());
            sql.append(",");
            sql.append(rows);
        }

        List<T> list = list(sql.toString(), clazz, objects);
        result.setList(list);

        return result;
    }

    public <T> List<T> list(String sql, Class<T> clazz, Object... args) {
        long start = System.currentTimeMillis();

        if (log.isDebugEnabled()) {
            log.debug("params:" + Arrays.toString(args));
            log.debug("query sql:" + sql);
        }
        List<T> list = null;

        if (null == args || 0 == args.length || args[0] == null) {
            if (DbHelperUtils.isSingleClass(clazz)) {
                list = jdbcTemplate.queryForList(sql, clazz);
            } else {
                list = jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(clazz));
            }
        } else {
            if (DbHelperUtils.isSingleClass(clazz)) {
                list = jdbcTemplate.queryForList(sql, clazz, args);
            } else {
                list = jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(clazz), args);
            }
        }
        // 执行时间
        if (log.isDebugEnabled()) {
            log.debug("execute time:" + (System.currentTimeMillis() - start) + "ms");
        }
        return list;
    }

    /**
     * 指定参数查询返回对象
     *
     * @param sql
     * @param clazz
     * @param args
     */
    public <T> T queryForObject(String sql, Class<T> clazz, Object... args) {
        List<T> list = list(sql, clazz, args);
        if (null != list && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    /**
     * 查询一条纪录
     *
     * @param clazz
     * @param id
     */
    public <T> T queryById(Class<T> clazz, Object id) {
        StringBuffer sb = new StringBuffer("select ");
        sb.append(DbHelperUtils.getTableColumnAsFieldName(clazz));
        sb.append(" from ");
        sb.append(DbHelperUtils.getTableName(clazz));
        sb.append(" where `");
        sb.append(DbHelperUtils.getIdColumn(clazz));
        sb.append("` = ?");
        String sql = sb.toString();

        List<T> list = list(sql, clazz, id);

        if (null != list && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    /**
     * 删除
     *
     * @param clazz
     * @param id
     */
    public <T> boolean deleteById(Class<T> clazz, Object id) {
        StringBuffer sb = new StringBuffer("delete from ");
        sb.append(DbHelperUtils.getTableName(clazz));
        sb.append(" where `");
        sb.append(DbHelperUtils.getIdColumn(clazz));
        sb.append("` = ?");
        return 1 == execute(sb.toString(), id);
    }

}
