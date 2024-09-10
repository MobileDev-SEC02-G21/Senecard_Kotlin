package com.mobiles.senecard

import android.os.Bundle
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ActivityAuth : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Initial View
    private lateinit var mainLayout: ConstraintLayout
    private lateinit var authAuxiliaryButtonEnter: Button
    private lateinit var authAuxiliaryButtonRegister: Button

    // Login View
    private lateinit var authLoginForm: LinearLayout
    private lateinit var authLoginFormUser: EditText
    private lateinit var authLoginFormPassword: EditText
    private lateinit var authLoginFormForgotPasswordText: TextView
    private lateinit var authLoginFormCheckBox: CheckBox
    private lateinit var authLoginFormButtonEnter: Button
    private lateinit var authLoginFormSignUpText: TextView

    // Register View
    private lateinit var authSignUpFormOne: LinearLayout
    private lateinit var authSignUpFormOneName: EditText
    private lateinit var authSignUpFormOneStudentCode: EditText
    private lateinit var authSignUpFormOnePhone: EditText
    private lateinit var authSignUpFormOneAuxiliaryButtonNext: Button
    private lateinit var authSignUpFormOneLoginText: TextView
    private lateinit var authSignUpFormTwo: LinearLayout
    private lateinit var authSignUpFormTwoUser: EditText
    private lateinit var authSignUpFormTwoPassword: EditText
    private lateinit var authSignUpFormTwoConfirmPassword: EditText
    private lateinit var authSignUpFormTwoButtonRegister: Button
    private lateinit var authSignUpFormTwoLoginText: TextView

    // Animations auxiliaries
    private var isAuthLoginFormVisible: Boolean = false
    private var isAuthSignupFormOneVisible: Boolean = false
    private var isAuthSignupFormTwoVisible: Boolean = false

    private fun setElements() {
        mainLayout = findViewById(R.id.main_layout)
        authAuxiliaryButtonEnter = findViewById(R.id.auth_auxiliary_button_enter)
        authAuxiliaryButtonRegister = findViewById(R.id.auth_auxiliary_button_register)
        authLoginForm = findViewById(R.id.auth_login_form)
        authLoginFormUser = findViewById(R.id.auth_login_form_user)
        authLoginFormPassword = findViewById(R.id.auth_login_form_password)
        authLoginFormForgotPasswordText = findViewById(R.id.auth_login_form_forgot_password_text)
        authLoginFormCheckBox = findViewById(R.id.auth_login_form_check_box)
        authLoginFormButtonEnter = findViewById(R.id.auth_login_form_button_enter)
        authLoginFormSignUpText = findViewById(R.id.auth_login_form_sign_up_text)
        authSignUpFormOne = findViewById(R.id.auth_sign_up_form_one)
        authSignUpFormOneName = findViewById(R.id.auth_sign_up_form_one_name)
        authSignUpFormOneStudentCode = findViewById(R.id.auth_sign_up_form_one_student_code)
        authSignUpFormOnePhone = findViewById(R.id.auth_sign_up_form_one_phone)
        authSignUpFormOneAuxiliaryButtonNext = findViewById(R.id.auth_sign_up_form_one_auxiliary_button_next)
        authSignUpFormOneLoginText = findViewById(R.id.auth_sign_up_form_one_login_text)
        authSignUpFormTwo = findViewById(R.id.auth_sign_up_form_two)
        authSignUpFormTwoUser = findViewById(R.id.auth_sign_up_form_two_user)
        authSignUpFormTwoPassword = findViewById(R.id.auth_sign_up_form_two_password)
        authSignUpFormTwoConfirmPassword = findViewById(R.id.auth_sign_up_form_two_confirm_password)
        authSignUpFormTwoButtonRegister = findViewById(R.id.auth_sign_up_form_two_button_register)
        authSignUpFormTwoLoginText = findViewById(R.id.auth_sign_up_form_two_login_text)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_auth)
        setElements()
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setButtons()
    }

    private fun goHomePage(user: String) {
        resetAuthActivity()
        val homeIntent = Intent(this, ActivityHome::class.java).apply {
            putExtra("user", user)
        }
        startActivity(homeIntent)
    }

    private fun logicAuthLoginFormButtonEnter() {
        if (authLoginFormUser.text.isEmpty()) {
            CustomDialog(getString(R.string.auth_enter_user), "info").show(supportFragmentManager, "customDialog")
        }
        else if (authLoginFormPassword.text.isEmpty()) {
            CustomDialog(getString(R.string.auth_enter_password), "info").show(supportFragmentManager, "customDialog")
        }
        else {
            db.collection("users").document(authLoginFormUser.text.toString()).get().addOnSuccessListener { document ->
                if (document.exists()) {
                    auth.signInWithEmailAndPassword(authLoginFormUser.text.toString()+"@uniandes.edu.co", authLoginFormPassword.text.toString()).addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            goHomePage(authLoginFormUser.text.toString())
                        } else {
                            CustomDialog(getString(R.string.auth_error_not_correct_password), "error").show(supportFragmentManager, "customDialog")
                        }
                    }
                } else {
                    CustomDialog(getString(R.string.auth_user_not_found), "error").show(supportFragmentManager, "customDialog")
                }
            }.addOnFailureListener {
                CustomDialog(getString(R.string.auth_error_querying_database), "error").show(supportFragmentManager, "customDialog")
            }
        }
    }

    private fun logicAuthSignUpFormOneButtonNext() {
        if (authSignUpFormOneName.text.isEmpty()) {
            CustomDialog(getString(R.string.auth_enter_name), "info").show(supportFragmentManager, "customDialog")
        }
        else if (authSignUpFormOneStudentCode.text.isEmpty()) {
            CustomDialog(getString(R.string.auth_enter_student_code), "info").show(supportFragmentManager, "customDialog")
        }
        else if (authSignUpFormOnePhone.text.isEmpty()) {
            CustomDialog(getString(R.string.auth_enter_phone), "info").show(supportFragmentManager, "customDialog")
        }
        else {
            CustomDialog(getString(R.string.auth_message_button_next), "info") {
                hideAuthSignUpFormOne {
                    showAuthSignUpFormTwo()
                }
            }.show(supportFragmentManager, "customDialog")
        }
    }

    private fun logicAuthSignUpFormTwoButtonRegister() {
        if (authSignUpFormTwoUser.text.isEmpty()) {
            CustomDialog(getString(R.string.auth_enter_user), "info").show(supportFragmentManager, "customDialog")
        }
        else if (!(authSignUpFormTwoUser.text.toString()).matches(Regex("[a-zA-Z0-9.]+"))) {
            CustomDialog(getString(R.string.auth_user_not_correct), "error").show(supportFragmentManager, "customDialog")
        }
        else if (authSignUpFormTwoPassword.text.isEmpty()) {
            CustomDialog(getString(R.string.auth_enter_password), "info").show(supportFragmentManager, "customDialog")
        }
        else if (authSignUpFormTwoPassword.text.length < 6) {
            CustomDialog(getString(R.string.auth_password_minimum_characters), "info").show(supportFragmentManager, "customDialog")
        }
        else if (authSignUpFormTwoConfirmPassword.text.isEmpty()) {
            CustomDialog(getString(R.string.auth_enter_confirm_password), "info").show(supportFragmentManager, "customDialog")
        }
        else if (authSignUpFormTwoPassword.text.toString() != authSignUpFormTwoConfirmPassword.text.toString()) {
            CustomDialog(getString(R.string.auth_not_equal_passwords), "info").show(supportFragmentManager, "customDialog")
        }
        else {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                authSignUpFormTwoUser.text.toString() + "@uniandes.edu.co",
                authSignUpFormTwoPassword.text.toString()
            ).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    db.collection("users").document(authSignUpFormTwoUser.text.toString()).set(
                        hashMapOf(
                            "name" to authSignUpFormOneName.text.toString(),
                            "student_code" to authSignUpFormOneStudentCode.text.toString(),
                            "phone" to authSignUpFormOnePhone.text.toString()
                        )
                    )
                    goHomePage(authSignUpFormTwoUser.text.toString())
                } else {
                    CustomDialog(getString(R.string.auth_error_registering_user), "error").show(supportFragmentManager, "customDialog")
                }
            }
        }
    }

    private fun setButtons() {
        authAuxiliaryButtonEnter.setOnClickListener {
            hideAuthAuxiliaryButtons {
                showAuhLoginForm()
            }
        }
        authAuxiliaryButtonRegister.setOnClickListener {
            hideAuthAuxiliaryButtons {
                showAuthSignUpFormOne()
            }
        }
        authLoginFormButtonEnter.setOnClickListener {
            logicAuthLoginFormButtonEnter()
        }
        authSignUpFormOneAuxiliaryButtonNext.setOnClickListener {
            logicAuthSignUpFormOneButtonNext()
        }
        authSignUpFormTwoButtonRegister.setOnClickListener {
            logicAuthSignUpFormTwoButtonRegister()
        }
    }

    private fun resetAuthActivity() {
        authLoginForm.visibility = View.GONE
        authSignUpFormOne.visibility = View.GONE
        authSignUpFormTwo.visibility = View.GONE

        isAuthLoginFormVisible = false
        isAuthSignupFormOneVisible = false
        isAuthSignupFormTwoVisible = false

        authLoginFormUser.setText(getString(R.string.empty_string))
        authLoginFormPassword.setText(getString(R.string.empty_string))
        authSignUpFormOneName.setText(getString(R.string.empty_string))
        authSignUpFormOneStudentCode.setText(getString(R.string.empty_string))
        authSignUpFormOnePhone.setText(getString(R.string.empty_string))
        authSignUpFormTwoUser.setText(getString(R.string.empty_string))
        authSignUpFormTwoPassword.setText(getString(R.string.empty_string))
        authSignUpFormTwoConfirmPassword.setText(getString(R.string.empty_string))

        authAuxiliaryButtonEnter.visibility = View.VISIBLE
        authAuxiliaryButtonRegister.visibility = View.VISIBLE
        authAuxiliaryButtonEnter.translationY = 0f
        authAuxiliaryButtonRegister.translationY = 0f
    }

    private fun hideAuthAuxiliaryButtons(onAnimationEnd: () -> Unit) {
        val animatorAuthAuxiliaryButtonEnter = ObjectAnimator.ofFloat(authAuxiliaryButtonEnter, "translationY", 0f, resources.displayMetrics.heightPixels.toFloat())
        animatorAuthAuxiliaryButtonEnter.duration = 500
        animatorAuthAuxiliaryButtonEnter.interpolator = AccelerateInterpolator()

        val animatorAuthAuxiliaryButtonRegister = ObjectAnimator.ofFloat(authAuxiliaryButtonRegister, "translationY", 0f, resources.displayMetrics.heightPixels.toFloat())
        animatorAuthAuxiliaryButtonRegister.duration = 500
        animatorAuthAuxiliaryButtonRegister.interpolator = AccelerateInterpolator()

        animatorAuthAuxiliaryButtonEnter.start()
        animatorAuthAuxiliaryButtonRegister.start()

        animatorAuthAuxiliaryButtonEnter.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                onAnimationEnd()
            }
        })
    }

    private fun showAuthAuxiliaryButtons() {
        isAuthLoginFormVisible = false
        isAuthSignupFormOneVisible = false
        isAuthSignupFormTwoVisible = false

        val animatorAuthAuxiliaryButtonEnter = ObjectAnimator.ofFloat(authAuxiliaryButtonEnter, "translationY", resources.displayMetrics.heightPixels.toFloat(), 0f)
        animatorAuthAuxiliaryButtonEnter.duration = 500
        animatorAuthAuxiliaryButtonEnter.interpolator = DecelerateInterpolator()

        val animatorAuthAuxiliaryButtonRegister = ObjectAnimator.ofFloat(authAuxiliaryButtonRegister, "translationY", resources.displayMetrics.heightPixels.toFloat(), 0f)
        animatorAuthAuxiliaryButtonRegister.duration = 500
        animatorAuthAuxiliaryButtonRegister.interpolator = DecelerateInterpolator()

        animatorAuthAuxiliaryButtonEnter.start()
        animatorAuthAuxiliaryButtonRegister.start()
    }

    private fun hideAuthLoginForm(onAnimationEnd: () -> Unit) {
        val animatorAuthLoginForm = ObjectAnimator.ofFloat(authLoginForm, "translationY", 0f, resources.displayMetrics.heightPixels.toFloat())
        animatorAuthLoginForm.duration = 500
        animatorAuthLoginForm.interpolator = AccelerateInterpolator()
        animatorAuthLoginForm.start()

        animatorAuthLoginForm.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                authLoginForm.visibility = View.GONE
                onAnimationEnd()
            }
        })
    }

    private fun showAuhLoginForm() {
        isAuthLoginFormVisible = true
        isAuthSignupFormOneVisible = false
        isAuthSignupFormTwoVisible = false
        authLoginForm.visibility = View.VISIBLE
        val animatorAuthLoginForm = ObjectAnimator.ofFloat(authLoginForm, "translationY", resources.displayMetrics.heightPixels.toFloat(), 0f)
        animatorAuthLoginForm.duration = 500
        animatorAuthLoginForm.interpolator = DecelerateInterpolator()
        animatorAuthLoginForm.start()
    }

    private fun hideAuthSignUpFormOne(onAnimationEnd: () -> Unit) {
        val animatorAuthSignUpFormOne = ObjectAnimator.ofFloat(authSignUpFormOne, "translationY", 0f, resources.displayMetrics.heightPixels.toFloat())
        animatorAuthSignUpFormOne.duration = 500
        animatorAuthSignUpFormOne.interpolator = AccelerateInterpolator()
        animatorAuthSignUpFormOne.start()

        animatorAuthSignUpFormOne.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                authSignUpFormOne.visibility = View.GONE
                onAnimationEnd()
            }
        })
    }

    private fun showAuthSignUpFormOne() {
        isAuthLoginFormVisible = false
        isAuthSignupFormOneVisible = true
        isAuthSignupFormTwoVisible = false
        authSignUpFormOne.visibility = View.VISIBLE
        val animatorAuthSignUpFormOne = ObjectAnimator.ofFloat(authSignUpFormOne, "translationY", resources.displayMetrics.heightPixels.toFloat(), 0f)
        animatorAuthSignUpFormOne.duration = 500
        animatorAuthSignUpFormOne.interpolator = DecelerateInterpolator()
        animatorAuthSignUpFormOne.start()
    }

    private fun hideAuthSignUpFormTwo(onAnimationEnd: () -> Unit) {
        val animatorAuthSignUpFormTwo = ObjectAnimator.ofFloat(authSignUpFormTwo, "translationY", 0f, resources.displayMetrics.heightPixels.toFloat())
        animatorAuthSignUpFormTwo.duration = 500
        animatorAuthSignUpFormTwo.interpolator = AccelerateInterpolator()
        animatorAuthSignUpFormTwo.start()

        animatorAuthSignUpFormTwo.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                authSignUpFormTwo.visibility = View.GONE
                onAnimationEnd()
            }
        })
    }

    private fun showAuthSignUpFormTwo() {
        isAuthLoginFormVisible = false
        isAuthSignupFormOneVisible = false
        isAuthSignupFormTwoVisible = true
        authSignUpFormTwo.visibility = View.VISIBLE

        val animatorAuthSignUpFormTwo = ObjectAnimator.ofFloat(authSignUpFormTwo, "translationY", resources.displayMetrics.heightPixels.toFloat(), 0f)
        animatorAuthSignUpFormTwo.duration = 500
        animatorAuthSignUpFormTwo.interpolator = DecelerateInterpolator()
        animatorAuthSignUpFormTwo.start()
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        fun handleTouchOutsideForm(formView: View, isVisible: Boolean, hideFormAction: () -> Unit): Boolean {
            if (ev?.action == MotionEvent.ACTION_DOWN && isVisible) {
                val rect = IntArray(2)
                formView.getLocationOnScreen(rect)
                val x = ev.rawX.toInt()
                val y = ev.rawY.toInt()
                val formLeft = rect[0]
                val formTop = rect[1]
                val formRight = formLeft + formView.width
                val formBottom = formTop + formView.height

                if (x < formLeft || x > formRight || y < formTop || y > formBottom) {
                    hideFormAction()
                    return true
                }
            }
            return false
        }

        if (handleTouchOutsideForm(authLoginForm, isAuthLoginFormVisible) {
                hideAuthLoginForm { showAuthAuxiliaryButtons() }
            }) {
            return true
        }

        if (handleTouchOutsideForm(authSignUpFormOne, isAuthSignupFormOneVisible) {
                hideAuthSignUpFormOne { showAuthAuxiliaryButtons() }
            }) {
            return true
        }

        if (handleTouchOutsideForm(authSignUpFormTwo, isAuthSignupFormTwoVisible) {
                hideAuthSignUpFormTwo { showAuthAuxiliaryButtons() }
            }) {
            return true
        }

        return super.dispatchTouchEvent(ev)
    }
}