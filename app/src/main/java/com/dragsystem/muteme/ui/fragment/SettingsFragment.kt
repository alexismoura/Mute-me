package com.dragsystem.muteme.ui.fragment

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.dragsystem.muteme.R
import com.dragsystem.muteme.data.AppDatabase
import com.dragsystem.muteme.data.entity.ConfiguracoesEntity

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var db: AppDatabase

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.sync_preferences, rootKey)

        db = AppDatabase.getInstance(requireContext())

        val modoBloqueioPref = findPreference<ListPreference>("modo_bloqueio")
        val notificacoesPref = findPreference<SwitchPreferenceCompat>("notificacoes")

        // 🔁 Carregar valores do banco e atualizar visualmente
        val config = db.configuracoesDao().obterConfiguracoes()
        config?.let {
            modoBloqueioPref?.value = it.modoBloqueio
            notificacoesPref?.isChecked = it.notificacoes
        }

        // 📝 Listeners para salvar quando o usuário altera
        modoBloqueioPref?.setOnPreferenceChangeListener { _, newValue ->
            atualizarConfiguracoes(modoBloqueio = newValue.toString(), notificacoes = null)
            true // indica que o valor pode ser alterado
        }

        notificacoesPref?.setOnPreferenceChangeListener { _, newValue ->
            atualizarConfiguracoes(modoBloqueio = null, notificacoes = newValue as Boolean)
            true
        }
    }

    private fun atualizarConfiguracoes(modoBloqueio: String?, notificacoes: Boolean?) {
        val configAtual = db.configuracoesDao().obterConfiguracoes()
            ?: ConfiguracoesEntity() // Cria default se não existir ainda

        val novaConfig = ConfiguracoesEntity(
            id = 1,
            modoBloqueio = modoBloqueio ?: configAtual.modoBloqueio,
            notificacoes = notificacoes ?: configAtual.notificacoes
        )

        db.configuracoesDao().salvarConfiguracoes(novaConfig)
    }

}