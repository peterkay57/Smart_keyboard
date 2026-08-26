package com.example.data.hid

object HidConstants {
    // HID Report IDs
    const val REPORT_ID_KEYBOARD: Byte = 1
    const val REPORT_ID_CONSUMER: Byte = 2
    const val REPORT_ID_MOUSE: Byte = 3

    // Modifiers bitmask
    const val MOD_NONE: Byte = 0x00
    const val MOD_LCTRL: Byte = 0x01
    const val MOD_LSHIFT: Byte = 0x02
    const val MOD_LALT: Byte = 0x04
    const val MOD_LGUI: Byte = 0x08
    const val MOD_RCTRL: Byte = 0x10
    const val MOD_RSHIFT: Byte = 0x20
    const val MOD_RALT: Byte = 0x40
    const val MOD_RGUI: Byte = 0x80.toByte()

    // Keycodes (Standard USB HID Keyboard Page 0x07)
    const val KEY_NONE: Byte = 0x00
    const val KEY_A: Byte = 0x04
    const val KEY_B: Byte = 0x05
    const val KEY_C: Byte = 0x06
    const val KEY_D: Byte = 0x07
    const val KEY_E: Byte = 0x08
    const val KEY_F: Byte = 0x09
    const val KEY_G: Byte = 0x0A
    const val KEY_H: Byte = 0x0B
    const val KEY_I: Byte = 0x0C
    const val KEY_J: Byte = 0x0D
    const val KEY_K: Byte = 0x0E
    const val KEY_L: Byte = 0x0F
    const val KEY_M: Byte = 0x10
    const val KEY_N: Byte = 0x11
    const val KEY_O: Byte = 0x12
    const val KEY_P: Byte = 0x13
    const val KEY_Q: Byte = 0x14
    const val KEY_R: Byte = 0x15
    const val KEY_S: Byte = 0x16
    const val KEY_T: Byte = 0x17
    const val KEY_U: Byte = 0x18
    const val KEY_V: Byte = 0x19
    const val KEY_W: Byte = 0x1A
    const val KEY_X: Byte = 0x1B
    const val KEY_Y: Byte = 0x1C
    const val KEY_Z: Byte = 0x1D

    const val KEY_1: Byte = 0x1E
    const val KEY_2: Byte = 0x1F
    const val KEY_3: Byte = 0x20
    const val KEY_4: Byte = 0x21
    const val KEY_5: Byte = 0x22
    const val KEY_6: Byte = 0x23
    const val KEY_7: Byte = 0x24
    const val KEY_8: Byte = 0x25
    const val KEY_9: Byte = 0x26
    const val KEY_0: Byte = 0x27

    const val KEY_ENTER: Byte = 0x28
    const val KEY_ESC: Byte = 0x29
    const val KEY_BACKSPACE: Byte = 0x2A
    const val KEY_TAB: Byte = 0x2B
    const val KEY_SPACE: Byte = 0x2C
    const val KEY_MINUS: Byte = 0x2D
    const val KEY_EQUAL: Byte = 0x2E
    const val KEY_LEFT_BRACKET: Byte = 0x2F
    const val KEY_RIGHT_BRACKET: Byte = 0x30
    const val KEY_BACKSLASH: Byte = 0x31
    const val KEY_SEMICOLON: Byte = 0x33
    const val KEY_QUOTE: Byte = 0x34
    const val KEY_GRAVE: Byte = 0x35
    const val KEY_COMMA: Byte = 0x36
    const val KEY_PERIOD: Byte = 0x37
    const val KEY_SLASH: Byte = 0x38
    const val KEY_CAPS_LOCK: Byte = 0x39

    const val KEY_F1: Byte = 0x3A
    const val KEY_F2: Byte = 0x3B
    const val KEY_F3: Byte = 0x3C
    const val KEY_F4: Byte = 0x3D
    const val KEY_F5: Byte = 0x3E
    const val KEY_F6: Byte = 0x3F
    const val KEY_F7: Byte = 0x40
    const val KEY_F8: Byte = 0x41
    const val KEY_F9: Byte = 0x42
    const val KEY_F10: Byte = 0x43
    const val KEY_F11: Byte = 0x44
    const val KEY_F12: Byte = 0x45

