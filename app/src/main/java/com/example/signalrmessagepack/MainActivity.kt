package com.example.signalrmessagepack

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.microsoft.messagepack.MessagePackHubProtocol
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.TransportEnum
import com.microsoft.signalr.TypeReference

data class Custom(val propA: String = "", val propB: Int = 0)

class MainActivity : AppCompatActivity() {
    private lateinit var hubConnection: HubConnection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val textView = findViewById<TextView>(R.id.textView)
        val textInput = findViewById<TextInputEditText>(R.id.textInput)
        val sendButton = findViewById<Button>(R.id.button)

        textView.text = "Hello from code."

        // 10.0.2.2 is the localhost on the emulator host (dev machine)
        hubConnection = HubConnectionBuilder.create("http://10.0.2.2:5000/default?name=Android")
                .withProtocol(MessagePackHubProtocol())
                .withTransport(TransportEnum.LONG_POLLING)
                // I was thinking about configuring the HttpClient not to validate TLS certs, but that looks difficult.
                // Decided to use cleartext instead which required adding android:usesCleartextTraffic="true" to the manifest.
                //.setHttpClientBuilderCallback({builder -> builder.})
                .build()

        hubConnection.on("Send", { message: String -> runOnUiThread { textView.text = message } }, String::class.java)

        hubConnection.on<ArrayList<Custom>>("Send2", { list -> runOnUiThread { textView.text = "From on callback: ${list[0].propA}:${list[0].propB}" } },
                (object: TypeReference<ArrayList<Custom>>() {}).type)

        // The following doesn't work due to type erasure.
        //hubConnection.on("Send2", { list -> runOnUiThread { textView.text = "From on callback: ${list[0].propA}:${list[0].propB}" } }, ArrayList<Custom>().javaClass)

        hubConnection.start().blockingAwait();

        textView.text = "Connected!"

        sendButton.setOnClickListener { hubConnection.invoke("Send", "Android", textInput.text.toString()).blockingAwait() }

        hubConnection.invoke("Send2", arrayListOf(Custom("Testing", -1), Custom("Testing", 123) , Custom("Testing", 456))).blockingAwait()
    }
}
