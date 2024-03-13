package presentation.app.compose

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import common.enum.CalendarState
import common.enum.isDatePicker
import common.enum.isVisible
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import presentation.app.arch.AppAction
import presentation.app.model.RemindItemModel
import remindersapp.composeapp.generated.resources.Res
import remindersapp.composeapp.generated.resources.app_date_picker_confirm_button
import remindersapp.composeapp.generated.resources.app_date_picker_dismiss_button
import remindersapp.composeapp.generated.resources.app_time_picker_confirm_button
import remindersapp.composeapp.generated.resources.app_time_picker_dismiss_button

@OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
@Composable
fun PickersCompose(
    state: CalendarState,
    editingModel: RemindItemModel?,
    onEvent: (AppAction) -> Unit
) {
    if (state.isVisible()) {

        //Date picker state
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = editingModel?.selectedDate
                ?: Clock.System.now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .toInstant(TimeZone.currentSystemDefault())
                    .toEpochMilliseconds(),
            selectableDates = ReminderSelectableDates
        )

        //Time picker state
        val timePickerState = rememberTimePickerState(
            initialHour = editingModel?.selectedHour?.toInt() ?: 12,
            initialMinute = editingModel?.selectedMinute?.toInt() ?: 0,
            is24Hour = true
        )

        val positiveAction = when (state) {
            CalendarState.DATE_PICKER -> {
                {
                    onEvent(
                        AppAction.DatePickerPositiveClick(
                            date = datePickerState.selectedDateMillis ?: Clock
                                .System
                                .now()
                                .toEpochMilliseconds()
                        )
                    )
                }
            }

            CalendarState.TIME_PICKER -> {
                {
                    onEvent(
                        AppAction.TimePickerPositiveClick(
                            hour = timePickerState.hour,
                            minute = timePickerState.minute
                        )
                    )
                }
            }

            else -> {
                {}
            }
        }

        val negativeAction = when (state) {
            CalendarState.DATE_PICKER -> {
                { onEvent(AppAction.DismissPicker) }
            }

            CalendarState.TIME_PICKER -> {
                { onEvent(AppAction.ShowDatePicker) }
            }

            else -> {
                {}
            }
        }

        DatePickerDialog(
            onDismissRequest = { onEvent(AppAction.DismissPicker) },
            confirmButton = {
                Button(
                    onClick = positiveAction,
                ) {
                    Text(
                        text = stringResource(
                            if (state.isDatePicker())
                                Res.string.app_date_picker_confirm_button
                            else
                                Res.string.app_time_picker_confirm_button
                        )
                    )
                }
            },
            dismissButton = {
                Button(
                    onClick = negativeAction
                ) {
                    Text(
                        text = stringResource(
                            if (state.isDatePicker())
                                Res.string.app_date_picker_dismiss_button
                            else
                                Res.string.app_time_picker_dismiss_button
                        )
                    )
                }
            }
        ) {
            when (state) {
                CalendarState.DATE_PICKER -> {
                    DatePicker(state = datePickerState)
                }

                CalendarState.TIME_PICKER -> {
                    TimePicker(
                        state = timePickerState,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 30.dp)
                    )
                }

                else -> {}
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private object ReminderSelectableDates : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
        return utcTimeMillis + 86400000 >= Clock.System.now().toEpochMilliseconds()
    }

    override fun isSelectableYear(year: Int): Boolean {
        return year >= Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year
    }
}