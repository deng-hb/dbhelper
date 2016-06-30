package com.denghb.dbhelper.impl;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import com.denghb.dbhelper.DbHelper;
import com.denghb.dbhelper.annotation.Column;
import com.denghb.dbhelper.annotation.Id;
import com.denghb.dbhelper.domain.Paging;
import com.denghb.dbhelper.domain.PagingResult;
import com.denghb.dbhelper.utils.SqlUtils;

/**
 * 
 * @author denghb
 *
 */
public class DbHelperImpl implements DbHelper {

	private final static Logger log = LoggerFactory.getLogger(DbHelperImpl.class);

	private JdbcTemplate jdbcTemplate;

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public boolean insert(Object object) {
		// 用来存放sql语句
		StringBuffer sql = new StringBuffer();
		// 用来存放?号的语句
		StringBuffer paramsSql = new StringBuffer();

		// 用来存放参数值
		List<Object> params = new ArrayList<Object>();

		sql.append("insert into ");
		sql.append(SqlUtils.getTableName(object.getClass()));
		sql.append(" (");

		// 计数器
		int count = 0;

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
				}
				Column column = field.getAnnotation(Column.class);
				if (null == column) {
					// 没有列注解的直接跳过
					continue;
				}

				Object value = SqlUtils.getValue(object, fieldName);
				if (value == null) {
					// 如果参数值是null就直接跳过（不允许覆盖为null值，规范要求更新的每个字段都要有值，没有值就是空字符串）
					continue;
				}

				if (count != 0) {
					sql.append(',');
				}
				// 字段名
				sql.append('`');
				sql.append(column.name());
				sql.append('`');

				if (count != 0) {
					paramsSql.append(',');
				}
				paramsSql.append('?');

				params.add(value);
				count++;
			}
			sql.append(") values (");
			sql.append(paramsSql);
			sql.append(')');

			Object[] objects = params.toArray();

			// 执行insert
			boolean res = 1 == execute(sql.toString(), objects);

			// TODO 成功了获取自动生成的ID
			if (res && null != idField) {
				Integer id = queryForObject("SELECT LAST_INSERT_ID() as id", Integer.class);
				idField.setAccessible(true);
				idField.set(object, id);
			}
			return res;

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return false;

	}

	@Override
	public boolean updateById(Object object) {
		// 用来存放sql语句
		StringBuffer sql = new StringBuffer();

		// 用来存放参数值
		List<Object> params = new ArrayList<Object>();

		sql.append("update ");
		sql.append(SqlUtils.getTableName(object.getClass()));
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
				Object value = SqlUtils.getValue(object, fieldName);
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
		}
		sql.append(" where ");
		if (null == idField) {
			throw new RuntimeException("Id Error");
		}
		Column column = idField.getAnnotation(Column.class);
		if (null == column) {
			throw new RuntimeException("Id Error");
		}
		sql.append('`');
		sql.append(column.name());
		sql.append("` = ?");
		params.add(SqlUtils.getValue(object, idField.getName()));

		Object[] objects = params.toArray();

		return 1 == execute(sql.toString(), params.toArray(objects));
	}

	@Override
	public int execute(String sql, Object... args) {
		long start = System.currentTimeMillis();

		// 是debug的才执行
		if (log.isDebugEnabled()) {
			log.debug("params:" + Arrays.toString(args));
			log.debug("update sql:" + sql.toString());
		}
		int res = jdbcTemplate.update(sql, args);
		if (log.isDebugEnabled()) {
			log.debug("execute time:{}ms", System.currentTimeMillis() - start);
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
			String totalSql = "select count(*) as size ";
			int fromIndex = sql.indexOf("from");
			if (0 < fromIndex) {
				totalSql += sql.substring(fromIndex, sql.length());
			}

			int total = queryForObject(totalSql, Integer.class, objects);

			paging.setTotal(total);
			if (0 == total) {
				return result;
			}
		}
		// 不分页 end

		// start
		long page = paging.getPage() - 1;

		// 排序
		String[] sorts = paging.getSorts();
		if (null != sorts && 0 < sorts.length) {
			// TODO 排序字段
			sql.append(" order by ");
			for (int i = 0; i < sorts.length; i++) {
				if (i != 0) {
					sql.append(",");
				}
				sql.append('`');
				sql.append(sorts[i]);
				sql.append('`');
			}

			// 排序方式
			if (paging.isDesc()) {
				sql.append(" desc");
			} else {
				sql.append(" asc");
			}
		}

		if (0 != rows) {
			// 分页
			sql.append(" limit ");
			sql.append(page * rows);
			sql.append(",");
			sql.append(rows);
		}

		List<T> list = list(sql.toString(), clazz, objects);
		result.setList(list);

		return result;
	}

	@Override
	public <T> List<T> list(String sql, Class<T> clazz, Object... args) {
		long start = System.currentTimeMillis();

		if (log.isDebugEnabled()) {
			log.debug("params:[{}]", args);
			log.debug("query sql:[{}]", sql);
		}
		List<T> list = null;

		Object[] objects = args;
		if (null == objects || 0 == objects.length || objects[0] == null) {
			if (isSingleClass(clazz)) {
				list = jdbcTemplate.queryForList(sql, clazz);
			} else {
				list = jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(clazz));
			}
		} else {
			if (isSingleClass(clazz)) {
				list = jdbcTemplate.queryForList(sql, clazz, args);
			} else {
				list = jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(clazz), args);
			}
		}
		// 执行时间
		if (log.isDebugEnabled()) {
			log.debug("execute time:{}ms", System.currentTimeMillis() - start);
		}
		return list;
	}

	// TODO 基本类型
	private static <T> boolean isSingleClass(Class<T> clazz) {
		if (clazz.equals(Integer.class) || clazz.equals(String.class) || clazz.equals(Double.class)
				|| clazz.equals(Long.class) || clazz.equals(BigDecimal.class)) {
			return true;
		}
		return false;
	}

	@Override
	public <T> T queryForObject(String sql, Class<T> clazz, Object... args) {
		List<T> list = list(sql, clazz, args);
		if (null != list && !list.isEmpty()) {
			return list.get(0);
		}
		return null;
	}

	@Override
	public <T> T queryById(Class<T> clazz, Object id) {
		StringBuffer sb = new StringBuffer("select * from ");
		sb.append(SqlUtils.getTableName(clazz));
		sb.append(" where `");
		sb.append(SqlUtils.getIdColumn(clazz));
		sb.append("` = ?");
		String sql = sb.toString();

		List<T> list = list(sql, clazz, id);

		if (null != list && !list.isEmpty()) {
			return list.get(0);
		}
		return null;
	}

	@Override
	public <T> boolean deleteById(Class<T> clazz, Object id) {
		StringBuffer sb = new StringBuffer("delete from ");
		sb.append(SqlUtils.getTableName(clazz));
		sb.append(" where `");
		sb.append(SqlUtils.getIdColumn(clazz));
		sb.append("` = ?");
		return 1 == execute(sb.toString(), id);
	}

}