    const val KEY_PRINT_SCREEN: Byte = 0x46
    const val KEY_SCROLL_LOCK: Byte = 0x47
    const val KEY_PAUSE: Byte = 0x48
    const val KEY_INSERT: Byte = 0x49
    const val KEY_HOME: Byte = 0x4A
    const val KEY_PAGE_UP: Byte = 0x4B
    const val KEY_DELETE: Byte = 0x4C
    const val KEY_END: Byte = 0x4D
    const val KEY_PAGE_DOWN: Byte = 0x4E
    const val KEY_RIGHT_ARROW: Byte = 0x4F
    const val KEY_LEFT_ARROW: Byte = 0x50
    const val KEY_DOWN_ARROW: Byte = 0x51
    const val KEY_UP_ARROW: Byte = 0x52

    // Consumer Control (Media Usages)
    const val CONSUMER_PLAY_PAUSE = 0x00CD
    const val CONSUMER_SCAN_NEXT = 0x00B5
    const val CONSUMER_SCAN_PREV = 0x00B6
    const val CONSUMER_STOP = 0x00B7
    const val CONSUMER_VOLUME_UP = 0x00E9
    const val CONSUMER_VOLUME_DOWN = 0x00EA
    const val CONSUMER_MUTE = 0x00E2
    const val CONSUMER_BRIGHTNESS_UP = 0x006F
    const val CONSUMER_BRIGHTNESS_DOWN = 0x0070
    const val CONSUMER_CALCULATOR = 0x0192
    const val CONSUMER_BROWSER_HOME = 0x0223

