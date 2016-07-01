package com.denghb.dbhelper.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by denghb on 15/11/18.
 */
public abstract class Paging implements Serializable {

	private static final long serialVersionUID = 1760699317506200256L;

	/** 总数 */
	private long total = 0;

	/** 当前页数 */
	private long page = 1;

	/** 每页数量 */
	private long rows = 20;

	/** 总页数 */
	private long totalPage = 1;

	/** 是否排序 */
	private boolean isSort = false;

	/** 排序字段 */
	private String[] sorts;

	/** 排序字段下标 默认：0 */
	private int sortIndex = 0;

	/** 默认降序 */
	private boolean desc = true;

	/** 参数列表 */
	private List<Object> params = new ArrayList<Object>();

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;

		if (0 != total && 0 != rows) {
			totalPage = total / rows;
			if (total % rows != 0) {
				totalPage++;
			}
		}

	}

	public long getPage() {
		return page;
	}

	public void setPage(long page) {
		this.page = page;
	}

	public long getRows() {
		return rows;
	}

	public void setRows(long rows) {
		this.rows = rows;
	}

	public long getTotalPage() {
		return totalPage;
	}

	public void setTotalPage(long totalPage) {
		this.totalPage = totalPage;
	}

	/**
	 * 设置需要排序的字段
	 * 
	 * @return
	 */
	public abstract String[] getSorts();

	public void setSorts(String[] sorts) {
		this.sorts = sorts;
	}

	public boolean isDesc() {
		return desc;
	}

	public void setDesc(boolean desc) {
		this.desc = desc;
	}

	public List<Object> getParams() {
		return params;
	}

	public void setParams(List<Object> params) {
		this.params = params;
	}

	public boolean isSort() {
		return isSort;
	}

	public void setSort(boolean isSort) {
		this.isSort = isSort;
	}

	public int getSortIndex() {
		return sortIndex;
	}

	public void setSortIndex(int sortIndex) {
		this.sortIndex = sortIndex;
	}

	@Override
	public String toString() {
		return "Paging [total=" + total + ", page=" + page + ", rows=" + rows + ", totalPage=" + totalPage + ", sorts="
				+ Arrays.toString(sorts) + ", desc=" + desc + ", params=" + params + "]";
	}

}
