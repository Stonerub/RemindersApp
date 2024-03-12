package presentation.app.model

import androidx.compose.runtime.Immutable
import common.fucntions.generateUUID
import kotlinx.datetime.LocalDateTime

@Immutable
data class RemindItemModel(
    val id: Long = 0,
    val text: String = "",
    val uuid: String = generateUUID(),
    val secondUuid: String = generateUUID(),
    val done: Boolean = false,
    val date: LocalDateTime? = null,
    val selectedDate: Long? = null,
    val selectedHour: Long? = null,
    val selectedMinute: Long? = null,
    val needValidate: Boolean = false
) {

    fun isError() = needValidate && text.isEmpty()
}