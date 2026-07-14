package com.example.anew

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.anew.ui.theme.NewTheme
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class LoginActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        enableEdgeToEdge()
        setContent {
            var correo by remember { mutableStateOf("") }
            var clave by remember { mutableStateOf("") }
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Login", style = MaterialTheme.typography.headlineLarge)

                OutlinedTextField(
                    label = { Text("Correo electrónico") },
                    value = correo,
                    onValueChange = { correo = it })
                OutlinedTextField(
                    label = { Text("Contraseña") },
                    value = clave,
                    visualTransformation = PasswordVisualTransformation(),
                    onValueChange = { clave = it })

                Button(onClick = { iniciarSesion(correo, clave) }) {
                    Text("Logear usuario")
                }
                Text(text = "Iniciar sesión", modifier = Modifier
                    .padding(top = 16.dp)
                    .clickable {
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                })
            }


        }

    }
    fun iniciarSesion(correo:String, clave:String){
        auth.signInWithEmailAndPassword(correo, clave)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        baseContext,
                        "Login completado",
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(this, PerfilActivity::class.java))
                } else {
                    Toast.makeText(
                        baseContext,
                        "Falló el login.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }
}
