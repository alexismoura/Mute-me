package com.dragsystem.muteme.ui.fragment

import android.app.role.RoleManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.provider.Telephony
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.dragsystem.muteme.MainActivity
import com.dragsystem.muteme.R
import com.dragsystem.muteme.data.AppDatabase
import com.dragsystem.muteme.data.entity.ConfiguracoesEntity
import com.google.android.material.button.MaterialButton

class SettingsFragment : Fragment() {

    private lateinit var db: AppDatabase
    private lateinit var txtStatusValor: TextView
    private lateinit var btnDefinirComoPadrao: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = AppDatabase.getInstance(requireContext())
        
        txtStatusValor = view.findViewById(R.id.txtStatusValor)
        btnDefinirComoPadrao = view.findViewById(R.id.btnDefinirComoPadrao)

        // Configurar botão de app padrão
        btnDefinirComoPadrao.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Ativar bloqueio")
                .setMessage("O MuteMe precisa ser definido como app padrão de chamadas e SMS para bloquear contatos indesejados.")
                .setPositiveButton("Ativar") { _, _ ->
                    (activity as? MainActivity)?.solicitarRoleDialer()
                    (activity as? MainActivity)?.solicitarRoleSms()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        // Verificar status inicial
        verificarStatus()
    }

    override fun onResume() {
        super.onResume()
        verificarStatus()
    }

    private fun verificarStatus() {
        val isSms = Telephony.Sms.getDefaultSmsPackage(requireContext()) == requireContext().packageName
        val isDialer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = requireContext().getSystemService(RoleManager::class.java)
            roleManager?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) == true
        } else false

        if (isDialer && isSms) {
            txtStatusValor.text = "Bloqueio ativado"
            btnDefinirComoPadrao.text = "Desativar como App padrão"
            btnDefinirComoPadrao.setOnClickListener {
                startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
            }
        } else {
            txtStatusValor.text = "Bloqueio desativado"
            btnDefinirComoPadrao.text = "Ativar como App padrão"
            btnDefinirComoPadrao.setOnClickListener {
                AlertDialog.Builder(requireContext())
                    .setTitle("Ativar bloqueio")
                    .setMessage("O MuteMe precisa ser definido como app padrão de chamadas e SMS para bloquear contatos indesejados.")
                    .setPositiveButton("Ativar") { _, _ ->
                        (activity as? MainActivity)?.solicitarRoleDialer()
                        (activity as? MainActivity)?.solicitarRoleSms()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }
    }
}