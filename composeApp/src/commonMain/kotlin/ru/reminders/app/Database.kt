package ru.reminders.app

import common.toIntOrNull
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import presentation.app.model.RemindItemModel

internal class Database(databaseDriverFactory: DatabaseDriverFactory) {
    private val database = AppDatabase(databaseDriverFactory.createDriver())
    private val dbQuery = database.appDatabaseQueries

    internal fun deleteReminder(uuid: String) {
        dbQuery.transaction {
            deleteBaseItem(uuid)
        }
    }

    private fun deleteBaseItem(uuid: String) {
        dbQuery.deleteReminder(uuid)
    }

    internal fun updateReminder(reminder: RemindItemModel) {
        dbQuery.transaction {
            insertUpdate(reminder)
        }
    }

    private fun insertUpdate(reminder: RemindItemModel) {
        dbQuery.updateReminder(
            reminderText = reminder.text,
            year = reminder.date?.year?.toLong(),
            month = reminder.date?.monthNumber?.toLong(),
            day = reminder.date?.dayOfMonth?.toLong(),
            hour = reminder.date?.hour?.toLong(),
            minute = reminder.date?.minute?.toLong(),
            uuid = reminder.uuid
        )
    }

    internal fun getAllReminder(): List<RemindItemModel> {
        return dbQuery.getUndoneReminders()
            .executeAsList()
            .map {
                val selectedDateTime = if (it.year != null) LocalDateTime(
                    date = LocalDate(
                        year = it.year.toIntOrNull() ?: 0,
                        monthNumber = it.month?.toIntOrNull() ?: 1,
                        dayOfMonth = it.day?.toIntOrNull() ?: 0
                    ),
                    time = LocalTime(
                        hour = it.hour?.toIntOrNull() ?: 0,
                        minute = it.minute?.toIntOrNull() ?: 0
                    )
                ) else null
                RemindItemModel(
                    id = it.id,
                    uuid = it.uuid,
                    text = it.reminderText,
                    date = selectedDateTime,
                    selectedDate = selectedDateTime?.toInstant(TimeZone.currentSystemDefault())
                        ?.toEpochMilliseconds(),
                    selectedHour = selectedDateTime?.hour?.toLong(),
                    selectedMinute = selectedDateTime?.minute?.toLong()
                )
            }
    }

    internal fun getAllDoneReminds(): List<RemindItemModel> {
        return dbQuery.getDoneReminders()
            .executeAsList()
            .map {
                RemindItemModel(
                    id = it.id,
                    uuid = it.uuid,
                    text = it.reminderText,
                )
            }
    }

    internal fun createReminder(reminder: RemindItemModel) {
        dbQuery.transaction {
            insertReminder(reminder)
        }
    }

    private fun insertReminder(reminder: RemindItemModel) {
        dbQuery.insertReminder(
            uuid = reminder.uuid,
            secondUuid = reminder.secondUuid,
            reminderText = reminder.text
        )
    }

    internal fun getIdByUuid(uuid: String): Long {
        return dbQuery.getIdByUuid(uuid).executeAsOne().id
    }

    internal fun updateDoneStatus(uuid: String, done: Boolean) {
        dbQuery.transaction {
            updateDone(uuid, done)
        }
    }

    private fun updateDone(uuid: String, done: Boolean) {
        dbQuery.updateDoneStatus(
            done = done,
            uuid = uuid
        )
    }

    internal fun getLastId(): Long {
        return (dbQuery.getLastId().executeAsList().size + 1).toLong()
    }
}