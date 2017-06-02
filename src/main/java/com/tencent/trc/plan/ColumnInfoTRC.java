/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tencent.trc.plan;

import java.io.Serializable;

import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfoFactory;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfoUtils;

/**
 * Implementation for ColumnInfo which contains the internal name for the column
 * (the one that is used by the operator to access the column) and the type
 * (identified by a java class).
 **/

public class ColumnInfoTRC implements Serializable {

	private static final long serialVersionUID = 1L;

	private String internalName;

	private String alias = null; // [optional] alias of the column (external
									// name
	// as seen by the users)
	/**
	 * Indicates whether the column is a skewed column.
	 */
	private boolean isSkewedCol;

	/**
	 * Store the alias of the table where available.
	 */
	private String tabAlias;

	/**
	 * Indicates whether the column is a virtual column.
	 */
	private boolean isVirtualCol;

	private transient ObjectInspector objectInspector;

	private boolean isHiddenVirtualCol;

	public ColumnInfoTRC() {
	}

	public ColumnInfoTRC(String internalName, TypeInfo type, String tabAlias,
			boolean isVirtualCol) {
		this(internalName, type, tabAlias, isVirtualCol, false);
	}

	public ColumnInfoTRC(String internalName,
			@SuppressWarnings("rawtypes") Class type, String tabAlias,
			boolean isVirtualCol) {
		this(internalName, TypeInfoFactory
				.getPrimitiveTypeInfoFromPrimitiveWritable(type), tabAlias,
				isVirtualCol, false);
	}

	public ColumnInfoTRC(String internalName, TypeInfo type, String tabAlias,
			boolean isVirtualCol, boolean isHiddenVirtualCol) {
		this(internalName, TypeInfoUtils
				.getStandardWritableObjectInspectorFromTypeInfo(type),
				tabAlias, isVirtualCol, isHiddenVirtualCol);
	}

	public ColumnInfoTRC(String internalName, ObjectInspector objectInspector,
			String tabAlias, boolean isVirtualCol) {
		this(internalName, objectInspector, tabAlias, isVirtualCol, false);
	}

	public ColumnInfoTRC(String internalName, ObjectInspector objectInspector,
			String tabAlias, boolean isVirtualCol, boolean isHiddenVirtualCol) {
		this.internalName = internalName;
		this.objectInspector = objectInspector;
		this.tabAlias = tabAlias;
		this.isVirtualCol = isVirtualCol;
		this.isHiddenVirtualCol = isHiddenVirtualCol;
	}

	public ColumnInfoTRC(ColumnInfoTRC columnInfo) {
		this.internalName = columnInfo.getInternalName();
		this.alias = columnInfo.getAlias();
		this.isSkewedCol = columnInfo.isSkewedCol();
		this.tabAlias = columnInfo.getTabAlias();
		this.isVirtualCol = columnInfo.getIsVirtualCol();
		this.isHiddenVirtualCol = columnInfo.isHiddenVirtualCol();
		this.setType(columnInfo.getType());
	}

	public TypeInfo getType() {
		return TypeInfoUtils.getTypeInfoFromObjectInspector(objectInspector);
	}

	public ObjectInspector getObjectInspector() {
		return objectInspector;
	}

	public String getInternalName() {
		return internalName;
	}

	public void setType(TypeInfo type) {
		objectInspector = TypeInfoUtils
				.getStandardWritableObjectInspectorFromTypeInfo(type);
	}

	public void setInternalName(String internalName) {
		this.internalName = internalName;
	}

	public String getTabAlias() {
		return tabAlias;
	}

	public boolean getIsVirtualCol() {
		return isVirtualCol;
	}

	public boolean isHiddenVirtualCol() {
		return isHiddenVirtualCol;
	}

	/**
	 * Returns the string representation of the ColumnInfo.
	 */
	@Override
	public String toString() {
		return tabAlias + ":" + alias + ":" + internalName + ":"
				+ objectInspector.getTypeName();
	}

	public void setAlias(String col_alias) {
		alias = col_alias;
	}

	public String getAlias() {
		return alias;
	}

	public void setTabAlias(String tabAlias) {
		this.tabAlias = tabAlias;
	}

	public void setVirtualCol(boolean isVirtualCol) {
		this.isVirtualCol = isVirtualCol;
	}

	public void setHiddenVirtualCol(boolean isHiddenVirtualCol) {
		this.isHiddenVirtualCol = isHiddenVirtualCol;
	}

	/**
	 * @return the isSkewedCol
	 */
	public boolean isSkewedCol() {
		return isSkewedCol;
	}

	/**
	 * @param isSkewedCol
	 *            the isSkewedCol to set
	 */
	public void setSkewedCol(boolean isSkewedCol) {
		this.isSkewedCol = isSkewedCol;
	}

	private boolean checkEquals(Object obj1, Object obj2) {
		return obj1 == null ? obj2 == null : obj1.equals(obj2);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ColumnInfoTRC) || (obj == null)) {
			return false;
		}

		ColumnInfoTRC dest = (ColumnInfoTRC) obj;
		if ((!checkEquals(internalName, dest.getInternalName()))
				|| (!checkEquals(alias, dest.getAlias()))
				|| (!checkEquals(getType(), dest.getType()))
				|| (isSkewedCol != dest.isSkewedCol())
				|| (isVirtualCol != dest.getIsVirtualCol())
				|| (isHiddenVirtualCol != dest.isHiddenVirtualCol())) {
			return false;
		}

		return true;
	}
}