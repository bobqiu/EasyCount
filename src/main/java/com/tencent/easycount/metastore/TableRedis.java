package com.tencent.easycount.metastore;

import java.util.ArrayList;
import java.util.HashMap;

public class TableRedis extends Table {

	private static final long serialVersionUID = -8143401473875162088L;

	public TableRedis(String tableName, ArrayList<Field> fields,
			HashMap<String, String> attrs) throws Exception {
		super(tableName, fields, attrs, TableType.redis);
	}
}
