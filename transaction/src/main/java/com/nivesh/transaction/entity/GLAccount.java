package com.nivesh.transaction.entity;

import com.nivesh.transaction.entity.enums.TransactionFlow;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "gl_accounts")
@Entity
public class GLAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "gl_account_id")
    private UUID id;

    private String glCode;

    private String name;

    private Category category;

    private AccountType accountType;

    private TransactionFlow normalBalance;

    private boolean isActive;

    private enum Category{
        CUSTOMER,
        INTERNAL_GL
    }

    private enum AccountType {
        ASSET,
        LIABILITY,
        INCOME,
        EXPENSE
    }
}
