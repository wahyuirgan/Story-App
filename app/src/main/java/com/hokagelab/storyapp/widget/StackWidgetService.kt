package com.hokagelab.storyapp.widget

import android.content.Intent
import android.widget.RemoteViewsService
import com.hokagelab.storyapp.utils.StackRemoteViewsFactory

class StackWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory =
        StackRemoteViewsFactory(this.applicationContext)
}