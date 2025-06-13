package com.dragsystem.muteme.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.dragsystem.muteme.MainActivity
import com.dragsystem.muteme.R
import com.dragsystem.muteme.data.AppDatabase
import com.dragsystem.muteme.data.entity.ChamadaEntity
import java.text.SimpleDateFormat
import java.util.Locale


class HistoricoChamadasFragment : Fragment() {

    private val mask = "(##) #####-####"
    private var isUpdating = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Infla o layout
        val view: View = inflater.inflate(R.layout.fragment_lista, container, false)
        val listView = view.findViewById<ListView>(R.id.listView)

        // Dados fictícios para exibição
        val db = AppDatabase.getInstance(requireContext())
        val chamadasList: List<ChamadaEntity?>? = db?.chamadaDao()?.listarChamadas()
        val chamadas: MutableList<String> = ArrayList()
        if (chamadasList != null) {
            for (c in chamadasList) {
                if (c != null) {
                    chamadas.add(c.numero?.let { formatTel(it) } + " - " + c.dataHora?.let { formatarData(it) } )
                }
            }
        }

        // Adapter padrão de string
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            chamadas
        )

        listView.adapter = adapter
        listView.setOnItemClickListener { _, _, position, _ ->
            val valor = chamadas[position]
            val partes = valor.split(" - ", limit = 3)
            val titulo = partes.getOrNull(0) ?: ""
            val descricao = partes.drop(1).joinToString(" - ")

            val fragment = DetalheFragment.novaInstancia(titulo, descricao)
            MainActivity.exibirFragmento(fragment);
        }
        return view
    }

    fun formatTel(numero: String): String
    {
        val digits = numero.substring(1, numero.length - 1).replace(Regex("[^\\d]"), "")
        var masked = ""
        var i = 0

        for (m in mask.toCharArray()) {
            if (m == '#') {
                if (i >= digits.length) break
                masked += digits[i]
                i++
            } else {
                masked += m
            }
        }
        return masked
    }

    fun formatarData(dataOriginal: String): String {
        return try {
            val formatoEntrada = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
            val formatoSaida = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

            val data = formatoEntrada.parse(dataOriginal)
            formatoSaida.format(data!!)
        } catch (e: Exception) {
            dataOriginal // se falhar, retorna o original
        }
    }

}
