import androidx.compose.ui.window.ComposeUIViewController
import presentation.app.App
import common.compose.AppTheme
import ru.reminders.app.DatabaseDriverFactory

fun MainViewController() = ComposeUIViewController {
    val driverFactory = DatabaseDriverFactory()
    AppTheme {
        App(driverFactory)
    }
}