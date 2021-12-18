package com.example.alpha

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.alpha.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    lateinit var binding : ActivityMainBinding
    var array : MutableList<UserDTO> = arrayListOf()
    var uids : MutableList<String> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // Configure Google Sign In && Logout
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            Firebase.auth.signOut()
            googleSignInClient?.signOut()
            val logoutIntent = Intent(this, LoginActivity::class.java) // move Login page
            logoutIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK // 초기화 * 재로그인
            startActivity(logoutIntent)
            finish()
        }


        // Get user list
        FirebaseFirestore.getInstance().collection("users").get().addOnCompleteListener{
            task ->
            array.clear()
            uids.clear()
            for ( item in task.result!!.documents) {
                array.add(item.toObject(UserDTO::class.java)!!)
                uids.add(item.id)
            }
            binding.userListRecyclerview.adapter?.notifyDataSetChanged()
        }
        binding.userListRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.userListRecyclerview.adapter = RecyclerviewAdaptor()
    }
    //List
    inner class RecyclerviewAdaptor : RecyclerView.Adapter<RecyclerviewAdaptor.ViewHolder>(){
        override fun onCreateViewHolder( parent: ViewGroup, viewType: Int ): RecyclerviewAdaptor.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user,parent,false)
            return ViewHolder(view)
        }
        override fun getItemCount(): Int {
            return array.size
        }
        override fun onBindViewHolder(holder: RecyclerviewAdaptor.ViewHolder, position: Int) {
            holder.itemEmail.text = array[position].email
            holder.itemCountry.text = array[position].country
        }
        inner class ViewHolder(view : View) : RecyclerView.ViewHolder(view){
            val itemEmail: TextView = view.findViewById<TextView>(R.id.itemEmail)
            val itemCountry: TextView = view.findViewById<TextView>(R.id.itemCountry)

        }
    }
} 