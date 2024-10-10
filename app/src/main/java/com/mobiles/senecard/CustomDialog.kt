package com.mobiles.senecard

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.mobiles.senecard.databinding.CustomDialogBinding

class CustomDialog : DialogFragment() {

    private lateinit var message: String
    private lateinit var type: String

    private var _binding: CustomDialogBinding? = null
    private val binding get() = _binding!!

    companion object {
        private var currentDialog: CustomDialog? = null

        fun showCustomDialog(
            fragmentManager: androidx.fragment.app.FragmentManager,
            message: String,
            type: String,
        ) {
            hideCustomDialog()

            val dialog = CustomDialog().apply {
                this.message = message
                this.type = type
            }

            currentDialog = dialog
            dialog.show(fragmentManager, "CustomDialog")
        }

        fun hideCustomDialog() {
            currentDialog?.dismissAllowingStateLoss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        dialog.setOnKeyListener { _, keyCode, _ ->
            keyCode == android.view.KeyEvent.KEYCODE_BACK
        }

        return dialog
    }

    @SuppressLint("ResourceAsColor")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CustomDialogBinding.inflate(inflater, container, false)

        binding.dialogMessage.text = message

        when (type) {
            "info" -> {
                binding.dialogType.text = getString(R.string.custom_dialog_info)
                binding.dialogIcon.setImageResource(R.mipmap.icon_custom_dialog_info)
            }
            "error" -> {
                binding.dialogType.text = getString(R.string.custom_dialog_error)
                binding.dialogType.setTextColor(ContextCompat.getColor(requireContext(), R.color.red_highlight))
                binding.dialogIcon.setImageResource(R.mipmap.icon_custom_dialog_error)
            }
            "success" -> {
                binding.dialogType.text = getString(R.string.custom_dialog_success)
                binding.dialogType.setTextColor(ContextCompat.getColor(requireContext(), R.color.green_highlight))
                binding.dialogIcon.setImageResource(R.mipmap.icon_custom_dialog_success)
            }
            "loading" -> {
                binding.dialogType.text = getString(R.string.custom_dialog_loading)
                binding.dialogIcon.visibility = View.GONE
                binding.dialogButton.visibility = View.GONE
                binding.loadingAnimation.visibility = View.VISIBLE
            }
        }

        binding.dialogButton.setOnClickListener {
            hideCustomDialog()
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        if (currentDialog === this) {
            currentDialog = null
        }
    }
}