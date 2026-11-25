package com.scc.ui;

public class OperationCancelledException extends RuntimeException {
    public OperationCancelledException() {
        super("Operation cancelled by user.");
    }
}