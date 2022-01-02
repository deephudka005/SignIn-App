package com.example.signin.model

import android.provider.ContactsContract

data class User(val uid: String ="",
                val displayName:String?="",
                val phoneNumber: String?="",
                //val last_name: String?="",
                //val gender: String?="",
                val imageUrl: String="",
                val email: String="")