package com.example.visionmate

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.applozic.mobicomkit.api.account.register.RegistrationResponse
import com.applozic.mobicomkit.api.conversation.MessageBuilder
import com.applozic.mobicomkit.exception.ApplozicException
import com.applozic.mobicomkit.listners.MediaUploadProgressHandler
import com.example.visionmate.Constants.APP_ID
import io.kommunicate.KmConversationBuilder
import io.kommunicate.Kommunicate
import io.kommunicate.callbacks.KMLoginHandler
import io.kommunicate.callbacks.KmCallback
import io.kommunicate.users.KMUser

class ChatBotActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initChatBot()
        loginChatBot()

    }

    private fun loginChatBot() {
        val user = KMUser().apply {
            userId = "1234"
        }
        Kommunicate.login(this, user, object : KMLoginHandler {
            override fun onSuccess(registrationResponse: RegistrationResponse, context: Context) {
                // You can perform operations such as opening the conversation, creating a new conversation or update user details on success
                Log.e("",""+registrationResponse)
                launchConversation(user)
            }

            override fun onFailure(
                registrationResponse: RegistrationResponse,
                exception: java.lang.Exception
            ) {
                Log.e("",""+exception.printStackTrace())
                // You can perform actions such as repeating the login call or throw an error message on failure
            }
        })
    }


    private fun launchConversation(user: KMUser) {
        KmConversationBuilder(this)
            .setKmUser(user)
            .launchConversation(object : KmCallback {
                override fun onSuccess(message: Any) {
                    Log.d("Conversation", "Success : $message")
                    sendMessge(this@ChatBotActivity, "Hello there")
                }


                override fun onFailure(error: Any) {
                    Log.d("Conversation", "Failure : $error")
                }
            })
    }

    private fun sendMessge(context: Context,message:String) {
        Kommunicate.openConversation(this, null)
        MessageBuilder(context)
            .setContentType(com.applozic.mobicomkit.api.conversation.Message.ContentType.ATTACHMENT.value)
            .setGroupId(12345)
            .setFilePath("the files absolute path in string")
            .send(object : MediaUploadProgressHandler {
                override fun onUploadStarted(e: ApplozicException?, oldMessageKey: String?) {
                    if (e == null) {
                        // the upload has started
                    }
                }

                override fun onProgressUpdate(percentage: Int, e: ApplozicException?, oldMessageKey: String?) {
                    if (e == null) {
                        // display this upload percentage on the UI
                    }
                }

                override fun onCancelled(e: ApplozicException?, oldMessageKey: String?) {
                    // the upload was interrupted, most of the times by the user
                }

                override fun onCompleted(e: ApplozicException?, oldMessageKey: String?) {
                    if (e == null) {
                        // The upload has finished
                    } else {
                        // The upload has failed, due to network error or server error
                    }
                }

                override fun onSent(
                    message: com.applozic.mobicomkit.api.conversation.Message?,
                    oldMessageKey: String?
                ) {
                    // The message containing the attachment has been sent to the server.
                }

            })
    }

    private fun initChatBot() {
        Kommunicate.init(this, APP_ID);
    }
}