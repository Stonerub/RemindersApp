package presentation.app.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

data class PushModel(
    val id: Long,
    val title: String = "RemindersApp",
    val text: String,
    val time: List<ScheduleTimeModel>,
)

data class ScheduleTimeModel(
    val date: Long = 0,
    val localDateTime: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
) {
    fun getScheduleTimeModel(selectedDateTime: LocalDateTime): List<ScheduleTimeModel> {
        val items = mutableListOf<ScheduleTimeModel>()
        val date = selectedDateTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        repeat(PUSH_COUNT) {
            val newDate = date + (it * 60000 * 5)
            val newLocalDateTime = Instant.fromEpochMilliseconds(newDate)
                .toLocalDateTime(TimeZone.currentSystemDefault())
            items.add(
                ScheduleTimeModel(
                    date = newDate,
                    localDateTime = newLocalDateTime,
                )
            )
        }
        return items
    }
}

private const val PUSH_COUNT = 3
