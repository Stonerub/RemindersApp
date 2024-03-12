package common.fucntions

import platform.Foundation.NSUUID

actual fun generateUUID(): String = NSUUID().UUIDString()