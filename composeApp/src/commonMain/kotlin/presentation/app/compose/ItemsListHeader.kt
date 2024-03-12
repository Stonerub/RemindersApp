package presentation.app.compose

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import common.compose.functions.getMainTextStyle

@Composable
fun ItemsListHeaderCompose(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        style = getMainTextStyle().copy(
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp
        )
    )
}