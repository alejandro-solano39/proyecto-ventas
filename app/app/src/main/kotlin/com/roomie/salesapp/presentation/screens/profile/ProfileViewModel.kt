package com.roomie.salesapp.presentation.screens.profile

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.roomie.salesapp.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val prefs: SharedPreferences
) : ViewModel() {
    val userName: String = prefs.getString(Constants.PREFS_USER, null) ?: "Vendedor Demo"
}
