package com.example.export

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build

data class LauncherTarget(
    val id: String,
    val name: String,
    val packageNames: List<String>,
    val primaryStorePackage: String,
    val supportTag: String,
    val applySteps: String,
    val isInstalled: Boolean,
    val installedPackage: String?
)

object LauncherIntegrationManager {

    private data class LauncherDefinition(
        val id: String,
        val name: String,
        val packageNames: List<String>,
        val supportTag: String,
        val applySteps: String
    )

    private val knownLaunchers = listOf(
        LauncherDefinition(
            id = "niagara",
            name = "Niagara Launcher",
            packageNames = listOf("bitpit.launcher"),
            supportTag = "Per-App PNG + Icon Pack",
            applySteps = "Export the Clean PNG Set (.zip) and extract to Downloads. In Niagara, long-press any app → Edit icon → Pick from Files/Gallery."
        ),
        LauncherDefinition(
            id = "nova",
            name = "Nova Launcher",
            packageNames = listOf("com.teslacoilsw.launcher"),
            supportTag = "appfilter.xml + Per-App PNG",
            applySteps = "Export PNGs for instant per-app editing (Long-press icon → Edit → Tap icon → Gallery/Files) or use the Icon Pack Bundle with appfilter.xml."
        ),
        LauncherDefinition(
            id = "lawnchair",
            name = "Lawnchair",
            packageNames = listOf("app.lawnchair", "app.lawnchair.play", "ch.deletescape.lawnchair.plah"),
            supportTag = "Icon Pack Bundle + PNG",
            applySteps = "Export the PNG set or Icon Pack Bundle. Long-press any icon on Lawnchair's home screen → Customize → Tap icon to pick a custom PNG."
        ),
        LauncherDefinition(
            id = "smart",
            name = "Smart Launcher 6",
            packageNames = listOf("ginlemon.flowerfree"),
            supportTag = "Icon Pack Studio + PNG",
            applySteps = "Long-press an app icon in Smart Launcher → Edit icon → From Gallery/Files, or import the exported Icon Pack Bundle into Icon Pack Studio."
        ),
        LauncherDefinition(
            id = "action",
            name = "Action Launcher",
            packageNames = listOf("com.actionlauncher.playstore"),
            supportTag = "Custom PNG + appfilter.xml",
            applySteps = "Long-press any icon on your desktop → Edit → Tap icon → Choose from Files to apply your custom Fjord PNG."
        ),
        LauncherDefinition(
            id = "hyperion",
            name = "Hyperion Launcher",
            packageNames = listOf("projekt.launcher"),
            supportTag = "appfilter.xml + PNG",
            applySteps = "Long-press an icon → Edit → Select custom image from your extracted Fjord PNG folder."
        ),
        LauncherDefinition(
            id = "microsoft",
            name = "Microsoft Launcher",
            packageNames = listOf("com.microsoft.launcher"),
            supportTag = "Per-App Custom PNG",
            applySteps = "Long-press an icon on Microsoft Launcher → Edit icon → Pick custom PNG from Files."
        )
    )

    fun detectLaunchers(context: Context): List<LauncherTarget> {
        val pm = context.packageManager
        return knownLaunchers.map { def ->
            val foundPkg = def.packageNames.firstOrNull { isPackageInstalled(pm, it) }
            LauncherTarget(
                id = def.id,
                name = def.name,
                packageNames = def.packageNames,
                primaryStorePackage = def.packageNames.first(),
                supportTag = def.supportTag,
                applySteps = def.applySteps,
                isInstalled = foundPkg != null,
                installedPackage = foundPkg
            )
        }.sortedByDescending { it.isInstalled }
    }

    fun openLauncher(context: Context, packageName: String): Boolean {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return false
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(launchIntent)
        return true
    }

    fun openPlayStoreListing(context: Context, packageName: String) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun shareFile(context: Context, uri: Uri, mimeType: String, title: String) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(sendIntent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    private fun isPackageInstalled(pm: PackageManager, pkg: String): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(pkg, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(pkg, 0)
            }
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }
}
