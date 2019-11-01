package com.pretty.es.core.vo;

public class ESPage {
    private long totalNumber;//当前表中总条目数量
    private int currentPage = 1;//当前页的位置
    private int totalPage;//总页数
    private int size = 10;//页面大小
    private int from = 0;//检索的起始位置

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

    public ESPage(int currentPage, int size) {
        super();
        this.currentPage = currentPage;
        this.size = size;
    }

    public ESPage(int totalNumber, int currentPage, int totalPage, int from, int size) {
        super();
        this.totalNumber = totalNumber;
        this.currentPage = currentPage;
        this.totalPage = totalPage;
        this.from = from;
        this.size = size;
    }

    public void count() {
        int totalPageTemp = (int) this.totalNumber / this.size;
        int plus = (this.totalNumber % this.size) == 0 ? 0 : 1;
        totalPageTemp = totalPageTemp + plus;
        if (totalPageTemp <= 0) {
            totalPageTemp = 1;
        }
        this.totalPage = totalPageTemp;//总页数

        if (this.totalPage < this.currentPage) {
            this.currentPage = this.totalPage;
        }
        if (this.currentPage < 1) {
            this.currentPage = 1;
        }
        this.from = (this.currentPage - 1) * this.size;//起始位置等于之前所有页面输乘以页面大小
    }

    public void countFromAndSize() {
        this.from = (this.currentPage - 1) * this.size;//起始位置等于之前所有页面输乘以页面大小
    }
}
