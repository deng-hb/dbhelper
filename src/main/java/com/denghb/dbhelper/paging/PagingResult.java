package com.denghb.dbhelper.paging;

import java.io.Serializable;
import java.util.List;

/**
 * Created by denghb on 15/11/18.
 */
public class PagingResult<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	public PagingResult(Paging paging) {
		this.paging = paging;
	}

	private List<T> list;

	private Paging paging;

	public List<T> getList() {
		return list;
	}

	public void setList(List<T> list) {
		this.list = list;
	}

	public Paging getPaging() {
		return paging;
	}

	public void setPaging(Paging paging) {
		this.paging = paging;
	}

	@Override
	public String toString() {
		return "PagingResult [list=" + list + ", paging=" + paging + "]";
	}

}
