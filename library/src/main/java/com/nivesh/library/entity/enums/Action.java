package com.nivesh.library.entity.enums;

/**
 * Defines authorization actions available to authenticated users.
 *
 * @author Roshan
 */
public enum Action {
    /** Permission to view/retrieve data */
    READ,

    /** Permission to create or modify data */
    WRITE,

    /** Permission to approve transactions or requests */
    APPROVE,

    /** Administrative permissions */
    ADMIN
}
