package de.eiswind.xino.customtypes

import org.jooq.Converter
import java.sql.Timestamp
import java.time.LocalDateTime

class LocalDateTimeConverter : Converter<Timestamp, LocalDateTime> {



    override fun from(t: Timestamp?): LocalDateTime? {

        return if (t == null)
            null
        else
            t.toLocalDateTime()

    }

    override fun to(u:  LocalDateTime?): Timestamp? {
        return if (u == null )
            null
        else
            Timestamp.valueOf(u)

    }

    override fun fromType(): Class<Timestamp> {
        return Timestamp::class.java
    }

    override fun toType(): Class<LocalDateTime> {
        return LocalDateTime::class.java
    }
}

