package com.dragsystem.muteme.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import com.dragsystem.muteme.data.AppDatabase
import com.dragsystem.muteme.data.entity.ConfiguracoesEntity
import com.dragsystem.muteme.data.entity.SmsEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.provider.Telephony.SMS_RECEIVED" ||
            intent.action == "android.provider.Telephony.SMS_DELIVER") {
            val bundle: Bundle? = intent.extras
            val pdus = bundle?.get("pdus") as? Array<*>
            val format = bundle?.getString("format")

            val db = AppDatabase.getInstance(context)
            val config = db.configuracoesDao().obterConfiguracoes()
                ?: ConfiguracoesEntity() // valores padrão

            pdus?.forEach {
                val message = SmsMessage.createFromPdu(it as ByteArray, format)
                val sender = message.originatingAddress ?: return@forEach
                val body = message.messageBody

                Log.d("MuteMe", "📩 SMS de: $sender")
                Log.d("MuteMe", "Conteúdo: $body")

                val deveBloquear = when (config.tipoBloqueio) {
                    "todos" -> true
                    "fora_contatos" -> !isNumberInContacts(context, sender)
                    else -> false
                }

                if (deveBloquear) {
                    abortBroadcast()
                }

                Log.d("MuteMe", "Configuração: ${config.tipoBloqueio}, Bloquear: $deveBloquear")

                // Salvar no histórico
                val sms = SmsEntity()
                sms.tipo = if (deveBloquear) "Bloqueado" else "Recebido"
                sms.numero = sender
                sms.mensagem = body
                sms.dataHora = getCurrentDateTimeString()
                db.smsDao()?.inserir(sms)

                if (deveBloquear && Telephony.Sms.getDefaultSmsPackage(context) == context.packageName) {
                    abortBroadcast() // só funciona se for default SMS app
                    Log.d("MuteMe", "❌ SMS bloqueado de: $sender")
                } else {
                    Log.w("MuteMe", "⚠️ App não é SMS padrão — não é possível bloquear")
                }
            }
        }
    }

    private fun isNumberInContacts(context: Context, phoneNumber: String): Boolean {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )
        val cursor = context.contentResolver.query(
            uri,
            arrayOf(ContactsContract.PhoneLookup._ID),
            null,
            null,
            null
        )
        return cursor?.use { it.count > 0 } ?: false
    }

    private fun getCurrentDateTimeString(): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return formatter.format(Date())
    }
}

