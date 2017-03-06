package de.eiswind.xino.datalayer.entities

import de.eiswind.xino.datalayer.hibernate.JsonBinaryType
import de.eiswind.xino.spring.jpa.tenancy.ContextProvider
import de.eiswind.xino.spring.jpa.tenancy.TenantProvider
import org.hibernate.annotations.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import javax.persistence.*
import javax.persistence.AccessType
import javax.validation.constraints.NotNull

@MappedSuperclass
@FilterDef(name = "tenantFilter", parameters = arrayOf(ParamDef(name = "tenant", type = "string")))
@Filters(Filter(name = "tenantFilter", condition = ":tenant = tenant"))
@Access(AccessType.PROPERTY)
@TypeDef(name = "jsonb", typeClass = JsonBinaryType::class)
@EntityListeners(AuditingEntityListener::class)
abstract class BaseTenantEntity {


    open var version = 0
        @Version
        @Column(name = "version", nullable = false) get


    open var creationDate: LocalDateTime = LocalDateTime.now()
        @Column(name = "creation_date", nullable = false)
        @CreatedDate @NotNull get


    open var lastModificationDate: LocalDateTime = LocalDateTime.now()
        @LastModifiedDate @NotNull
        @Column(name = "last_modification_date", nullable = false) get

    open var lastModificationBy: String = ""
        @LastModifiedBy
        @Column(name = "last_modification_by", nullable = false) get

    open var tenant = ""
        @Column(name = "tenant", nullable = false) @NotNull get

    @PrePersist
    open fun tenantPrePersist() {
        val cc = ContextProvider.context
        this.tenant = cc.getBean(TenantProvider::class.java).currentTenant()
    }

}


