package org.pretty.es.core.vo.es;

import java.util.Map;

/**
 * ES参数Vo
 * 
 * @author lihaohao
 *
 */
public class ESParamVo {

	// 索引名
	private String index;

	// type
	private String type;

	// 字段对应map,键是ES的字段名，值是入参值
	private Map<String, Object> columnMap;

	// 分页信息
	private ESPage esPage;

	public ESParamVo() {
	};

	public ESParamVo(String index, String type) {
		this.index = index;
		this.type = type;
	};

	public ESParamVo(String index, String type, Map<String, Object> columnMap) {
		this.index = index;
		this.type = type;
		this.columnMap = columnMap;
	};
	
	public ESParamVo(String index, String type, ESPage esPage) {
		this.index = index;
		this.type = type;
		this.esPage = esPage;
	};

	public ESParamVo(String index, String type, Map<String, Object> columnMap, ESPage esPage) {
		this.index = index;
		this.type = type;
		this.columnMap = columnMap;
		this.esPage = esPage;
	};

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Map<String, Object> getColumnMap() {
		return columnMap;
	}

	public void setColumnMap(Map<String, Object> columnMap) {
		this.columnMap = columnMap;
	}

	public ESPage getEsPage() {
		return esPage;
	}

	public void setEsPage(ESPage esPage) {
		this.esPage = esPage;
	}

}
