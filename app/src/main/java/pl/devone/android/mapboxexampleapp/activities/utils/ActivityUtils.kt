package pl.devone.android.mapboxexampleapp.activities.utils

import android.app.Fragment
import android.app.FragmentManager
import android.content.Context
import android.net.ConnectivityManager
import pl.devone.android.mapboxexampleapp.R

/**
 * Created by ljedrzynski on 20.01.2018.
 */
object ActivityUtils {

    fun setFragment(manager: FragmentManager, cls: Class<out Fragment>): Fragment {
        return (cls.getConstructor().newInstance() as Fragment).apply {
            manager.beginTransaction()
                    .replace(R.id.container, this)
                    .commit()
        }
    }

    fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnectedOrConnecting
    }


}