package com.dragsystem.muteme.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.telephony.SmsManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dragsystem.muteme.R
import com.dragsystem.muteme.data.AppDatabase
import com.dragsystem.muteme.data.entity.SmsEntity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ConversaSmsFragment : Fragment() {

    companion object {
        private const val PICK_CONTACT_REQUEST = 1
        private const val PHONE_MASK = "(##) #####-####"
        
        fun novaInstancia(numero: String): ConversaSmsFragment {
            val args = Bundle()
            args.putString("numero", numero)
            val fragment = ConversaSmsFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var numeroDestino: String
    private lateinit var adapter: MensagensAdapter
    private val mensagens = mutableListOf<Mensagem>()
    private lateinit var edtNumero: TextInputEditText
    private lateinit var edtMensagem: TextInputEditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_sms_conversa, container, false)

        numeroDestino = arguments?.getString("numero") ?: ""

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_mensagens)
        edtNumero = view.findViewById(R.id.edt_numero)
        edtMensagem = view.findViewById(R.id.edt_mensagem)
        val btnContatos = view.findViewById<MaterialButton>(R.id.btn_contatos)
        val btnEnviar = view.findViewById<MaterialButton>(R.id.btn_enviar)

        // Configurar RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context).apply {
            stackFromEnd = true
        }
        adapter = MensagensAdapter(mensagens)
        recyclerView.adapter = adapter

        // Configurar número inicial se fornecido
        if (numeroDestino.isNotEmpty()) {
            edtNumero.setText(formatarNumero(numeroDestino))
        }

        // Configurar formatação do número
        edtNumero.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            private var oldText = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                oldText = s.toString()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return
                isUpdating = true

                val digits = s.toString().replace(Regex("[^\\d]"), "")
                val formatted = formatarNumero(digits)

                if (formatted != s.toString()) {
                    edtNumero.setText(formatted)
                    edtNumero.setSelection(formatted.length)
                }

                isUpdating = false
            }
        })

        // Configurar botão de contatos
        btnContatos.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
            startActivityForResult(intent, PICK_CONTACT_REQUEST)
        }

        // Carregar histórico
        carregarHistoricoSms(numeroDestino)

        // Configurar botão de enviar
        btnEnviar.setOnClickListener {
            val numero = edtNumero.text.toString().trim()
            val mensagem = edtMensagem.text.toString().trim()
            
            if (numero.isNotEmpty() && mensagem.isNotEmpty()) {
                enviarSms(numero, mensagem)
                adicionarMensagem(mensagem, true)
                edtMensagem.text?.clear()
            }
        }

        return view
    }

    private fun formatarNumero(numero: String): String {
        val digits = numero.replace(Regex("[^\\d]"), "")
        var formatted = ""
        var i = 0

        for (m in PHONE_MASK.toCharArray()) {
            if (m == '#') {
                if (i >= digits.length) break
                formatted += digits[i]
                i++
            } else {
                formatted += m
            }
        }
        return formatted
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_CONTACT_REQUEST && data != null) {
            val contactUri = data.data
            val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
            
            contactUri?.let { uri ->
                requireContext().contentResolver.query(
                    uri,
                    projection,
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        val number = cursor.getString(numberIndex)
                        edtNumero.setText(formatarNumero(number))
                    }
                }
            }
        }
    }

    private fun carregarHistoricoSms(numero: String) {
        val db = AppDatabase.getInstance(requireContext())
        val smsList = db.smsDao()?.listarSms()?.filter { it?.numero == numero } ?: listOf()
        
        mensagens.clear()
        smsList.forEach { sms ->
            if (sms != null) {
                adicionarMensagem(sms.mensagem ?: "", sms.tipo == "Enviado")
            }
        }
        adapter.notifyDataSetChanged()
    }

    private fun adicionarMensagem(texto: String, enviada: Boolean) {
        val mensagem = Mensagem(
            texto = texto,
            enviada = enviada,
            hora = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        )
        mensagens.add(mensagem)
        adapter.notifyItemInserted(mensagens.size - 1)
    }

    private fun enviarSms(numero: String, mensagem: String) {
        val smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(numero, null, mensagem, null, null)

        // Salvar no banco
        val sms = SmsEntity().apply {
            this.numero = numero
            this.mensagem = mensagem
            this.tipo = "Enviado"
            this.dataHora = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        }
        AppDatabase.getInstance(requireContext()).smsDao()?.inserir(sms)
    }

    data class Mensagem(
        val texto: String,
        val enviada: Boolean,
        val hora: String
    )

    inner class MensagensAdapter(private val mensagens: List<Mensagem>) : 
        RecyclerView.Adapter<MensagensAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val txtMensagem: TextView = view.findViewById(R.id.txt_mensagem)
            val txtHora: TextView = view.findViewById(R.id.txt_hora)
            val imgStatus: ImageView = view.findViewById(R.id.img_status)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_mensagem, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val mensagem = mensagens[position]
            holder.txtMensagem.text = mensagem.texto
            holder.txtHora.text = mensagem.hora
            
            if (mensagem.enviada) {
                holder.imgStatus.visibility = View.VISIBLE
                holder.imgStatus.setImageResource(android.R.drawable.ic_menu_send)
            } else {
                holder.imgStatus.visibility = View.GONE
            }
        }

        override fun getItemCount() = mensagens.size
    }
}
