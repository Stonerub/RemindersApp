package presentation.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import common.compose.TopAppBarCompose
import dev.icerock.moko.mvvm.compose.getViewModel
import dev.icerock.moko.mvvm.compose.viewModelFactory
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.PermissionsControllerFactory
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import presentation.app.arch.AppAction
import presentation.app.compose.DeniedPushPermissionDialogCompose
import presentation.app.compose.ItemsListHeaderCompose
import presentation.app.compose.PickersCompose
import presentation.app.compose.RemindItemCompose
import presentation.app.compose.RemindViewMode
import presentation.app.compose.ScheduledItemCompose
import remindersapp.composeapp.generated.resources.Res
import remindersapp.composeapp.generated.resources.app_done_items_header
import remindersapp.composeapp.generated.resources.app_title
import remindersapp.composeapp.generated.resources.app_undone_items_header
import remindersapp.composeapp.generated.resources.ic_add
import ru.reminders.app.DatabaseDriverFactory

@OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalResourceApi::class,
)
@Composable
fun App(
    databaseDriverFactory: DatabaseDriverFactory,
) {

    val factory: PermissionsControllerFactory = rememberPermissionsControllerFactory()
    val viewModel = getViewModel(
        key = "app viewModel",
        factory = viewModelFactory {
            AppViewModel(databaseDriverFactory, factory.createPermissionsController())
        }
    )

    BindEffect(viewModel.permissionsController)

    val coroutineScope = rememberCoroutineScope()

    val state by viewModel.state.collectAsState()

    val pullRefreshState = rememberPullRefreshState(
        state.isRefreshing(),
        { viewModel.onEvent(AppAction.AddNewRemind) })

    val listState = rememberLazyListState()

    coroutineScope.launch {
        listState.animateScrollToItem(state.doneReminds.size + state.items.size + 1)
    }

    Box {
        Scaffold(
            topBar = {
                TopAppBarCompose(title = stringResource(Res.string.app_title))
            },
            bottomBar = {
                FloatingActionButton(
                    onClick = {
                        viewModel.onEvent(AppAction.AddNewRemind)
                    },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .navigationBarsPadding()
                        .padding(bottom = 6.dp),
                    content = {
                        Image(
                            painter = painterResource(Res.drawable.ic_add),
                            contentDescription = null
                        )
                    }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.pullRefresh(pullRefreshState)) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = paddingValues.calculateTopPadding()),
                    state = listState,
                    reverseLayout = true,
                    verticalArrangement = Arrangement.Top,
                ) {

                    //Bottom padding because of edge to edge
                    item {
                        Box(Modifier.height(paddingValues.calculateBottomPadding()))
                    }

                    //Done reminders list
                    items(
                        items = state.doneReminds,
                        key = { it.secondUuid }
                    ) { model ->
                        Column {
                            RemindItemDivider()
                            RemindItemCompose(
                                model = model,
                                viewMode = RemindViewMode.DONE,
                                onEvent = { viewModel.onEvent(it) }
                            )
                        }
                    }

                    item {
                        AnimatedVisibility(
                            visible = state.showDoneHeader(),
                        ) {
                            ItemsListHeaderCompose(text = stringResource(Res.string.app_done_items_header))
                            if (state.showUndoneHeader()) Spacer(Modifier.height(10.dp))
                        }
                    }

                    //Actual reminders list
                    items(
                        items = state.items,
                        key = { it.uuid }
                    ) { model ->
                        Column(
                            modifier = Modifier.clickable {
                                viewModel.onEvent(
                                    AppAction.RemindClick(model)
                                )
                            }
                        ) {
                            val viewMode = if (model != state.editingModel)
                                RemindViewMode.VIEW
                            else
                                RemindViewMode.EDIT

                            RemindItemDivider()
                            RemindItemCompose(
                                model = model,
                                viewMode = viewMode,
                                onEvent = { viewModel.onEvent(it) }
                            )
                        }
                    }

                    item {
                        AnimatedVisibility(
                            visible = state.showUndoneHeader(),
                        ) {
                            ItemsListHeaderCompose(text = stringResource(Res.string.app_undone_items_header))
                        }
                    }
                }
                PullRefreshIndicator(
                    refreshing = state.isRefreshing(),
                    state = pullRefreshState,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = paddingValues.calculateTopPadding())
                )

                //Date time pickers
                PickersCompose(
                    state = state.calendarState,
                    editingModel = state.editingModel ?: state.scheduleEditingModel,
                    onEvent = { viewModel.onEvent(it) }
                )

                //No permissions dialog
                if (state.showDialog()) {
                    DeniedPushPermissionDialogCompose(
                        action = { viewModel.onEvent(AppAction.PushPermissionAlertDialogAction(it)) }
                    )
                }
            }
        }

        //Scheduled reminders dialog
        if (state.scheduledItems.isNotEmpty()) {
            with(state.scheduledItems.last()) {
                ScheduledItemCompose(
                    item = this,
                    showDatePicker = { viewModel.onEvent(AppAction.ShowDatePicker) },
                    alertAction = { viewModel.onEvent(AppAction.ScheduledAlertDialogAction(it)) }
                )
            }
        }
    }
}

@Composable
private fun RemindItemDivider() {
    HorizontalDivider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        thickness = 0.25.dp,
        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(
            alpha = 0.5f
        )
    )
}