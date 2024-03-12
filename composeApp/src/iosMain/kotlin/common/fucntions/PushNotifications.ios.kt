package common.fucntions

import GENERAL_PUSH_CHANNEL
import platform.Foundation.NSCalendar
import platform.Foundation.NSDateComponents
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationCategory
import platform.UserNotifications.UNNotificationCategoryOptions
import platform.UserNotifications.UNNotificationInterruptionLevel
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound.Companion.defaultSound
import platform.UserNotifications.UNUserNotificationCenter
import presentation.app.model.PushModel

actual fun setupNotificationChannel() {
    val generalCategory = UNNotificationCategory.categoryWithIdentifier(
        identifier = GENERAL_PUSH_CHANNEL,
        actions = listOf<Any>(),
        intentIdentifiers = listOf<Any>(),
        options = UNNotificationCategoryOptions.MAX_VALUE
    )
    val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()
    notificationCenter.setNotificationCategories(setOf(generalCategory))
    notificationCenter.removeAllDeliveredNotifications()
}

actual fun sendPush(pushModel: PushModel) {
    val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()
    val requests = buildRequest(pushModel)
    notificationCenter.removePendingNotificationRequestsWithIdentifiers(requests.map { it.identifier })
    requests.forEach {
        notificationCenter.addNotificationRequest(it) {}
    }
}

actual fun deletePush(pushModel: PushModel) {
    val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()
    val identifiersList = buildRequest(pushModel).map { it.identifier() }
    notificationCenter.removePendingNotificationRequestsWithIdentifiers(
        buildRequest(pushModel).map {
            it.identifier()
        }
    )
    notificationCenter.removePendingNotificationRequestsWithIdentifiers(identifiersList)
}

private fun buildRequest(pushModel: PushModel): List<UNNotificationRequest> = pushModel.time.map {
    val content = UNMutableNotificationContent()
    content.setInterruptionLevel(UNNotificationInterruptionLevel.UNNotificationInterruptionLevelTimeSensitive)
    content.setTitle(pushModel.title)
    content.setBody(pushModel.text)
    content.setSound(defaultSound())

    val dataComponents = NSDateComponents()

    with(it.localDateTime) {
        dataComponents.calendar = NSCalendar.currentCalendar
        dataComponents.year = year.toLong()
        dataComponents.hour = hour.toLong()
        dataComponents.minute = minute.toLong()
        dataComponents.month = monthNumber.toLong()
        dataComponents.day = dayOfMonth.toLong()
    }

    val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(dataComponents, false)

    UNNotificationRequest.requestWithIdentifier(
        (pushModel.id * (pushModel.time.indexOf(it) + 1) * 1000).toString(),
        content,
        trigger
    )
}