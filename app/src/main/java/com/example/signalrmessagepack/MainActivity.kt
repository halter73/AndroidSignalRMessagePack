package com.example.signalrmessagepack

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.microsoft.messagepack.MessagePackHubProtocol
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder

class MainActivity : AppCompatActivity() {
    private lateinit var hubConnection: HubConnection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val textView = findViewById<TextView>(R.id.textView)

        textView.text = "Hello from code."

        // 10.0.2.2 is the localhost on the emulator host (dev machine)
        hubConnection = HubConnectionBuilder.create("http://10.0.2.2:5000/default?name=Android")
                .withProtocol(MessagePackHubProtocol())
                //.setHttpClientBuilderCallback({builder -> builder.})
                .build()

        hubConnection.on("Send", { message: String -> runOnUiThread { textView.text = message } }, String::class.java)

        hubConnection.start().blockingAwait();

        textView.text = "Connected!"
    }
}
