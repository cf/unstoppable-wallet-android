package io.horizontalsystems.bankwallet.ui.extensions

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import io.horizontalsystems.bankwallet.R

class ConfirmationDialog(
        private val listener: Listener,
        private val title: String,
        private val subtitle: String,
        private val icon: Int?,
        private val contentText: String,
        private val actionButtonTitle: String?,
        private val cancelButtonTitle: String?)
    : BaseBottomSheetDialogFragment() {

    interface Listener {
        fun onActionButtonClick() {}
        fun onCancelButtonClick() {}
    }

    private lateinit var contentTextView: TextView
    private lateinit var btnAction: Button
    private lateinit var btnCancel: Button

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        listener.onCancelButtonClick()
    }

    override fun close() {
        super.close()
        listener.onCancelButtonClick()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setContentView(R.layout.fragment_confirmation_dialog)

        setTitle(title)
        setSubtitle(subtitle)

        // if null set default "Attention" ICON
        icon?.let {
          setHeaderIcon(it)
        }?:setHeaderIcon(R.drawable.ic_attention_yellow)

        contentTextView = view.findViewById(R.id.contentText)
        btnAction = view.findViewById(R.id.btnYellow)
        btnCancel = view.findViewById(R.id.btnGrey)

        contentTextView.text = contentText

        bindActions()
    }

    private fun bindActions() {

        // Set Visibility based on title is NULL or not
        btnAction.visibility = if(actionButtonTitle == null) View.GONE else View.VISIBLE
        btnCancel.visibility = if(cancelButtonTitle == null) View.GONE else View.VISIBLE

        actionButtonTitle?.let {

            btnAction.text = actionButtonTitle
            btnAction.setOnClickListener {
                listener.onActionButtonClick()
                dismiss()
            }
        }

        cancelButtonTitle?.let {

            btnCancel.text = cancelButtonTitle
            btnCancel.setOnClickListener {
                listener.onCancelButtonClick()
                dismiss()
            }
        }
    }

    companion object {

        fun show(icon: Int? = null, title: String, subtitle: String, contentText: String,
                 actionButtonTitle: String? = "", cancelButtonTitle: String? = "", activity: FragmentActivity, listener: Listener) {

            val fragment = ConfirmationDialog(listener, title, subtitle, icon, contentText, actionButtonTitle, cancelButtonTitle)
            val transaction = activity.supportFragmentManager.beginTransaction()

            transaction.add(fragment, "bottom_coin_settings_alert_dialog")
            transaction.commitAllowingStateLoss()
        }
    }
}
