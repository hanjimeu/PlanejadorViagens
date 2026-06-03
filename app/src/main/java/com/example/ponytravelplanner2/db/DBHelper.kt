package com.example.ponytravelplanner.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// =====================================================
// DATA CLASS VIAGEM
// =====================================================

data class Viagem(
    val id: Int,
    val destino: String,
    val plano: String,
    val status: String,
    val usuarioId: Int = 0,
    val email: String = ""
)

// =====================================================
// DATA CLASS POST
// =====================================================

data class Post(
    val id: Int,
    val texto: String,
    val usuarioId: Int = 0,
    val viagemId: Int? = null,
    val email: String = ""
)

// =====================================================
// DB HELPER
// =====================================================

class DBHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    // =====================================================
    // CREATE
    // =====================================================

    override fun onCreate(db: SQLiteDatabase) {

        // =========================
        // TABELA USUARIOS
        // =========================

        val queryUsuarios = """
            CREATE TABLE $TABLE_USUARIOS (
                $USUARIO_ID_COL INTEGER PRIMARY KEY AUTOINCREMENT,
                $USUARIO_NOME_COL TEXT,
                $USUARIO_EMAIL_COL TEXT UNIQUE,
                $USUARIO_SENHA_COL TEXT
            )
        """.trimIndent()

        // =========================
        // TABELA VIAGENS
        // =========================

        val queryViagens = """
            CREATE TABLE $TABLE_VIAGENS (
                $ID_COL INTEGER PRIMARY KEY AUTOINCREMENT,
                $DESTINO_COL TEXT,
                $PLANO_COL TEXT,
                $STATUS_COL TEXT,
                $USER_ID_COL INTEGER,
                $EMAIL_COL TEXT
            )
        """.trimIndent()

        // =========================
        // TABELA POSTS
        // =========================

        val queryPosts = """
            CREATE TABLE $TABLE_POSTS (
                $POST_ID_COL INTEGER PRIMARY KEY AUTOINCREMENT,
                $POST_TEXTO_COL TEXT,
                $USER_ID_COL INTEGER,
                $VIAGEM_ID_COL INTEGER,
                $EMAIL_COL TEXT
            )
        """.trimIndent()

        // =========================
        // TABELA LUGARES RECENTES
        // =========================

        val queryLugares = """
            CREATE TABLE $TABLE_LUGARES (
                $LUGAR_ID_COL INTEGER PRIMARY KEY AUTOINCREMENT,
                $LUGAR_NOME_COL TEXT,
                $EMAIL_COL TEXT
            )
        """.trimIndent()

        db.execSQL(queryUsuarios)
        db.execSQL(queryViagens)
        db.execSQL(queryPosts)
        db.execSQL(queryLugares)
    }

    // =====================================================
    // UPGRADE
    // =====================================================

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USUARIOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_VIAGENS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_POSTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_LUGARES")
        onCreate(db)
    }

    // =====================================================
    // USUARIOS
    // =====================================================

    fun cadastrarUsuario(nome: String, email: String, senha: String): Boolean {
        return try {
            val db = writableDatabase
            val values = ContentValues().apply {
                put(USUARIO_NOME_COL, nome)
                put(USUARIO_EMAIL_COL, email)
                put(USUARIO_SENHA_COL, senha)
            }
            db.insertOrThrow(TABLE_USUARIOS, null, values)
            db.close()
            true
        } catch (e: Exception) {
            false // email já cadastrado
        }
    }

    fun loginUsuario(email: String, senha: String): String? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $USUARIO_NOME_COL FROM $TABLE_USUARIOS WHERE $USUARIO_EMAIL_COL = ? AND $USUARIO_SENHA_COL = ?",
            arrayOf(email, senha)
        )
        val nome = if (cursor.moveToFirst()) {
            cursor.getString(cursor.getColumnIndexOrThrow(USUARIO_NOME_COL))
        } else null
        cursor.close()
        db.close()
        return nome
    }

    // =====================================================
    // VIAGENS
    // =====================================================

    fun addViagem(
        destino: String,
        plano: String,
        status: String,
        email: String,
        usuarioId: Int = 0
    ) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(DESTINO_COL, destino)
            put(PLANO_COL, plano)
            put(STATUS_COL, status)
            put(USER_ID_COL, usuarioId)
            put(EMAIL_COL, email)
        }
        db.insert(TABLE_VIAGENS, null, values)
        db.close()
    }

    fun getViagens(email: String): List<Viagem> {
        val lista = mutableListOf<Viagem>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_VIAGENS WHERE $EMAIL_COL = ?",
            arrayOf(email)
        )
        while (cursor.moveToNext()) {
            lista.add(
                Viagem(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(ID_COL)),
                    destino = cursor.getString(cursor.getColumnIndexOrThrow(DESTINO_COL)),
                    plano = cursor.getString(cursor.getColumnIndexOrThrow(PLANO_COL)),
                    status = cursor.getString(cursor.getColumnIndexOrThrow(STATUS_COL)),
                    usuarioId = cursor.getInt(cursor.getColumnIndexOrThrow(USER_ID_COL)),
                    email = cursor.getString(cursor.getColumnIndexOrThrow(EMAIL_COL))
                )
            )
        }
        cursor.close()
        db.close()
        return lista
    }

    fun updateViagemStatus(id: Int, novoStatus: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(STATUS_COL, novoStatus)
        }
        db.update(TABLE_VIAGENS, values, "$ID_COL = ?", arrayOf(id.toString()))
        db.close()
    }

    fun updateViagemDestino(id: Int, novoDestino: String, novoPlano: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(DESTINO_COL, novoDestino)
            put(PLANO_COL, novoPlano)
        }
        db.update(TABLE_VIAGENS, values, "$ID_COL = ?", arrayOf(id.toString()))
        db.close()
    }

    fun deleteViagem(id: Int) {
        val db = writableDatabase
        db.delete(TABLE_VIAGENS, "$ID_COL = ?", arrayOf(id.toString()))
        db.close()
    }

    // =====================================================
    // POSTS
    // =====================================================

    fun addPost(
        texto: String,
        email: String,
        usuarioId: Int = 0,
        viagemId: Int? = null
    ) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(POST_TEXTO_COL, texto)
            put(USER_ID_COL, usuarioId)
            put(VIAGEM_ID_COL, viagemId)
            put(EMAIL_COL, email)
        }
        db.insert(TABLE_POSTS, null, values)
        db.close()
    }

    fun getPosts(email: String): List<Post> {
        val lista = mutableListOf<Post>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_POSTS WHERE $EMAIL_COL = ?",
            arrayOf(email)
        )
        while (cursor.moveToNext()) {
            lista.add(
                Post(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(POST_ID_COL)),
                    texto = cursor.getString(cursor.getColumnIndexOrThrow(POST_TEXTO_COL)),
                    usuarioId = cursor.getInt(cursor.getColumnIndexOrThrow(USER_ID_COL)),
                    viagemId = if (cursor.isNull(cursor.getColumnIndexOrThrow(VIAGEM_ID_COL))) {
                        null
                    } else {
                        cursor.getInt(cursor.getColumnIndexOrThrow(VIAGEM_ID_COL))
                    },
                    email = cursor.getString(cursor.getColumnIndexOrThrow(EMAIL_COL))
                )
            )
        }
        cursor.close()
        db.close()
        return lista
    }

    fun getPostsByViagem(viagemId: Int, email: String): List<Post> {
        val lista = mutableListOf<Post>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_POSTS WHERE $VIAGEM_ID_COL = ? AND $EMAIL_COL = ?",
            arrayOf(viagemId.toString(), email)
        )
        while (cursor.moveToNext()) {
            lista.add(
                Post(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(POST_ID_COL)),
                    texto = cursor.getString(cursor.getColumnIndexOrThrow(POST_TEXTO_COL)),
                    usuarioId = cursor.getInt(cursor.getColumnIndexOrThrow(USER_ID_COL)),
                    viagemId = cursor.getInt(cursor.getColumnIndexOrThrow(VIAGEM_ID_COL)),
                    email = cursor.getString(cursor.getColumnIndexOrThrow(EMAIL_COL))
                )
            )
        }
        cursor.close()
        db.close()
        return lista
    }

    fun updatePost(id: Int, texto: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(POST_TEXTO_COL, texto)
        }
        db.update(TABLE_POSTS, values, "$POST_ID_COL = ?", arrayOf(id.toString()))
        db.close()
    }

    fun deletePost(id: Int) {
        val db = writableDatabase
        db.delete(TABLE_POSTS, "$POST_ID_COL = ?", arrayOf(id.toString()))
        db.close()
    }

    fun deleteViagemComPosts(viagemId: Int) {
        val db = writableDatabase
        db.delete(TABLE_POSTS, "$VIAGEM_ID_COL = ?", arrayOf(viagemId.toString()))
        db.delete(TABLE_VIAGENS, "$ID_COL = ?", arrayOf(viagemId.toString()))
        db.close()
    }

    // =====================================================
    // LUGARES RECENTES
    // =====================================================

    fun addLugarRecente(nome: String, email: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(LUGAR_NOME_COL, nome)
            put(EMAIL_COL, email)
        }
        db.insert(TABLE_LUGARES, null, values)
        db.close()
    }

    fun getLugaresRecentes(email: String): List<String> {
        val lista = mutableListOf<String>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_LUGARES WHERE $EMAIL_COL = ?",
            arrayOf(email)
        )
        while (cursor.moveToNext()) {
            lista.add(cursor.getString(cursor.getColumnIndexOrThrow(LUGAR_NOME_COL)))
        }
        cursor.close()
        db.close()
        return lista.distinct()
    }

    fun deleteLugarRecente(nome: String, email: String) {
        val db = writableDatabase
        db.delete(
            TABLE_LUGARES,
            "$LUGAR_NOME_COL = ? AND $EMAIL_COL = ?",
            arrayOf(nome, email)
        )
        db.close()
    }

    // =====================================================
    // CONSTANTES
    // =====================================================

    companion object {

        private const val DATABASE_NAME = "PonyTripDB"
        private const val DATABASE_VERSION = 7

        // USUARIOS
        const val TABLE_USUARIOS = "Usuarios"
        const val USUARIO_ID_COL = "id"
        const val USUARIO_NOME_COL = "nome"
        const val USUARIO_EMAIL_COL = "email"
        const val USUARIO_SENHA_COL = "senha"

        // VIAGENS
        const val TABLE_VIAGENS = "Viagens"
        const val ID_COL = "id"
        const val DESTINO_COL = "destino"
        const val PLANO_COL = "plano"
        const val STATUS_COL = "status"

        // POSTS
        const val TABLE_POSTS = "Posts"
        const val POST_ID_COL = "id"
        const val POST_TEXTO_COL = "texto"

        // RELAÇÃO
        const val USER_ID_COL = "usuario_id"
        const val VIAGEM_ID_COL = "viagem_id"
        const val EMAIL_COL = "email"

        // LUGARES
        const val TABLE_LUGARES = "LugaresRecentes"
        const val LUGAR_ID_COL = "id"
        const val LUGAR_NOME_COL = "nome"
    }
}