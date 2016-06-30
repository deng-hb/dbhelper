package com.denghb.dbhelper.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.denghb.dbhelper.annotation.Column;
import com.denghb.dbhelper.annotation.Id;
import com.denghb.dbhelper.annotation.Table;

public class SqlUtils {
	private final static Logger log = LoggerFactory.getLogger(SqlUtils.class);

	/**
	 * 获取对象私有字段的值
	 * 
	 * @param object
	 * @param fieldName
	 * @return
	 */
	public static <T> Object getValue(Object object, String fieldName) {
		String getterName = "get" + firstCharToUpperCase(fieldName);
		Object value = null;
		try {
			Method getter = object.getClass().getMethod(getterName);
			value = getter.invoke(object);
		} catch (Exception e) {
			log.error(getterName + " doesn't exist!", e);
		}
		return value;
	}

	// 首字母转大写
	private static String firstCharToUpperCase(String string) {
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
	 * @return
	 */
	public static <T> String getTableName(Class<T> clazz) {
		StringBuffer tableName = new StringBuffer("`");
		// 获取表名
		Table table = clazz.getAnnotation(Table.class);
		if (null == table) {
			throw new RuntimeException("not find table name...");
		}
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

	public static <T> String getIdColumn(Class<T> clazz) {
		// 分析列
		Field[] fields = clazz.getDeclaredFields();
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

}
