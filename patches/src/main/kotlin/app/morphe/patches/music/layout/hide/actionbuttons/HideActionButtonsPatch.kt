/*
 * Copyright 2026 Morphe.
 * https://github.com/MorpheApp/morphe-patches
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 */

package app.morphe.patches.music.layout.hide.actionbuttons

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.music.misc.extension.sharedExtensionPatch
import app.morphe.patches.music.misc.litho.filter.lithoFilterPatch
import app.morphe.patches.music.misc.litho.node.treeNodeElementHookPatch
import app.morphe.patches.music.misc.settings.PreferenceScreen
import app.morphe.patches.music.misc.settings.settingsPatch
import app.morphe.patches.music.shared.Constants.COMPATIBILITY_YOUTUBE_MUSIC
import app.morphe.patches.shared.misc.litho.filter.addLithoFilter
import app.morphe.patches.shared.misc.litho.node.hookTreeNodeResult
import app.morphe.patches.shared.misc.settings.preference.PreferenceScreenPreference
import app.morphe.patches.shared.misc.settings.preference.SwitchPreference

private const val ACTION_BUTTONS_FILTER =
    "Lapp/morphe/extension/music/patches/components/ActionButtonsFilter;"

@Suppress("unused")
val hideActionButtonsPatch = bytecodePatch(
    name = "Hide music action buttons",
    description = "Adds options to hide action buttons under the player."
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        lithoFilterPatch,
        treeNodeElementHookPatch
    )

    compatibleWith(COMPATIBILITY_YOUTUBE_MUSIC)

    execute {
        PreferenceScreen.PLAYER.addPreferences(
            PreferenceScreenPreference(
                key = "morphe_music_action_buttons_screen",
                preferences = setOf(
                    SwitchPreference("morphe_music_hide_action_bar"),
                    SwitchPreference("morphe_music_hide_like_dislike_button"),
                    SwitchPreference("morphe_music_hide_comments_button"),
                    SwitchPreference("morphe_music_hide_lyrics_button"),
                    SwitchPreference("morphe_music_hide_share_button"),
                    SwitchPreference("morphe_music_hide_save_button"),
                    SwitchPreference("morphe_music_hide_download_button"),
                    SwitchPreference("morphe_music_hide_radio_button")
                )
            )
        )

        addLithoFilter(ACTION_BUTTONS_FILTER)
        hookTreeNodeResult("$ACTION_BUTTONS_FILTER->onLazilyConvertedElementLoaded")
    }
}
