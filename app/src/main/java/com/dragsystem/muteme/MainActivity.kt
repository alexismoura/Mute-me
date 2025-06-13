package com.dragsystem.muteme

import android.Manifest
import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.provider.Telephony
import android.telecom.TelecomManager
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.dragsystem.muteme.ui.fragment.DashboardFragment
import com.dragsystem.muteme.ui.fragment.SettingsFragment
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST = 1
    private val PERMISSIONS_REQUEST_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.title = getString(R.string.app_name)

        val button = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_preferences)
            setBackgroundResource(android.R.color.transparent)
            setColorFilter(ContextCompat.getColor(context, android.R.color.white))
            setOnClickListener {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, SettingsFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }
        val layoutParams = Toolbar.LayoutParams(
            Toolbar.LayoutParams.WRAP_CONTENT,
            Toolbar.LayoutParams.WRAP_CONTENT,
            Gravity.END or Gravity.CENTER_VERTICAL
        )
        layoutParams.setMargins(0, 0, 32, 0)
        toolbar.addView(button, layoutParams)

        //requestPermissionsIfNeeded()
        //solicitarPermissoesSeNecessario()
        configurarPreferenciasIniciais()
        mostrarJustificativaAoUsuarioComAtraso()

        //loadMessages()

        val fragment = DashboardFragment()
        supportFragmentManager.beginTransaction()
            .add(R.id.frameLayout, fragment)
            .commit()

        MobileAds.initialize(this) {}

        val adView = findViewById<AdView>(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
        instance = this
    }

    private fun mostrarJustificativaAoUsuarioComAtraso() {
        Handler(Looper.getMainLooper()).postDelayed({

            val isSms = Telephony.Sms.getDefaultSmsPackage(this) == packageName
            val isDialer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                getSystemService(RoleManager::class.java)?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) == true
            } else false

            Log.d("MuteMe", "📱 App é discador padrão? $isDialer | SMS padrão? $isSms")

            if (!isDialer || !isSms) {
                AlertDialog.Builder(this)
                    .setTitle("Permissões necessárias")
                    .setMessage("Para bloquear chamadas e SMS, o MuteMe precisa ser definido como app padrão de telefone e mensagens.")
                    .setPositiveButton("Permitir") { _, _ ->
                        solicitarRoleDialer()
                        solicitarRoleSms()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }, 100) // Pequeno atraso de 0.1 segundos
    }


    private val REQUEST_CODE_SET_DEFAULT_DIALER = 1001
    private val REQUEST_CODE_SET_DEFAULT_SMS = 1002

    fun solicitarRoleDialer() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java)
            if (roleManager?.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) == true &&
                !roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {

                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                Log.d("MuteMe", "➡️ Solicitando ser discador padrão...")
                try {
                    startActivityForResult(intent, REQUEST_CODE_SET_DEFAULT_DIALER)
                } catch (e: Exception) {
                    // fallback para config do sistema
                    abrirConfiguracoesDeAppTelefonePadrao()
                }
            } else {
                Log.d("MuteMe", "✅ Já é app de chamadas padrão")
            }
        } else {
            val telecomManager = getSystemService(TelecomManager::class.java)
            if (telecomManager?.defaultDialerPackage != packageName) {
                val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
                intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
                Log.d("MuteMe", "➡️ Solicitando ser discador padrão...")
                try {
                    startActivityForResult(intent, REQUEST_CODE_SET_DEFAULT_DIALER)
                } catch (e: Exception) {
                    abrirConfiguracoesDeAppTelefonePadrao()
                }
            }
        }
    }

    fun solicitarRoleSms() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java)
            if (roleManager?.isRoleAvailable(RoleManager.ROLE_SMS) == true &&
                !roleManager.isRoleHeld(RoleManager.ROLE_SMS)
            ) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
                startActivityForResult(intent, REQUEST_CODE_SET_DEFAULT_SMS)
            }
        } else {
            val smsApp = Telephony.Sms.getDefaultSmsPackage(this)
            if (smsApp != packageName) {
                val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                intent.putExtra("package", packageName)
                startActivityForResult(intent, REQUEST_CODE_SET_DEFAULT_SMS)
            }
        }
    }

    private fun abrirConfiguracoesDeAppTelefonePadrao() {
        val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
        startActivity(intent)
    }

    private fun loadMessages() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java)
            if (roleManager!!.isRoleAvailable(RoleManager.ROLE_SMS)) {
                if (roleManager.isRoleHeld(RoleManager.ROLE_SMS)) {
                    //askPermissions()
                } else {
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
                    startActivity(intent)
                }
            } else {
                //toast(com.simplemobiletools.commons.R.string.unknown_error_occurred)
                finish()
            }
        } else {
            if (Telephony.Sms.getDefaultSmsPackage(this) == packageName) {
                //askPermissions()
            } else {
                val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
                startActivity(intent)
            }
        }
    }

    private fun configurarPreferenciasIniciais() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        if (!prefs.contains("modo_bloqueio")) {
            prefs.edit()
                .putString("modo_bloqueio", "todos_exceto_contatos")
                .putStringSet("numeros_bloqueio", setOf("+558899999999"))
                .apply()
        }
    }

    private fun solicitarIdentificadorDeChamadas() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java)
            if (
                roleManager?.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) == true &&
                !roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
            ) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                startActivity(intent)
            } else {
                Log.d("MuteMe", "✅ Já é identificador de chamadas")
            }
        }
    }

    private fun verificarOuSolicitarAppDeTelefonePadrao() {
        val telecomManager = getSystemService(TelecomManager::class.java)
        val meuPacote = packageName

        val isTelefonePadrao = telecomManager?.defaultDialerPackage == meuPacote

        if (!isTelefonePadrao) {
            // Redireciona o usuário para escolher o MuteMe como app de telefone padrão
            val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
            intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, meuPacote)
            startActivity(intent)
        }
    }

    private val permissoesNecessarias = arrayOf(
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS
    ) + if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        arrayOf(Manifest.permission.ANSWER_PHONE_CALLS)
    } else emptyArray()

    private fun solicitarPermissoesSeNecessario() {
        val faltando = permissoesNecessarias.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (faltando.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, faltando.toTypedArray(), PERMISSIONS_REQUEST_CODE)
        }
    }

    fun requestPermissionsIfNeeded() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            arrayOf(
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ANSWER_PHONE_CALLS
            )
        } else {
            arrayOf(
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_PHONE_STATE
            )
        }

        if (permissions.any {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            val negadas = permissions.zip(grantResults.toTypedArray()).filter {
                it.second != PackageManager.PERMISSION_GRANTED
            }

            if (negadas.isNotEmpty()) {
                Toast.makeText(
                    this,
                    "Algumas permissões não foram concedidas. O app pode não funcionar corretamente.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SET_DEFAULT_DIALER) {
            Log.d("MuteMe", "🎯 onActivityResult requestCode=$requestCode result=$resultCode")
            if (resultCode == Activity.RESULT_OK) {
                Log.d("MuteMe", "✅ App definido como discador padrão.")
            } else {
                val telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
                val isPadrao = telecomManager.defaultDialerPackage == packageName
                Log.d("MuteMe", if (isPadrao) "✅ Agora é discador padrão" else "❌ Ainda não é discador padrão")
                abrirConfiguracoesDeAppTelefonePadrao()
            }
        }
    }


    companion object {
        private val _blockedNumbers = MutableStateFlow<List<String>>(listOf())
        val blockedNumbers: StateFlow<List<String>> = _blockedNumbers
        private var instance: MainActivity? = null

        fun exibirFragmento(fragment: Fragment) {
            instance?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.frameLayout, fragment)
                ?.addToBackStack(null)
                ?.commit()
        }

        fun addBlockedNumber(number: String) {
            _blockedNumbers.value = _blockedNumbers.value + number
        }

        fun isBlocked(number: String): Boolean {
            return _blockedNumbers.value.contains(number)
        }
    }
}