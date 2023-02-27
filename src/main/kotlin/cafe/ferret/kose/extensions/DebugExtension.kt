/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kose.extensions

import com.kotlindiscord.kord.extensions.checks.types.CheckContext
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.utils.env
import dev.kord.common.entity.Snowflake
import dev.kord.core.event.Event

class DebugExtension : Extension() {
    override val name = "debug"

    override suspend fun setup() {

    }
}

suspend fun <T : Event> CheckContext<T>.isDeveloper(arg: suspend () -> Snowflake) {
    if (!passed) {
        return
    }

    val id = arg()

    if (id != Snowflake(env("DEVELOPER").toLong())) {
        fail()
    } else {
        pass()
    }
}