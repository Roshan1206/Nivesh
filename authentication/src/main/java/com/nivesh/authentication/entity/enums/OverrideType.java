package com.nivesh.authentication.entity.enums;

/**
 * Represent whether a per-user permission override adds or remove a permission
 */
public enum OverrideType {
    /** Permission override explicitly grants access. */
    GRANT,

    /** Permission override explicitly revokes access. */
    REVOKE
}
