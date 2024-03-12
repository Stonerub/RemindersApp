package presentation.app.compose

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.LocalDateTime

@Composable
fun DateTimeCompose(date: LocalDateTime) {
    Card(
        modifier = Modifier.padding(horizontal = 16.dp).padding(top = 8.dp, start = 8.dp),
        colors = CardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
            disabledContentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        val minutes = if (date.minute < 10) {
            "0${date.minute}"
        } else {
            date.minute.toString()
        }
        Text(
            text = "${date.dayOfMonth} ${date.month.name.lowercase()} ${date.hour}:${minutes}",
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 4.dp),
            style = TextStyle(
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 0.5.sp,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            )
        )
    }
}