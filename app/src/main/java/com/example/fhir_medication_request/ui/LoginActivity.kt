package com.example.fhir_medication_request.ui

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.fhir_medication_request.R
import com.example.fhir_medication_request.common.SNACK
import com.example.fhir_medication_request.common.goToMain
import com.example.fhir_medication_request.databinding.ActivityLoginBinding
import com.example.fhir_medication_request.notification.LogoutJob
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var binding: ActivityLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private val jobScheduler by lazy { getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler }

    private val auth by lazy { Firebase.auth }
    private val googleSignInCallback = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val googleUser = GoogleSignIn.getSignedInAccountFromIntent(it.data)
        try {
            val account = googleUser.getResult(ApiException::class.java)
            auth.signInWithCredential(GoogleAuthProvider.getCredential(account?.idToken, null))
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser ?: return@addOnCompleteListener
                        startActivity(goToMain(this, user.uid, user.isAnonymous).putExtra(SNACK, "Login successful!"))
                        startJobScheduler()
                        finish()
                    } else {
                        Snackbar.make(binding.root, "User login failed: ${task.exception?.message}", Snackbar.LENGTH_LONG).show()
                    }
                }
        } catch (e: ApiException) {
            Log.w(LoginActivity::class.simpleName, "Google sign in failed", e)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user = auth.currentUser
        if (user != null) {
            startActivity(goToMain(this, user.uid, user.isAnonymous))
            startJobScheduler()
            finish()
        }

        intent.getStringExtra(SNACK)?.let {
            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
        }

        emailEditText = binding.loginEmail.editText!!
        passwordEditText = binding.loginPassword.editText!!

        googleSignInClient = GoogleSignIn.getClient(
            this,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        )
    }

    fun loginGuest(source: View) = auth.signInAnonymously().addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val user = auth.currentUser ?: return@addOnCompleteListener
            startActivity(goToMain(this, user.uid, user.isAnonymous).putExtra(SNACK, "Login successful!"))
            startJobScheduler()
            finish()
        } else {
            Snackbar.make(source, "User login failed: ${task.exception?.message}", Snackbar.LENGTH_LONG).show()
        }
    }

    fun login(source: View) {
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
//            Toast.makeText(this, R.string.invalid_email, Toast.LENGTH_LONG).show()
            Snackbar.make(source, R.string.invalid_email, Snackbar.LENGTH_LONG).show()
            return
        }
        if (password.isEmpty() || password.length < 6) {
//            Toast.makeText(this, R.string.invalid_password, Toast.LENGTH_LONG).show()
            Snackbar.make(source, R.string.invalid_password, Snackbar.LENGTH_LONG).show()
            return
        }
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser ?: return@addOnCompleteListener
                startActivity(goToMain(this, user.uid, user.isAnonymous).putExtra(SNACK, "Login successful!"))
                startJobScheduler()
                finish()
            } else {
//                Toast.makeText(this, "User login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                Snackbar.make(source, "User login failed: ${task.exception?.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    fun register(@Suppress("UNUSED_PARAMETER") source: View)
        = startActivity(Intent(this, RegisterActivity::class.java))

    fun loginGoogle(@Suppress("UNUSED_PARAMETER") source: View)
        = googleSignInCallback.launch(googleSignInClient.signInIntent)

    private fun startJobScheduler() = jobScheduler.schedule(
        JobInfo.Builder(0, ComponentName(packageName, LogoutJob::class.java.name))
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setPeriodic(4 * 3_600_000) // 4 ora -> nem lehet kevesebb mint 15 perc mert nem hivodik meg
            .build()
    )
}
