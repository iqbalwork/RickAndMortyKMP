package com.rickandmorty.app.core.designsystem

import androidx.compose.ui.graphics.Color
import com.rickandmorty.app.core.designsystem.components.CharacterStatus
import com.rickandmorty.app.core.designsystem.theme.BorderSlate
import com.rickandmorty.app.core.designsystem.theme.CardSurface
import com.rickandmorty.app.core.designsystem.theme.CyberYellow
import com.rickandmorty.app.core.designsystem.theme.DarkVoid
import com.rickandmorty.app.core.designsystem.theme.ElectricCyan
import com.rickandmorty.app.core.designsystem.theme.PortalGreen
import com.rickandmorty.app.core.designsystem.theme.PortalGreenDark
import com.rickandmorty.app.core.designsystem.theme.SpaceBlack
import com.rickandmorty.app.core.designsystem.theme.StatusAlive
import com.rickandmorty.app.core.designsystem.theme.StatusDead
import com.rickandmorty.app.core.designsystem.theme.StatusUnknown
import com.rickandmorty.app.core.designsystem.theme.TextMuted
import com.rickandmorty.app.core.designsystem.theme.TextPrimary
import com.rickandmorty.app.core.designsystem.theme.TextSecondary
import kotlin.test.Test
import kotlin.test.assertEquals

class ThemeTest {

    @Test
    fun testColorDefinitions() {
        assertEquals(Color(0xFF00FF85), PortalGreen)
        assertEquals(Color(0xFF00B35C), PortalGreenDark)
        assertEquals(Color(0xFF00B5CC), ElectricCyan)
        assertEquals(Color(0xFFFEE12B), CyberYellow)

        assertEquals(Color(0xFF0B0E14), SpaceBlack)
        assertEquals(Color(0xFF151921), DarkVoid)
        assertEquals(Color(0xFF1B212D), CardSurface)
        assertEquals(Color(0xFF2B3444), BorderSlate)

        assertEquals(Color(0xFFF0F4F8), TextPrimary)
        assertEquals(Color(0xFF94A3B8), TextSecondary)
        assertEquals(Color(0xFF64748B), TextMuted)

        assertEquals(Color(0xFF00FF85), StatusAlive)
        assertEquals(Color(0xFFFF3366), StatusDead)
        assertEquals(Color(0xFF8892B0), StatusUnknown)
    }

    @Test
    fun testCharacterStatusMapping() {
        assertEquals(CharacterStatus.ALIVE, CharacterStatus.fromString("Alive"))
        assertEquals(CharacterStatus.ALIVE, CharacterStatus.fromString("alive"))
        assertEquals(CharacterStatus.ALIVE, CharacterStatus.fromString("  ALIVE  "))
        assertEquals(StatusAlive, CharacterStatus.ALIVE.color)

        assertEquals(CharacterStatus.DEAD, CharacterStatus.fromString("Dead"))
        assertEquals(CharacterStatus.DEAD, CharacterStatus.fromString("dead"))
        assertEquals(CharacterStatus.DEAD, CharacterStatus.fromString("DEAD"))
        assertEquals(StatusDead, CharacterStatus.DEAD.color)

        assertEquals(CharacterStatus.UNKNOWN, CharacterStatus.fromString("unknown"))
        assertEquals(CharacterStatus.UNKNOWN, CharacterStatus.fromString("Unknown"))
        assertEquals(CharacterStatus.UNKNOWN, CharacterStatus.fromString(""))
        assertEquals(CharacterStatus.UNKNOWN, CharacterStatus.fromString(null))
        assertEquals(CharacterStatus.UNKNOWN, CharacterStatus.fromString("random_value"))
        assertEquals(StatusUnknown, CharacterStatus.UNKNOWN.color)
    }
}
