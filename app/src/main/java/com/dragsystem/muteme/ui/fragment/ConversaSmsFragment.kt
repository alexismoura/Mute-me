package com.dragsystem.muteme.ui.fragment

import android.os.Bundle
import android.telephony.SmsManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import com.dragsystem.muteme.R
import com.dragsystem.muteme.data.AppDatabase
import com.dragsystem.muteme.data.entity.SmsEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ConversaSmsFragment : Fragment() {

    companion object {
        fun novaInstancia(numero: String): ConversaSmsFragment {
            val args = Bundle()
            args.putString("numero", numero)
            val fragment = ConversaSmsFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var numeroDestino: String
    private lateinit var adapter: ArrayAdapter<String>
    private val mensagens = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_sms_conversa, container, false)

        numeroDestino = arguments?.getString("numero") ?: ""

        val listView = view.findViewById<ListView>(R.id.list_conversa)
        val edtMensagem = view.findViewById<EditText>(R.id.edt_mensagem)
        val btnEnviar = view.findViewById<Button>(R.id.btn_enviar)

        // carregar histórico fictício ou do banco
        mensagens.addAll(carregarHistoricoSms(numeroDestino))

        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, mensagens)
        listView.adapter = adapter

        btnEnviar.setOnClickListener {
            val mensagem = edtMensagem.text.toString().trim()
            if (mensagem.isNotEmpty()) {
                enviarSms(numeroDestino, mensagem)
                mensagens.add("Você: $mensagem")
                adapter.notifyDataSetChanged()
                edtMensagem.text.clear()
            }
        }

        return view
    }

    private fun carregarHistoricoSms(numero: String): List<String> {
        val db = AppDatabase.getInstance(requireContext())
        return db.smsDao()?.listarSms()
            ?.filter { it?.numero == numero }
            ?.map { "Contato: ${it?.mensagem}" }
            ?: listOf()
    }

    private fun enviarSms(numero: String, mensagem: String) {
        val smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(numero, null, mensagem, null, null)

        // salvar no banco, se desejar
        val sms = SmsEntity().apply {
            this.numero = numero
            this.mensagem = mensagem
            this.tipo = "Enviado"
            this.dataHora = SimpleDateFormat("dd-MM-yyyy HH:mm:ss"
                , Locale.getDefault()).format(Date())
        }
        AppDatabase.getInstance(requireContext()).smsDao()?.inserir(sms)
    }
}
