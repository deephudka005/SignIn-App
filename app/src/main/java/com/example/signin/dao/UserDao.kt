package com.example.signin.dao

import com.example.signin.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class UserDao {
    private val db = FirebaseFirestore.getInstance()
    private val userscollection =db.collection("users")

    fun addUser(user: User?){
        user?.let {
            GlobalScope.launch(Dispatchers.IO) {
                userscollection.document(user.uid).set(it)
            }
        }
    }
}