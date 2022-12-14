/*
 * Copyright (c) 2010-2020 Belledonne Communications SARL.
 *
 * This file is part of linphone-android
 * (see https://www.linphone.org).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package bsolution.phone.activities.assistant

import android.os.Bundle
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.ViewModelProvider
import bsolution.phone.LinphoneApplication.Companion.corePreferences
import bsolution.phone.R
import bsolution.phone.activities.GenericActivity
import bsolution.phone.activities.SnackBarActivity
import bsolution.phone.activities.assistant.viewmodels.SharedAssistantViewModel
import com.google.android.material.snackbar.Snackbar

class AssistantActivity : GenericActivity(), SnackBarActivity {
    private lateinit var sharedViewModel: SharedAssistantViewModel
    private lateinit var coordinator: CoordinatorLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.assistant_activity)

        sharedViewModel = ViewModelProvider(this)[SharedAssistantViewModel::class.java]

        coordinator = findViewById(R.id.coordinator)

        corePreferences.firstStart = false
    }

    override fun showSnackBar(resourceId: Int) {
        Snackbar.make(coordinator, resourceId, Snackbar.LENGTH_LONG).show()
    }

    override fun showSnackBar(resourceId: Int, action: Int, listener: () -> Unit) {
        Snackbar
            .make(findViewById(R.id.coordinator), resourceId, Snackbar.LENGTH_LONG)
            .setAction(action) {
                listener()
            }
            .show()
    }

    override fun showSnackBar(message: String) {
        Snackbar.make(coordinator, message, Snackbar.LENGTH_LONG).show()
    }
}
