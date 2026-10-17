package com.nexun.nexres

import android.app.Application
import android.content.Context
import com.nexun.nexres.i18n.LocaleHelper

class NexResApp : Application() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.wrap(base))
    }
}
