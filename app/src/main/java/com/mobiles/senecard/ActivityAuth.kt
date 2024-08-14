package com.mobiles.senecard

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Bundle
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

class ActivityAuth : AppCompatActivity() {

    private lateinit var mainLayout: ConstraintLayout

    // Initial View
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

    private fun setViews() {
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
        setViews()
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setButtons()
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

        }

        authSignUpFormOneAuxiliaryButtonNext.setOnClickListener {
            hideAuthSignUpFormOne {
                showAuthSignUpFormTwo()
            }
        }

        authSignUpFormTwoButtonRegister.setOnClickListener {

        }

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

    private fun showAuthAuxiliaryButton() {
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
        if (ev?.action == MotionEvent.ACTION_DOWN && isAuthLoginFormVisible) {
            val rect = IntArray(2)
            authLoginForm.getLocationOnScreen(rect)
            val x = ev.rawX.toInt()
            val y = ev.rawY.toInt()
            val formLeft = rect[0]
            val formTop = rect[1]
            val formRight = formLeft + authLoginForm.width
            val formBottom = formTop + authLoginForm.height

            if (x < formLeft || x > formRight || y < formTop || y > formBottom) {
                hideAuthLoginForm {
                    showAuthAuxiliaryButton()
                }
                return true
            }
        }
        else if (ev?.action == MotionEvent.ACTION_DOWN && isAuthSignupFormOneVisible) {
            val rect = IntArray(2)
            authSignUpFormOne.getLocationOnScreen(rect)
            val x = ev.rawX.toInt()
            val y = ev.rawY.toInt()
            val formLeft = rect[0]
            val formTop = rect[1]
            val formRight = formLeft + authSignUpFormOne.width
            val formBottom = formTop + authSignUpFormOne.height

            if (x < formLeft || x > formRight || y < formTop || y > formBottom) {
                hideAuthSignUpFormOne {
                    showAuthAuxiliaryButton()
                }
                return true
            }
        }
        else if (ev?.action == MotionEvent.ACTION_DOWN && isAuthSignupFormTwoVisible) {
            val rect = IntArray(2)
            authSignUpFormTwo.getLocationOnScreen(rect)
            val x = ev.rawX.toInt()
            val y = ev.rawY.toInt()
            val formLeft = rect[0]
            val formTop = rect[1]
            val formRight = formLeft + authSignUpFormTwo.width
            val formBottom = formTop + authSignUpFormTwo.height

            if (x < formLeft || x > formRight || y < formTop || y > formBottom) {
                hideAuthSignUpFormTwo {
                    showAuthAuxiliaryButton()
                }
                return true
            }
        }
        return super.dispatchTouchEvent(ev)
    }

}