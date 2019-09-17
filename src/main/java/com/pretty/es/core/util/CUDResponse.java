package com.pretty.es.core.util;

import java.util.List;

public class CUDResponse {

    private long successed;//成功数

    private long failed;//失败数

    private List<String> successedIdList;//成功ID列表

    private List<String> failedIdList;//失败ID列表

    public CUDResponse() {
    }

    public CUDResponse(long successed, long failed, List<String> successedIdList, List<String> failedIdList) {
        this.successed = successed;
        this.failed = failed;
        this.successedIdList = successedIdList;
        this.failedIdList = failedIdList;
    }

    public long getSuccessed() {
        return successed;
    }

    public void setSuccessed(long successed) {
        this.successed = successed;
    }

    public long getFailed() {
        return failed;
    }

    public void setFailed(long failed) {
        this.failed = failed;
    }

    public List<String> getSuccessedIdList() {
        return successedIdList;
    }

    public void setSuccessedIdList(List<String> successedIdList) {
        this.successedIdList = successedIdList;
    }

    public List<String> getFailedIdList() {
        return failedIdList;
    }

    public void setFailedIdList(List<String> failedIdList) {
        this.failedIdList = failedIdList;
    }
}
