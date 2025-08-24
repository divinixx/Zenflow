package com.divinixx.zenflow.ui.components.keyboard

/**
 * Predefined keyboard layouts for different use cases
 */
object KeyboardLayouts {
    
    /**
     * Standard QWERTY keyboard layout
     */
    val QWERTY = KeyboardLayout(
        name = "QWERTY",
        functionKeys = createFunctionKeys(),
        mainKeys = listOf(
            // Number row
            listOf(
                KeyDefinition("`", "GRAVE", shiftLabel = "~"),
                KeyDefinition("1", "1", shiftLabel = "!"),
                KeyDefinition("2", "2", shiftLabel = "@"),
                KeyDefinition("3", "3", shiftLabel = "#"),
                KeyDefinition("4", "4", shiftLabel = "$"),
                KeyDefinition("5", "5", shiftLabel = "%"),
                KeyDefinition("6", "6", shiftLabel = "^"),
                KeyDefinition("7", "7", shiftLabel = "&"),
                KeyDefinition("8", "8", shiftLabel = "*"),
                KeyDefinition("9", "9", shiftLabel = "("),
                KeyDefinition("0", "0", shiftLabel = ")"),
                KeyDefinition("-", "MINUS", shiftLabel = "_"),
                KeyDefinition("=", "EQUALS", shiftLabel = "+"),
                KeyDefinition("⌫", "BACKSPACE", width = 1.5f, keyType = KeyType.BACKSPACE)
            ),
            // First letter row
            listOf(
                KeyDefinition("Tab", "TAB", width = 1.5f, keyType = KeyType.SPECIAL),
                KeyDefinition("Q", "Q"),
                KeyDefinition("W", "W"),
                KeyDefinition("E", "E"),
                KeyDefinition("R", "R"),
                KeyDefinition("T", "T"),
                KeyDefinition("Y", "Y"),
                KeyDefinition("U", "U"),
                KeyDefinition("I", "I"),
                KeyDefinition("O", "O"),
                KeyDefinition("P", "P"),
                KeyDefinition("[", "LEFT_BRACKET", shiftLabel = "{"),
                KeyDefinition("]", "RIGHT_BRACKET", shiftLabel = "}"),
                KeyDefinition("\\", "BACKSLASH", width = 1.5f, shiftLabel = "|")
            ),
            // Second letter row
            listOf(
                KeyDefinition("Caps", "CAPS_LOCK", width = 1.75f, keyType = KeyType.MODIFIER),
                KeyDefinition("A", "A"),
                KeyDefinition("S", "S"),
                KeyDefinition("D", "D"),
                KeyDefinition("F", "F"),
                KeyDefinition("G", "G"),
                KeyDefinition("H", "H"),
                KeyDefinition("J", "J"),
                KeyDefinition("K", "K"),
                KeyDefinition("L", "L"),
                KeyDefinition(";", "SEMICOLON", shiftLabel = ":"),
                KeyDefinition("'", "QUOTE", shiftLabel = "\""),
                KeyDefinition("Enter", "ENTER", width = 2.25f, keyType = KeyType.SPECIAL)
            ),
            // Third letter row
            listOf(
                KeyDefinition("Shift", "LEFT_SHIFT", width = 2.25f, keyType = KeyType.MODIFIER),
                KeyDefinition("Z", "Z"),
                KeyDefinition("X", "X"),
                KeyDefinition("C", "C"),
                KeyDefinition("V", "V"),
                KeyDefinition("B", "B"),
                KeyDefinition("N", "N"),
                KeyDefinition("M", "M"),
                KeyDefinition(",", "COMMA", shiftLabel = "<"),
                KeyDefinition(".", "PERIOD", shiftLabel = ">"),
                KeyDefinition("/", "SLASH", shiftLabel = "?"),
                KeyDefinition("Shift", "RIGHT_SHIFT", width = 2.75f, keyType = KeyType.MODIFIER)
            ),
            // Bottom row
            listOf(
                KeyDefinition("Ctrl", "LEFT_CTRL", width = 1.25f, keyType = KeyType.MODIFIER),
                KeyDefinition("Win", "LEFT_WIN", width = 1.25f, keyType = KeyType.MODIFIER),
                KeyDefinition("Alt", "LEFT_ALT", width = 1.25f, keyType = KeyType.MODIFIER),
                KeyDefinition("Space", "SPACE", width = 6.25f, keyType = KeyType.SPACE),
                KeyDefinition("Alt", "RIGHT_ALT", width = 1.25f, keyType = KeyType.MODIFIER),
                KeyDefinition("Win", "RIGHT_WIN", width = 1.25f, keyType = KeyType.MODIFIER),
                KeyDefinition("Menu", "MENU", width = 1.25f, keyType = KeyType.SPECIAL),
                KeyDefinition("Ctrl", "RIGHT_CTRL", width = 1.25f, keyType = KeyType.MODIFIER)
            )
        ),
        modifierKeys = createModifierKeys(),
        numpadKeys = createNumpadKeys()
    )
    
