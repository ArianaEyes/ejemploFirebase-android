package com.example.anew

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.anew.ui.theme.NewTheme
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.launch
import java.util.UUID

// 1. Modelo de datos para el Mensaje
data class Message(
    val id: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

class BaseRealActivity : ComponentActivity() {

    // Generamos un ID único para este usuario/dispositivo durante esta sesión
    private val myUserId = UUID.randomUUID().toString().take(6)
    private val database = Firebase.database
    private val messagesRef = database.getReference("chats")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NewTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ChatScreen(
                        modifier = Modifier.padding(innerPadding),
                        userId = myUserId,
                        onSendMessage = { text ->
                            val newMessageKey = messagesRef.push().key ?: ""
                            val message = Message(
                                id = newMessageKey,
                                senderId = myUserId,
                                text = text
                            )
                            messagesRef.child(newMessageKey).setValue(message)
                        },
                        // Pasamos la referencia para que el Composable maneje su ciclo de vida
                        messagesRef = messagesRef
                    )
                }
            }
        }
    }
}

@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    userId: String,
    onSendMessage: (String) -> Unit,
    messagesRef: com.google.firebase.database.DatabaseReference
) {
    var textInput by rememberSaveable { mutableStateOf("") }
    val messagesList = remember { mutableStateListOf<Message>() }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // 2. Escuchar cambios de Firebase respetando el ciclo de vida de Compose
    DisposableEffect(messagesRef) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tempMap = mutableListOf<Message>()
                for (child in snapshot.children) {
                    val message = child.getValue(Message::class.java)
                    if (message != null) {
                        tempMap.add(message)
                    }

                }
                messagesList.clear()
                // Ordenamos por timestamp para asegurar la cronología
                messagesList.addAll(tempMap.sortedBy { it.timestamp })

                // Hace scroll automático al último mensaje cuando llega uno nuevo
                coroutineScope.launch {
                    if (messagesList.isNotEmpty()) {
                        listState.animateScrollToItem(messagesList.size - 1)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar error de lectura de base de datos aquí si es necesario
            }
        }

        messagesRef.addValueEventListener(listener)

        // Limpia el listener cuando el Composable sale de la pantalla
        onDispose {
            messagesRef.removeEventListener(listener)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Usuario actual: $userId",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // 3. Lista de mensajes (LazyColumn)
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messagesList) { message ->
                val isMe = message.senderId == userId
                ChatBubble(message = message, isMe = isMe)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 4. Área de entrada de texto y botón enviar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                label = { Text("Escribe un mensaje...") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (textInput.isNotBlank()) {
                        onSendMessage(textInput)
                        textInput = "" // Limpiar input
                    }
                }
            ) {
                Text("Enviar")
            }
        }
    }
}

// 5. Componente visual para los globos de texto (burbujas del chat)
@Composable
fun ChatBubble(message: Message, isMe: Boolean) {
    val alignment = if (isMe) Alignment.End else Alignment.Start
    val bubbleColor = if (isMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val textColor = if (isMe) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (isMe) 12.dp else 0.dp,
                        bottomEnd = if (isMe) 0.dp else 12.dp
                    )
                )
                .background(bubbleColor)
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = if (isMe) "Tú" else "ID: ${message.senderId}",
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )
            }
        }
    }
}