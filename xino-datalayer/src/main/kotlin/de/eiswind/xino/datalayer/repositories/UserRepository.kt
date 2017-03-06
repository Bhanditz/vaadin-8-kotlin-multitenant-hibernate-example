package de.eiswind.xino.datalayer.repositories

import de.eiswind.xino.datalayer.entities.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.QueryHints
import javax.persistence.QueryHint


/**
 * Created by thomas on 16.05.16.
 */
interface UserRepository : JpaRepository<User, Long> {

    @QueryHints(QueryHint(name = org.hibernate.jpa.QueryHints.HINT_READONLY, value = "true"))
    fun findByEmail(email: String?): User?


    @QueryHints(QueryHint(name = org.hibernate.jpa.QueryHints.HINT_READONLY, value = "true"))
    override fun findAll(): List<User>
}
