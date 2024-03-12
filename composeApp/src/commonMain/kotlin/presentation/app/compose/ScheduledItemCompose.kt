package presentation.app.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import common.compose.BasicAlertDialogCompose
import common.compose.functions.getButtonBackgroundColor
import common.compose.functions.getMainTextStyle
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import presentation.app.arch.AlertDialogAction
import presentation.app.model.RemindItemModel
import remindersapp.composeapp.generated.resources.Res
import remindersapp.composeapp.generated.resources.app_scheduled_alert_delete_timer_button
import remindersapp.composeapp.generated.resources.app_scheduled_alert_done_button
import remindersapp.composeapp.generated.resources.ic_schedule

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ScheduledItemCompose(
    item: RemindItemModel,
    showDatePicker: () -> Unit,
    alertAction: (AlertDialogAction) -> Unit
) {
    alertAction(AlertDialogAction.SetScheduledEditingModel(item))
    BasicAlertDialogCompose(
        onDismissAction = {}
    ) {
        val mainTextStyle = getMainTextStyle()
        val buttonTextStyle = mainTextStyle.copy(
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Box(Modifier.fillMaxWidth()) {
            Text(
                text = item.text,
                style = mainTextStyle,
                modifier = Modifier.padding(top = 10.dp, end = 56.dp)
            )
            IconButton(
                onClick = showDatePicker,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 8.dp),
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_schedule),
                    contentDescription = "null",
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer)
                )
            }
        }
        Row(Modifier.align(Alignment.End)) {
            Button(
                onClick = { alertAction(AlertDialogAction.DeleteTimer) },
                colors = getButtonBackgroundColor()
            ) {
                Text(
                    text = stringResource(Res.string.app_scheduled_alert_delete_timer_button),
                    style = buttonTextStyle,
                )
            }
            Button(
                onClick = { alertAction(AlertDialogAction.ReminderDoneClick(item)) },
                colors = getButtonBackgroundColor()
            ) {
                Text(
                    text = stringResource(Res.string.app_scheduled_alert_done_button),
                    style = buttonTextStyle,
                )
            }
        }
    }
}