    // Standard Combined HID Report Descriptor (Keyboard + Consumer Devices + Mouse)
    val HID_REPORT_DESCRIPTOR: ByteArray = byteArrayOf(
        // Keyboard (Report ID 1)
        0x05.toByte(), 0x01.toByte(), // USAGE_PAGE (Generic Desktop)
        0x09.toByte(), 0x06.toByte(), // USAGE (Keyboard)
        0xa1.toByte(), 0x01.toByte(), // COLLECTION (Application)
        0x85.toByte(), 0x01.toByte(), //   REPORT_ID (1)
        0x05.toByte(), 0x07.toByte(), //   USAGE_PAGE (Keyboard)
        0x19.toByte(), 0xe0.toByte(), //   USAGE_MINIMUM (Keyboard LeftControl)
        0x29.toByte(), 0xe7.toByte(), //   USAGE_MAXIMUM (Keyboard Right GUI)
        0x15.toByte(), 0x00.toByte(), //   LOGICAL_MINIMUM (0)
        0x25.toByte(), 0x01.toByte(), //   LOGICAL_MAXIMUM (1)
        0x75.toByte(), 0x01.toByte(), //   REPORT_SIZE (1)
        0x95.toByte(), 0x08.toByte(), //   REPORT_COUNT (8)
        0x81.toByte(), 0x02.toByte(), //   INPUT (Data,Var,Abs) - Modifier bits
        0x95.toByte(), 0x01.toByte(), //   REPORT_COUNT (1)
        0x75.toByte(), 0x08.toByte(), //   REPORT_SIZE (8)
        0x81.toByte(), 0x03.toByte(), //   INPUT (Cnst,Var,Abs) - Reserved
        0x95.toByte(), 0x05.toByte(), //   REPORT_COUNT (5)
        0x75.toByte(), 0x01.toByte(), //   REPORT_SIZE (1)
        0x05.toByte(), 0x08.toByte(), //   USAGE_PAGE (LEDs)
        0x19.toByte(), 0x01.toByte(), //   USAGE_MINIMUM (Num Lock)
        0x29.toByte(), 0x05.toByte(), //   USAGE_MAXIMUM (Kana)
        0x91.toByte(), 0x02.toByte(), //   OUTPUT (Data,Var,Abs) - LED report
        0x95.toByte(), 0x01.toByte(), //   REPORT_COUNT (1)
        0x75.toByte(), 0x03.toByte(), //   REPORT_SIZE (3)
        0x91.toByte(), 0x03.toByte(), //   OUTPUT (Cnst,Var,Abs) - LED padding
        0x95.toByte(), 0x06.toByte(), //   REPORT_COUNT (6)
        0x75.toByte(), 0x08.toByte(), //   REPORT_SIZE (8)
        0x15.toByte(), 0x00.toByte(), //   LOGICAL_MINIMUM (0)
        0x25.toByte(), 0x65.toByte(), //   LOGICAL_MAXIMUM (101)
        0x05.toByte(), 0x07.toByte(), //   USAGE_PAGE (Keyboard)
        0x19.toByte(), 0x00.toByte(), //   USAGE_MINIMUM (Reserved)
        0x29.toByte(), 0x65.toByte(), //   USAGE_MAXIMUM (Keyboard Application)
        0x81.toByte(), 0x00.toByte(), //   INPUT (Data,Ary,Abs) - 6 keycodes
        0xc0.toByte(),               // END_COLLECTION

        // Consumer Control (Report ID 2)
        0x05.toByte(), 0x0c.toByte(), // USAGE_PAGE (Consumer Devices)
        0x09.toByte(), 0x01.toByte(), // USAGE (Consumer Control)
        0xa1.toByte(), 0x01.toByte(), // COLLECTION (Application)
        0x85.toByte(), 0x02.toByte(), //   REPORT_ID (2)
        0x15.toByte(), 0x00.toByte(), //   LOGICAL_MINIMUM (0)
        0x26.toByte(), 0xff.toByte(), 0x03.toByte(), // LOGICAL_MAXIMUM (1023)
        0x19.toByte(), 0x00.toByte(), //   USAGE_MINIMUM (0)
        0x2a.toByte(), 0xff.toByte(), 0x03.toByte(), // USAGE_MAXIMUM (1023)
        0x75.toByte(), 0x10.toByte(), //   REPORT_SIZE (16)
        0x95.toByte(), 0x01.toByte(), //   REPORT_COUNT (1)
        0x81.toByte(), 0x00.toByte(), //   INPUT (Data,Ary,Abs)
        0xc0.toByte(),               // END_COLLECTION

        // Mouse (Report ID 3)
        0x05.toByte(), 0x01.toByte(), // USAGE_PAGE (Generic Desktop)
        0x09.toByte(), 0x02.toByte(), // USAGE (Mouse)
        0xa1.toByte(), 0x01.toByte(), // COLLECTION (Application)
        0x09.toByte(), 0x01.toByte(), //   USAGE (Pointer)
        0xa1.toByte(), 0x00.toByte(), //   COLLECTION (Physical)
        0x85.toByte(), 0x03.toByte(), //     REPORT_ID (3)
        0x05.toByte(), 0x09.toByte(), //     USAGE_PAGE (Button)
        0x19.toByte(), 0x01.toByte(), //     USAGE_MINIMUM (Button 1)
        0x29.toByte(), 0x03.toByte(), //     USAGE_MAXIMUM (Button 3)
        0x15.toByte(), 0x00.toByte(), //     LOGICAL_MINIMUM (0)
        0x25.toByte(), 0x01.toByte(), //     LOGICAL_MAXIMUM (1)
        0x75.toByte(), 0x01.toByte(), //     REPORT_SIZE (1)
        0x95.toByte(), 0x03.toByte(), //     REPORT_COUNT (3)
        0x81.toByte(), 0x02.toByte(), //     INPUT (Data,Var,Abs)
        0x75.toByte(), 0x05.toByte(), //     REPORT_SIZE (5)
        0x95.toByte(), 0x01.toByte(), //     REPORT_COUNT (1)
        0x81.toByte(), 0x03.toByte(), //     INPUT (Cnst,Var,Abs)
        0x05.toByte(), 0x01.toByte(), //     USAGE_PAGE (Generic Desktop)
        0x09.toByte(), 0x30.toByte(), //     USAGE (X)
        0x09.toByte(), 0x31.toByte(), //     USAGE (Y)
        0x09.toByte(), 0x38.toByte(), //     USAGE (Wheel)
        0x15.toByte(), 0x81.toByte(), //     LOGICAL_MINIMUM (-127)
        0x25.toByte(), 0x7f.toByte(), //     LOGICAL_MAXIMUM (127)
        0x75.toByte(), 0x08.toByte(), //     REPORT_SIZE (8)
        0x95.toByte(), 0x03.toByte(), //     REPORT_COUNT (3)
        0x81.toByte(), 0x06.toByte(), //     INPUT (Data,Var,Rel)
        0xc0.toByte(),               //   END_COLLECTION
        0xc0.toByte()                // END_COLLECTION
    )

