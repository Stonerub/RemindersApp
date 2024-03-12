package presentation.app

import common.enum.CalendarState
import common.fucntions.deletePush
import common.fucntions.sendPush
import common.fucntions.setupNotificationChannel
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import presentation.app.arch.AlertDialogAction
import presentation.app.arch.AlertState
import presentation.app.arch.AppAction
import presentation.app.arch.AppState
import presentation.app.arch.PushPermissionAction
import presentation.app.model.PushModel
import presentation.app.model.RemindItemModel
import ru.reminders.app.Database
import ru.reminders.app.DatabaseDriverFactory

class AppViewModel(
    databaseDriverFactory: DatabaseDriverFactory,
    val permissionsController: PermissionsController
) : ViewModel() {

    private val dataBase = Database(databaseDriverFactory)

    private val _state = MutableStateFlow(
        AppState(
            items = dataBase.getAllReminder(),
            doneReminds = dataBase.getAllDoneReminds()
        )
    )
    val state = _state.asStateFlow()

    init {
        setupNotificationChannel()
        viewModelScope.launch {
            try {
                permissionsController.providePermission(Permission.REMOTE_NOTIFICATION)
            } catch (deniedAlways: DeniedAlwaysException) {
                // nothing
            } catch (denied: DeniedException) {
                //nothing
            }
        }
        viewModelScope.launch {
            val scheduledItems = mutableListOf<RemindItemModel>()
            _state.value.let { state ->
                state.items.forEach { model ->
                    model.selectedDate?.let {
                        val timeZone = TimeZone.currentSystemDefault()
                        if (Instant.fromEpochMilliseconds(it)
                                .toLocalDateTime(timeZone) <= Clock.System.now()
                                .toLocalDateTime(timeZone)
                        ) {
                            state.getPushModel(model)?.let {
                                deletePush(it)
                            }
                            scheduledItems.add(model)
                        }
                    }
                }
            }
            _state.update {
                it.copy(
                    scheduledItems = scheduledItems,
                    scheduleEditingModel = scheduledItems.lastOrNull()
                )
            }
        }
    }

    fun onEvent(event: AppAction) {
        when (event) {
            is AppAction.AddNewRemind -> {
                viewModelScope.launch {
                    val model = RemindItemModel(
                        id = dataBase.getLastId()
                    )
                    dataBase.createReminder(model)
                    _state.update {
                        it.copy(
                            items = it.items + model
                        )
                    }
                    onEvent(AppAction.RemindClick(model))
                }
            }

            is AppAction.RemindClick -> {
                if (_state.value.editingModel != event.model) {
                    _state.update { state ->
                        val newItems = state.items.toMutableList()
                        if (state.editingModel != null) {
                            if (state.editingModel.text.isEmpty()) {
                                newItems.remove(state.editingModel)
                            }
                        }
                        newItems[newItems.indexOf(event.model)] = event.model
                        state.copy(
                            items = newItems,
                            editingModel = event.model
                        )
                    }
                }
            }

            is AppAction.UpdateRemindText -> {
                _state.update { state ->
                    val newItems = state.items.toMutableList()
                    val newModel = state.editingModel?.copy(
                        text = event.newText
                    )
                    if (state.editingModel != null && newModel != null) {
                        val oldModel = newItems.find { it.uuid == state.editingModel.uuid }
                        newItems[newItems.indexOf(oldModel)] = newModel
                        viewModelScope.launch {
                            dataBase.updateReminder(newModel)
                        }
                    }
                    state.copy(
                        items = newItems,
                        editingModel = newModel
                    )
                }
            }

            is AppAction.OnDoneAction -> {
                _state.update {
                    val newItems = it.items.toMutableList()
                    if (it.editingModel != null) {
                        if (it.editingModel.text.isEmpty()) {
                            newItems.remove(it.editingModel)
                        }
                    }
                    it.copy(
                        items = newItems,
                        editingModel = null
                    )
                }
            }

            is AppAction.DeleteRemind -> {
                _state.update {
                    it.getPushModel(event.model)?.let {
                        deletePush(it)
                    }
                    viewModelScope.launch {
                        dataBase.deleteReminder(event.model.uuid)
                    }
                    it.copy(
                        items = if (it.items.contains(event.model)) it.items - event.model else it.items,
                        doneReminds = if (it.doneReminds.contains(event.model)) it.doneReminds - event.model else it.doneReminds,
                        editingModel = if (event.model == it.editingModel) null else it.editingModel
                    )
                }
            }

            is AppAction.DismissPicker -> {
                _state.update {
                    it.copy(
                        editingModel = it.editingModel?.copy(
                            selectedDate = null
                        ),
                        calendarState = CalendarState.HIDE
                    )
                }
            }

            is AppAction.DatePickerPositiveClick -> {
                _state.update {
                    if (it.editingModel != null) {
                        it.copy(
                            editingModel = it.editingModel.copy(
                                selectedDate = event.date
                            ),
                            calendarState = CalendarState.TIME_PICKER
                        )
                    } else if (it.scheduleEditingModel != null) {
                        it.copy(
                            scheduleEditingModel = it.scheduleEditingModel.copy(
                                selectedDate = event.date
                            ),
                            calendarState = CalendarState.TIME_PICKER
                        )
                    } else {
                        it.copy()
                    }

                }
            }

            is AppAction.TimePickerPositiveClick -> {
                _state.update {
                    val newItems = it.items.toMutableList()
                    val model = it.editingModel ?: it.scheduleEditingModel
                    model?.let { model ->
                        val oldModel = newItems.find { oldModel -> model.uuid == oldModel.uuid }
                        val dateTime = Instant.fromEpochMilliseconds(model.selectedDate ?: 0)
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                        val selectedDateTime = LocalDateTime(
                            date = LocalDate(
                                year = dateTime.year,
                                monthNumber = dateTime.monthNumber,
                                dayOfMonth = dateTime.dayOfMonth
                            ),
                            time = LocalTime(
                                hour = event.hour,
                                minute = event.minute
                            )
                        )
                        val newModel = model.copy(
                            date = selectedDateTime,
                            selectedHour = event.hour.toLong(),
                            selectedMinute = event.minute.toLong()
                        )
                        newItems[newItems.indexOf(oldModel)] = newModel
                        dataBase.updateReminder(reminder = newModel)
                        it.getPushModel(newModel)?.let { pushModel ->
                            sendPushNotification(model = pushModel)
                        }
                    }
                    it.copy(
                        items = newItems,
                        editingModel = null,
                        scheduleEditingModel = null,
                        calendarState = CalendarState.HIDE,
                        scheduledItems = if (it.scheduledItems.isNotEmpty())
                            it.scheduledItems - (it.scheduleEditingModel
                                ?: it.scheduledItems.last())
                        else
                            emptyList()
                    )
                }
            }

            is AppAction.ShowDatePicker -> {
                viewModelScope.launch {
                    if (permissionsController.isPermissionGranted(Permission.REMOTE_NOTIFICATION)) {
                        if ((_state.value.editingModel?.needValidate == true && _state.value.editingModel?.isError() == false) || _state.value.scheduleEditingModel != null) {
                            _state.update {
                                it.copy(
                                    calendarState = CalendarState.DATE_PICKER
                                )
                            }
                        } else {
                            _state.update {
                                val newEditingModel = it.editingModel?.copy(
                                    needValidate = true
                                )
                                val newItems = it.items.toMutableList()
                                if (newEditingModel != null) {
                                    val oldModel =
                                        newItems.find { model -> model.uuid == newEditingModel.uuid }
                                    newItems[newItems.indexOf(oldModel)] = newEditingModel
                                }
                                it.copy(
                                    items = newItems,
                                    editingModel = newEditingModel
                                )
                            }
                            if (_state.value.editingModel?.isError() == false) {
                                _state.update {
                                    it.copy(
                                        calendarState = CalendarState.DATE_PICKER
                                    )
                                }
                            }
                        }
                    } else {
                        _state.update { it.copy(alertState = AlertState.NO_PERMISSIONS) }
                    }
                }
            }

            is AppAction.ShowTimePicker -> {
                _state.update {
                    it.copy(
                        calendarState = CalendarState.TIME_PICKER
                    )
                }
            }

            is AppAction.UpdatePushText -> {
                _state.value.getPushModel(event.model)?.let {
                    sendPushNotification(model = it)
                }
            }

            is AppAction.ScheduledAlertDialogAction -> {
                when (event.action) {
                    is AlertDialogAction.DeleteTimer -> {
                        _state.update {
                            val newItems = it.items.toMutableList()
                            val newModel = it.scheduledItems.last().copy(
                                date = null,
                                selectedDate = null,
                                selectedHour = null,
                                selectedMinute = null
                            )
                            val oldModel = newItems.find { it.uuid == newModel.uuid }
                            newItems[newItems.indexOf(oldModel)] = newModel
                            viewModelScope.launch {
                                dataBase.updateReminder(newModel)
                            }
                            it.copy(items = newItems)
                        }
                        deleteLastScheduledItem()
                    }

                    is AlertDialogAction.SetScheduledEditingModel -> {
                        _state.update {
                            it.copy(scheduleEditingModel = event.action.model)
                        }
                    }

                    is AlertDialogAction.ReminderDoneClick -> {
                        _state.update {
                            event.action.model?.let { model ->
                                it.getPushModel(model)?.let {
                                    deletePush(it)
                                }
                                val newModel = model.copy(
                                    done = !model.done,
                                    selectedDate = null,
                                    date = null,
                                    selectedHour = null,
                                    selectedMinute = null
                                )
                                viewModelScope.launch {
                                    dataBase.updateDoneStatus(newModel.uuid, newModel.done)
                                }
                                it.copy(
                                    items = it.items - model,
                                    scheduledItems = it.scheduledItems - model,
                                    doneReminds = it.doneReminds + newModel
                                )
                            } ?: it.copy(items = it.items)
                        }
                    }

                    else -> {}
                }
            }

            is AppAction.PushPermissionAlertDialogAction -> {
                when (event.action) {
                    is PushPermissionAction.HideAlertDialog -> {
                        _state.update { it.copy(alertState = AlertState.NONE) }
                    }

                    is PushPermissionAction.OpenSettings -> {
                        viewModelScope.launch {
                            permissionsController.openAppSettings()
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    private fun sendPushNotification(model: PushModel) {
        viewModelScope.launch {
            if (permissionsController.isPermissionGranted(Permission.REMOTE_NOTIFICATION)) {
                sendPush(model)
            }
        }
    }

    private fun deleteLastScheduledItem() {
        _state.update {
            it.copy(scheduledItems = it.scheduledItems - it.scheduledItems.last())
        }
    }
}