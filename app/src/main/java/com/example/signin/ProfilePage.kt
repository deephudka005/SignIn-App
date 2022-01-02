package com.example.signin

import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.android.volley.Request
import com.android.volley.Response
import com.bumptech.glide.Glide
import com.android.volley.toolbox.JsonObjectRequest
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

import kotlinx.android.synthetic.main.activity_profile_page.*
import javax.sql.DataSource


class ProfilePage : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_page)
        auth= Firebase.auth
        val account = auth.currentUser
        val fbphotourl= auth.getCurrentUser()?.getPhotoUrl()
        username.setText(account?.displayName)
        email.setText(account?.email)
        phoneNumber.setText(account?.phoneNumber)

        //Glide.with(this)
        //    .load(account?.photoUrl)
        //    .into(profileImage);

        Picasso.get().load(fbphotourl).into(profileImage)

    }

}


