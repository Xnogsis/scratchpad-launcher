package app.olauncher.helper

import java.io.InputStream
import java.nio.ByteBuffer

object ScratchpadBackup {
    const val MAX_BYTES = 1024 * 1024

    fun read(input: InputStream): String {
        val bytes = ByteArray(MAX_BYTES + 1)
        var size = 0
        while (size < bytes.size) {
            val count = input.read(bytes, size, bytes.size - size)
            if (count < 0) break
            size += count
        }
        require(size <= MAX_BYTES) { "Backup exceeds 1 MiB" }
        val text = Charsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bytes, 0, size)).toString().removePrefix("\uFEFF")
        require('\u0000' !in text) { "Backup is not a text file" }
        return text
    }

    fun encode(text: String): ByteArray = text.toByteArray(Charsets.UTF_8).also {
        require(it.size <= MAX_BYTES && '\u0000' !in text) { "Backup must be UTF-8 text up to 1 MiB" }
    }
}
