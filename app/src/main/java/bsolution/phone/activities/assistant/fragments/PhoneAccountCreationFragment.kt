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
package bsolution.phone.activities.assistant.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import bsolution.phone.R
import bsolution.phone.activities.assistant.AssistantActivity
import bsolution.phone.activities.assistant.viewmodels.PhoneAccountCreationViewModel
import bsolution.phone.activities.assistant.viewmodels.PhoneAccountCreationViewModelFactory
import bsolution.phone.activities.assistant.viewmodels.SharedAssistantViewModel
import bsolution.phone.activities.navigateToPhoneAccountValidation
import bsolution.phone.databinding.AssistantPhoneAccountCreationFragmentBinding
import org.linphone.mediastream.Version

class PhoneAccountCreationFragment :
    AbstractPhoneFragment<AssistantPhoneAccountCreationFragmentBinding>() {
    private lateinit var sharedViewModel: SharedAssistantViewModel
    override lateinit var viewModel: PhoneAccountCreationViewModel

    override fun getLayoutId(): Int = R.layout.assistant_phone_account_creation_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner

        sharedViewModel = requireActivity().run {
            ViewModelProvider(this)[SharedAssistantViewModel::class.java]
        }

        viewModel = ViewModelProvider(
            this,
            PhoneAccountCreationViewModelFactory(sharedViewModel.getAccountCreator())
        )[PhoneAccountCreationViewModel::class.java]
        binding.viewModel = viewModel

        binding.setInfoClickListener {
            showPhoneNumberInfoDialog()
        }

        binding.setSelectCountryClickListener {
            CountryPickerFragment(viewModel).show(childFragmentManager, "CountryPicker")
        }

        viewModel.goToSmsValidationEvent.observe(
            viewLifecycleOwner
        ) {
            it.consume {
                val args = Bundle()
                args.putBoolean("IsCreation", true)
                args.putString("PhoneNumber", viewModel.accountCreator.phoneNumber)
                navigateToPhoneAccountValidation(args)
            }
        }

        viewModel.onErrorEvent.observe(
            viewLifecycleOwner
        ) {
            it.consume { message ->
                (requireActivity() as AssistantActivity).showSnackBar(message)
            }
        }

        if (Version.sdkAboveOrEqual(Version.API23_MARSHMALLOW_60)) {
            checkPermissions()
        }
    }
}
