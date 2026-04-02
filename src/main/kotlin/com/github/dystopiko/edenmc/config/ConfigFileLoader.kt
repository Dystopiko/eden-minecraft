package com.github.dystopiko.edenmc.config

import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.TomlIndentation
import com.akuleshov7.ktoml.TomlInputConfig
import com.akuleshov7.ktoml.TomlOutputConfig
import com.akuleshov7.ktoml.exceptions.TomlDecodingException
import com.akuleshov7.ktoml.file.TomlFileReader
import com.github.dystopiko.edenmc.EdenMod.ID
import com.github.dystopiko.edenmc.EdenMod.logger
import com.github.dystopiko.edenmc.exceptions.InternalException
import com.github.dystopiko.edenmc.config.EdenModConfig
import kotlinx.serialization.serializer
import net.fabricmc.loader.api.FabricLoader
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

class ConfigFileLoader(val file: Path) {
    fun exists(): Boolean = file.exists()

    fun load(): EdenModConfig {
        if (!this.exists()) {
            this.createFolder()
            this.saveDefault()
            logger.info("Saved default config file at: ${file.toAbsolutePath()}")
        }

        return try {
            TomlFileReader.decodeFromFile(
                toml.serializersModule.serializer(),
                file.toString()
            )
        } catch (e: TomlDecodingException) {
            error(
                """Failed to load config file at: ${file.toAbsolutePath()}
                
                ${e.message ?: "Unknown parse error"}
                """.trimIndent())
        }
    }

    @Throws(IOException::class)
    fun saveDefault() {
        if (file.exists()) {
            throw InternalException("`saveDefault` should only be used if the file does not exists")
        }
        this.createFolder()
        Files.copy(defaultFile, file)
    }

    private fun createFolder(): ConfigFileLoader {
        val folder = file.parent
        if (folder.exists()) return this
        if (!folder.toFile().mkdirs()) {
            logger.warn("Failed to create config folder for $FILE_NAME: $folder")
        }
        return this
    }

    companion object {
        const val FILE_NAME = "eden.mod.toml"
    }
}

private val defaultFile = FabricLoader
    .getInstance()
    .getModContainer(ID)
    .get()
    .findPath(ConfigFileLoader.FILE_NAME)
    .orElseThrow { InternalException("Cannot find `eden.mod.toml` resource file!") }

private val toml = Toml(
    inputConfig = TomlInputConfig(
        allowNullValues = true,
        ignoreUnknownNames = true
    ),
    outputConfig = TomlOutputConfig(
        indentation = TomlIndentation.FOUR_SPACES,
        allowEscapedQuotesInLiteralStrings = true,
    ),
)
