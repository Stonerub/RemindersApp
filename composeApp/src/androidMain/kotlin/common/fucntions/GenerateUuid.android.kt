package common.fucntions

import java.util.UUID

actual fun generateUUID(): String = UUID.randomUUID().toString()