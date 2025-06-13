package com.dragsystem.muteme.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import com.dragsystem.muteme.R

class DetalheFragment : Fragment() {

    companion object {
        fun novaInstancia(id: String, conteudo: String): DetalheFragment {
            val fragment = DetalheFragment()
            val args = Bundle()
            args.putString("id", id)
            args.putString("conteudo", conteudo)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_detalhe, container, false)
        val idText = view.findViewById<TextView>(R.id.txt_id)
        val content = view.findViewById<TextView>(R.id.txt_conteudo)

        val id = arguments?.getString("id") ?: ""
        val conteudo = arguments?.getString("conteudo") ?: ""

        idText.text = id
        content.text = HtmlCompat.fromHtml(conteudo, HtmlCompat.FROM_HTML_MODE_LEGACY)

        return view
    }
}
