package com.example.nothingcollector

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Bitmap
import android.widget.RemoteViews
import java.io.File

class CollectorWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        updateWidgets(context, manager, ids)
    }

    companion object {
        fun updateWidgets(context: Context, manager: AppWidgetManager, ids: IntArray) {
            val photo = File(context.filesDir, "collector_photo.jpg")
            val bitmap = if (photo.exists()) {
                BitmapFactory.decodeFile(photo.absolutePath)?.let { source ->
                    val rounded = roundedCorners(source, 0f)
                    if (rounded !== source) source.recycle()
                    rounded
                }
            } else null

            ids.forEach { id ->
                val views = RemoteViews(context.packageName, R.layout.collector_widget)
                if (bitmap != null) views.setImageViewBitmap(R.id.collector_image, bitmap)
                val openPicker = Intent(context, PickerActivity::class.java)
                val pending = PendingIntent.getActivity(
                    context, id, openPicker,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_root, pending)
                manager.updateAppWidget(id, views)
            }
            bitmap?.recycle()
        }

        private fun roundedCorners(source: Bitmap, radius: Float): Bitmap {
            if (radius <= 0f) return source
            val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = android.graphics.BitmapShader(
                    source,
                    android.graphics.Shader.TileMode.CLAMP,
                    android.graphics.Shader.TileMode.CLAMP
                )
            }
            canvas.drawRoundRect(RectF(0f, 0f, source.width.toFloat(), source.height.toFloat()), radius, radius, paint)
            return output
        }
    }
}
