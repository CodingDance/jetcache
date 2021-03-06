package com.alicp.jetcache;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

/**
 * Created on 2016/9/28.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheResult {

    public static final String MSG_ILLEGAL_ARGUMENT = "illegal argument";

    public static final CacheResult SUCCESS_WITHOUT_MSG = new CacheResult(CacheResultCode.SUCCESS, null);
    public static final CacheResult PART_SUCCESS_WITHOUT_MSG = new CacheResult(CacheResultCode.PART_SUCCESS, null);
    public static final CacheResult FAIL_WITHOUT_MSG = new CacheResult(CacheResultCode.FAIL, null);
    public static final CacheResult FAIL_ILLEGAL_ARGUMENT = new CacheResult(CacheResultCode.FAIL, MSG_ILLEGAL_ARGUMENT);
    public static final CacheResult EXISTS_WITHOUT_MSG = new CacheResult(CacheResultCode.EXISTS, null);

    private CacheResultCode resultCode;
    private String message;
    private CompletionStage<ResultData> future;

    public CacheResult(CompletionStage<ResultData> future) {
        this.future = future;
    }

    public CacheResult(CacheResultCode resultCode, String message) {
        this(CompletableFuture.completedFuture(new ResultData(resultCode, message, null)));
    }

    public CacheResult(Throwable ex) {
        future = CompletableFuture.completedFuture(new ResultData(ex));
    }

    public boolean isSuccess() {
        return getResultCode() == CacheResultCode.SUCCESS;
    }

    protected void waitForResult() {
        if (resultCode != null) {
            return;
        }
        try {
            ResultData resultData = future.toCompletableFuture().get();
            fetchResultSuccess(resultData);
        } catch (InterruptedException | ExecutionException e) {
            fetchResultFail();
        }
    }

    protected void fetchResultSuccess(ResultData resultData) {
        resultCode = resultData.getResultCode();
        message = resultData.getMessage();
    }

    protected void fetchResultFail() {
        resultCode = CacheResultCode.FAIL;
        message = null;
    }

    public CacheResultCode getResultCode() {
        waitForResult();
        return resultCode;
    }

    public String getMessage() {
        waitForResult();
        return message;
    }

    public CompletionStage<ResultData> future() {
        return future;
    }
}
