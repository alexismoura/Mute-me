package com.dragsystem.muteme.ui.fragment

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.provider.Telephony
import android.telecom.TelecomManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.dragsystem.muteme.MainActivity
import com.dragsystem.muteme.R
import com.dragsystem.muteme.ui.adapter.DashboardPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class DashboardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dashboard_fragment, container, false)
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        val viewPager = view.findViewById<ViewPager2>(R.id.viewPager)
        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)

        val adapter = DashboardPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = if (position == 0) "Chamadas" else "SMS"
        }.attach()

        // Lógica de status do bloqueio
        val txtStatusValor = view.findViewById<TextView>(R.id.txtStatusValor)
        val btnDefinir = view.findViewById<Button>(R.id.btnDefinirComoPadrao)

        val isSms = Telephony.Sms.getDefaultSmsPackage(requireContext()) == requireContext().packageName

        val isDialer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = requireContext().getSystemService(RoleManager::class.java)
            roleManager?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) == true
        } else false


        if (isDialer && isSms) {
            txtStatusValor.text = "Bloqueio ativado"
            btnDefinir.text = "Desativar como App padrão"
            btnDefinir.setOnClickListener {
                startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
            }
        } else {
            txtStatusValor.text = "Bloqueio desativado"
            btnDefinir.setOnClickListener {
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
