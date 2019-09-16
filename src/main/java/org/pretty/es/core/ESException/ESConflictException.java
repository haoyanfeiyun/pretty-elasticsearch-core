package org.pretty.es.core.ESException;

/**
 * ES版本冲突异常，仅针对于ESId重复插入的问题
 */
public class ESConflictException extends Exception{
    private final String userErrMsg;
    private final int errorCode;

    public ESConflictException(int errorCode, String sysErrMsg, String userErrMsg) {
        super(sysErrMsg);
        this.userErrMsg = userErrMsg;
        this.errorCode = errorCode;
    }

    public ESConflictException(int errorCode, String sysErrMsg, String userErrMsg, Throwable cause) {
        super(sysErrMsg, cause);
        this.userErrMsg = userErrMsg;
        this.errorCode = errorCode;
    }

    public ESConflictException(int errorCode, String sysErrMsg) {
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
