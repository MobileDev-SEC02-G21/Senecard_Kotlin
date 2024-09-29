package com.mobiles.senecard

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment

class CustomDialog(
    private val message: String,
    private val type: String,
    private val onPositiveClick: (() -> Unit)? = null
) : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.custom_dialog, container, false)

        val messageTextView: TextView = view.findViewById(R.id.dialog_message)
        val typeTextView: TextView = view.findViewById(R.id.dialog_type)
        val iconImageView: ImageView = view.findViewById(R.id.dialog_icon)
        val button: Button = view.findViewById(R.id.dialog_button)

        messageTextView.text = message
        messageTextView.gravity = Gravity.CENTER

        when (type) {
            "info" -> {
                typeTextView.text = getString(R.string.custom_dialog_info)
                typeTextView.setTextColor(resources.getColor(R.color.primary, null))
                iconImageView.setImageResource(R.mipmap.icon_custom_dialog_info)
                iconImageView.contentDescription = getString(R.string.custom_dialog_info)
            }
            "error" -> {
                typeTextView.text = getString(R.string.custom_dialog_error)
                typeTextView.setTextColor(resources.getColor(R.color.red_highlight, null))
                iconImageView.setImageResource(R.mipmap.icon_custom_dialog_error)
                iconImageView.contentDescription = getString(R.string.custom_dialog_error)
            }
            "success" -> {
                typeTextView.text = getString(R.string.custom_dialog_success)
                typeTextView.setTextColor(resources.getColor(R.color.green_highlight, null))
                iconImageView.setImageResource(R.mipmap.icon_custom_dialog_success)
                iconImageView.contentDescription = getString(R.string.custom_dialog_success)
            }
        }

        button.setOnClickListener {
            onPositiveClick?.invoke()
            dismiss()
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
}