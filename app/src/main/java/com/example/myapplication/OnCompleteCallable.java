package com.example.myapplication;

public interface OnCompleteCallable {
    /**
     * @param msg pass by completed task
     * @param success whether the task has successfully completed
     */
    void call(String msg, boolean success);
}
