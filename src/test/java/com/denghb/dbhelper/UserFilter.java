package com.denghb.dbhelper;

import com.denghb.dbhelper.domain.Paging;

public class UserFilter extends Paging {

	private static final long serialVersionUID = -5551254985965806929L;

	@Override
	public String[] getSorts() {
		return new String[] { "id", "email" };
	}

}
