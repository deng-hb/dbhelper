package com.denghb.dbhelper;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import com.denghb.dbhelper.domain.Paging;
import com.denghb.dbhelper.domain.PagingResult;
import com.denghb.dbhelper.utils.SqlUtils;

@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
public class AppTest extends AbstractJUnit4SpringContextTests {

	@Autowired
	private DbHelper db;

	@Test
	public void insert() {
		User user = new User();
		user.setAge(18);
		user.setEmail("insert@qq.com");
		user.setName("张三");

		boolean res = db.insert(user);
		Assert.assertTrue(res);
		System.out.println("insert:" + user);
	}

	@Test
	public void update() {
		User user = new User();
		user.setId(1);
		user.setEmail("update@qq.com");

		boolean res = db.updateById(user);
		Assert.assertTrue(res);
		System.out.println("update:" + user);
	}

	@Test
	public void queryById() {
		User user = db.queryById(User.class, 1);
		Assert.assertNotNull(user);
		System.out.println("queryById:" + user);
	}

	@Test
	public void list() {
		List<User> list = db.list("select * from user where name = ?", User.class, "张三");

		Assert.assertNotNull(list);
		for (int i = 0; i < list.size(); i++) {
			System.out.println(list.get(i));
		}
	}

	@Test
	public void listPage() {
		StringBuffer sql = new StringBuffer();
		sql.append("select * from ");
		sql.append(SqlUtils.getTableName(User.class));

		Paging paging = new UserFilter();
		paging.setDesc(true);
		PagingResult<User> result = db.list(sql, User.class, paging);

		List<User> list = result.getList();
		Assert.assertNotNull(list);
		for (int i = 0; i < list.size(); i++) {
			System.out.println(list.get(i));
		}
	}

	@Test
	public void deleteById() {
		boolean res = db.deleteById(User.class, 1);
		Assert.assertTrue(res);
	}

}
