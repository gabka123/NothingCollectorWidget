package com.example.nothingcollector

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import java.io.File
import java.io.FileOutputStream

class PickerActivity : Activity() {
    private val pickImageCode = 41

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        startActivityForResult(intent, pickImageCode)
    }

    @Deprecated("Deprecated by Android, retained for broad device compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pickImageCode && resultCode == RESULT_OK) {
            val uri: Uri? = data?.data
            if (uri != null) {
                try {
                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (_: Exception) { }
                try {
                    val input = contentResolver.openInputStream(uri)
                    val file = File(filesDir, "collector_photo.jpg")
                    input?.use { stream ->
                        FileOutputStream(file).use { output -> stream.copyTo(output) }
                    }
                    val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeFile(file.absolutePath, opts)
                    if (file.exists() && file.length() > 0L) {
                        val manager = AppWidgetManager.getInstance(this)
                        val ids = manager.getAppWidgetIds(ComponentName(this, CollectorWidgetProvider::class.java))
                        CollectorWidgetProvider.updateWidgets(this, manager, ids)
                    }
                } catch (_: Exception) { }
            }
        }
        finish()
    }
}
