package com.skapps1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.user_row_new_message.view.*


class NewMessageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)


        supportActionBar?.title = "Select User"


/*        val adapter = GroupAdapter<GroupieViewHolder>()
        recylcerview_newmessage.setAdapter(adapter)
        //recylcerview_newmessage.layoutManager = LinearLayoutManager(this)

      */

        fetchUsers()



    }
        companion object {
            val USER_KEY = "USER_KEY"
        }
    private fun fetchUsers(){
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {

                val adapter = GroupAdapter<GroupieViewHolder>()

                p0.children.forEach {
                    Log.d("NewMessage", it.toString())
                    val user = it.getValue(User::class.java)
                    if (user!= null) {
                        adapter.add(UserItem(user))
                    }

                }

                adapter.setOnItemClickListener{item, view->

                    val userItem = item as UserItem
                    val intent = Intent(view.context, ChatLogActivity::class.java)
                    //intent.putExtra(USER_KEY, userItem.user.username)
                    intent.putExtra(USER_KEY, userItem.user)
                    startActivity(intent)
                    finish()
                }
                recylcerview_newmessage.setAdapter(adapter)

            }
            override fun onCancelled(p0: DatabaseError) {

            }
        })

    }

    class UserItem(val user: User): Item<GroupieViewHolder>() {

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            //this will be called in our list 4 each user object
            viewHolder.itemView.username_textview_new_message.text = user.username
            Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.imageView_from_chat)
        }
        override fun getLayout(): Int {
            return R.layout.user_row_new_message
        }

    }
}


