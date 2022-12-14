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
package bsolution.phone.activities.main.history.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import bsolution.phone.LinphoneApplication.Companion.coreContext
import bsolution.phone.R
import bsolution.phone.activities.*
import bsolution.phone.activities.main.MainActivity
import bsolution.phone.activities.main.history.viewmodels.CallLogViewModel
import bsolution.phone.activities.main.history.viewmodels.CallLogViewModelFactory
import bsolution.phone.activities.main.viewmodels.SharedMainViewModel
import bsolution.phone.activities.navigateToChatRoom
import bsolution.phone.activities.navigateToContact
import bsolution.phone.activities.navigateToContacts
import bsolution.phone.activities.navigateToFriend
import bsolution.phone.databinding.HistoryDetailFragmentBinding
import bsolution.phone.utils.Event
import org.linphone.core.tools.Log

class DetailCallLogFragment : GenericFragment<HistoryDetailFragmentBinding>() {
    private lateinit var viewModel: CallLogViewModel
    private lateinit var sharedViewModel: SharedMainViewModel

    override fun getLayoutId(): Int = R.layout.history_detail_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner

        sharedViewModel = requireActivity().run {
            ViewModelProvider(this)[SharedMainViewModel::class.java]
        }
        binding.sharedMainViewModel = sharedViewModel

        val callLogGroup = sharedViewModel.selectedCallLogGroup.value
        if (callLogGroup == null) {
            Log.e("[History] Call log group is null, aborting!")
            // (activity as MainActivity).showSnackBar(R.string.error)
            findNavController().navigateUp()
            return
        }

        viewModel = ViewModelProvider(
            this,
            CallLogViewModelFactory(callLogGroup.lastCallLog)
        )[CallLogViewModel::class.java]
        binding.viewModel = viewModel

        useMaterialSharedAxisXForwardAnimation = sharedViewModel.isSlidingPaneSlideable.value == false

        viewModel.addRelatedCallLogs(callLogGroup.callLogs)

        binding.setBackClickListener {
            goBack()
        }

        binding.setNewContactClickListener {
            val copy = viewModel.callLog.remoteAddress.clone()
            copy.clean()
            Log.i("[History] Creating contact with SIP URI: ${copy.asStringUriOnly()}")
            sharedViewModel.updateContactsAnimationsBasedOnDestination.value = Event(R.id.masterCallLogsFragment)
            navigateToContacts(copy.asStringUriOnly())
        }

        binding.setContactClickListener {
            sharedViewModel.updateContactsAnimationsBasedOnDestination.value = Event(R.id.masterCallLogsFragment)
            val contactId = viewModel.contact.value?.refKey
            if (contactId != null) {
                Log.i("[History] Displaying contact $contactId")
                navigateToContact(contactId)
            } else {
                val copy = viewModel.callLog.remoteAddress.clone()
                copy.clean()
                Log.i("[History] Displaying friend with address ${copy.asStringUriOnly()}")
                navigateToFriend(copy)
            }
        }

        viewModel.startCallEvent.observe(
            viewLifecycleOwner
        ) {
            it.consume { callLog ->
                val address = callLog.remoteAddress
                if (coreContext.core.callsNb > 0) {
                    Log.i("[History] Starting dialer with pre-filled URI ${address.asStringUriOnly()}, is transfer? ${sharedViewModel.pendingCallTransfer}")
                    sharedViewModel.updateDialerAnimationsBasedOnDestination.value =
                        Event(R.id.masterCallLogsFragment)

                    val args = Bundle()
                    args.putString("URI", address.asStringUriOnly())
                    args.putBoolean("Transfer", sharedViewModel.pendingCallTransfer)
                    args.putBoolean(
                        "SkipAutoCallStart",
                        true
                    ) // If auto start call setting is enabled, ignore it
                    navigateToDialer(args)
                } else {
                    val localAddress = callLog.localAddress
                    coreContext.startCall(address, localAddress = localAddress)
                }
            }
        }

        viewModel.chatRoomCreatedEvent.observe(
            viewLifecycleOwner
        ) {
            it.consume { chatRoom ->
                val args = Bundle()
                args.putString("LocalSipUri", chatRoom.localAddress.asStringUriOnly())
                args.putString("RemoteSipUri", chatRoom.peerAddress.asStringUriOnly())
                navigateToChatRoom(args)
            }
        }

        viewModel.onErrorEvent.observe(
            viewLifecycleOwner
        ) {
            it.consume { messageResourceId ->
                (activity as MainActivity).showSnackBar(messageResourceId)
            }
        }
    }

    override fun goBack() {
        if (sharedViewModel.isSlidingPaneSlideable.value == true) {
            sharedViewModel.closeSlidingPaneEvent.value = Event(true)
        } else {
            navigateToEmptyCallHistory()
        }
    }
}
