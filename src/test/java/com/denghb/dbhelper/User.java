package com.denghb.dbhelper;

import java.io.Serializable;

import com.denghb.dbhelper.annotation.Column;
import com.denghb.dbhelper.annotation.Id;
import com.denghb.dbhelper.annotation.Table;

//DROP TABLE IF EXISTS `user`;
//
//CREATE TABLE `test`.`user` (
//`id` int(11) unsigned NOT NULL AUTO_INCREMENT,
//`age` int(11) DEFAULT NULL,
//`name` varchar(20) DEFAULT NULL,
//`email` varchar(100) DEFAULT NULL,
//PRIMARY KEY (`id`)
//) ENGINE=InnoDB DEFAULT CHARSET=utf8;

@Table(name = "user", database = "test")
public class User implements Serializable {

	private static final long serialVersionUID = 9039138825374953147L;

	@Id
	@Column(name = "id")
	private Integer id;

	@Column(name = "age")
	private Integer age;

	@Column(name = "name")
	private String name;

	@Column(name = "email")
	private String email;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", age=" + age + ", name=" + name + ", email=" + email + "]";
	}

}
