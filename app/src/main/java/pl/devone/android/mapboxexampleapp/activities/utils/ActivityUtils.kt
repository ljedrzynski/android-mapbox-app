package pl.devone.android.mapboxexampleapp.activities.utils

import android.app.Fragment
import android.app.FragmentManager
import pl.devone.android.mapboxexampleapp.R

/**
 * Created by ljedrzynski on 20.01.2018.
 */
class ActivityUtils {

    companion object {
        fun setFragment(manager: FragmentManager, cls: Class<*>): Fragment {
            val fragment: Fragment
            try {
                fragment = cls.getConstructor().newInstance() as Fragment
            } catch (exc: Exception) {
                throw RuntimeException("Kurwa blad!")
            }
            manager.beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit()
            return fragment
        }
    }
}