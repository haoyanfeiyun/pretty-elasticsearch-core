package org.pretty.es.core.vo.es;

public class ESPage {
	 	private long totalNumber;//当前表中总条目数量
	    private int currentPage = 1;//当前页的位置
	    private int totalPage;//总页数
	    private int pageSize = 10;//页面大小

	    private int from;//检索的起始位置
	    private int size;//检索的总数目

	    public int getCurrentPage() {
			return currentPage;
		}

		public void setCurrentPage(int currentPage) {
			this.currentPage = currentPage;
		}

		public int getTotalPage() {
			return totalPage;
		}

		public void setTotalPage(int totalPage) {
			this.totalPage = totalPage;
		}

		public int getPageSize() {
			return pageSize;
		}

		public void setPageSize(int pageSize) {
			this.pageSize = pageSize;
		}



		public int getFrom() {
			return from;
		}

		public void setFrom(int from) {
			this.from = from;
		}

		public int getSize() {
			return size;
		}

		public void setSize(int size) {
			this.size = size;
		}

		public long getTotalNumber() {
			return totalNumber;
		}

		public void setTotalNumber(long totalNumber) {
	        this.totalNumber = totalNumber;
	        this.count();
	    }

		public ESPage() {
	        super();
	    }

		public ESPage(int currentPage,int pageSize) {
	        super();
	        this.currentPage = currentPage;
	        this.pageSize = pageSize;
	    }

	    public ESPage(int totalNumber, int currentPage, int totalPage, int pageSize, int from, int size) {
	        super();
	        this.totalNumber = totalNumber;
	        this.currentPage = currentPage;
	        this.totalPage = totalPage;
	        this.pageSize = pageSize;
	        this.from = from;
	        this.size = size;
	    }

	    public void count(){
	        int totalPageTemp = (int) this.totalNumber/this.pageSize;
	        int plus = (this.totalNumber%this.pageSize)==0?0:1;
	        totalPageTemp = totalPageTemp+plus;
	        if(totalPageTemp<=0){
	            totalPageTemp=1;
	        }
	        this.totalPage = totalPageTemp;//总页数

	        if(this.totalPage<this.currentPage){
	            this.currentPage = this.totalPage;
	        }
	        if(this.currentPage<1){
	            this.currentPage=1;
	        }
	        this.from = (this.currentPage-1)*this.pageSize;//起始位置等于之前所有页面输乘以页面大小
	        this.size = this.pageSize;//检索数量等于页面大小
	    }

	    public void countFromAndSize() {
	    	this.from = (this.currentPage-1)*this.pageSize;//起始位置等于之前所有页面输乘以页面大小
	        this.size = this.pageSize;//检索数量等于页面大小
	    	}
}
