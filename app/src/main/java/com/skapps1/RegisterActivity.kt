package com.skapps1

import android.app.Activity

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.os.PersistableBundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class RegisterActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        register_button_register.setOnClickListener {

            performRegister()

        }

        already_have_account_textview.setOnClickListener {

            Log.d("RegisterActivity", "Try to show login activity")

            //launch the login activity

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        profile.setOnClickListener {
            Log.d("RegisterActivity", "Try to show photo selector" )

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)

        }


    }

    var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode==0 && resultCode == Activity.RESULT_OK && data!= null) {
            //proceed and check which image selected

            Log.d("RegisterActivity", "Photo was selected")
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            circleprofile_register.setImageBitmap(bitmap)

            profile.alpha = 0f
           // val bitmapDrawable = BitmapDrawable(this.resources, bitmap)
           // profile.setBackground(bitmapDrawable)
        }
    }

    private fun performRegister() {
        val email = email_edittext_register.text.toString()
        val password = password_edittext_register.text.toString()

        if(email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter text in email/pw", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("RegisterActivity", "Email is: " + email)
        Log.d("RegisterActivity", "Password is: $password")

        //Firebase authentication

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener

                //else if successful

                Log.d("RegisterActivity", "Successfully created user with uid: ${it.result?.user?.uid} ")

                uploadImageToFirebaseStorage()


            }
            .addOnFailureListener{
                Log.d("RegisterActivity", "Fail to create user: ${it.message}")
                Toast.makeText(this, "Fail to create user: ${it.message}", Toast.LENGTH_SHORT).show()
            }

    }
    private fun uploadImageToFirebaseStorage() {

        if(selectedPhotoUri==null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        Log.d("RegisterActivity", "Uploading...")
        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {

                Log.d("RegisterActivity", "Successfully uploaded image: ${it.metadata?.path}")


                ref.downloadUrl.addOnSuccessListener {
                    it.toString()
                    Log.d("RegisterActivity", "File Location: $it")

                    saveUserToFirebaseDatabase(it.toString())
                }


            }
            .addOnFailureListener {
                Log.d("RegisterActivity", "Failed to upload image to storage: ${it.message}")
            }

    }
    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref =  FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid, username_edittext_register.text.toString(), profileImageUrl )
        Log.d("RegisterActivity", "Saving user to database...")
        ref.setValue(user).addOnSuccessListener {
            Log.d("RegisterActivity", "Finally the user is saved to database")

            val intent = Intent(this, LatestMessages::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or((Intent.FLAG_ACTIVITY_NEW_TASK)) //clears the stack or when you click back it goes back to the screenk
            startActivity(intent)


        }
            .addOnFailureListener {
               Log.d("RegisterActivity", "Failed saving user to database")
            }
    }

}

@Parcelize
class User(val uid: String, val username: String, val profileImageUrl: String): Parcelable{
    constructor() : this("", "", "")
}


