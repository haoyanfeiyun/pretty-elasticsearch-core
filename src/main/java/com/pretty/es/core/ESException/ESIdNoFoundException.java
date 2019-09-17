package com.pretty.es.core.ESException;

/**
 * ESID不存在异常
 */
public class ESIdNoFoundException extends Exception{
    private final String userErrMsg;
    private final int errorCode;

    public ESIdNoFoundException(int errorCode, String sysErrMsg, String userErrMsg) {
        super(sysErrMsg);
        this.userErrMsg = userErrMsg;
        this.errorCode = errorCode;
    }

    public ESIdNoFoundException(int errorCode, String sysErrMsg, String userErrMsg, Throwable cause) {
        super(sysErrMsg, cause);
        this.userErrMsg = userErrMsg;
        this.errorCode = errorCode;
    }

    public ESIdNoFoundException(int errorCode, String sysErrMsg) {
        super(sysErrMsg);
        this.userErrMsg = null;
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getUserErrMsg() {
        return userErrMsg;
    }

    public String getSysErrMsg() {
        return getMessage();
    }
}
