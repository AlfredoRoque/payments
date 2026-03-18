package com.rorideas.payments.enums;

import lombok.Getter;

/**
 * Enum representing the possible payment statuses in the payment processing system.
 * Each status indicates a specific state of a payment transaction, such as whether it has been paid, canceled, pending cancellation, pending confirmation, or failed.
 */
@Getter
public enum PaymentStatus {
    PAGADO,CANCELADO, PENDIENTE_CANCELAR, PENDIENTE_CONFIRMAR, FALLIDO
}
