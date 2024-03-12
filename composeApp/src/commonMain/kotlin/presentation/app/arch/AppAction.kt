package presentation.app.arch

import presentation.app.model.RemindItemModel

sealed class AppAction {
    data object AddNewRemind : AppAction()
    class RemindClick(val model: RemindItemModel) : AppAction()
    data class UpdateRemindText(val newText: String) : AppAction()
    data object OnDoneAction : AppAction()
    class DeleteRemind(val model: RemindItemModel) : AppAction()
    data object DismissPicker : AppAction()
    data class DatePickerPositiveClick(val date: Long) : AppAction()
    data class TimePickerPositiveClick(val hour: Int, val minute: Int) : AppAction()
    data object ShowDatePicker : AppAction()
    data object ShowTimePicker : AppAction()
    data class UpdatePushText(val model: RemindItemModel) : AppAction()
    data class ScheduledAlertDialogAction(val action: AlertDialogAction?) : AppAction()
    data class PushPermissionAlertDialogAction(val action: PushPermissionAction?) : AppAction()
}

sealed class AlertDialogAction {
    data object DeleteTimer : AlertDialogAction()
    data class SetScheduledEditingModel(val model: RemindItemModel) : AlertDialogAction()
    data class ReminderDoneClick(val model: RemindItemModel?) : AlertDialogAction()
}

sealed class PushPermissionAction {
    data object HideAlertDialog : PushPermissionAction()
    data object OpenSettings : PushPermissionAction()
}