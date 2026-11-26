package com.nexun.nexres.ui.theme

import androidx.compose.ui.graphics.Color

data class AccentPalette(val seed: Color, val light: Color, val dark: Color, val containerLight: Color, val containerDark: Color)

val AccentPalettes = linkedMapOf(
    "DEFAULT" to AccentPalette(Color(0xFF5F6368), Color(0xFF4B5F7A), Color(0xFFB7C7DD), Color(0xFFDCE6F3), Color(0xFF34495E)),
    "BLUE" to AccentPalette(Color(0xFF1A73E8), Color(0xFF1A73E8), Color(0xFF8AB4F8), Color(0xFFD7E3FF), Color(0xFF284777)),
    "GREEN" to AccentPalette(Color(0xFF1E8E3E), Color(0xFF1E8E3E), Color(0xFF81C995), Color(0xFFCFEAD6), Color(0xFF245136)),
    "ORANGE" to AccentPalette(Color(0xFFE8710A), Color(0xFFE8710A), Color(0xFFF9AB00), Color(0xFFFFE0C2), Color(0xFF6B3A00)),
    "PURPLE" to AccentPalette(Color(0xFF6750A4), Color(0xFF6750A4), Color(0xFFD0BCFF), Color(0xFFEADDFF), Color(0xFF4F378B)),
    "PINK" to AccentPalette(Color(0xFFD01884), Color(0xFFD01884), Color(0xFFFFB1C8), Color(0xFFFFD9E2), Color(0xFF703348)),
    "RED" to AccentPalette(Color(0xFFD93025), Color(0xFFD93025), Color(0xFFF28B82), Color(0xFFFFDAD6), Color(0xFF73332D)),
    "TEAL" to AccentPalette(Color(0xFF00796B), Color(0xFF00796B), Color(0xFF4DB6AC), Color(0xFFCDE8E1), Color(0xFF1F4E47)),
    "CYAN" to AccentPalette(Color(0xFF0097A7), Color(0xFF0097A7), Color(0xFF4DD0E1), Color(0xFFCFEFF4), Color(0xFF1E4D55)),
    "INDIGO" to AccentPalette(Color(0xFF3F51B5), Color(0xFF3F51B5), Color(0xFF9FA8DA), Color(0xFFDDE1FF), Color(0xFF334174)),
    "YELLOW" to AccentPalette(Color(0xFFF9AB00), Color(0xFFB77900), Color(0xFFFFD666), Color(0xFFFFF0C2), Color(0xFF6B5300)),
    "LIME" to AccentPalette(Color(0xFF7CB342), Color(0xFF689F38), Color(0xFFC5E1A5), Color(0xFFE4F2D1), Color(0xFF3E5A20)),
    "BROWN" to AccentPalette(Color(0xFF795548), Color(0xFF795548), Color(0xFFBCAAA4), Color(0xFFEFE0DB), Color(0xFF4E342E)),
)
