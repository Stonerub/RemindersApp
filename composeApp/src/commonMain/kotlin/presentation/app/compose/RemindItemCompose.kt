package presentation.app.compose

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import common.compose.SwipeToDismissCompose
import common.compose.functions.getMainTextStyle
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import presentation.app.arch.AlertDialogAction
import presentation.app.arch.AppAction
import presentation.app.model.RemindItemModel
import remindersapp.composeapp.generated.resources.Res
import remindersapp.composeapp.generated.resources.ic_delete
import remindersapp.composeapp.generated.resources.ic_done
import remindersapp.composeapp.generated.resources.ic_schedule

const val ANIMATION_DURATION = 200

@OptIn(ExperimentalResourceApi::class)
@Composable
fun RemindItemCompose(
    model: RemindItemModel,
    viewMode: RemindViewMode,
    onEvent: (AppAction) -> Unit,
) {
    val textStyle = getMainTextStyle()

    val deleteAction: () -> Unit
    val icon: DrawableResource
    val color: Color
    if (!viewMode.isDone() && model.date != null) {
        deleteAction = { onEvent(AppAction.ScheduledAlertDialogAction(AlertDialogAction.ReminderDoneClick(model))) }
        icon = Res.drawable.ic_done
        color = MaterialTheme.colorScheme.primary
    } else {
        deleteAction = { onEvent(AppAction.DeleteRemind(model)) }
        icon = Res.drawable.ic_delete
        color = MaterialTheme.colorScheme.error
    }

    SwipeToDismissCompose(
        onDelete = deleteAction,
        dismissIcon = icon,
        color = color,
        content = {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxWidth()
            ) {
                model.date?.let {
                    DateTimeCompose(it)
                }
                when (viewMode) {
                    RemindViewMode.VIEW, RemindViewMode.DONE -> {
                        Text(
                            text = model.text,
                            style = textStyle,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 17.dp)
                                .padding(end = 40.dp, top = if (model.date != null) 28.dp else 0.dp, start = 10.dp)
                        )
                    }

                    RemindViewMode.EDIT -> {
                        var shouldUpdatePush by remember { mutableStateOf(false) }
                        val focusRequester = remember { FocusRequester() }
                        LaunchedEffect(Unit) {
                            focusRequester.requestFocus()
                        }
                        TextField(
                            value = model.text,
                            onValueChange = {
                                if (model.date != null) {
                                    shouldUpdatePush = true
                                }
                                onEvent(AppAction.UpdateRemindText(it))
                            },
                            textStyle = textStyle,
                            isError = model.isError(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                disabledBorderColor = Color.Transparent,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 40.dp, top = if (model.date != null) 28.dp else 0.dp, start = 10.dp)
                                .focusRequester(focusRequester)
                                .onFocusChanged {
                                    if (!it.isFocused && shouldUpdatePush) onEvent(
                                        AppAction.UpdatePushText(model)
                                    )
                                },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = { onEvent(AppAction.OnDoneAction) }
                            ),
                        )
                    }
                }
                val animatedAlpha by animateFloatAsState(
                    targetValue = if (viewMode.showIcon()) 1.0f else 0f,
                    animationSpec = tween(durationMillis = ANIMATION_DURATION, easing = LinearEasing),
                    label = "alpha"
                )
                IconButton(
                    onClick = { if (viewMode.showIconEnableClick()) onEvent(AppAction.ShowDatePicker) },
                    enabled = !viewMode.isDone(),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = if (model.date != null) 32.dp else 4.dp,end = 8.dp)
                        .graphicsLayer { alpha = animatedAlpha }
                ) {
                    viewMode.resource?.let {
                        val colorFilter = ColorFilter.tint(
                            if (viewMode.isDone())
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Image(
                            painter = painterResource(it),
                            contentDescription = "item action icon",
                            colorFilter = colorFilter
                        )
                    }
                }
            }
        }
    )
}

enum class RemindViewMode @OptIn(ExperimentalResourceApi::class) constructor(val resource: DrawableResource? = null) {
    VIEW,
    @OptIn(ExperimentalResourceApi::class)
    EDIT(Res.drawable.ic_schedule),
    @OptIn(ExperimentalResourceApi::class)
    DONE(Res.drawable.ic_done)
}

fun RemindViewMode.showIcon(): Boolean = this != RemindViewMode.VIEW

fun RemindViewMode.showIconEnableClick() : Boolean = this == RemindViewMode.EDIT

fun RemindViewMode.isDone(): Boolean = this == RemindViewMode.DONE
