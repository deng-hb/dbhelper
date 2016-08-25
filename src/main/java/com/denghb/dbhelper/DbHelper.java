package com.denghb.dbhelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import com.denghb.dbhelper.annotation.Column;
import com.denghb.dbhelper.annotation.Id;

/**
 * <pre>
 * 常用的一些数据库操作
 * </pre>
 * 
 * {@link https://github.com/deng-hb/dbhelper}
 * 
 * @author denghb
 */
public abstract class DbHelper {

	private final static Log log = LogFactory.getLog(DbHelper.class);

	private JdbcTemplate jdbcTemplate;

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * 创建一条纪录
	 * 
	 * @param domain
	 * @return
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
		boolean idAuto = false;
		try {
			// 分析列
			Field[] fields = object.getClass().getDeclaredFields();
			for (Field field : fields) {
				String fieldName = field.getName();
				if ("serialVersionUID".equals(fieldName)) {
					continue;
				}
				// ID
				Id id = field.getAnnotation(Id.class);
				if (null != id) {
					idField = field;
					idAuto = id.auto();
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

			Object[] objects = params.toArray();

			// 执行insert
			boolean res = 1 == execute(sql.toString(), objects);

			// TODO MySql 成功了获取自动生成的ID
			if (res && idAuto) {
				try {
					Long id = queryForObject("select LAST_INSERT_ID() as id", Long.class);
					idField.setAccessible(true);
					idField.set(object, id);
				} catch (Exception e) {
					log.warn("id only MySql ..." + e.getMessage(), e);
				}
			}
			return res;

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * 更新一条纪录
	 * 
	 * @param domain
	 * @return
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
			throw new RuntimeException(e.getMessage(), e);
		}
		sql.append(" where ");
		if (null == idField) {
			throw new RuntimeException("Id not find");
		}
		Column column = idField.getAnnotation(Column.class);
		if (null == column) {
			throw new RuntimeException("Id not find");
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
	 * @return
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

	/**
	 * 查询列表
	 * 
	 * 
	 * <pre>
	 * 
	 * Example
	 * 
	 * Bean:
	 * public class User implements Serializable {
	 * 	
	 *	private String name;
	 *  
	 *	public String getName() {
	 *		return name;
	 *	}
	 *
	 *	public void setName(String name) {
	 *		this.name = name;
	 *	}
	 *}
	 *  
	 * SQL:
	 * select c_name_v as name from user;
	 * 
	 * 只要是列名和对象字段名一致就能反射赋值
	 * 
	 * @see {@link org.springframework.jdbc.core.BeanPropertyRowMapper}
	 * </pre>
	 * 
	 * @param sql
	 * @param clazz
	 * @param args
	 * @return
	 */
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
	 * @return
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
	 * @return
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
	 * @return
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
