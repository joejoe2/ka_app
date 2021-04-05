package com.example.myapplication.util;

public interface OnCompleteCallable {
    /**
     * @param msg pass by completed task
     * @param success whether the task has successfully completed
     */
    void doOnComplete(String msg, boolean success);
}
