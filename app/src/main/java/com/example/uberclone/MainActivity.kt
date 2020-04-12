package com.example.uberclone

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Layout
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.rengwuxian.materialedittext.MaterialEditText
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_register.*
import uk.co.chrisjenx.calligraphy.CalligraphyConfig
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import java.lang.Exception

open class Validation(var isValid: Boolean, var errorMessage: String?)
class MainActivity : AppCompatActivity() {

    var auth: FirebaseAuth? = null
    var db: FirebaseDatabase? = null
    var users: DatabaseReference? = null
    var edtEmail: EditText? = null
    var edtpassword: EditText? = null
    var edtName: EditText? = null
    var edtPhone: EditText? = null
    var progressDialog: CustomeProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    private fun init(){

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        users = db?.getReference("Users")
        progressDialog =  CustomeProgressDialog(this)

        btnRegister?.setOnClickListener { v ->
            showRegisterDialog()
        }
        btnSignIn?.setOnClickListener { v ->
            showLoginDialog()
        }
    }

    private fun showLoginDialog(){
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Login")
        alertDialog.setMessage("Please use email to login")

        var inflator = LayoutInflater.from(this)
        var layout = inflator.inflate(R.layout.activity_login,null)
        alertDialog.setView(layout)

        edtEmail = layout.findViewById<MaterialEditText>(R.id.edtEmail)
        edtpassword = layout.findViewById<MaterialEditText>(R.id.edtpassword)

        progressDialog?.show()

        alertDialog.setPositiveButton("LOGIN", object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                val isValid = isValidLogin().isValid
                progressDialog?.hide()

                if (!isValid){
                    Toast.makeText(this@MainActivity,isValidLogin().errorMessage,Toast.LENGTH_SHORT).show()
                } else{
                    auth?.signInWithEmailAndPassword(edtEmail?.text?.trim().toString(),edtpassword?.text?.trim().toString())!!.addOnSuccessListener(object : OnSuccessListener<AuthResult> {
                        override fun onSuccess(p0: AuthResult?) {
                            var intent = Intent(this@MainActivity,WelcomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }).addOnFailureListener(object : OnFailureListener {
                        override fun onFailure(p0: Exception) {
                          Toast.makeText(this@MainActivity,"Failed "+p0?.message,Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }

        })
        alertDialog.setNegativeButton("CANCEL", object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                dialog?.dismiss()

            }

        })
        alertDialog.show()

    }

    private fun showRegisterDialog() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("REGISTER")
        alertDialog.setMessage("Please use email to register")

        var inflator = LayoutInflater.from(this)
        var layout = inflator.inflate(R.layout.activity_register,null)

        edtEmail = layout.findViewById<MaterialEditText>(R.id.edtEmail)
        edtpassword = layout.findViewById<MaterialEditText>(R.id.edtpassword)
        edtName = layout.findViewById<MaterialEditText>(R.id.edtName)
        edtPhone = layout.findViewById<MaterialEditText>(R.id.edtPhone)


        alertDialog.setView(layout)
        alertDialog.setPositiveButton("REGISTER", object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                var isLogin = isValidSignUp().isValid
                if (!isLogin){
                 Toast.makeText(this@MainActivity,isValidSignUp().errorMessage,Toast.LENGTH_SHORT).show()
                } else{
                    auth?.createUserWithEmailAndPassword(
                        edtEmail?.text?.trim().toString(),
                        edtpassword?.text?.trim().toString()
                    )!!.addOnSuccessListener(object : OnSuccessListener<AuthResult> {
                        override fun onSuccess(p0: AuthResult?) {
                            // save user to db
                            var user = User(edtEmail?.text?.toString()?.trim()!!, edtpassword?.text?.toString()?.trim()!!,
                                edtName?.text?.toString()?.trim()!!, edtPhone?.text?.toString()!!.trim()
                            )

                            // use email to key
                            users?.child(FirebaseAuth.getInstance().currentUser?.uid!!)?.setValue(user)
                                ?.addOnSuccessListener(object : OnSuccessListener<Void> {
                                    override fun onSuccess(p0: Void?) {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Registered succesfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                })?.addOnFailureListener(object : OnFailureListener {
                                    override fun onFailure(p0: Exception) {
                                        Toast.makeText(this@MainActivity, "Failed", Toast.LENGTH_SHORT).show()

                                    }

                                })

                        }

                    })
                }

            }

        })
        alertDialog.setNegativeButton("CANCEL", object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
               // dialog?.dismiss()

            }

        })
        alertDialog.show()

    }

    private fun isValidLogin(): Validation{
        if (TextUtils.isEmpty(edtEmail?.text?.trim().toString())){
            return Validation(false,"Please enter email number")
        }
        else if (TextUtils.isEmpty(edtpassword?.text?.trim().toString())){
            return Validation(false,"Please enter password")
        }
        else if(edtpassword?.text?.trim().toString().length<10){
            return Validation(false,"Password is too short")
        }
        return Validation(true,"")
    }

    private fun isValidSignUp(): Validation{

        if (TextUtils.isEmpty(edtEmail?.text?.trim().toString())){
            return Validation(false,"Please enter email number")
        }
        else if (TextUtils.isEmpty(edtpassword?.text?.trim().toString())){
            return Validation(false,"Please enter password")
        }
        else if (TextUtils.isEmpty(edtName?.text?.trim().toString())){
            return Validation(false,"Please enter your name")
        }
        else if (TextUtils.isEmpty(edtPhone?.text?.trim().toString())){
            return Validation(false,"Please enter your phone number")
        }
        else if (edtPhone?.text?.trim().toString().length<10){
            return Validation(false,"Please enter valid phone number")
        }
        else {
            return Validation(true, "")


        }
    }

}
