package com.tce.smart.platform.client.supplier;

/** 局部HTTP拒绝只携带固定状态，不接收扫码或人员资料作为错误文本。 */
public final class SupplierAccessHttpException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final int status;
    public SupplierAccessHttpException(int status) { super("供应商通行请求未完成"); this.status = status; }
    public int getStatus() { return status; }
}
