package com.tce.smart.common.core.model;

import java.util.concurrent.atomic.AtomicInteger;

public class Count {
    private AtomicInteger success = new AtomicInteger(0);
    private AtomicInteger fail = new AtomicInteger(0);

    public void successCount() {
        success.incrementAndGet();
    }
    public void successOrFail(Integer ret) {
        if (ret > 0) {
            successCount();
        } else {
            failCount();
        }
    }

    public void failCount() {
        fail.incrementAndGet();
    }
    public void failCount(String lastErrorMessage) {
        fail.incrementAndGet();
    }

    public void successDiscount() {
        success.decrementAndGet();
    }

    public void failDecrease() {
        fail.decrementAndGet();
    }

    public void failCount(int i) {
        fail.addAndGet(i);
    }

    public int success(){
        return success.intValue();
    }
    public int fail(){
        return fail.intValue();
    }

}
