/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kose

import kotlinx.datetime.Instant
import java.text.SimpleDateFormat

fun formatTime(instant: Instant): String {
    val format = SimpleDateFormat("yyyy MMMM dd, HH:mm")
    return format.format(instant.epochSeconds * 1000L)
}