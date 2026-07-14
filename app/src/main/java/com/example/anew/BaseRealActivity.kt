package com.example.anew

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.snap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.anew.ui.theme.NewTheme
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class BaseRealActivity : ComponentActivity() {
    val database = Firebase.database
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val myRef = database.getReference("message")
        var textoMensaje by mutableStateOf("")
        var textoRecibido by mutableStateOf("")

        myRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot){
                val value = snapshot.value
                if(value != null){
                    textoRecibido = textoRecibido + "\n" + value.toString()
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
        enableEdgeToEdge()
        setContent {
            Column(modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {

                Text(text="Base Real",style = MaterialTheme.typography.labelLarge)
                OutlinedTextField(
                    value = textoMensaje,
                    onValueChange = {textoMensaje = it},
                    label = {Text("Mensaje")}
                )

                Button(onClick = {
                    myRef.setValue(textoMensaje)
                }) {
                    Text("Enviar")

                }
                Text(text = textoRecibido)
            }
        }
    }
}
