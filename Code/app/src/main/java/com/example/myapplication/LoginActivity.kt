package com.example.myapplication

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        prefs = getSharedPreferences("users", MODE_PRIVATE)

        // auto logins the user if they are already logged in
        val savedUser = prefs.getString("loggedInUser", null)
        if (savedUser != null) {
            goToGame()
            return
        }

        val usernameInput = findViewById<EditText>(R.id.inputUsername)
        val passwordInput = findViewById<EditText>(R.id.inputPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnLogin.setOnClickListener {
            val user = usernameInput.text.toString()
            val pass = passwordInput.text.toString()

            val storedPass = prefs.getString("user_$user", null)

            if (storedPass == pass) {
                prefs.edit().putString("loggedInUser", user).apply()
                Toast.makeText(this, "Welcome back $user!", Toast.LENGTH_SHORT).show()
                goToGame()
            } else {
                Toast.makeText(this, "Invalid login", Toast.LENGTH_SHORT).show()
            }
        }

        btnRegister.setOnClickListener {
            val user = usernameInput.text.toString()
            val pass = passwordInput.text.toString()

            val storedPass = prefs.getString("user_$user", null)
            if (storedPass != null) {
                Toast.makeText(this, "User already exists!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            prefs.edit().putString("user_$user", pass).apply()
            prefs.edit().putString("loggedInUser", user).apply()

            Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show()

            goToGame()
        }
    }

    private fun goToGame() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}