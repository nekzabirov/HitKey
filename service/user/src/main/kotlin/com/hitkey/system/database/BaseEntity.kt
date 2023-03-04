package com.hitkey.system.database

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDate

abstract class BaseEntity {
    @Id
    @Column("id")
    var id: Long = 0
        private set

    @CreatedDate
    var dateCreated: LocalDate = LocalDate.now()
        private set


    @LastModifiedDate
    var lastModifiedDate: LocalDate = LocalDate.now()
        private set
}