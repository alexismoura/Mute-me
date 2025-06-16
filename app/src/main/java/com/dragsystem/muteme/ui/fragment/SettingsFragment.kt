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
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.dragsystem.muteme.MainActivity
import com.dragsystem.muteme.R
import com.dragsystem.muteme.data.AppDatabase
import com.dragsystem.muteme.data.entity.ConfiguracoesEntity
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private lateinit var db: AppDatabase
    private lateinit var txtStatusValor: TextView
    private lateinit var btnDefinirComoPadrao: MaterialButton
    private lateinit var switchNotificacoes: SwitchMaterial
    private lateinit var radioGroupTipoBloqueio: RadioGroup

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
        switchNotificacoes = view.findViewById(R.id.switch_notificacoes)
        radioGroupTipoBloqueio = view.findViewById(R.id.radio_group_tipo_bloqueio)

        // Carregar configurações salvas
        carregarConfiguracoes()

        // Configurar botão de app padrão
        btnDefinirComoPadrao.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Ativar Bloqueio")
                .setMessage("O MuteMe precisa ser definido como app padrão de chamadas e SMS para bloquear contatos indesejados.")
                .setPositiveButton("Ativar") { _, _ ->
                    (activity as? MainActivity)?.solicitarRoleDialer()
                    (activity as? MainActivity)?.solicitarRoleSms()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        // Configurar switch de notificações
        switchNotificacoes.setOnCheckedChangeListener { _, isChecked ->
            salvarConfiguracaoNotificacoes(isChecked)
        }

        // Configurar radio group de tipo de bloqueio
        radioGroupTipoBloqueio.setOnCheckedChangeListener { _, checkedId ->
            val tipoBloqueio = when (checkedId) {
                R.id.radio_bloqueio_nenhum -> "nenhum"
                R.id.radio_bloqueio_fora_contatos -> "fora_contatos"
                R.id.radio_bloqueio_todos -> "todos"
                else -> "nenhum"
            }
            salvarConfiguracaoTipoBloqueio(tipoBloqueio)
        }

        // Verificar status inicial
        verificarStatus()
    }

    override fun onResume() {
        super.onResume()
        verificarStatus()
    }

    private fun carregarConfiguracoes() {
        CoroutineScope(Dispatchers.IO).launch {
            val configuracoes = db.configuracoesDao().obterConfiguracoes()
            configuracoes?.let {
                requireActivity().runOnUiThread {
                    switchNotificacoes.isChecked = it.notificacoesAtivas
                    when (it.tipoBloqueio) {
                        "fora_contatos" -> radioGroupTipoBloqueio.check(R.id.radio_bloqueio_fora_contatos)
                        "todos" -> radioGroupTipoBloqueio.check(R.id.radio_bloqueio_todos)
                        else -> radioGroupTipoBloqueio.check(R.id.radio_bloqueio_nenhum)
                    }
                }
            }
        }
    }

    private fun salvarConfiguracaoNotificacoes(ativado: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            val configuracoes = db.configuracoesDao().obterConfiguracoes() ?: ConfiguracoesEntity()
            configuracoes.notificacoesAtivas = ativado
            db.configuracoesDao().salvarConfiguracoes(configuracoes)
        }
    }

    private fun salvarConfiguracaoTipoBloqueio(tipo: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val configuracoes = db.configuracoesDao().obterConfiguracoes() ?: ConfiguracoesEntity()
            configuracoes.tipoBloqueio = tipo
            db.configuracoesDao().salvarConfiguracoes(configuracoes)
        }
    }

    private fun verificarStatus() {
        val isSms = Telephony.Sms.getDefaultSmsPackage(requireContext()) == requireContext().packageName
        val isDialer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = requireContext().getSystemService(RoleManager::class.java)
            roleManager?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) == true
        } else false

        if (isDialer && isSms) {
            txtStatusValor.text = "Bloqueio Ativado"
            btnDefinirComoPadrao.text = "Desativar como App Padrão"
            btnDefinirComoPadrao.setOnClickListener {
                startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
            }
        } else {
            txtStatusValor.text = "Bloqueio Desativado"
            btnDefinirComoPadrao.text = "Ativar como App Padrão"
            btnDefinirComoPadrao.setOnClickListener {
                AlertDialog.Builder(requireContext())
                    .setTitle("Ativar Bloqueio")
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