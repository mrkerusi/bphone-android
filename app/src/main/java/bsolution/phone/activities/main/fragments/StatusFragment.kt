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
package bsolution.phone.activities.main.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import bsolution.phone.LinphoneApplication.Companion.coreContext
import bsolution.phone.R
import bsolution.phone.activities.GenericFragment
import bsolution.phone.activities.main.viewmodels.SharedMainViewModel
import bsolution.phone.activities.main.viewmodels.StatusViewModel
import bsolution.phone.databinding.StatusFragmentBinding
import bsolution.phone.utils.Event
import org.linphone.core.tools.Log

class StatusFragment : GenericFragment<StatusFragmentBinding>() {
    private lateinit var viewModel: StatusViewModel
    private lateinit var sharedViewModel: SharedMainViewModel

    override fun getLayoutId(): Int = R.layout.status_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner
        useMaterialSharedAxisXForwardAnimation = false

        viewModel = ViewModelProvider(this)[StatusViewModel::class.java]
        binding.viewModel = viewModel

        sharedViewModel = requireActivity().run {
            ViewModelProvider(this)[SharedMainViewModel::class.java]
        }

        sharedViewModel.accountRemoved.observe(
            viewLifecycleOwner
        ) {
            Log.i("[Status Fragment] An account was removed, update default account state")
            val defaultAccount = coreContext.core.defaultAccount
            if (defaultAccount != null) {
                viewModel.updateDefaultAccountRegistrationStatus(defaultAccount.state)
            }
        }

        binding.setMenuClickListener {
            sharedViewModel.toggleDrawerEvent.value = Event(true)
        }

        binding.setRefreshClickListener {
            viewModel.refreshRegister()
        }

        onBackPressedCallback.isEnabled = false
    }
}
