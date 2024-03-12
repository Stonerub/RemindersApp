package presentation.app.arch

import androidx.compose.runtime.Immutable
import common.enum.CalendarState
import presentation.app.model.RemindItemModel
import common.enum.LoadingState
import presentation.app.model.PushModel
import presentation.app.model.ScheduleTimeModel

@Immutable
data class AppState(
    val items: List<RemindItemModel> = emptyList(),
    val doneReminds: List<RemindItemModel> = emptyList(),
    val scheduledItems: List<RemindItemModel> = emptyList(),
    val editingModel: RemindItemModel? = null,
    val scheduleEditingModel: RemindItemModel? = null,
    val loadingState: LoadingState = LoadingState.NONE,
    val calendarState: CalendarState = CalendarState.HIDE,
    val alertState: AlertState = AlertState.NONE
) {
    fun isRefreshing() = loadingState == LoadingState.REFRESHING

    fun showDialog() = alertState != AlertState.NONE

    fun showUndoneHeader() = items.isNotEmpty()

    fun showDoneHeader() = doneReminds.isNotEmpty()

    fun getPushModel(model: RemindItemModel): PushModel? = model.date?.let {
        return PushModel(
            id = model.id,
            text = model.text,
            time = ScheduleTimeModel().getScheduleTimeModel(it)
        )
    }
}

enum class AlertState {
    NONE,
    NO_PERMISSIONS
}