    /**
     * Compact keyboard layout for smaller screens
     */
    val COMPACT = KeyboardLayout(
        name = "COMPACT",
        functionKeys = emptyList(),
        mainKeys = listOf(
            // Number row
            listOf(
                KeyDefinition("1", "1", shiftLabel = "!"),
                KeyDefinition("2", "2", shiftLabel = "@"),
                KeyDefinition("3", "3", shiftLabel = "#"),
                KeyDefinition("4", "4", shiftLabel = "$"),
                KeyDefinition("5", "5", shiftLabel = "%"),
                KeyDefinition("6", "6", shiftLabel = "^"),
                KeyDefinition("7", "7", shiftLabel = "&"),
                KeyDefinition("8", "8", shiftLabel = "*"),
                KeyDefinition("9", "9", shiftLabel = "("),
                KeyDefinition("0", "0", shiftLabel = ")"),
                KeyDefinition("⌫", "BACKSPACE", width = 1.2f, keyType = KeyType.BACKSPACE)
            ),
            // First letter row
            listOf(
                KeyDefinition("Q", "Q"),
                KeyDefinition("W", "W"),
                KeyDefinition("E", "E"),
                KeyDefinition("R", "R"),
                KeyDefinition("T", "T"),
                KeyDefinition("Y", "Y"),
                KeyDefinition("U", "U"),
                KeyDefinition("I", "I"),
                KeyDefinition("O", "O"),
                KeyDefinition("P", "P")
            ),
            // Second letter row
            listOf(
                KeyDefinition("A", "A"),
                KeyDefinition("S", "S"),
                KeyDefinition("D", "D"),
                KeyDefinition("F", "F"),
                KeyDefinition("G", "G"),
                KeyDefinition("H", "H"),
                KeyDefinition("J", "J"),
                KeyDefinition("K", "K"),
                KeyDefinition("L", "L"),
                KeyDefinition("Enter", "ENTER", width = 1.5f, keyType = KeyType.SPECIAL)
            ),
            // Third letter row
            listOf(
                KeyDefinition("⇧", "SHIFT", width = 1.2f, keyType = KeyType.MODIFIER),
                KeyDefinition("Z", "Z"),
                KeyDefinition("X", "X"),
                KeyDefinition("C", "C"),
                KeyDefinition("V", "V"),
                KeyDefinition("B", "B"),
                KeyDefinition("N", "N"),
                KeyDefinition("M", "M"),
                KeyDefinition("⇧", "SHIFT", width = 1.2f, keyType = KeyType.MODIFIER)
            ),
            // Bottom row
            listOf(
                KeyDefinition("Ctrl", "CTRL", keyType = KeyType.MODIFIER),
                KeyDefinition("Alt", "ALT", keyType = KeyType.MODIFIER),
                KeyDefinition("Space", "SPACE", width = 4.0f, keyType = KeyType.SPACE),
                KeyDefinition(".", "PERIOD"),
                KeyDefinition(",", "COMMA"),
                KeyDefinition("?", "QUESTION_MARK")
            )
        ),
        modifierKeys = listOf(
            KeyDefinition("Ctrl", "CTRL", keyType = KeyType.MODIFIER),
            KeyDefinition("Alt", "ALT", keyType = KeyType.MODIFIER),
            KeyDefinition("⇧", "SHIFT", keyType = KeyType.MODIFIER)
        ),
        numpadKeys = null,
        isCompact = true
    )
    
    private fun createFunctionKeys(): List<KeyDefinition> {
        return (1..12).map { num ->
            KeyDefinition("F$num", "F$num", keyType = KeyType.FUNCTION)
        } + listOf(
            KeyDefinition("Esc", "ESCAPE", keyType = KeyType.SPECIAL),
            KeyDefinition("PrtSc", "PRINT_SCREEN", keyType = KeyType.SPECIAL),
            KeyDefinition("ScrLk", "SCROLL_LOCK", keyType = KeyType.SPECIAL),
            KeyDefinition("Pause", "PAUSE", keyType = KeyType.SPECIAL)
        )
    }
    
