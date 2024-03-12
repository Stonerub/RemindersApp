package common.fucntions

import presentation.app.model.PushModel

expect fun setupNotificationChannel()

expect fun sendPush(pushModel: PushModel)

expect fun deletePush(pushModel: PushModel)