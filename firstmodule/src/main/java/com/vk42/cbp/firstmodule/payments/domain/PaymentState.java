package com.vk42.cbp.firstmodule.payments.domain;

public enum PaymentState {
    CREATED,
    AUTHORIZED,
    SUBMITTED,
    CONFIRMED,
    FAILED,
    RECONCILED;

    public boolean canTransactionTo(PaymentState nextState) {
        return switch (this) {
            case CREATED -> nextState == AUTHORIZED || nextState == FAILED;
            case AUTHORIZED -> nextState == SUBMITTED || nextState == FAILED;
            case SUBMITTED -> nextState == CONFIRMED || nextState == FAILED;
            case FAILED -> nextState == RECONCILED;
            case CONFIRMED, RECONCILED -> false;
        };
    }
}
