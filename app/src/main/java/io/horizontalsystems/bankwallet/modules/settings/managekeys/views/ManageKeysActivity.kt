package io.horizontalsystems.bankwallet.modules.settings.managekeys.views

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import io.horizontalsystems.bankwallet.BaseActivity
import io.horizontalsystems.bankwallet.R
import io.horizontalsystems.bankwallet.core.EosUnsupportedException
import io.horizontalsystems.bankwallet.core.utils.ModuleCode
import io.horizontalsystems.bankwallet.entities.AccountType
import io.horizontalsystems.bankwallet.lib.AlertDialogFragment
import io.horizontalsystems.bankwallet.modules.backup.BackupModule
import io.horizontalsystems.bankwallet.modules.restore.eos.RestoreEosModule
import io.horizontalsystems.bankwallet.modules.restore.words.RestoreWordsModule
import io.horizontalsystems.bankwallet.modules.settings.managekeys.ManageKeysViewModel
import io.horizontalsystems.bankwallet.ui.dialogs.ManageKeysDeleteAlert
import io.horizontalsystems.bankwallet.ui.dialogs.ManageKeysDialog
import io.horizontalsystems.bankwallet.ui.dialogs.ManageKeysDialog.ManageAction
import io.horizontalsystems.bankwallet.ui.extensions.TopMenuItem
import kotlinx.android.synthetic.main.activity_manage_keys.*

class ManageKeysActivity : BaseActivity(), ManageKeysDialog.Listener {

    private lateinit var viewModel: ManageKeysViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(ManageKeysViewModel::class.java)
        viewModel.init()

        setContentView(R.layout.activity_manage_keys)
        shadowlessToolbar.bind(getString(R.string.ManageKeys_Title), TopMenuItem(R.drawable.back) { onBackPressed() })

        val adapter = ManageKeysAdapter(viewModel)
        recyclerView.adapter = adapter

        viewModel.confirmUnlinkEvent.observe(this, Observer { item ->
            item.account?.let { account ->

                val confirmationList = listOf(
                        getString(R.string.ManageKeys_Unlink_ConfirmationRemove, getString(item.predefinedAccountType.title)),
                        getString(R.string.ManageKeys_Unlink_ConfirmationDisable, getString(item.predefinedAccountType.coinCodes)),
                        getString(R.string.ManageKeys_Unlink_ConfirmationLoose)
                )

                val confirmListener = object : ManageKeysDeleteAlert.Listener {
                    override fun onConfirmationSuccess() {
                        viewModel.delegate.onConfirmUnlink(account.id)
                    }
                }

                ManageKeysDeleteAlert.show(this, confirmationList, confirmListener)
            }
        })

        viewModel.confirmCreateEvent.observe(this, Observer {
            ManageKeysDialog.show(getString(it.predefinedAccountType.title), getString(R.string.ManageCoins_AddCoin_Text, getString(it.predefinedAccountType.coinCodes)), this, this, ManageAction.CREATE)
        })

        viewModel.confirmBackupEvent.observe(this, Observer {
            ManageKeysDialog.show(getString(it.predefinedAccountType.title), getString(R.string.ManageKeys_UnlinkAlert), this, this, ManageAction.BACKUP)
        })

        viewModel.showErrorEvent.observe(this, Observer {
            if (it is EosUnsupportedException) {
                AlertDialogFragment.newInstance(
                        R.string.Alert_TitleWarning,
                        R.string.ManageCoins_EOSAlert_CreateButton,
                        R.string.Alert_Ok
                ).show(supportFragmentManager, "alert_dialog")
            }
        })

        viewModel.startBackupModuleLiveEvent.observe(this, Observer {
            it.account?.let { account ->
                BackupModule.start(this, account, getString(it.predefinedAccountType.coinCodes))
            }
        })

        viewModel.startRestoreWordsLiveEvent.observe(this, Observer { wordsCount ->
            RestoreWordsModule.startForResult(this, wordsCount, ModuleCode.RESTORE_WORDS)
        })

        viewModel.startRestoreEosLiveEvent.observe(this, Observer {
            RestoreEosModule.startForResult(this, ModuleCode.RESTORE_EOS)
        })

        viewModel.showItemsEvent.observe(this, Observer { list ->
            adapter.items = list
            adapter.notifyDataSetChanged()
        })

        viewModel.closeLiveEvent.observe(this, Observer {
            finish()
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data == null || resultCode != RESULT_OK)
            return

        val accountType = data.getParcelableExtra<AccountType>("accountType")

        when (requestCode) {
            ModuleCode.RESTORE_WORDS -> {
                viewModel.delegate.onConfirmRestore(accountType, data.getParcelableExtra("syncMode"))
            }
            ModuleCode.RESTORE_EOS -> {
                viewModel.delegate.onConfirmRestore(accountType)
            }
        }
    }

    //  ManageKeysDialog Listener

    override fun onClickCreateKey() {
        viewModel.delegate.onConfirmCreate()
    }

    override fun onClickBackupKey() {
        viewModel.delegate.onConfirmBackup()
    }
}