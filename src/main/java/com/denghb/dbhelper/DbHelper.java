package com.denghb.dbhelper;

import java.util.List;

import com.denghb.dbhelper.domain.Paging;
import com.denghb.dbhelper.domain.PagingResult;

/**
 * <pre>
 * 常用的一些数据库操作
 * 
 * </pre>
 * 
 * @author denghb
 */
public interface DbHelper {
	
	/**
	 * 创建一条纪录
	 * 
	 * @param domain
	 * @return
	 */
	public boolean insert(Object object);

	/**
	 * 更新一条纪录
	 * 
	 * @param domain
	 * @return
	 */
	public boolean updateById(Object object);

	/**
	 * 执行一条SQL
	 * 
	 * @param sql
	 * @param args
	 * @return
	 */
	public int execute(String sql, Object... args);

	/**
	 * 查询并返回分页
	 * 
	 * @param sql
	 * @param clazz
	 * @param paging
	 * @return
	 */
	public <T> PagingResult<T> list(StringBuffer sql, Class<T> clazz, Paging paging);

	/**
	 * 查询列表
	 * 
	 * @param sql
	 * @param clazz
	 * @param args
	 * @return
	 */
	public <T> List<T> list(String sql, Class<T> clazz, Object... args);

	/**
	 * 指定参数查询返回对象
	 * 
	 * @param sql
	 * @param clazz
	 * @param args
	 * @return
	 */
	public <T> T queryForObject(String sql, Class<T> clazz, Object... args);

	/**
	 * 查询一条纪录
	 * 
	 * @param clazz
	 * @param id
	 * @return
	 */
	public <T> T queryById(Class<T> clazz, Object id);

	/**
	 * 物理删除
	 * 
	 * @param clazz
	 * @param id
	 * @return
	 */
	public <T> boolean deleteById(Class<T> clazz, Object id);

}
