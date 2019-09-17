package com.pretty.es.core.util;

public class ESConstant {

	
	/**
	 * ES查询参数
	 */
	public static interface esCoreSizeParam {
		/**
		 * 桶聚合size
		 */
		public static final int TERMS_AGGREGATION_SIZE = 10000;

		/**
		 * 桶聚合最大size
		 */
		public static final int TERMS_AGGREGATION_MAX_SIZE = Integer.MAX_VALUE;
		
		/**
		 * 分页size
		 */
		public static final int PAGINATION_SIZE = 10000;
	}
}
