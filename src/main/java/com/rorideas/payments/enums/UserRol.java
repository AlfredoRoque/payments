package com.rorideas.payments.enums;

import lombok.Getter;

/**
 * Enum that represents the different roles a user can have in the system. It is used to define the access level and permissions of each user.
 * ADMIN: This role has full access to the system, including the ability to manage users, patients, and fluid balances. Users with this role can perform all operations,
 *        such as creating, reading, updating, and deleting records.
 * USER: This role has limited access to the system, typically restricted to viewing and managing their
 */
@Getter
public enum UserRol {
    ADMIN, PATIENT
}
