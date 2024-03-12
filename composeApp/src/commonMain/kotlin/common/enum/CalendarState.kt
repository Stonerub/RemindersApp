package common.enum

enum class CalendarState {
    HIDE,
    DATE_PICKER,
    TIME_PICKER
}

fun CalendarState.isVisible(): Boolean = this != CalendarState.HIDE

fun CalendarState.isDatePicker(): Boolean = this == CalendarState.DATE_PICKER

fun CalendarState.isTimePicker(): Boolean = this == CalendarState.TIME_PICKER
