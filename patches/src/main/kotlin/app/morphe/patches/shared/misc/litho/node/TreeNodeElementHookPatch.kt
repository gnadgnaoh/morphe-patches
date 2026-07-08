/*
 * Copyright 2026 Morphe.
 * https://github.com/MorpheApp/morphe-patches
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 */

package app.morphe.patches.shared.misc.litho.node

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.BytecodePatch
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import app.morphe.patches.shared.misc.litho.context.EXTENSION_CONTEXT_INTERFACE
import app.morphe.util.addInstructionsAtControlFlowLabel
import app.morphe.util.getFreeRegisterProvider
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import java.lang.ref.WeakReference

internal const val EXTENSION_CLASS =
    "Lapp/morphe/extension/shared/patches/TreeNodeElementPatch;"

private lateinit var componentLoadedMethodRef: WeakReference<MutableMethod>
private lateinit var lazilyConvertedElementLoadedMethodRef: WeakReference<MutableMethod>

/**
 * Shared factory for the tree-node element hook patch used by both YouTube and YT Music.
 *
 * Hooks the tree-node result list from Litho so that patched extensions can inspect (and
 * physically remove entries from) the list before it is converted into rendered components.
 *
 * @param sharedExtensionPatchDep The app-specific `sharedExtensionPatch`.
 * @param conversionContextPatchDep The app-specific `conversionContextPatch`.
 */
internal fun createTreeNodeElementHookPatch(
    sharedExtensionPatchDep: BytecodePatch,
    conversionContextPatchDep: BytecodePatch,
): BytecodePatch = bytecodePatch(
    description = "Hooks the tree node element lists to the extension."
) {
    dependsOn(
        sharedExtensionPatchDep,
        conversionContextPatchDep,
    )

    execute {
        TreeNodeResultListFingerprint.method.apply {
            val insertIndex = implementation!!.instructions.lastIndex
            val listRegister = getInstruction<OneRegisterInstruction>(insertIndex).registerA

            val registerProvider = getFreeRegisterProvider(insertIndex, 1)
            val freeRegister = registerProvider.getFreeRegister()

            addInstructionsAtControlFlowLabel(
                insertIndex,
                """
                    move-object/from16 v$freeRegister, p2
                    invoke-static { v$freeRegister, v$listRegister }, $EXTENSION_CLASS->onTreeNodeResultLoaded(${EXTENSION_CONTEXT_INTERFACE}Ljava/util/List;)V
                """
            )
        }

        val componentLoadedMethod = ComponentPatchFingerprint.method
        componentLoadedMethodRef = WeakReference(componentLoadedMethod)

        val lazilyConvertedElementLoadedMethod = LazilyConvertedElementPatchFingerprint.method
        lazilyConvertedElementLoadedMethodRef = WeakReference(lazilyConvertedElementLoadedMethod)
    }
}

fun hookTreeNodeResult(
    descriptor: String,
    isLazilyConvertedElement: Boolean = true
) {
    val method = if (isLazilyConvertedElement) lazilyConvertedElementLoadedMethodRef.get()!!
    else componentLoadedMethodRef.get()!!

    method.addInstruction(
        0,
        "invoke-static { p0, p1 }, $descriptor(Ljava/lang/String;Ljava/util/List;)V"
    )
}
