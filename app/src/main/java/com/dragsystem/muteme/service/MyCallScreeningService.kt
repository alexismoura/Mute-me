package com.dragsystem.muteme.service

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.preference.PreferenceManager
import com.dragsystem.muteme.data.AppDatabase
import com.dragsystem.muteme.data.entity.ChamadaEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MyCallScreeningService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        Log.d("MuteMe", "🛑 onScreenCall foi chamado")

        val number = callDetails.handle?.schemeSpecificPart ?: run {
            Log.d("MuteMe", "Número inválido ou não disponível.")
            return
        }

        Log.d("MuteMe", "Número recebido: $number")

        val db = AppDatabase.getInstance(applicationContext)
        val config = db?.configuracoesDao()?.obterConfiguracoes()
        val tipoBloqueio = config?.tipoBloqueio

        val deveBloquear = when (tipoBloqueio) {
            "todos" -> true
            "fora_contatos" -> !isNumberInContacts(this, number)
            else -> false
        }

        Log.d("MuteMe", "Recebendo chamada de: $number")
        Log.d("MuteMe", "Modo: $tipoBloqueio | Deve bloquear: $deveBloquear")

        if (deveBloquear) {
            try {
                val chamada = ChamadaEntity().apply {
                    tipo = "Bloqueada"
                    numero = number
                    dataHora = getCurrentDateTimeString()
                }
                if (db != null) {
                    db.chamadaDao()?.inserir(chamada)
                }
            } catch (e: Exception) {
                Log.e("MuteMe", "Erro ao salvar chamada: ${e.message}")
            }

            respondToCall(
                callDetails,
                CallResponse.Builder()
                    .setDisallowCall(true)
                    .setRejectCall(true)
                    .setSkipCallLog(true)
                    .setSkipNotification(true)
                    .build()
            )
        } else {
            respondToCall(
                callDetails,
                CallResponse.Builder()
                    .setDisallowCall(false)
                    .setRejectCall(false)
                    .setSkipCallLog(false)
                    .setSkipNotification(false)
                    .build()
            )
        }
    }

    private fun isNumberInContacts(context: Context, number: String): Boolean {
        val contentResolver = context.contentResolver
        val uri = android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER)
        val cursor = contentResolver.query(uri, projection, null, null, null)

        val normalizedInput = number.replace(Regex("[^\\d]"), "")

        cursor?.use {
            while (it.moveToNext()) {
                val contactNumber = it.getString(0)
                val normalizedContact = contactNumber.replace(Regex("[^\\d]"), "")
                if (normalizedContact.endsWith(normalizedInput) || normalizedInput.endsWith(normalizedContact)) {
                    return true
                }
            }
        }
        return false
    }

    private fun getCurrentDateTimeString(): String {
        val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        return formatter.format(Date())
    }

}
