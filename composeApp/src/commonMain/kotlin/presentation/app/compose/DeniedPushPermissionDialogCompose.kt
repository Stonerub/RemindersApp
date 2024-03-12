package presentation.app.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import common.compose.BasicAlertDialogCompose
import common.compose.functions.getButtonBackgroundColor
import common.compose.functions.getMainTextStyle
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import presentation.app.arch.PushPermissionAction
import remindersapp.composeapp.generated.resources.Res
import remindersapp.composeapp.generated.resources.app_permissions_alert_dismiss_button_text
import remindersapp.composeapp.generated.resources.app_permissions_alert_settings_button_text
import remindersapp.composeapp.generated.resources.app_permissions_alert_text

@OptIn(ExperimentalResourceApi::class)
@Composable
fun DeniedPushPermissionDialogCompose(action: (PushPermissionAction) -> Unit) {
    val mainTextStyle = getMainTextStyle()
    val buttonTextStyle = mainTextStyle.copy(
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.primary
    )
    val dismissAction = { action(PushPermissionAction.HideAlertDialog) }

    BasicAlertDialogCompose(
        onDismissAction = dismissAction,
        content = {
            Text(
                text = stringResource(Res.string.app_permissions_alert_text),
                style = mainTextStyle,
                textAlign = TextAlign.Start
            )
            Spacer(Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        action(PushPermissionAction.OpenSettings)
                        dismissAction.invoke()
                    },
                    colors = getButtonBackgroundColor()
                ) {
                    Text(
                        text = stringResource(Res.string.app_permissions_alert_settings_button_text),
                        style = buttonTextStyle
                    )
                }
                Button(
                    onClick = dismissAction,
                    colors = getButtonBackgroundColor()
                ) {
                    Text(
                        text = stringResource(Res.string.app_permissions_alert_dismiss_button_text),
                        style = buttonTextStyle
                    )
                }
                Spacer(Modifier.width(8.dp))
            }
        }
    )
}