    /**
     * Maps a character to (Modifier, Keycode).
     * If character is not directly supported, returns null.
     */
    fun charToHid(char: Char): Pair<Byte, Byte>? {
        return when (char) {
            in 'a'..'z' -> Pair(MOD_NONE, (KEY_A + (char - 'a')).toByte())
            in 'A'..'Z' -> Pair(MOD_LSHIFT, (KEY_A + (char - 'A')).toByte())
            '1' -> Pair(MOD_NONE, KEY_1)
            '2' -> Pair(MOD_NONE, KEY_2)
            '3' -> Pair(MOD_NONE, KEY_3)
            '4' -> Pair(MOD_NONE, KEY_4)
            '5' -> Pair(MOD_NONE, KEY_5)
            '6' -> Pair(MOD_NONE, KEY_6)
            '7' -> Pair(MOD_NONE, KEY_7)
            '8' -> Pair(MOD_NONE, KEY_8)
            '9' -> Pair(MOD_NONE, KEY_9)
            '0' -> Pair(MOD_NONE, KEY_0)
            '!' -> Pair(MOD_LSHIFT, KEY_1)
            '@' -> Pair(MOD_LSHIFT, KEY_2)
            '#' -> Pair(MOD_LSHIFT, KEY_3)
            '$' -> Pair(MOD_LSHIFT, KEY_4)
            '%' -> Pair(MOD_LSHIFT, KEY_5)
            '^' -> Pair(MOD_LSHIFT, KEY_6)
            '&' -> Pair(MOD_LSHIFT, KEY_7)
            '*' -> Pair(MOD_LSHIFT, KEY_8)
            '(' -> Pair(MOD_LSHIFT, KEY_9)
            ')' -> Pair(MOD_LSHIFT, KEY_0)
            '\n', '\r' -> Pair(MOD_NONE, KEY_ENTER)
            '\t' -> Pair(MOD_NONE, KEY_TAB)
            ' ' -> Pair(MOD_NONE, KEY_SPACE)
            '-' -> Pair(MOD_NONE, KEY_MINUS)
            '_' -> Pair(MOD_LSHIFT, KEY_MINUS)
            '=' -> Pair(MOD_NONE, KEY_EQUAL)
            '+' -> Pair(MOD_LSHIFT, KEY_EQUAL)
            '[' -> Pair(MOD_NONE, KEY_LEFT_BRACKET)
            '{' -> Pair(MOD_LSHIFT, KEY_LEFT_BRACKET)
            ']' -> Pair(MOD_NONE, KEY_RIGHT_BRACKET)
            '}' -> Pair(MOD_LSHIFT, KEY_RIGHT_BRACKET)
            '\\' -> Pair(MOD_NONE, KEY_BACKSLASH)
            '|' -> Pair(MOD_LSHIFT, KEY_BACKSLASH)
            ';' -> Pair(MOD_NONE, KEY_SEMICOLON)
            ':' -> Pair(MOD_LSHIFT, KEY_SEMICOLON)
            '\'' -> Pair(MOD_NONE, KEY_QUOTE)
            '"' -> Pair(MOD_LSHIFT, KEY_QUOTE)
            '`' -> Pair(MOD_NONE, KEY_GRAVE)
            '~' -> Pair(MOD_LSHIFT, KEY_GRAVE)
            ',' -> Pair(MOD_NONE, KEY_COMMA)
            '<' -> Pair(MOD_LSHIFT, KEY_COMMA)
            '.' -> Pair(MOD_NONE, KEY_PERIOD)
            '>' -> Pair(MOD_LSHIFT, KEY_PERIOD)
            '/' -> Pair(MOD_NONE, KEY_SLASH)
            '?' -> Pair(MOD_LSHIFT, KEY_SLASH)
            else -> null
        }
    }
}
