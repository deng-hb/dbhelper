package com.denghb.dbhelper.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

import com.denghb.dbhelper.annotation.Column;
import com.denghb.dbhelper.annotation.Id;
import com.denghb.dbhelper.annotation.Table;

/**
 * 
 * @author denghb
 *
 */
public class DbHelperUtils {
	private final static Log log = LogFactory.getLog(DbHelperUtils.class);

	/**
	 * 基本类型
	 */
	private static Set<Class<?>> classes = new HashSet<Class<?>>();

	static {
		classes.add(java.lang.Integer.class);
		classes.add(java.lang.Long.class);
		classes.add(java.lang.Short.class);

		classes.add(java.lang.Double.class);
		classes.add(java.lang.String.class);
		classes.add(java.math.BigDecimal.class);

		classes.add(java.sql.Time.class);
		classes.add(java.util.Date.class);
		classes.add(java.sql.Timestamp.class);

	}

	/**
	 * 是否是基本类型
	 * 
	 * @param clazz
	 */
	public static <T> boolean isSingleClass(final Class<T> clazz) {
		return classes.contains(clazz);
	}

	/**
	 * 获取对象私有字段的值
	 * 
	 * @param object
	 * @param fieldName
	 */
	public static <T> Object getFieldValue(final Object object, final String fieldName) {
		String up1 = firstCharToUpperCase(fieldName);
		Object value = null;
		try {
			Method getter = object.getClass().getMethod("get" + up1);
			if (null == getter) {
				// TODO Boolean
				getter = object.getClass().getMethod("is" + up1);
			}
			value = getter.invoke(object);
		} catch (Exception e) {
			log.error(fieldName + " 'get' or 'is' Method doesn't exist!", e);
		}
		return value;
	}

	// 首字母转大写
	public static String firstCharToUpperCase(final String string) {
		if (StringUtils.hasText(string)) {
			int length = string.length();
			// 获取第一个转大写
			String first = string.substring(0, 1);
			first = first.toUpperCase();

			// 判断字符长度
			if (1 == length) {
				return first;
			}

			String other = string.substring(1, length);
			return first + other;
		}
		return string;
	}

	/**
	 * 获取表名
	 * 
	 * @param <T>
	 * 
	 */
	public static <T> String getTableName(final Class<T> clazz) {
		// 获取表名
		Table table = clazz.getAnnotation(Table.class);
		if (null == table) {
			throw new RuntimeException("not find table name...");
		}
		StringBuffer tableName = new StringBuffer("`");
		// 获取数据库名称
		String database = table.database();
		if (StringUtils.hasText(database)) {
			tableName.append(database);
			tableName.append("`.`");
		}
		// 获取注解的表名
		tableName.append(table.name());
		tableName.append("`");
		return tableName.toString();
	}

	public static <T> String getIdColumn(final Class<T> clazz) {
		// 分析列
		Field[] fields = clazz.getDeclaredFields();
		if (null == fields) {
			throw new RuntimeException("Id Conlmn not find");
		}
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			String fieldName = field.getName();
			if ("serialVersionUID".equals(fieldName)) {
				continue;
			}
			// ID
			Id id = field.getAnnotation(Id.class);
			if (null != id) {
				Column column = field.getAnnotation(Column.class);
				if (null != column) {
					return column.name();
				}
			}
		}
		throw new RuntimeException("Id Conlmn not find");
	}

	/**
	 * 获取列as字段
	 * 
	 * @param clazz
	 */
	public static <T> String getTableColumnAsFieldName(final Class<T> clazz) {
		StringBuffer stringBuffer = new StringBuffer();
		// 分析列
		Field[] fields = clazz.getDeclaredFields();
		if (null == fields) {
			return stringBuffer.toString();
		}
		int i = 0;
		for (Field field : fields) {
			String fieldName = field.getName();
			if ("serialVersionUID".equals(fieldName)) {
				continue;
			}
			Column column = field.getAnnotation(Column.class);
			if (null != column) {
				if (0 != i) {
					stringBuffer.append(',');
				}
				stringBuffer.append('`');
				stringBuffer.append(column.name());
				stringBuffer.append("` as `");
				stringBuffer.append(fieldName);
				stringBuffer.append("`");
				++i;
			}
		}
		return stringBuffer.toString();
	}

	/**
	 * 获取默认查询语句
	 * 
	 * @param clazz
	 */
	public static <T> String getSelectSql(final Class<T> clazz) {
		StringBuffer sql = new StringBuffer("select ");
		sql.append(DbHelperUtils.getTableColumnAsFieldName(clazz));
		sql.append(" from ");
		sql.append(DbHelperUtils.getTableName(clazz));

		return sql.toString();

	}
}
