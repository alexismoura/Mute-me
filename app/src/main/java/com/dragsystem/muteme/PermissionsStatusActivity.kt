package com.dragsystem.muteme

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import android.provider.Telephony
import android.util.Log

class PermissionsStatusActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permissions_status)

        val btnTelefone = findViewById<Button>(R.id.btnSetTelefone)
        val btnSms = findViewById<Button>(R.id.btnSetSms)
        val btnIdentificador = findViewById<Button>(R.id.btnSetIdentificador)

        atualizarStatus()

        btnTelefone.setOnClickListener {
            Log.i("MuteMe", "btnTelefone")
            val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
            intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
            startActivity(intent)
            //atualizarStatus()
        }

        btnSms.setOnClickListener {
            val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
            startActivity(intent)
            Log.i("MuteMe", "btnSms")

            //atualizarStatus()
        }

        btnIdentificador.setOnClickListener {
            Log.i("MuteMe", "btnIdentificador")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val roleManager = getSystemService(RoleManager::class.java)
                val intent = roleManager?.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                if (intent != null) startActivity(intent)
            } else {
                startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
            }
            //atualizarStatus()
        }
    }

    private fun atualizarStatus() {
        val txtTelefone = findViewById<TextView>(R.id.txtTelefoneStatus)
        val txtSms = findViewById<TextView>(R.id.txtSmsStatus)
        val txtIdentificador = findViewById<TextView>(R.id.txtIdentificadorStatus)

        val isDialer = getSystemService<TelecomManager>()?.defaultDialerPackage == packageName
        val isSms = Telephony.Sms.getDefaultSmsPackage(this) == packageName
        val isIdentificador = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getSystemService(RoleManager::class.java)?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) == true
        } else false

        txtTelefone.text = if (isDialer) "\u2705 App de telefone: Ativado" else "\u274C App de telefone: N\u00e3o ativado"
        txtSms.text = if (isSms) "\u2705 App de SMS: Ativado" else "\u274C App de SMS: N\u00e3o ativado"
        txtIdentificador.text = if (isIdentificador) "\u2705 Identificador de chamadas: Ativado" else "\u274C Identificador de chamadas: N\u00e3o ativado"

        if (isDialer && isSms && isIdentificador) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        atualizarStatus() // Revalida os status quando voltar de cada tela
    }

}
