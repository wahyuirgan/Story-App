package com.hokagelab.storyapp.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.net.toUri
import com.hokagelab.storyapp.R
import com.hokagelab.storyapp.data.source.local.entity.StoryEntity

/**
 * Implementation of App Widget functionality.
 */
class StoryWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action != null) {
            if (intent.action == TOAST_ACTION) {

                val storyDataDetail = StoryEntity(
                    id = intent.getStringExtra("id").toString(),
                    name = intent.getStringExtra("name").toString(),
                    description = intent.getStringExtra("description").toString(),
                    photoUrl = intent.getStringExtra("photoUrl").toString(),
                    createdAt = intent.getStringExtra("createdAt").toString(),
                )

                val i = Intent()
                i.setClassName("com.hokagelab.storyapp", "com.hokagelab.storyapp.ui.detail.DetailStoryActivity")
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                i.putExtra("detail_story", storyDataDetail)
                context.startActivity(i)
            }
        }
    }

    companion object {
        private const val TOAST_ACTION = "com.hokagelab.storyapp.TOAST_ACTION"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
        ) {
            val intent = Intent(context, StackWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = this.toUri(Intent.URI_INTENT_SCHEME).toUri()
            }

            val toastIntent = Intent(context, StoryWidget::class.java)
            toastIntent.action = TOAST_ACTION
            toastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

            val toastPendingIntent = PendingIntent.getBroadcast(
                context, 0, toastIntent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                else 0
            )

            val views = RemoteViews(context.packageName, R.layout.story_widget).apply {
                setRemoteAdapter(R.id.stackViewStoryList, intent)
                setEmptyView(R.id.stackViewStoryList, R.id.tvWidgetStoryListEmpty)
                setPendingIntentTemplate(R.id.stackViewStoryList, toastPendingIntent)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}