    private fun createModifierKeys(): List<KeyDefinition> {
        return listOf(
            KeyDefinition("Ctrl", "CTRL", keyType = KeyType.MODIFIER),
            KeyDefinition("Alt", "ALT", keyType = KeyType.MODIFIER),
            KeyDefinition("Shift", "SHIFT", keyType = KeyType.MODIFIER),
            KeyDefinition("Win", "WIN", keyType = KeyType.MODIFIER),
            KeyDefinition("Fn", "FN", keyType = KeyType.MODIFIER)
        )
    }
    
    private fun createNumpadKeys(): List<List<KeyDefinition>> {
        return listOf(
            listOf(
                KeyDefinition("Num", "NUM_LOCK", keyType = KeyType.MODIFIER),
                KeyDefinition("/", "NUMPAD_DIVIDE"),
                KeyDefinition("*", "NUMPAD_MULTIPLY"),
                KeyDefinition("-", "NUMPAD_MINUS")
            ),
            listOf(
                KeyDefinition("7", "NUMPAD_7"),
                KeyDefinition("8", "NUMPAD_8"),
                KeyDefinition("9", "NUMPAD_9"),
                KeyDefinition("+", "NUMPAD_PLUS", height = 2.0f)
            ),
            listOf(
                KeyDefinition("4", "NUMPAD_4"),
                KeyDefinition("5", "NUMPAD_5"),
                KeyDefinition("6", "NUMPAD_6")
            ),
            listOf(
                KeyDefinition("1", "NUMPAD_1"),
                KeyDefinition("2", "NUMPAD_2"),
                KeyDefinition("3", "NUMPAD_3"),
                KeyDefinition("Enter", "NUMPAD_ENTER", height = 2.0f, keyType = KeyType.SPECIAL)
            ),
            listOf(
                KeyDefinition("0", "NUMPAD_0", width = 2.0f),
                KeyDefinition(".", "NUMPAD_PERIOD")
            )
        )
    }
    
    /**
     * Arrow keys layout
     */
    val ARROW_KEYS = listOf(
        KeyDefinition("↑", "UP", keyType = KeyType.ARROW),
        KeyDefinition("←", "LEFT", keyType = KeyType.ARROW),
        KeyDefinition("↓", "DOWN", keyType = KeyType.ARROW),
        KeyDefinition("→", "RIGHT", keyType = KeyType.ARROW)
    )
    
    /**
     * Additional special keys
     */
    val SPECIAL_KEYS = listOf(
        KeyDefinition("Home", "HOME", keyType = KeyType.SPECIAL),
        KeyDefinition("End", "END", keyType = KeyType.SPECIAL),
        KeyDefinition("PgUp", "PAGE_UP", keyType = KeyType.SPECIAL),
        KeyDefinition("PgDn", "PAGE_DOWN", keyType = KeyType.SPECIAL),
        KeyDefinition("Ins", "INSERT", keyType = KeyType.SPECIAL),
        KeyDefinition("Del", "DELETE", keyType = KeyType.DELETE)
    )

    /**
     * Media control keys
     */
    val MEDIA_KEYS = listOf(
        KeyDefinition("⏯️", "MEDIA_PLAY_PAUSE", keyType = KeyType.SPECIAL),
        KeyDefinition("⏮️", "MEDIA_PREVIOUS", keyType = KeyType.SPECIAL),
        KeyDefinition("⏭️", "MEDIA_NEXT", keyType = KeyType.SPECIAL),
        KeyDefinition("🔇", "VOLUME_MUTE", keyType = KeyType.SPECIAL),
        KeyDefinition("🔉", "VOLUME_DOWN", keyType = KeyType.SPECIAL),
        KeyDefinition("🔊", "VOLUME_UP", keyType = KeyType.SPECIAL)
    )

    /**
     * Quick shortcut keys
     */
    val SHORTCUT_KEYS = listOf(
        KeyDefinition("Ctrl+C", "CTRL_C", keyType = KeyType.SPECIAL),
        KeyDefinition("Ctrl+V", "CTRL_V", keyType = KeyType.SPECIAL),
        KeyDefinition("Ctrl+Z", "CTRL_Z", keyType = KeyType.SPECIAL),
        KeyDefinition("Ctrl+Y", "CTRL_Y", keyType = KeyType.SPECIAL),
        KeyDefinition("Alt+Tab", "ALT_TAB", keyType = KeyType.SPECIAL),
        KeyDefinition("Ctrl+A", "CTRL_A", keyType = KeyType.SPECIAL),
        KeyDefinition("Ctrl+S", "CTRL_S", keyType = KeyType.SPECIAL),
        KeyDefinition("Ctrl+F", "CTRL_F", keyType = KeyType.SPECIAL)
    )
}
