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
import com.dragsystem.muteme.util.NotificationManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION || intent.action == Telephony.Sms.Intents.SMS_DELIVER_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            messages?.forEach { sms ->
                val sender = sms.originatingAddress ?: return@forEach
                val body = sms.messageBody

                Log.d("MuteMe", "📩 SMS de: $sender")
                Log.d("MuteMe", "Conteúdo: $body")

                val db = AppDatabase.getInstance(context)
                val config = db.configuracoesDao().obterConfiguracoes()
                val deveBloquear = when (config?.tipoBloqueio) {
                    "todos" -> true
                    "fora_contatos" -> !isNumberInContacts(context, sender)
                    else -> false
                }

                if (deveBloquear) {
                    abortBroadcast()
                }

                if (config != null) {
                    if (config.notificacoesAtivas) {
                        // Mostrar notificação apenas se não for bloqueado e notificações estiverem ativas
                        NotificationManager(context).showSmsNotification(sender, body)
                    }
                }

                if (config != null) {
                    Log.d("MuteMe", "Configuração: ${config.tipoBloqueio}, Bloquear: $deveBloquear")
                }

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
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return formatter.format(Date())
    }
}

