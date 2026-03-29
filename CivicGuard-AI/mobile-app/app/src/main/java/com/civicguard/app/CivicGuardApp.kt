package com.civicguard.app

import android.app.Application
import org.osmdroid.config.Configuration

class CivicGuardApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize mock data store
        com.civicguard.app.data.MockDataStore.init(this)
        
        // Initialize OSMDroid with app-specific cache
        Configuration.getInstance().apply {
            userAgentValue = packageName
            osmdroidBasePath = cacheDir
            osmdroidTileCache = java.io.File(cacheDir, "osmdroid")
        }
    }
}
