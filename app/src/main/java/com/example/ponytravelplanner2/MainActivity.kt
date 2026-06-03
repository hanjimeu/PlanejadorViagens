package com.example.ponytravelplanner2

import android.content.Context
import androidx.compose.ui.text.style.TextDecoration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.ponytravelplanner.db.DBHelper
import com.example.ponytravelplanner.db.Post
import com.example.ponytravelplanner.db.Viagem
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PonyTripApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PonyTripApp() {

    val context = LocalContext.current
    val db = remember { DBHelper(context, null) }

    val sharedPreferences = context.getSharedPreferences("PonyTrip", Context.MODE_PRIVATE)

    //telas

    var telaAtual by rememberSaveable { mutableStateOf("splash") }

    // login

    var emailLogin by remember(telaAtual) { mutableStateOf("") }
    var senhaLogin by remember(telaAtual) { mutableStateOf("") }

    // cadastro

    var nomeCadastro by remember(telaAtual) { mutableStateOf("") }
    var emailCadastro by remember(telaAtual) { mutableStateOf("") }
    var senhaCadastro by remember(telaAtual) { mutableStateOf("") }
    var confirmarSenha by remember(telaAtual) { mutableStateOf("") }

    // erro

    var mensagemErro by remember { mutableStateOf("") }

    // usuario

    var usuarioSalvo by rememberSaveable {
        mutableStateOf(sharedPreferences.getString("usuario", "") ?: "")
    }

    var emailSalvo by rememberSaveable {
        mutableStateOf(sharedPreferences.getString("email", "") ?: "")
    }

    // autentiçação

    var usuarioAutenticado by rememberSaveable { mutableStateOf(false) }

    // foto do perfil

    var fotoPerfilUri by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(emailSalvo) {
        fotoPerfilUri = sharedPreferences.getString("fotoPerfil_$emailSalvo", null)
    }

    val launcherImagem = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        fotoPerfilUri = uri.toString()
        sharedPreferences.edit()
            .putString("fotoPerfil_$emailSalvo", fotoPerfilUri)
            .apply()
    }

    // menu

    var menuAberto by remember { mutableStateOf(false) }
    var menuFabAberto by remember { mutableStateOf(false) }

    // telas adicionais

    var modalNovaViagem by remember { mutableStateOf(false) }
    var modalPost by remember { mutableStateOf(false) }
    var editandoPerfil by remember { mutableStateOf(false) }
    var editandoHistorico by remember { mutableStateOf(false) }
    var confirmarSaida by remember { mutableStateOf(false) }

    // viagens

    var destinoViagem by remember { mutableStateOf("") }
    var planoViagem by remember { mutableStateOf("") }

    val viagens = remember {
        mutableStateListOf<Viagem>().also {
            it.addAll(db.getViagens(emailSalvo))
        }
    }

    // posts

    val listaPosts = remember { mutableStateListOf<Post>() }
    var postagemViagem by remember { mutableStateOf("") }
    var editingPostId by remember { mutableStateOf<Int?>(null) }
    var viagemSelecionadaId by remember { mutableStateOf<Int?>(null) }

    fun carregarPosts() {
        listaPosts.clear()
        if (viagemSelecionadaId == null) {
            listaPosts.addAll(db.getPosts(emailSalvo).shuffled())
        } else {
            listaPosts.addAll(db.getPostsByViagem(viagemSelecionadaId!!, emailSalvo))
        }
    }

    LaunchedEffect(viagemSelecionadaId) {
        carregarPosts()
    }

    // lugares

    val lugaresPopulares = remember { mutableStateListOf<String>() }
    val lugaresRecentes = remember { mutableStateListOf<String>() }

    fun carregarLugares() {
        lugaresPopulares.clear()
        lugaresPopulares.addAll(listOf(
            "Ponyville", "Canterlot", "Cloudsdale",
            "Manehattan", "Everfree Forest", "Crystal Empire"
        ))
    }

    fun carregarLugaresRecentes() {
        lugaresRecentes.clear()
        lugaresRecentes.addAll(
            db.getViagens(emailSalvo).map { it.destino }.distinct()
        )
    }

    LaunchedEffect(Unit) {
        carregarLugares()
        carregarLugaresRecentes()
    }

    // splash

    LaunchedEffect(Unit) {
        delay(2500)
        usuarioAutenticado = sharedPreferences.getBoolean("usuarioAutenticado", false)
        telaAtual = if (usuarioAutenticado) "home" else "login"
    }

    // tela do splash

    if (telaAtual == "splash") {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFE3F1),
                            Color(0xFFF2E4FF),
                            Color(0xFFE3F1FF)
                        )
                    )
                ),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier.size(220.dp)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "PonyTrip 🌙",
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFFF77C8)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Carregando viagens mágicas!",
                color = Color(0xFF9C7BFF),
                fontSize = 18.sp
            )
        }
    }

    // tela login

    else if (telaAtual == "login") {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFF1F7), Color.White, Color(0xFFEAF4FF))
                    )
                )
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Card(shape = RoundedCornerShape(40.dp)) {

                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "Entrar",
                        color = Color(0xFFFF77C8),
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable {
                            emailLogin = ""
                            senhaLogin = ""
                            mensagemErro = ""
                            telaAtual = "login"
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = emailLogin,
                        onValueChange = { emailLogin = it },
                        label = { Text("Email") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = senhaLogin,
                        onValueChange = { senhaLogin = it },
                        label = { Text("Senha") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val nome = db.loginUsuario(emailLogin, senhaLogin)
                            if (nome != null) {
                                usuarioSalvo = nome
                                emailSalvo = emailLogin

                                sharedPreferences.edit()
                                    .putString("usuario", nome)
                                    .putString("email", emailLogin)
                                    .putBoolean("usuarioAutenticado", true)
                                    .apply()

                                viagens.clear()
                                viagens.addAll(db.getViagens(emailSalvo))
                                carregarPosts()
                                carregarLugaresRecentes()

                                mensagemErro = ""
                                telaAtual = "home"
                            } else {
                                mensagemErro = "Email ou senha incorretos"
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF77C8))
                    ) {
                        Text("Entrar")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = "Não possui conta? ")
                        Text(
                            text = "Cadastre-se",
                            color = Color(0xFFFF77C8),
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.clickable { telaAtual = "cadastro" }
                        )
                    }

                    if (mensagemErro.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = mensagemErro, color = Color.Red)
                    }
                }
            }
        }
    }

    // tela cadastro

    else if (telaAtual == "cadastro") {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFF1F7), Color.White, Color(0xFFEAF4FF))
                    )
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(50.dp))

            Card(shape = RoundedCornerShape(40.dp)) {

                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "Cadastre-se",
                        color = Color(0xFFFF77C8),
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable {
                            nomeCadastro = ""
                            emailCadastro = ""
                            senhaCadastro = ""
                            confirmarSenha = ""
                            mensagemErro = ""
                            telaAtual = "cadastro"
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = nomeCadastro,
                        onValueChange = { nomeCadastro = it },
                        label = { Text("Nome") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = emailCadastro,
                        onValueChange = { emailCadastro = it },
                        label = { Text("Email") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = senhaCadastro,
                        onValueChange = { senhaCadastro = it },
                        label = { Text("Senha") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = confirmarSenha,
                        onValueChange = { confirmarSenha = it },
                        label = { Text("Confirmar senha") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (
                                nomeCadastro.isNotEmpty() &&
                                emailCadastro.isNotEmpty() &&
                                senhaCadastro.isNotEmpty() &&
                                senhaCadastro == confirmarSenha
                            ) {
                                val sucesso = db.cadastrarUsuario(nomeCadastro, emailCadastro, senhaCadastro)
                                if (sucesso) {
                                    mensagemErro = ""
                                    telaAtual = "login"
                                } else {
                                    mensagemErro = "Este email já está cadastrado"
                                }
                            } else {
                                mensagemErro = "Preencha todos os campos corretamente"
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF77C8))
                    ) {
                        Text("Criar Conta")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = "Já possui conta? ")
                        Text(
                            text = "Entrar",
                            color = Color(0xFFFF77C8),
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.clickable { telaAtual = "login" }
                        )
                    }

                    if (mensagemErro.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = mensagemErro, color = Color.Red)
                    }
                }
            }
        }
    }

    // tela princiapl

    else if (telaAtual == "home") {

        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFFFF1F7), Color.White, Color(0xFFEAF4FF))
                        )
                    )
            ) {

                // topbar
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { menuAberto = true }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = Color(0xFFFF77C8)
                            )
                        }
                    },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(R.drawable.logo),
                                contentDescription = null,
                                modifier = Modifier.size(42.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "PonyTrip 🌙", color = Color(0xFFFF77C8))
                        }
                    }
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                ) {

                    // perfil
                    item {
                        Card(shape = RoundedCornerShape(30.dp)) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "Olá, $usuarioSalvo ✨",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF77C8)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(text = "Planeje viagens mágicas!")
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // viagens
                    item {
                        Text(
                            text = "Itinerários ativos",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF9C7BFF)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    items(viagens) { viagem ->

                        var expandido by remember { mutableStateOf(false) }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .clickable {
                                    expandido = !expandido
                                    viagemSelecionadaId =
                                        if (viagemSelecionadaId == viagem.id) null else viagem.id
                                    carregarPosts()
                                },
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = viagem.destino,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Color(0xFF9C7BFF)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = viagem.status,
                                    color = when (viagem.status) {
                                        "Finalizada" -> Color(0xFF4CAF50)
                                        "Cancelada" -> Color.Red
                                        else -> Color(0xFFFF77C8)
                                    }
                                )
                                if (expandido) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(text = viagem.plano)
                                }
                            }
                        }
                    }

                    // diário
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (viagemSelecionadaId == null) {
                                    "Diário"
                                } else {
                                    val viagem = viagens.find { it.id == viagemSelecionadaId }
                                    "Diário de ${viagem?.destino ?: "viagem"}"
                                },
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF77C8)
                            )
                            if (viagemSelecionadaId != null) {
                                OutlinedButton(onClick = {
                                    viagemSelecionadaId = null
                                    carregarPosts()
                                }) {
                                    Text("Todos os Posts")
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    items(listaPosts) { post ->

                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {

                                var menuPostAberto by remember { mutableStateOf(false) }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(50.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFFFD6EC)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (fotoPerfilUri != null) {
                                                Image(
                                                    painter = rememberAsyncImagePainter(fotoPerfilUri),
                                                    contentDescription = null,
                                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Text("Foto")
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = usuarioSalvo,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFF77C8)
                                        )
                                    }

                                    Box {
                                        Text(
                                            text = "⋮",
                                            fontSize = 24.sp,
                                            modifier = Modifier.clickable { menuPostAberto = true }
                                        )
                                        DropdownMenu(
                                            expanded = menuPostAberto,
                                            onDismissRequest = { menuPostAberto = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Editar") },
                                                onClick = {
                                                    editingPostId = post.id
                                                    postagemViagem = post.texto
                                                    modalPost = true
                                                    menuPostAberto = false
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Excluir", color = Color.Red) },
                                                onClick = {
                                                    db.deletePost(post.id)
                                                    carregarPosts()
                                                    menuPostAberto = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Text(text = post.texto, fontSize = 16.sp)
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(120.dp)) }
                }
            }

            // tela post
            if (modalPost) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable {
                            modalPost = false
                            editingPostId = null
                            postagemViagem = ""
                        }
                )
                Card(
                    modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
                    shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = if (editingPostId != null) "Editar Post ✏️" else "Novo Post ✨",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        if (viagemSelecionadaId != null) {
                            Text(
                                text = "Post vinculado à viagem selecionada",
                                color = Color(0xFF9C7BFF)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        OutlinedTextField(
                            value = postagemViagem,
                            onValueChange = { postagemViagem = it },
                            label = { Text("Compartilhe sua viagem") },
                            modifier = Modifier.fillMaxWidth().height(180.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = {
                                if (postagemViagem.isNotEmpty()) {
                                    if (editingPostId != null) {
                                        db.updatePost(editingPostId!!, postagemViagem)
                                        editingPostId = null
                                    } else {
                                        db.addPost(
                                            texto = postagemViagem,
                                            email = emailSalvo,
                                            viagemId = viagemSelecionadaId
                                        )
                                    }
                                    carregarPosts()
                                    postagemViagem = ""
                                    modalPost = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Publicar")
                        }
                    }
                }
            }

            // fab
            Column(
                modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
                horizontalAlignment = Alignment.End
            ) {
                if (menuFabAberto) {
                    ExtendedFloatingActionButton(onClick = {
                        modalPost = true
                        menuFabAberto = false
                    }) { Text("Novo Post") }
                    Spacer(modifier = Modifier.height(12.dp))
                    ExtendedFloatingActionButton(onClick = {
                        modalNovaViagem = true
                        menuFabAberto = false
                    }) { Text("Nova Viagem") }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                FloatingActionButton(onClick = { menuFabAberto = !menuFabAberto }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                }
            }

            // tela nova viagem
            if (modalNovaViagem) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable { modalNovaViagem = false }
                )
                Card(
                    modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
                    shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Nova Viagem ✨", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "✕",
                                fontSize = 28.sp,
                                modifier = Modifier.clickable { modalNovaViagem = false }
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(text = "Locais populares", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        lugaresPopulares.forEach { lugar ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp)
                                    .clickable { destinoViagem = lugar }
                            ) {
                                Text(text = lugar, modifier = Modifier.padding(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(text = "Locais recentes", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        lugaresRecentes.forEach { lugar ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = lugar,
                                        modifier = Modifier.weight(1f).clickable { destinoViagem = lugar }
                                    )
                                    Text(
                                        text = "🗑️",
                                        fontSize = 20.sp,
                                        modifier = Modifier.clickable {
                                            db.deleteLugarRecente(lugar, emailSalvo)
                                            lugaresRecentes.clear()
                                            lugaresRecentes.addAll(db.getLugaresRecentes(emailSalvo))
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = destinoViagem,
                            onValueChange = { destinoViagem = it },
                            label = { Text("Destino") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = planoViagem,
                            onValueChange = { planoViagem = it },
                            label = { Text("Plano da viagem") },
                            modifier = Modifier.fillMaxWidth().height(180.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = {
                                if (destinoViagem.isNotEmpty()) {
                                    db.addViagem(destinoViagem, planoViagem, "Nova viagem", emailSalvo)
                                    viagens.clear()
                                    viagens.addAll(db.getViagens(emailSalvo))
                                    carregarLugares()
                                    carregarLugaresRecentes()
                                    destinoViagem = ""
                                    planoViagem = ""
                                    modalNovaViagem = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Salvar Viagem")
                        }
                    }
                }
            }
        }
    }

    // sidebar

    if (menuAberto) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
                .clickable { menuAberto = false }
        )
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(320.dp)
                .background(Color.White, RoundedCornerShape(topEnd = 40.dp, bottomEnd = 40.dp))
                .padding(20.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFD6EC))
                        .clickable { launcherImagem.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (fotoPerfilUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(fotoPerfilUri),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text("Foto")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = usuarioSalvo, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF77C8))
            Spacer(modifier = Modifier.height(30.dp))

            Button(onClick = { editandoPerfil = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Editar Perfil")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { editandoHistorico = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Editar Histórico")
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(text = "Suas viagens ✈️", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9C7BFF))
            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(modifier = Modifier.height(250.dp)) {
                items(viagens) { viagem ->
                    var menuViagemAberto by remember { mutableStateOf(false) }
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = viagem.destino, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = viagem.status,
                                    color = when (viagem.status) {
                                        "Finalizada" -> Color(0xFF4CAF50)
                                        "Cancelada" -> Color.Red
                                        else -> Color(0xFFFF77C8)
                                    }
                                )
                            }
                            Box {
                                Text(
                                    text = "⋮",
                                    fontSize = 24.sp,
                                    modifier = Modifier.clickable { menuViagemAberto = true }
                                )
                                DropdownMenu(
                                    expanded = menuViagemAberto,
                                    onDismissRequest = { menuViagemAberto = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Finalizar") },
                                        onClick = {
                                            db.updateViagemStatus(viagem.id, "Finalizada")
                                            viagens.clear()
                                            viagens.addAll(db.getViagens(emailSalvo))
                                            menuViagemAberto = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Excluir", color = Color.Red) },
                                        onClick = {
                                            db.deleteViagemComPosts(viagem.id)
                                            db.deleteViagem(viagem.id)
                                            viagens.clear()
                                            viagens.addAll(db.getViagens(emailSalvo))
                                            carregarPosts()
                                            menuViagemAberto = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = { confirmarSaida = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sair da Conta")
            }
        }
    }

    // editar operifl

    if (editandoPerfil) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
                .clickable { editandoPerfil = false }
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Editar Perfil ✨", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFD6EC))
                        .clickable { launcherImagem.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (fotoPerfilUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(fotoPerfilUri),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text("Foto")
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                OutlinedTextField(
                    value = usuarioSalvo,
                    onValueChange = { usuarioSalvo = it },
                    label = { Text("Nome") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = emailSalvo,
                    onValueChange = { emailSalvo = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        sharedPreferences.edit()
                            .putString("usuario", usuarioSalvo)
                            .putString("email", emailSalvo)
                            .apply()
                        editandoPerfil = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Salvar")
                }
            }
        }
    }

    // editar historico

    if (editandoHistorico) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
                .clickable { editandoHistorico = false }
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
        ) {
            LazyColumn(modifier = Modifier.padding(24.dp).height(700.dp)) {
                item {
                    Text(text = "Histórico de viagens ✈️", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(24.dp))
                }
                items(viagens) { viagem ->
                    var destinoEditado by remember { mutableStateOf(viagem.destino) }
                    var planoEditado by remember { mutableStateOf(viagem.plano) }
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = destinoEditado,
                                onValueChange = { destinoEditado = it },
                                label = { Text("Destino") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = planoEditado,
                                onValueChange = { planoEditado = it },
                                label = { Text("Plano") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row {
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedButton(onClick = {
                                    db.updateViagemStatus(viagem.id, "Finalizada")
                                    viagens.clear()
                                    viagens.addAll(db.getViagens(emailSalvo))
                                }) { Text("Finalizar") }
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedButton(
                                    onClick = {
                                        db.deleteViagemComPosts(viagem.id)
                                        db.deleteViagem(viagem.id)
                                        viagens.clear()
                                        viagens.addAll(db.getViagens(emailSalvo))
                                        carregarPosts()
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                                ) { Text("Excluir") }
                            }
                            Button(
                                onClick = {
                                    db.updateViagemDestino(viagem.id, destinoEditado, planoEditado)
                                    viagens.clear()
                                    viagens.addAll(db.getViagens(emailSalvo))
                                    carregarLugaresRecentes()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Salvar alterações") }
                        }
                    }
                }
            }
        }
    }

    // confirmar saida

    if (confirmarSaida) {
        AlertDialog(
            onDismissRequest = { confirmarSaida = false },
            title = { Text("Sair da conta?") },
            text = { Text("Deseja realmente sair da conta?") },
            confirmButton = {
                Button(
                    onClick = {
                        viagens.clear()
                        listaPosts.clear()
                        lugaresRecentes.clear()
                        sharedPreferences.edit()
                            .putBoolean("usuarioAutenticado", false)
                            .apply()
                        telaAtual = "login"
                        confirmarSaida = false
                        menuAberto = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Sair") }
            },
            dismissButton = {
                OutlinedButton(onClick = { confirmarSaida = false }) { Text("Cancelar") }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPonyTripApp() {
    PonyTripApp()
}