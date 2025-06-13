package com.dragsystem.muteme.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.dragsystem.muteme.R
import com.dragsystem.muteme.ui.fragment.DetalheFragment


class HistoricoAdapter(context: Context, dados: List<Array<String?>?>) :
    ArrayAdapter<Array<String?>?>(context, 0, dados) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_historico, parent, false)
        }

        val idText = convertView!!.findViewById<TextView>(R.id.id_text)
        val content = convertView.findViewById<TextView>(R.id.content)

        val item = getItem(position)
        val id = item!![0] ?: ""
        val conteudo = item[1] ?: ""

        idText.text = id
        content.text = conteudo

        // Clique no item
        convertView.setOnClickListener {
            val activity = context as AppCompatActivity
            val fragment = DetalheFragment.novaInstancia(id, conteudo)

            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, fragment) // container que está na sua Activity
                .addToBackStack(null)
                .commit()
        }

        return convertView
    }

}
