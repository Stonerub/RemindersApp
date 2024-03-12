package common.compose.functions

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp

@Composable
fun getMainTextStyle(): TextStyle {
    return TextStyle(
        fontFamily = FontFamily.SansSerif,
        letterSpacing = 0.5.sp,
        fontSize = 18.sp,
        lineHeight = 20.sp,
        color = MaterialTheme.colorScheme.onBackground
    )
}